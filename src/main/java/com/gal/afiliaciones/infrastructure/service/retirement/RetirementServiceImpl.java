package com.gal.afiliaciones.infrastructure.service.retirement;

import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.retirement.*;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.service.retirement.exception.InvalidRetirementDateException;
import com.gal.afiliaciones.infrastructure.service.retirement.exception.InvalidRetirementRequestException;
import com.gal.afiliaciones.infrastructure.service.retirement.exception.WorkerNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RetirementServiceImpl implements RetirementService {

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Autowired
    private RetirementRepository retirementRepository;

    @Override
    public List<ContractListResponseDto> searchWorker(WorkerSearchRequestDto request) {
        if (request.getTipoDocumento() == null || request.getNumeroIdentificacion() == null) {
            throw new IllegalArgumentException("El tipo de documento y el número de identificación son obligatorios.");
        }

        Specification<Affiliate> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("documentType"), request.getTipoDocumento()));
            predicates.add(cb.equal(root.get("documentNumber"), request.getNumeroIdentificacion()));

            if (request.getEmpresa() != null && !request.getEmpresa().isEmpty()) {
                predicates.add(cb.equal(root.get("nitCompany"), request.getEmpresa()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<Affiliate> affiliates = affiliateRepository.findAll(spec);

        if (affiliates.isEmpty()) {
            throw new WorkerNotFoundException("El trabajador no fue encontrado, valida la información e intenta nuevamente");
        }

        return affiliates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public RetirementResponseDto requestRetirement(RetirementRequestDto request) {
        Affiliate contractToRetire = affiliateRepository.findById(request.getContractId())
                .orElseThrow(() -> new WorkerNotFoundException("Contrato no encontrado."));

        if (!"Activa".equals(contractToRetire.getAffiliationStatus())) {
            throw new InvalidRetirementRequestException("No se puede programar el retiro para un contrato que no está activo.");
        }

        boolean retirementExists = retirementRepository.findAll().stream()
                .anyMatch(r -> r.getIdAffiliate().equals(request.getContractId()));

        if (retirementExists) {
            throw new InvalidRetirementRequestException("Ya existe un retiro programado o procesado para este contrato.");
        }

        validateRetirementDate(request.getRetirementDate(), contractToRetire);

        if ("Independiente".equals(contractToRetire.getAffiliationType())) {
            validateIndependentWorker(contractToRetire);
        } else {
            validateDependentWorker(contractToRetire);
        }

        logRetirement(request, contractToRetire);

        String message = "El retiro ha sido programado exitosamente.";
        if (request.getRetirementDate().equals(LocalDate.now())) {
            message = "La fecha que seleccionaste es igual a la fecha del proceso (hoy), el retiro quedará registrado inmediatamente y se ejecutará una vez se finalice el día.";
        }

        return new RetirementResponseDto(message, request.getRetirementDate());
    }

    private void logRetirement(RetirementRequestDto request, Affiliate contract) {
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = jwt.getClaimAsString("preferred_username");

        Retirement retirement = new Retirement();
        retirement.setIdentificationDocumentType(contract.getDocumentType());
        retirement.setIdentificationDocumentNumber(contract.getDocumentNumber());
        retirement.setCompleteName(contract.getCompany());
        retirement.setAffiliationType(contract.getAffiliationType());
        retirement.setAffiliationSubType(contract.getAffiliationSubType());
        retirement.setRetirementDate(request.getRetirementDate());
        retirement.setFiledNumber(contract.getFiledNumber());
        retirement.setIdAffiliate(contract.getIdAffiliate());
        retirement.setUserWhoManagedRetirement(username);
        retirement.setManagementDateTime(LocalDateTime.now());
        retirement.setReason("Finalización de contrato o labor");
        retirement.setModifiedContract(String.valueOf(contract.getIdAffiliate()));

        retirementRepository.save(retirement);
    }

    private void validateRetirementDate(LocalDate retirementDate, Affiliate contract) {
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.minusDays(30);
        LocalDate maxDate = YearMonth.from(today).plusMonths(1).atEndOfMonth();

        if (retirementDate.isBefore(minDate) || retirementDate.isAfter(maxDate)) {
            throw new InvalidRetirementDateException("La fecha de retiro debe estar entre 30 días antes de la fecha actual y el último día del mes siguiente.");
        }

        Affiliate employer = affiliateRepository.findByNitCompanyAndAffiliationType(contract.getNitCompany(), "Empleador")
                .stream().findFirst().orElse(null);

        if (employer != null) {
            if (employer.getCoverageStartDate() != null && retirementDate.isBefore(employer.getCoverageStartDate())) {
                throw new InvalidRetirementDateException("La fecha de retiro no puede ser inferior a la fecha de última cobertura del empleador.");
            }
            if (employer.getAffiliationDate() != null && retirementDate.isBefore(employer.getAffiliationDate().toLocalDate())) {
                throw new InvalidRetirementDateException("La fecha de retiro no puede ser inferior a la fecha de vinculación del empleador.");
            }
        }
    }

    private void validateIndependentWorker(Affiliate contract) {
        String risk = contract.getRisk();
        if (!"4".equals(risk) && !"5".equals(risk)) {
            throw new IllegalArgumentException("El trabajador independiente no pertenece a riesgos 4 o 5.");
        }
    }

    private void validateDependentWorker(Affiliate contract) {
        Affiliate employer = affiliateRepository.findByNitCompanyAndAffiliationType(contract.getNitCompany(), "Empleador")
                .stream().findFirst().orElse(null);

        if (employer == null || !"Activa".equals(employer.getAffiliationStatus())) {
            throw new IllegalArgumentException("El empleador no está afiliado.");
        }
    }

    private ContractListResponseDto convertToDto(Affiliate affiliate) {
        ContractListResponseDto dto = new ContractListResponseDto();
        dto.setTipoVinculacion(affiliate.getAffiliationType());
        dto.setCargo(affiliate.getPosition());
        if (affiliate.getAffiliationDate() != null) {
            dto.setFechaVinculacionArl(affiliate.getAffiliationDate().toLocalDate());
        }
        dto.setFechaInicioContrato(affiliate.getCoverageStartDate());
        dto.setFechaFinContrato(affiliate.getRetirementDate());
        dto.setEstado(affiliate.getAffiliationStatus());
        dto.setAcciones("Retirar");
        return dto;
    }
}