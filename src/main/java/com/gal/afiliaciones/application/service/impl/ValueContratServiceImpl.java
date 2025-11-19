package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.IValueContratService;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dto.ValueContractDTO;
import com.gal.afiliaciones.infrastructure.dto.ValueContractRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.ValueUserContractDTO;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValueContratServiceImpl implements IValueContratService {

    private final IUserPreRegisterRepository userRepository;
    private final AffiliateRepository affiliateRepository;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final GenericWebClient webClient;

    @Override
    public ValueUserContractDTO getUserContractInfo(String typeDocument, String numberidentificacion) {

        log.info("Consultando datos del usuario y contrato para identificacion {}", numberidentificacion);

        Optional<UserMain> usuarioOptional = userRepository.findByIdentificationTypeAndIdentification(typeDocument, numberidentificacion);

        if(!usuarioOptional.isPresent()){
            log.warn("Usuario no encontrado {}", numberidentificacion);
            throw new RuntimeException("No se pudo obtener el usuario de BBDD");
        }

        UserMain user = usuarioOptional.get();

        List<Affiliate> independientesAffiliate = new ArrayList<>();

        List<Affiliate> affiliates = affiliateRepository.findAllByDocumentTypeAndDocumentNumber(
                typeDocument,numberidentificacion );

        affiliates.forEach(affiliate -> {
            String affiliationType = affiliate.getAffiliationType();

            if ("Trabajador Independiente".equals(affiliationType)) {
                independientesAffiliate.add(affiliate);
            }
        });

        // 2. Construir el DTO del usuario
        ValueUserContractDTO userDto = buildUserContract(user);

        // 3. Consultar y construir contratos
        List<ValueContractDTO> independentContracts = buildIndependentContractsList(user, independientesAffiliate);
        log.info("Se encontraron {} contratos independientes para {}", independentContracts.size(), numberidentificacion);

        userDto.setContracts(independentContracts);
        return userDto;
    }


    // ======================================================
    //                   BUILDER DEPENDIENTE
    // ======================================================
    private List<ValueContractDTO> buildDependentContractsList(UserMain user, List<Affiliate> depedentAffiliate) {

        if (depedentAffiliate == null || depedentAffiliate.isEmpty()) {
            return new ArrayList<>();
        }

        return depedentAffiliate.stream()
                .map(entry -> {


                    Optional<AffiliationDependent> dependentOptional =
                            affiliationDependentRepository.findByFiledNumber(entry.getFiledNumber());


                    if (dependentOptional.isPresent()) {
                        AffiliationDependent dep = dependentOptional.get();

                        Optional<Affiliate> affiliateEmployer = affiliateRepository.findByFiledNumber(
                                String.valueOf(dep.getIdAffiliateEmployer()));


                        return ValueContractDTO.builder()
                                .numContract(dep.getFiledNumber())
                                .contractType(dep.getContractType())
                                .contractStartDate(dep.getStartDate())
                                .contractEndDate(dep.getEndDate())
                                .contractDuration(dep.getDuration())
                                .contractTotalValue(dep.getContractTotalValue())
                                .contractMonthlyValue(dep.getSalary())
                                .contractIbcValue(dep.getIbcPercentage())
                                .contractStatus(entry.getAffiliationStatus())
                                .contractTypeVinculation(entry.getAffiliationSubType())
                                .typeContractUser(Constant.BONDING_TYPE_DEPENDENT)
                                .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }


    // ======================================================
    //                   BUILDER INDEPENDIENTE
    // ======================================================
    private List<ValueContractDTO> buildIndependentContractsList(
            UserMain user,
            List<Affiliate> independentAffiliationsList
    ) {

        if (independentAffiliationsList == null || independentAffiliationsList.isEmpty()) {
            return new ArrayList<>();
        }

        return independentAffiliationsList.stream()
                .map(entry -> {

                    Optional<Affiliation> affiliationOptional =
                            affiliationDetailRepository.findByFiledNumber(entry.getFiledNumber());

                    if (affiliationOptional.isPresent()) {
                        Affiliation aff = affiliationOptional.get();


                        return ValueContractDTO.builder()
                                .numContract(entry.getFiledNumber())
                                .contractType(aff.getContractType())
                                .contractStartDate(aff.getContractStartDate())
                                .contractEndDate(aff.getContractEndDate())
                                .contractDuration(aff.getContractDuration())
                                .contractTotalValue(aff.getContractTotalValue())
                                .contractMonthlyValue(aff.getContractMonthlyValue())
                                .contractIbcValue(aff.getContractIbcValue())
                                .contractStatus(entry.getAffiliationStatus())
                                .contractTypeVinculation(entry.getAffiliationSubType())
                                .typeContractUser(Constant.BONDING_TYPE_INDEPENDENT)
                                .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // ======================================================
    //                   BASE BUILDER USER INFO
    // ======================================================
    private ValueUserContractDTO buildUserContract(UserMain user) {
        return ValueUserContractDTO.builder()
                .identificationType(Constant.CC)
                .email(user.getEmail())
                .age(user.getAge())
                .sex(user.getSex())
                .firstName(user.getFirstName())
                .secondName(user.getSecondName())
                .surname(user.getSurname())
                .secondSurname(user.getSecondSurname())
                .identification(user.getIdentification())
                .phoneNumber(user.getPhoneNumber())
                .contracts(new ArrayList<>())
                .build();
    }


    // ======================================================
    //                       SAVE / UPDATE
    // ======================================================
    @Override
    @Transactional
    public ValueUserContractDTO saveContract(ValueContractRequestDTO dto) {

        if (dto.getNumContract() == null || dto.getNumContract().isEmpty()) {
            throw new IllegalArgumentException("El numero de contrato (numContract/filedNumber) es requerido");
        }

        log.info("Actualizando contrato con numero de contrato: {}", dto.getNumContract());

        //  boolean isDependent = Constant.BONDING_TYPE_DEPENDENT.equalsIgnoreCase(dto.getTypeContractUser());
        boolean isIndependent = Constant.BONDING_TYPE_INDEPENDENT.equalsIgnoreCase(dto.getTypeContractUser());

        if (!isIndependent) {
            throw new IllegalArgumentException("El tipo de contrato no es valido: " + dto.getTypeContractUser());
        }

        String identification = null;
        String typeDocument = null;

       /* if (isDependent) {
            identification = saveDependent(dto);
            AffiliationDependent dependent = affiliationDependentRepository
                    .findByFiledNumber(dto.getNumContract())
                    .orElseThrow(() -> new RuntimeException("Contrato dependiente no encontrado"));
            typeDocument = dependent.getIdentificationDocumentType();
        } else*/
        identification = saveIndependent(dto);
        Affiliation affiliation = affiliationDetailRepository
                .findByFiledNumber(dto.getNumContract())
                .orElseThrow(() -> new RuntimeException("Contrato independiente no encontrado"));
        typeDocument = affiliation.getIdentificationDocumentType();

        if (identification == null || typeDocument == null) {
            throw new RuntimeException("No se pudo obtener la identificacion del usuario");
        }

        return getUserContractInfo(typeDocument, identification);
    }


    // ======================================================
    //               SAVE TRABAJADOR DEPENDIENTE
    // ======================================================
    /*private String saveDependent(ValueContractRequestDTO contractDto) {

        Affiliate affiliateAux = affiliateRepository
                .findByFiledNumber(contractDto.getNumContract())
                .orElseThrow(() -> new RuntimeException("Contrato independiente no encontrado con numero: " + contractDto.getNumContract()));

        if (affiliateAux.getAffiliationStatus().equals(Constant.AFFILIATION_STATUS_ACTIVE)) {
            throw new IllegalArgumentException("Este contrato esta activo, por lo tanto no se puede modificar");
        }

        AffiliationDependent dependent = affiliationDependentRepository
                .findByFiledNumber(contractDto.getNumContract())
                .orElseThrow(() -> new RuntimeException("Contrato dependiente no encontrado con numero: " + contractDto.getNumContract()));

        boolean modified = false;

        // PASO 1: Validar y actualizar fechas (startDate debe ser menor que endDate)
        if (contractDto.getContractStartDate() != null && contractDto.getContractEndDate() != null) {
            if (contractDto.getContractStartDate().isAfter(contractDto.getContractEndDate())) {
                throw new IllegalArgumentException("La fecha de inicio debe ser menor que la fecha de fin");
            }

            boolean datesChanged = false;

            if (!contractDto.getContractStartDate().equals(dependent.getStartDate())) {
                dependent.setStartDate(contractDto.getContractStartDate());
                datesChanged = true;
            }

            if (!contractDto.getContractEndDate().equals(dependent.getEndDate())) {
                dependent.setEndDate(contractDto.getContractEndDate());
                datesChanged = true;
            }

            if (datesChanged) {
                // Calcular y actualizar duration basado en las fechas
                dependent.setDuration(calculateDuration(dependent.getStartDate(), dependent.getEndDate()));
                modified = true;
            }
        }

        // PASO 2: Orden de prioridad para valores
        BigDecimal currentMonthlyValue = dependent.getSalary();
        BigDecimal currentTotalValue = dependent.getContractTotalValue();

        BigDecimal salarioMinimo = obtenerSalarioMinimoVigente();
        int months = calculateMonthsFromDates(dependent.getStartDate(), dependent.getEndDate());

        // PRIORIDAD 1: Si contractMonthlyValue cambio, recalcular todo
        if (contractDto.getContractMonthlyValue() != null &&
                !contractDto.getContractMonthlyValue().equals(currentMonthlyValue)) {

            // Validar que contractMonthlyValue no sea menor al salario minimo
            if (contractDto.getContractMonthlyValue().compareTo(salarioMinimo) < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El valor mensual del contrato no puede ser menor al salario minimo.");
            }

            dependent.setSalary(contractDto.getContractMonthlyValue());

            BigDecimal meses = new BigDecimal(months);
            BigDecimal calculatedTotalValue = contractDto.getContractMonthlyValue().multiply(meses);

            dependent.setContractTotalValue(calculatedTotalValue);

            // Calcular IBC (40% del monthly, minimo salario minimo)
            BigDecimal ibc = calculateIBC(contractDto.getContractMonthlyValue());
            dependent.setContractIbcValue(ibc);

            modified = true;
        }
        // PRIORIDAD 2: Si contractMonthlyValue NO cambio Y contractTotalValue cambio
        else if (contractDto.getContractTotalValue() != null &&
                !contractDto.getContractTotalValue().equals(currentTotalValue) &&
                (contractDto.getContractMonthlyValue() == null ||
                        contractDto.getContractMonthlyValue().equals(currentMonthlyValue))) {

            // Recalcular contractMonthlyValue basado en totalValue y duration
            if (months > 0) {
                BigDecimal newMonthlyValue = contractDto.getContractTotalValue().divide(new BigDecimal(months), 2, java.math.RoundingMode.HALF_UP);

                // **ACUERDO:** No se valida contra el salario minimo aqui.

                dependent.setSalary(newMonthlyValue);
                dependent.setContractTotalValue(contractDto.getContractTotalValue());

                // Recalcular IBC (aplicando la regla de minimo SMLMV dentro de calculateIBC)
                BigDecimal ibc = calculateIBC(newMonthlyValue);
                dependent.setContractIbcValue(ibc);

                modified = true;
            }
        }

        if (modified) {
            affiliationDependentRepository.save(dependent);
            log.info("Contrato dependiente actualizado correctamente.");
        }

        return dependent.getIdentificationDocumentNumber();
    }
*/

    // ======================================================
    //               SAVE TRABAJADOR INDEPENDIENTE
    // ======================================================
    private String saveIndependent(ValueContractRequestDTO contractDto) {
        Affiliate affiliateAux = affiliateRepository
                .findByFiledNumber(contractDto.getNumContract())
                .orElseThrow(() -> new RuntimeException("Contrato independiente no encontrado con numero: " + contractDto.getNumContract()));

        validateAffiliateSubType(affiliateAux.getAffiliationSubType());
        validateAffiliateStatus(affiliateAux.getAffiliationStatus());

        Affiliation affiliation = affiliationDetailRepository
                .findByFiledNumber(contractDto.getNumContract())
                .orElseThrow(() -> new RuntimeException("Contrato independiente no encontrado con numero: " + contractDto.getNumContract()));

        boolean modified = updateContractDates(contractDto, affiliation);
        modified = updateContractValues(contractDto, affiliation) || modified;

        if (modified) {
            affiliationDetailRepository.save(affiliation);
            log.info("Contrato independiente actualizado correctamente.");
        }

        return affiliation.getIdentificationDocumentNumber();
    }

    private void validateAffiliateSubType(String affiliationSubType) {
        if (affiliationSubType != null 
                && !affiliationSubType.equalsIgnoreCase(Constant.AFFILIATION_SUBTYPE_TAXI_DRIVER)
                && !affiliationSubType.equalsIgnoreCase(Constant.AFFILIATION_SUBTYPE_VOLUNTARIO)
                && !affiliationSubType.equalsIgnoreCase(Constant.AFFILIATION_SUBTYPE_PRESTACION_DE_SERVICIOS)
                && !affiliationSubType.equalsIgnoreCase(Constant.AFFILIATION_SUBTYPE_CONSEJAL_EDIL)) {
            throw new IllegalArgumentException("No se puede editar por que el usuario no es un independiente autogestionado");
        }
    }

    private void validateAffiliateStatus(String affiliationStatus) {
        if (Constant.AFFILIATION_STATUS_ACTIVE.equals(affiliationStatus)) {
            throw new IllegalArgumentException("Este contrato esta activo, por lo tanto no se puede modificar");
        }
    }

    private boolean updateContractDates(ValueContractRequestDTO contractDto, Affiliation affiliation) {
        if (contractDto.getContractStartDate() == null || contractDto.getContractEndDate() == null) {
            return false;
        }

        if (contractDto.getContractStartDate().isAfter(contractDto.getContractEndDate())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser menor que la fecha de fin");
        }

        boolean datesChanged = false;
        if (!contractDto.getContractStartDate().equals(affiliation.getContractStartDate())) {
            affiliation.setContractStartDate(contractDto.getContractStartDate());
            datesChanged = true;
        }

        if (!contractDto.getContractEndDate().equals(affiliation.getContractEndDate())) {
            affiliation.setContractEndDate(contractDto.getContractEndDate());
            datesChanged = true;
        }

        if (datesChanged) {
            affiliation.setContractDuration(calculateDuration(affiliation.getContractStartDate(), affiliation.getContractEndDate()));
            return true;
        }

        return false;
    }

    private boolean updateContractValues(ValueContractRequestDTO contractDto, Affiliation affiliation) {
        BigDecimal currentMonthlyValue = affiliation.getContractMonthlyValue();
        BigDecimal currentTotalValue = affiliation.getContractTotalValue();

        if (isMonthlyValueChanged(contractDto, currentMonthlyValue)) {
            return updateFromMonthlyValue(contractDto, affiliation);
        }

        if (isTotalValueChanged(contractDto, currentMonthlyValue, currentTotalValue)) {
            return updateFromTotalValue(contractDto, affiliation);
        }

        return false;
    }

    private boolean isMonthlyValueChanged(ValueContractRequestDTO contractDto, BigDecimal currentMonthlyValue) {
        return contractDto.getContractMonthlyValue() != null
                && !contractDto.getContractMonthlyValue().equals(currentMonthlyValue);
    }

    private boolean isTotalValueChanged(ValueContractRequestDTO contractDto, BigDecimal currentMonthlyValue, BigDecimal currentTotalValue) {
        return contractDto.getContractTotalValue() != null
                && !contractDto.getContractTotalValue().equals(currentTotalValue)
                && (contractDto.getContractMonthlyValue() == null
                        || contractDto.getContractMonthlyValue().equals(currentMonthlyValue));
    }

    private boolean updateFromMonthlyValue(ValueContractRequestDTO contractDto, Affiliation affiliation) {
        BigDecimal salarioMinimo = obtenerSalarioMinimoVigente();
        if (contractDto.getContractMonthlyValue().compareTo(salarioMinimo) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El valor mensual del contrato no puede ser menor al salario minimo.");
        }

        int months = calculateMonthsFromDates(affiliation.getContractStartDate(), affiliation.getContractEndDate());
        affiliation.setContractMonthlyValue(contractDto.getContractMonthlyValue());

        BigDecimal meses = new BigDecimal(months);
        BigDecimal calculatedTotalValue = contractDto.getContractMonthlyValue().multiply(meses);
        affiliation.setContractTotalValue(calculatedTotalValue);

        BigDecimal ibc = calculateIBC(contractDto.getContractMonthlyValue());
        affiliation.setContractIbcValue(ibc);

        return true;
    }

    private boolean updateFromTotalValue(ValueContractRequestDTO contractDto, Affiliation affiliation) {
        int months = calculateMonthsFromDates(affiliation.getContractStartDate(), affiliation.getContractEndDate());
        if (months <= 0) {
            return false;
        }

        BigDecimal newMonthlyValue = contractDto.getContractTotalValue()
                .divide(new BigDecimal(months), 2, java.math.RoundingMode.HALF_UP);

        affiliation.setContractMonthlyValue(newMonthlyValue);
        affiliation.setContractTotalValue(contractDto.getContractTotalValue());

        BigDecimal ibc = calculateIBC(newMonthlyValue);
        affiliation.setContractIbcValue(ibc);

        return true;
    }


    // ======================================================
    //              METODOS AUXILIARES
    // ======================================================

    private String calculateDuration(LocalDate start, LocalDate end) {
        Period p = Period.between(start, end);
        int totalMonths = (p.getYears() * 12) + p.getMonths();
        return "Meses: " + totalMonths + ". Días: " + p.getDays();
    }

    private BigDecimal calculateIBC(BigDecimal salario) {
        BigDecimal ibc = salario.multiply(Constant.PERCENTAGE_40);
        BigDecimal salarioMinimo = obtenerSalarioMinimoVigente();
        BigDecimal maxIbc = salarioMinimo.multiply(new BigDecimal("25"));
        
        // Si el IBC calculado es menor que el SMLMV, retorna el SMLMV.
        if (ibc.compareTo(salarioMinimo) < 0) {
            return salarioMinimo;
        }
        // Si el IBC calculado es mayor que 25 veces el SMLMV, retorna 25 veces el SMLMV.
        if (ibc.compareTo(maxIbc) > 0) {
            return maxIbc;
        }
        return ibc;
    }

    /**
     * Obtiene el salario minimo legal vigente (SMLMV) para el año actual.
     */
    private BigDecimal obtenerSalarioMinimoVigente() {
        try {
            int currentYear = LocalDate.now().getYear();
            SalaryDTO salaryDTO = webClient.getSmlmvByYear(currentYear);
            if (salaryDTO == null || salaryDTO.getValue() == null) {
                log.warn("No se pudo obtener el salario minimo para el ano {}, usando valor por defecto", currentYear);
                return new BigDecimal("1423500.00");
            }
            return new BigDecimal(salaryDTO.getValue());
        } catch (Exception e) {
            log.error("Error al obtener el salario minimo vigente, usando valor por defecto", e);
            return new BigDecimal("1423500.00");
        }
    }

    /**
     * Calcula el numero de meses entre dos fechas.
     */
    private int calculateMonthsFromDates(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 1;
        }

        Period period = Period.between(startDate, endDate);
        int totalMonths = (period.getYears() * 12) + period.getMonths();
        int days = period.getDays();

        if (days > 0) {
            if (days >= 15) {
                totalMonths += 1;
            }
        }

        return Math.max(totalMonths, 1);
    }
}