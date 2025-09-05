package com.gal.afiliaciones.application.service.billing.impl;

import com.gal.afiliaciones.application.service.billing.BillingService;
import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.*;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.billing.BillDetailHistoryRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.billing.BillingHistoryRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.BillingRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliationDependentSpecification;
import com.gal.afiliaciones.infrastructure.dto.billing.AffiliationBillingDataDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.gal.afiliaciones.infrastructure.utils.Constant.AFFILIATION_STATUS_ACTIVE;

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

    /**
     * Generar la facturación mensual moviendo las facturas actuales al historial
     * y generando nuevas facturas basadas en las afiliaciones activas.
     */
    @Override
    @Transactional
    public void generateBilling() {
        // Mover facturas actuales al historial antes de reemplazarlas
        moveCurrentBillsToHistory();

        // Mover detalles de facturas actuales al historial antes de reemplazarlas
        moveCurrentBillDetailHistory();

        // Consultar las afiliaciones activas hasta el último día del mes anterior
        List<Affiliate> activeAffiliations = affiliationRepository.findAllByAffiliationStatusAndFiledNumberIsNotNull(AFFILIATION_STATUS_ACTIVE);

        // Generar nueva facturación para cada afiliación activa
        List<Billing> billingList = activeAffiliations.stream().map(this::generateBill).filter(Objects::nonNull).toList();
        billingRepository.saveAll(billingList);
    }

    /**
     * Mover las facturas actuales a la tabla de historial de facturación
     * antes de eliminarlas de la tabla actual.
     */
    private void moveCurrentBillsToHistory() {
        List<Billing> currentBills = billingRepository.findAll();

        //cambiar por un saveAll mapiando de billin a history

        List<BillingHistory> billingHistoryList = currentBills.stream().map(this::convertToBillingHistory).toList();

        // Guardar en el historial de facturación
        billingHistoryRepository.saveAll(billingHistoryList);
        // Después de moverlas al historial, eliminar las facturas actuales
        billingRepository.deleteAll();
    }

    /**
     * Mapea un Billing a un BillingHistory
     *
     * @param billing entrada
     * @return @BillingHistory
     */
    private BillingHistory convertToBillingHistory(Billing billing) {
        return BillingHistory.builder()
                .policy(billing.getPolicy().getId())
                .branch(billing.getBranch())
                .insuranceBranch(billing.getInsuranceBranch())
                .billingEffectiveDateFrom(billing.getBillingEffectiveDateFrom())
                .billingEffectiveDateTo(billing.getBillingEffectiveDateTo())
                .contributorType(billing.getContributorType())
                .contributorId(billing.getContributorId())
                .salary(billing.getSalary())
                .riskRate(billing.getRiskRate())
                .billingDays(billing.getBillingDays())
                .billingAmount(billing.getBillingAmount())
                .paymentPeriod(billing.getPaymentPeriod())
                .movedToHistoryDate(LocalDate.now()) // Registrar la fecha de movimiento al historial
                .build();
    }

    /**
     * Mover el detalle de las facturas actuales a la tabla de historial de detalle de facturación
     * antes de eliminarlas de la tabla actual.
     */
    private void moveCurrentBillDetailHistory() {
        List<BillDetail> currentBillDetail = billDetailRepository.findAll();

        //cambiar por un saveAll mapiando de billin a history

        List<BillDetailHistory> billingHistoryList = currentBillDetail.stream().map(this::convertToBillDetailHistory).toList();

        // Guardar en el historial de facturación
        billDetailHistoryRepository.saveAll(billingHistoryList);
        // Después de moverlas al historial, eliminar las facturas actuales
        billDetailRepository.deleteAll();
    }

    /**
     * Mapea un BillDetail a un BillDetailHistory
     *
     * @param billDetail entrada
     * @return @BillingHistory
     */
    private BillDetailHistory convertToBillDetailHistory(BillDetail billDetail) {
        return BillDetailHistory.builder()
                .policy(billDetail.getPolicy())
                .identificationType(billDetail.getIdentificationType())
                .identificationNumber(billDetail.getIdentificationNumber())
                .billingAmount(billDetail.getBillingAmount())
                .movedToHistoryDate(LocalDate.now()) // Registrar la fecha de movimiento al historial
                .build();
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
}