package com.gal.afiliaciones.application.service.updatedatesemployee.impl;

import com.gal.afiliaciones.application.service.updatedatesemployee.DateUpdateService;
import com.gal.afiliaciones.config.ex.DateUpdateException;
import com.gal.afiliaciones.domain.model.ObservationsAffiliation;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.ConsultLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.LegalRepresentativeResponse;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.observationsaffiliation.ObservationsAffiliationRepository;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import com.gal.afiliaciones.config.ex.Error;

@Service
@RequiredArgsConstructor
public class DateUpdateServiceImpl implements DateUpdateService {

    private static final String NIT_ESPECIAL = "899999061";
    private static final String REQUIRED_TYPE_AND_NUMBER_IDENTIFICATION = "Tipo y numero de identificacion son requeridos.";
    private static final String EMPLOYEE = "EMPLEADOR";
    private static final String NOT_FOUND_LINKS_BY_TYPE_IDENTIFICATION = "No se encontraron vinculaciones para la identificacion proporcionada.";
    private static final String DEPENDIENTE = "TRABAJADOR DEPENDIENTE";
    private static final String ACTIVO = "ACTIVO";
    private static final String INDEPENDIENTE = "TRABAJADOR INDEPENDIENTE";
    private static final String CODE_723 = "723";
    private static final String TYPE_LINK_NOT_VALID = "Tipo de vinculacion no valido: ";
    private static final String NEW_DATE_NOT_IS_FUTURE = "La nueva fecha no puede ser futura.";


    private final AffiliateRepository affiliateRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final ObservationsAffiliationRepository observationsRepository;
    private final ConsultEmployerClient consultEmployerClient;
    private final ConsultLegalRepresentativeClient consultLegalRepresentativeClient;

    @Override
    public List<VinculacionDTO> consultLinks(VinculacionQueryDTO query) {
        if (query.getTipoIdentificacion() == null || query.getNumeroIdentificacion() == null) {
            throw new DateUpdateException(Error.Type.INVALID_ARGUMENT, REQUIRED_TYPE_AND_NUMBER_IDENTIFICATION);
        }

        List<Affiliate> affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(
                query.getTipoIdentificacion(), query.getNumeroIdentificacion());

        List<AffiliationDependent> dependents = affiliationDependentRepository.findByMainAffiliateIdentification(
                query.getTipoIdentificacion(), query.getNumeroIdentificacion());

        List<VinculacionDTO> vinculaciones = Stream.concat(
                affiliates.stream().map(this::mapAffiliateToVinculacionDto),
                dependents.stream().map(this::mapDependentToVinculacionDto)
        ).toList();

        // Manejo del caso especial NIT 899999061
        vinculaciones.stream()
                .filter(v -> EMPLOYEE.equals(v.getTipoVinculacion()))
                .findAny()
                .ifPresent(vinculacion -> {
                    Affiliate affiliate = affiliates.get(0);
                    if (NIT_ESPECIAL.equals(affiliate.getNitCompany())) {
                        List<EmployerResponse> subEmpresasResponse = consultEmployerClient.consult(
                                affiliate.getDocumenTypeCompany(),
                                affiliate.getNitCompany(),
                                null // Consultar todas las subempresas
                        ).block();

                        if (subEmpresasResponse != null) {
                            vinculacion.setSubEmpresas(
                                    subEmpresasResponse.stream()
                                            .map(sub -> {
                                                SubEmpresaDTO dto = new SubEmpresaDTO();
                                                dto.setIdSubEmpresa(sub.getIdSubEmpresa());
                                                dto.setNombre(sub.getRazonSocialSubempresa());
                                                return dto;
                                            })
                                            .toList()
                            );
                        }
                    }
                });


        if (vinculaciones.isEmpty()) {
            throw new DateUpdateException(Error.Type.VINCULACION_NOT_FOUND, NOT_FOUND_LINKS_BY_TYPE_IDENTIFICATION);
        }

        return vinculaciones;
    }

    private VinculacionDTO mapAffiliateToVinculacionDto(Affiliate affiliate) {
        VinculacionDTO dto = new VinculacionDTO();
        dto.setIdVinculacion(affiliate.getIdAffiliate());
        // La distinción entre EMPLEADOR e INDEPENDIENTE se basa en affiliationType
        dto.setTipoVinculacion(affiliate.getAffiliationType());
        dto.setContratante(affiliate.getCompany());
        dto.setCargo(affiliate.getPosition());
        dto.setFechaVinculacion(affiliate.getAffiliationDate() != null ? affiliate.getAffiliationDate().toLocalDate() : null);
        dto.setFechaCobertura(affiliate.getCoverageStartDate());
        dto.setEstado(affiliate.getAffiliationStatus());
        return dto;
    }

    private VinculacionDTO mapDependentToVinculacionDto(AffiliationDependent dependent) {
        VinculacionDTO dto = new VinculacionDTO();
        dto.setIdVinculacion(dependent.getId());
        dto.setTipoVinculacion(DEPENDIENTE);
        // El contratante es la empresa del afiliado principal
        affiliateRepository.findById(dependent.getIdAffiliateEmployer())
                .ifPresent(employer -> dto.setContratante(employer.getCompany()));
        dto.setCargo(null); // Los dependientes no tienen cargo
        dto.setFechaVinculacion(dependent.getStartDate());
        dto.setFechaCobertura(dependent.getCoverageDate());
        dto.setEstado(ACTIVO); // Asumiendo que los dependientes recuperados están activos
        return dto;
    }

    @Override
    @Transactional
    public void updateDateCoverage(UpdateCoverageDateDTO updateDto) {
        validateRequest(updateDto);

        switch (updateDto.getTipoVinculacion().toUpperCase()) {
            case DEPENDIENTE:
                updateDependentCoverageDate(updateDto);
                break;
            case CODE_723,INDEPENDIENTE:
                updateIndependentCoverageDate(updateDto);
                break;
            case EMPLOYEE:
                updateEmployerCoverageDate(updateDto);
                break;
            default:
                throw new DateUpdateException(Error.Type.INVALID_ARGUMENT, TYPE_LINK_NOT_VALID + updateDto.getTipoVinculacion());
        }

        // Registrar causal y observaciones
        saveObservation(updateDto);
    }

    private void updateDependentCoverageDate(UpdateCoverageDateDTO dto) {
        AffiliationDependent dependent = affiliationDependentRepository.findById(dto.getIdVinculacion())
                .orElseThrow(() -> new DateUpdateException(Error.Type.VINCULACION_NOT_FOUND, "Dependiente no encontrado con ID: " + dto.getIdVinculacion()));

        // Validar que el funcionario no actualice sus propios datos
        validateNotSelfUpdate(dependent.getIdentificationDocumentNumber());

        Affiliate employer = affiliateRepository.findById(dependent.getIdAffiliateEmployer())
                .orElseThrow(() -> new DateUpdateException(Error.Type.VINCULACION_NOT_FOUND, "Empleador del dependiente no encontrado."));

        // Lógica de validación para dependientes:
        // 1. Relación activa (asumimos que si se encuentra, está activa, podría requerir chequeo de estado)
        // 2. Fecha >= ingreso empleador
        if (employer.getAffiliationDate() != null && dto.getNuevaFechaCobertura().isBefore(employer.getAffiliationDate().toLocalDate())) {
            throw new DateUpdateException(Error.Type.FECHA_INVALIDA_ANTERIOR_INGRESO, "La nueva fecha no puede ser anterior a la fecha de ingreso del empleador.");
        }
        // 3. Fecha <= fecha novedad (fecha actual)
        if (dto.getNuevaFechaCobertura().isAfter(java.time.LocalDate.now())) {
            throw new DateUpdateException(Error.Type.FECHA_INVALIDA_FUTURA, NEW_DATE_NOT_IS_FUTURE);
        }

        dependent.setCoverageDate(dto.getNuevaFechaCobertura());
        affiliationDependentRepository.save(dependent);
    }

    private void updateIndependentCoverageDate(UpdateCoverageDateDTO dto) {
        Affiliate independent = affiliateRepository.findById(dto.getIdVinculacion())
                .orElseThrow(() -> new DateUpdateException(Error.Type.VINCULACION_NOT_FOUND, "Independiente no encontrado con ID: " + dto.getIdVinculacion()));

        validateNotSelfUpdate(independent.getDocumentNumber());

        // Lógica de validación para independientes:
        // 1. Contrato activo (chequeo de estado)
        if (!ACTIVO.equalsIgnoreCase(independent.getAffiliationStatus())) {
            throw new DateUpdateException(Error.Type.VINCULACION_NO_ACTIVA, "La vinculación del independiente no está activa.");
        }
        // 2. Fecha >= inicio contrato (affiliationDate)
        if (independent.getAffiliationDate() != null && dto.getNuevaFechaCobertura().isBefore(independent.getAffiliationDate().toLocalDate())) {
            throw new DateUpdateException(Error.Type.FECHA_INVALIDA_ANTERIOR_INGRESO, "La nueva fecha no puede ser anterior al inicio del contrato.");
        }
        // 3. Fecha <= fecha novedad (fecha actual)
        if (dto.getNuevaFechaCobertura().isAfter(java.time.LocalDate.now())) {
            throw new DateUpdateException(Error.Type.FECHA_INVALIDA_FUTURA,NEW_DATE_NOT_IS_FUTURE);
        }

        independent.setCoverageStartDate(dto.getNuevaFechaCobertura());
        affiliateRepository.save(independent);
    }

    private void updateEmployerCoverageDate(UpdateCoverageDateDTO dto) {
        Affiliate employer = affiliateRepository.findById(dto.getIdVinculacion())
                .orElseThrow(() -> new DateUpdateException(Error.Type.VINCULACION_NOT_FOUND, "Empleador no encontrado con ID: " + dto.getIdVinculacion()));

        validateNotSelfUpdate(employer.getDocumentNumber());

        // Lógica de validación para empleadores:
        // 1. Relación activa
        if (!ACTIVO.equalsIgnoreCase(employer.getAffiliationStatus())) {
            throw new  DateUpdateException(Error.Type.VINCULACION_NO_ACTIVA, "La vinculación del empleador no está activa.");
        }
        // 2. Fecha <= fecha novedad (fecha actual)
        if (dto.getNuevaFechaCobertura().isAfter(java.time.LocalDate.now())) {
            throw new DateUpdateException(Error.Type.FECHA_INVALIDA_FUTURA, NEW_DATE_NOT_IS_FUTURE);
        }
        // 3. Si empresa inactiva -> nueva fecha <= inactivación (asumiendo que retirementDate es la fecha de inactivación)
        if (employer.getRetirementDate() != null && dto.getNuevaFechaCobertura().isAfter(employer.getRetirementDate())) {
            throw new DateUpdateException(Error.Type.FECHA_POSTERIOR_INACTIVACION, "Si la empresa está inactiva, la nueva fecha no puede ser posterior a la fecha de inactivación.");
        }
        // 4. Si nueva fecha > menor fecha de cobertura de sus dependientes -> rechazar
        affiliateRepository.findMinCoverageDateOfDependents(employer.getIdAffiliate()).ifPresent(minDate -> {
            if (dto.getNuevaFechaCobertura().isAfter(minDate)) {
                throw new DateUpdateException(Error.Type.FECHA_POSTERIOR_DEPENDIENTE, "La nueva fecha no puede ser posterior a la menor fecha de cobertura de sus dependientes (" + minDate + ").");
            }
        });

        // Lógica de actualización
        employer.setCoverageStartDate(dto.getNuevaFechaCobertura());
        // 5. Si nueva fecha < vinculación -> replicar en ambas fechas
        if (employer.getAffiliationDate() != null && dto.getNuevaFechaCobertura().isBefore(employer.getAffiliationDate().toLocalDate())) {
            employer.setAffiliationDate(dto.getNuevaFechaCobertura().atStartOfDay());
        }

        affiliateRepository.save(employer);
    }

    private void validateRequest(UpdateCoverageDateDTO dto) {
        if (dto.getIdVinculacion() == null || dto.getTipoVinculacion() == null || dto.getNuevaFechaCobertura() == null) {
            throw new DateUpdateException(Error.Type.INVALID_ARGUMENT, "idVinculacion, tipoVinculacion y nuevaFechaCobertura son requeridos.");
        }
    }

    private void saveObservation(UpdateCoverageDateDTO dto) {
        String filedNumber = getFiledNumberForVinculacion(dto.getIdVinculacion(), dto.getTipoVinculacion());

        ObservationsAffiliation obs = new ObservationsAffiliation();
        obs.setFiledNumber(filedNumber);
        obs.setObservations("Actualización de fecha de cobertura. Causal: " + dto.getCausalNovedad() + ". Obs: " + dto.getObservaciones());
        obs.setDate(LocalDateTime.now());

        // Obtener ID del funcionario desde el token JWT
        getOfficialIdFromToken().ifPresent(obs::setIdOfficial);

        observationsRepository.save(obs);
    }

    private String getFiledNumberForVinculacion(Long idVinculacion, String tipoVinculacion) {
        if (tipoVinculacion.equalsIgnoreCase(DEPENDIENTE)) {
            return affiliationDependentRepository.findById(idVinculacion)
                    .map(AffiliationDependent::getFiledNumber)
                    .orElse(null);
        } else {
            return affiliateRepository.findById(idVinculacion)
                    .map(Affiliate::getFiledNumber)
                    .orElse(null);
        }
    }

    private void validateNotSelfUpdate(String documentNumberToUpdate) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt jwt) {
            String loggedInUserDocument = jwt.getClaimAsString("document_number");
            if (documentNumberToUpdate != null && documentNumberToUpdate.equals(loggedInUserDocument)) {
                throw new DateUpdateException(Error.Type.FUNCIONARIO_NO_AUTORIZADO, "Un funcionario no puede actualizar sus propios datos.");
            }
        } else {
            // Fail closed if security context is not as expected
            throw new DateUpdateException(Error.Type.AUTHENTICATION_FAILED, "No se puede verificar la identidad del funcionario.");
        }
    }

    private Optional<Long> getOfficialIdFromToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && authentication.getPrincipal() instanceof Jwt jwt) {
            // Asumiendo que el claim 'official_id' existe y es un número.
            return Optional.ofNullable(jwt.getClaimAsString("official_id")).map(Long::parseLong);
        }
        return Optional.empty();
    }

    @Override
    public VinculacionDetalleDTO getLinksDetail(String tipo, Long id) {
        return switch (tipo.toUpperCase()) {
            case EMPLOYEE , INDEPENDIENTE -> createDetalleFromAffiliate(id, tipo);
            case DEPENDIENTE -> createDetalleFromDependent(id);
            default -> throw new DateUpdateException(Error.Type.INVALID_ARGUMENT, TYPE_LINK_NOT_VALID + tipo);
        };
    }

    private VinculacionDetalleDTO createDetalleFromAffiliate(Long id, String tipo) {
        Affiliate affiliate = affiliateRepository.findById(id)
                .orElseThrow(() -> new DateUpdateException(Error.Type.VINCULACION_NOT_FOUND, tipo + " no encontrado con ID: " + id));

        VinculacionDetalleDTO dto = new VinculacionDetalleDTO();
        dto.setTipoDocumentoIdentificacion(affiliate.getDocumentType());
        dto.setNumeroIdentificacion(affiliate.getDocumentNumber());
        dto.setNombreCompletoORazonSocial(affiliate.getCompany()); // Para empleador e independiente, el nombre está en 'company'
        dto.setCorreoElectronico(null); // Campo no disponible directamente en Affiliate
        dto.setFechaAfiliacion(affiliate.getAffiliationDate() != null ? affiliate.getAffiliationDate().toLocalDate() : null);
        dto.setFechaInicioCobertura(affiliate.getCoverageStartDate());

        // El teléfono y la dirección no están en la entidad Affiliate, se requeriría otra fuente de datos.
        // Se dejan en null por ahora.
        dto.setDireccionCompleta(null);
        dto.setTelefono1(null);
        dto.setTelefono2(null);

        if (EMPLOYEE.equalsIgnoreCase(tipo)) {
            // Suponiendo que el DV no está en la tabla, se deja null.
            dto.setDigitoVerificacion(null);

            // Llamada al servicio del representante legal
            List<LegalRepresentativeResponse> repResponses = consultLegalRepresentativeClient.consult(
                    affiliate.getDocumenTypeCompany(), affiliate.getNitCompany(), null).block();

            if (repResponses != null && !repResponses.isEmpty()) {
                LegalRepresentativeResponse rep = repResponses.get(0); // Tomar el primero
                dto.setTipoDocumentoRepLegal(rep.getIdTipoDoc());
                dto.setNumeroIdentificacionRepLegal(rep.getIdPersona());
                dto.setNombreCompletoRepLegal(String.join(" ", rep.getNombre1(), rep.getNombre2(), rep.getApellido1(), rep.getApellido2()).replaceAll("\\s+", " ").trim());
            }
        }

        return dto;
    }

    private VinculacionDetalleDTO createDetalleFromDependent(Long id) {
        AffiliationDependent dependent = affiliationDependentRepository.findById(id)
                .orElseThrow(() -> new DateUpdateException(Error.Type.VINCULACION_NOT_FOUND, "Dependiente no encontrado con ID: " + id));

        VinculacionDetalleDTO dto = new VinculacionDetalleDTO();
        dto.setTipoDocumentoIdentificacion(dependent.getIdentificationDocumentType());
        dto.setNumeroIdentificacion(dependent.getIdentificationDocumentNumber());
        dto.setNombreCompletoORazonSocial(String.join(" ", dependent.getFirstName(), dependent.getSecondName(), dependent.getSurname(), dependent.getSecondSurname()).replaceAll("\\s+", " ").trim());
        dto.setDireccionCompleta(dependent.getAddress());
        dto.setTelefono1(dependent.getPhone1());
        dto.setTelefono2(dependent.getPhone2());
        dto.setCorreoElectronico(dependent.getEmail());
        dto.setFechaAfiliacion(dependent.getStartDate());
        dto.setFechaInicioCobertura(dependent.getCoverageDate());

        return dto;
    }
}
