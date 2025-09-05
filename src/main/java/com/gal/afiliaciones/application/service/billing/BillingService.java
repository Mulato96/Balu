package com.gal.afiliaciones.application.service.billing;

import com.gal.afiliaciones.domain.model.Billing;

import java.time.LocalDate;
import java.util.List;

public interface BillingService {

    // Generar la facturación mensual
    void generateBilling();

    // Obtener todas las facturas
    List<Billing> getAllBills();

    // Obtener facturas por identificación del aportante
    List<Billing> getBillsByContributor(String contributorIdNumber);

    // Obtener facturas por rango de fechas
    List<Billing> getBillsByDates(LocalDate fromDate, LocalDate toDate);

    // Obtener una factura por su ID
    Billing getBillById(Long id);
}