package com.gal.afiliaciones.application.service.billing.impl;

import static com.gal.afiliaciones.infrastructure.utils.Constant.AFFILIATION_STATUS_ACTIVE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.gal.afiliaciones.application.service.billing.BillingService;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.BillDetail;
import com.gal.afiliaciones.domain.model.Billing;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.billing.BillDetailHistoryRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.billing.BillingHistoryRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillingRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dto.billing.AffiliationBillingDataDTO;
import com.gal.afiliaciones.infrastructure.enums.TypeCompanySettlement;
import com.gal.afiliaciones.infrastructure.enums.TypeCutSettlement;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingServiceImpl implements BillingService {

    private final BillingRepository billingRepository;
    private final BillingHistoryRepository billingHistoryRepository;
    private final AffiliateRepository affiliationRepository;
    private final PolicyRepository policyRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository iAffiliationEmployerDomesticServiceIndependentRepository;
    private final GenericWebClient webClient;
    private final AffiliationDependentRepository dependentRepository;
    private final BillDetailRepository billDetailRepository;
    private final BillDetailHistoryRepository billDetailHistoryRepository;
    private final CollectProperties collectProperties;

    private Long consecutiveEmployer;
    private Long consecutiveWorker;
    private Map<String, List<Long>> listConsecutiveEmployer = new HashMap<>();


    /**
     * Generar la facturación mensual moviendo las facturas actuales al historial
     * y generando nuevas facturas basadas en las afiliaciones activas.
     */
    @Override
    @Transactional
    public void generateBilling() {

        //asigna los consecutivos de la bd a las variables locales
        consecutive();

        // Consultar las afiliaciones activas hasta el último día del mes anterior
        List<Affiliate> activeAffiliations = findAffiliateByCut(affiliationRepository.findAllByAffiliationStatusAndFiledNumberIsNotNull(AFFILIATION_STATUS_ACTIVE));

        // Generar nueva facturación para cada afiliación activa
        List<Billing> billingList = activeAffiliations.stream().map(this::generateBill).filter(Objects::nonNull).toList();
        billingRepository.saveAll(billingList);
    }

    /**
     * Generar una nueva factura a partir de una afiliación activa.
     *
     * @param affiliate La afiliación activa para generar la factura.
     * @return La nueva factura generada.
     */
    private Billing generateBill(Affiliate affiliate) {

        // Lógica de negocio para generar la factura basada en la afiliación activa
        List<Policy> policyList = policyRepository.findByIdAffiliate(affiliate.getIdAffiliate());

        //consecutivos
        Long employer = consecutiveEmployer;
        Long worker = consecutiveWorker;


        if(policyList.isEmpty())
                return null;

        Affiliation affiliation = iAffiliationEmployerDomesticServiceIndependentRepository.findByFiledNumber(affiliate.getFiledNumber()).orElse(null);
        AffiliationDependent dependent = dependentRepository.findByFiledNumber(affiliate.getFiledNumber()).orElse(null);

        AffiliationBillingDataDTO affiliationBillingDataDTO = new AffiliationBillingDataDTO();
        if(affiliation != null) {
            affiliationBillingDataDTO.setContractMonthlyValue(affiliation.getContractMonthlyValue());
            affiliationBillingDataDTO.setPrice(affiliation.getPrice());
        }

        if(dependent != null) {
            affiliationBillingDataDTO.setContractMonthlyValue(dependent.getSalary());
            affiliationBillingDataDTO.setPrice(dependent.getPriceRisk());
        }

        if (affiliation == null && dependent == null)
            return null;

        Policy policy = policyList.get(0);
        Map<Integer, BigDecimal> calculateBillingAmountDependent = calculateBillingAmountDependent(policy.getCode());
        Map.Entry<Integer, BigDecimal> entry = calculateBillingAmountDependent.entrySet().iterator().next();

        BigDecimal calculateBillingAmount =
                calculateBillingAmount(
                        ajustarSalario(
                                affiliationBillingDataDTO.getContractMonthlyValue() != null ?
                                        affiliationBillingDataDTO.getContractMonthlyValue() : BigDecimal.ZERO
                        )
                        , affiliationBillingDataDTO.getPrice() != null ? affiliationBillingDataDTO.getPrice() : BigDecimal.ZERO
                ).add(entry.getValue());

        if (entry.getValue().compareTo(BigDecimal.ZERO) == 0) {
            billDetailRepository.save(
                    BillDetail.builder()
                            .identificationType(affiliate.getDocumentType())
                            .identificationNumber(affiliate.getDocumentNumber())
                            .billingAmount(calculateBillingAmount)
                            .policy(policy)
                            .build()
            );
        }

        if(affiliation != null
                && affiliation.getTypeAffiliation() != null
                && affiliation.getTypeAffiliation().contains(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER)){
            worker = null;
            employer++;
            consecutiveEmployer = employer;
            //guarda los consecutivos del empleador
            listConsecutiveEmployer.computeIfAbsent(affiliate.getNitCompany(), k -> new ArrayList<>()).add(employer);
        }else{
            worker++;
            //se le asigna el consecutivo del empleador
            employer = listConsecutiveEmployer.get(affiliate.getNitCompany())
                    .stream()
                    .findFirst()
                    .orElse(null);
            consecutiveWorker = worker;
        }


        return Billing.builder()
                .policy(policy)
                .branch("90")
                .insuranceBranch("ARP")
                .billingEffectiveDateFrom(policy.getEffectiveDateFrom())
                .billingEffectiveDateTo(policy.getEffectiveDateTo())
                .contributorType(affiliate.getDocumentType())
                .contributorId(affiliate.getDocumentNumber())
                .salary(affiliationBillingDataDTO.getContractMonthlyValue() != null ? affiliationBillingDataDTO.getContractMonthlyValue() : BigDecimal.ZERO)
                .riskRate(affiliationBillingDataDTO.getPrice() != null ? affiliationBillingDataDTO.getPrice() : BigDecimal.ZERO)
                .billingDays(30) // Facturación por 30 días
                .billingAmount(calculateBillingAmount)
                .paymentPeriod(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM")))
                .liquidatedContributors(entry.getKey() == 0 ? 1 : entry.getKey())
                .old(isFirsBilling(affiliate.getDocumentNumber()))
                .typeSettlementCut(typeCut())
                .consecutiveEmployer(employer)
                .consecutiveWorker(worker)
                .build();
    }

    /**
     * Calcular el valor de la facturación basado en el salario y la tarifa de riesgo.
     *
     * @param salary   El salario del afiliado.
     * @param riskRate La tarifa de riesgo aplicada.
     * @return El valor final de la facturación calculada.
     */
    private BigDecimal calculateBillingAmount(BigDecimal salary, BigDecimal riskRate) {
        // Multiplicar el salario por la tarifa de riesgo y dividir por 100
        return salary.multiply(riskRate).divide(new BigDecimal(100)).setScale(2, RoundingMode.HALF_UP);
    }

    // Método privado para ajustar el salario
    private BigDecimal ajustarSalario(BigDecimal salary) {
        //Consultar smlmv
        BigDecimal salarioMinimo = obtenerSalarioMinimoVigente();
        BigDecimal salarioMaximo = salarioMinimo.multiply(new BigDecimal(25));

        if (salary.compareTo(salarioMinimo) < 0) {
            return salarioMinimo;
        } else if (salary.compareTo(salarioMaximo) > 0) {
            return salarioMaximo;
        } else {
            return salary;
        }
    }

    // Método privado para obtener el salario mínimo vigente
    private BigDecimal obtenerSalarioMinimoVigente() {
        return BigDecimal.valueOf(webClient.getSmlmvByYear(LocalDate.now().getYear()).getValue()); // Ejemplo de salario mínimo
    }

    // Implementación del método para obtener todas las facturas
    @Override
    public List<Billing> getAllBills() {
        return billingRepository.findAll();
    }

    // Implementación del método para obtener facturas por identificación del aportante
    @Override
    public List<Billing> getBillsByContributor(String contributorIdNumber) {
        return billingRepository.findByContributorId(contributorIdNumber);
    }

    // Implementación del método para obtener facturas por rango de fechas
    @Override
    public List<Billing> getBillsByDates(LocalDate fromDate, LocalDate toDate) {
        return billingRepository.findByBillingEffectiveDateFromLessThanEqualAndBillingEffectiveDateToGreaterThanEqual(fromDate, toDate);
    }

    // Implementación del método para obtener una factura por su ID
    @Override
    public Billing getBillById(Long id) {
        Optional<Billing> bill = billingRepository.findById(id);
        return bill.orElse(null);
    }

    //Buscar en la tabla AffiliateDependent, los dependientes y hace el calculo del monto
    private Map<Integer, BigDecimal> calculateBillingAmountDependent(String policyCode){

        List<Policy> policyList = policyRepository.findByCode(policyCode);
        AtomicReference<Integer> count = new AtomicReference<>(0);

        BigDecimal calculateBillingAmount = policyList.stream().map(policy -> {
            Affiliate affiliate = affiliationRepository.findByIdAffiliate(policy.getIdAffiliate()).orElseThrow(
                    () -> new AffiliateNotFound("Affiliate not found"));
            Specification<AffiliationDependent> specDependent = AffiliationDependentSpecification.findByFieldNumber(affiliate.getFiledNumber());
            Optional<AffiliationDependent> optionalAffiliationDependent = dependentRepository.findOne(specDependent);

            if(optionalAffiliationDependent.isEmpty())
                return BigDecimal.ZERO;

            AffiliationDependent affiliationDependent = optionalAffiliationDependent.get();

            BigDecimal calculate = calculateBillingAmount(ajustarSalario(affiliationDependent.getSalary() != null ? affiliationDependent.getSalary() : BigDecimal.ZERO), affiliationDependent.getPriceRisk() != null ? affiliationDependent.getPriceRisk() : BigDecimal.ZERO);
            count.updateAndGet(v -> v + 1);

            Policy policyJoin = new Policy();
            policyJoin.setId(policy.getId());
            billDetailRepository.save(
                    BillDetail.builder()
                            .identificationType(affiliationDependent.getIdentificationDocumentType())
                            .identificationNumber(affiliationDependent.getIdentificationDocumentNumber())
                            .billingAmount(calculate)
                            .policy(policyJoin)
                            .build()
            );

            return calculate;
        }).reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(count.get(), calculateBillingAmount);
    }

    //agrupa las afiliaciones por numero de documento y hace la validacion si es primer o segundo corte
    private List<Affiliate> findAffiliateByCut(List<Affiliate> affiliateList){

        int day = LocalDate.now().getDayOfMonth();

        Map<String, List<Affiliate>> listAffiliate = affiliateList.stream()
                .collect(Collectors.groupingBy(Affiliate::getDocumentNumber));

            return listAffiliate.entrySet()
                            .stream()
                            .filter(e -> {
                                if(day == collectProperties.getCutSettlementOne())
                                    return e.getValue().size() <= collectProperties.getNumberMaxLaborRelation();
                                if(day == collectProperties.getCutSettlementTwo())
                                    return e.getValue().size() > collectProperties.getNumberMaxLaborRelation();
                                return false;
                            })
                            .flatMap(e -> e.getValue().stream())
                            .sorted(Comparator.comparing(affiliate -> affiliate.getAffiliationType().contains(Constant.NAME_CONTRIBUTOR_TYPE_EMPLOYER)))
                            .toList();

    }

    //calcula el consecutivo para empleador y trabajador
    private void consecutive(){

        consecutiveEmployer = billingRepository.findConsecutiveEmployer().orElse(1L);
        consecutiveWorker = billingRepository.findConsecutiveWorker().orElse(1L);

    }

    //valida si es la primera liquidacion
    private TypeCompanySettlement isFirsBilling(String contributorId){
        return billingRepository.existsByContributorId(contributorId)
                ? TypeCompanySettlement.A
                : TypeCompanySettlement.N;
    }

    //calcula el tipo de corte(primero o segundo)
    private TypeCutSettlement typeCut(){
        int day = LocalDate.now().getDayOfMonth();
        return day == collectProperties.getCutSettlementOne()
                ? TypeCutSettlement.FIRST_CUT
                : TypeCutSettlement.SECOND_CUT;
    }


}