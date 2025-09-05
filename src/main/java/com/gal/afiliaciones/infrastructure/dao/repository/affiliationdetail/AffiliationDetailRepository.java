package com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.employerreport.EmployerReportDTO;
import com.gal.afiliaciones.infrastructure.dto.officialreport.OfficialReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AffiliationDetailRepository extends JpaRepository<Affiliation, String>, JpaSpecificationExecutor<Affiliation> {

    @Query(value =
            """
                    (SELECT
                        CONCAT_WS('-', dep.identification_type, dep.identification_number) AS identification,
                        CONCAT_WS(' ', dep.first_name, dep.second_name, dep.surname, dep.second_surname) AS fullName,
                        INITCAP(o.nombre_ocupacion) AS occupation,
                        a.affiliation_type AS affiliationType,
                        a.affiliation_date AS affiliationDate,
                        a.affiliation_status AS affiliationStatus
                    FROM affiliate a
                    LEFT JOIN affiliation_dependent dep
                        ON dep.filed_number = a.filed_number
                    LEFT JOIN tmp_ocupaciones o
                        ON o.id_ocupacion = dep.id_occupation
                    WHERE a.nit_company = :nitCompany
                        AND a.affiliation_type IN :affiliationTypes
                        AND a.affiliation_date BETWEEN CAST(:startDate AS timestamp) AND CAST(:endDate AS timestamp)
                        AND dep.identification_number IS NOT NULL
                        AND dep.identification_number <> ''
                    )
                    UNION
                    (SELECT
                        CONCAT_WS('-', ind.identification_document_type, ind.identification_document_number) AS identification,
                        CONCAT_WS(' ', ind.first_name, ind.second_name, ind.surname, ind.second_surname) AS fullName,
                        ind.occupation AS occupation,
                        a.affiliation_type AS affiliationType,
                        a.affiliation_date AS affiliationDate,
                        a.affiliation_status AS affiliationStatus
                    FROM affiliate a
                    LEFT JOIN affiliation_detail ind
                        ON ind.filed_number = a.filed_number
                    WHERE a.nit_company = :nitCompany
                        AND a.affiliation_type IN :affiliationTypes
                        AND a.affiliation_date BETWEEN CAST(:startDate AS timestamp) AND CAST(:endDate AS timestamp)
                        AND ind.identification_document_number IS NOT NULL
                        AND ind.identification_document_number <> ''
                    )
                    """, nativeQuery = true)
    Page<EmployerReportDTO> findEmployerReportByWorkers(
            @Param("nitCompany") String nitCompany,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("affiliationTypes") List<String> affiliationTypes,
            Pageable pageable);


    @Query(value =
            """
                    SELECT
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN CONCAT_WS('-', dep.identification_type, dep.identification_number)
                            ELSE CONCAT_WS('-', ind.identification_document_type, ind.identification_document_number)
                        END AS identification,
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN CONCAT_WS(' ', dep.first_name, dep.second_name, dep.surname, dep.second_surname)
                            ELSE CONCAT_WS(' ', ind.first_name, ind.second_name, ind.surname, ind.second_surname)
                        END AS fullName,
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN o.nombre_ocupacion
                            ELSE ind.occupation
                        END AS occupation,
                        a.affiliation_type AS affiliationType,
                        a.novelty_type AS noveltyType,
                        a.affiliation_date AS affiliationDate,
                        a.affiliation_status AS affiliationStatus
                    FROM affiliate a
                    LEFT JOIN affiliation_detail ind
                    ON ind.filed_number = a.filed_number
                    LEFT JOIN affiliation_dependent dep
                    ON dep.filed_number = a.filed_number
                    JOIN tmp_ocupaciones o
                    ON o.id_ocupacion = dep.id_occupation
                    WHERE a.nit_company = :nitCompany
                    AND a.affiliation_date BETWEEN CAST(:startDate AS timestamp) AND CAST(:endDate AS timestamp)
                    AND a.novelty_type IN :noveltyTypes
                    AND a.affiliation_type IN ('Trabajador Dependiente', 'Trabajador Independiente');
                    """, nativeQuery = true)
    Page<EmployerReportDTO> findEmployerReportByNovelty(
            @Param("nitCompany") String nitCompany,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("noveltyTypes") List<String> noveltyTypes,
            Pageable pageable);

    @Query(value =
            """
                    SELECT
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN CONCAT_WS('-', dep.identification_type, dep.identification_number)
                            ELSE CONCAT_WS('-', ind.identification_document_type, ind.identification_document_number)
                        END AS identification,
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN CONCAT_WS(' ', dep.first_name, dep.second_name, dep.surname, dep.second_surname)
                            ELSE CONCAT_WS(' ', ind.first_name, ind.second_name, ind.surname, ind.second_surname)
                        END AS fullName,
                        o.nombre_ocupacion AS occupation,
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN '' || dep.age
                            ELSE ind.age
                        END AS age,
                        a.affiliation_type AS affiliationType,
                        a.affiliation_status AS affiliationStatus,
                        a.affiliation_date AS affiliationDate,
                        d.nombre_departamento AS department,
                        m.nombre_municipio AS city
                    FROM affiliate a
                    LEFT JOIN affiliation_detail ind
                    ON ind.filed_number = a.filed_number
                    LEFT JOIN affiliation_dependent dep
                    ON dep.filed_number = a.filed_number
                    LEFT JOIN tmp_departamentos d
                    ON
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN d.id_departamento = dep.id_department
                            ELSE d.nombre_departamento = UPPER(ind.department)
                        END
                    LEFT JOIN tmp_municipality m
                    ON
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN m.id_municipio = dep.id_city_municipality
                            ELSE m.nombre_municipio = UPPER(ind.municipality_employer)
                        END
                    LEFT JOIN tmp_ocupaciones o
                    ON
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN o.id_ocupacion = dep.id_occupation
                            ELSE o.nombre_ocupacion = ind.occupation
                        END
                    WHERE
                    a.affiliation_date BETWEEN CAST(:startDate AS timestamp) AND CAST(:endDate AS timestamp)
                    AND a.affiliation_type IN :affiliationTypes
                    AND (:department IS NULL OR d.id_departamento = :department)
                    AND (:city IS NULL OR m.id_municipio = :city)
                    AND (:occupation IS NULL OR o.id_ocupacion = :occupation)
                    AND a.filed_number IS NOT NULL
                    """, nativeQuery = true)
    Page<OfficialReportDTO> findOfficialReportByWorkers(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("affiliationTypes") List<String> affiliationTypes,
            @Param("department") Integer department,
            @Param("city") Integer city,
            @Param("occupation") Integer occupation,
            Pageable pageable);

    @Query(value =
            """
                    SELECT
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN CONCAT_WS('-', dep.identification_type, dep.identification_number)
                            WHEN a.affiliation_type = 'Empleador'
                              THEN CONCAT_WS('-', mer.type_document_identification, mer.number_identification)
                            ELSE CONCAT_WS('-', ind.identification_document_type, ind.identification_document_number)
                        END AS identification,
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN CONCAT_WS(' ', dep.first_name, dep.second_name, dep.surname, dep.second_surname)
                            WHEN a.affiliation_type = 'Empleador'
                              THEN mer.business_name
                            ELSE CONCAT_WS(' ', ind.first_name, ind.second_name, ind.surname, ind.second_surname)
                        END AS fullName,
                        COALESCE(ea.class_risk||ea.code_ciiu||ea.additional_code, '') AS economicActivityCode,
                        COALESCE(ea.description, '') AS descriptionEconomicActivity,
                        a.coverage_start_date AS coverageStartDate,
                        CASE
                            WHEN a.novelty_type = 'Afiliación'
                            THEN a.affiliation_date
                            ELSE a.retirement_date
                        END AS noveltyDate,
                        a.novelty_type AS noveltyType,
                        a.affiliation_status AS affiliationStatus,
                        d.nombre_departamento AS department,
                        m.nombre_municipio AS city
                      FROM affiliate a
                      LEFT JOIN affiliation_detail ind
                      ON ind.filed_number = a.filed_number
                      LEFT JOIN affiliation_dependent dep
                      ON dep.filed_number = a.filed_number
                      LEFT JOIN affiliate_mercantile mer
                      ON mer.filed_number = a.filed_number
                      LEFT JOIN tmp_departamentos d
                      ON
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN d.id_departamento = dep.id_department
                            WHEN a.affiliation_type = 'Empleador'
                              THEN d.nombre_departamento = UPPER(mer.department)
                            ELSE d.nombre_departamento = UPPER(ind.department)
                        END
                      LEFT JOIN tmp_municipality m
                      ON
                        CASE
                            WHEN a.affiliation_type = 'Trabajador Dependiente'
                            THEN m.id_municipio = dep.id_city_municipality
                            WHEN a.affiliation_type = 'Empleador'
                              THEN m.nombre_municipio = UPPER(mer.city_municipality)
                            ELSE m.nombre_municipio = UPPER(ind.municipality_employer)
                        END
                      WHERE
                      a.affiliation_date BETWEEN CAST(:startDate AS timestamp) AND CAST(:endDate AS timestamp)
                      AND a.novelty_type IN :noveltyTypes
                      AND (:department IS NULL OR d.id_departamento = :department)
                      AND (:city IS NULL OR m.id_municipio = :city)
                      AND a.filed_number IS NOT NULL
                    """, nativeQuery = true)
    Page<OfficialReportDTO> findOfficialReportByNovelty(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("noveltyTypes") List<String> noveltyTypes,
            @Param("department") Integer department,
            @Param("city") Integer city,
            Pageable pageable);

    @Query(value =
            """
                    SELECT
                        CASE
                            WHEN a.affiliation_type = 'Empleador'
                            THEN CONCAT_WS('-', dep.type_document_identification, dep.number_identification)
                            ELSE CONCAT_WS('-', ind.identification_document_type, ind.identification_document_number)
                        END AS identification,
                        CASE
                            WHEN a.affiliation_type = 'Empleador'
                            THEN dep.business_name
                            ELSE CONCAT_WS(' ', ind.first_name, ind.second_name, ind.surname, ind.second_surname)
                        END AS fullName,
                        COALESCE(ea.class_risk||ea.code_ciiu||ea.additional_code, '') AS economicActivityCode,
                        COALESCE(ea.description, '') AS descriptionEconomicActivity,
                        CASE
                            WHEN a.affiliation_type = 'Empleador'
                            THEN dep.type_person
                            ELSE COALESCE(ind.person_type, '')
                        END AS personType,
                        a.coverage_start_date AS coverageStartDate,
                        a.affiliation_date AS affiliationDate,
                        a.affiliation_status AS affiliationStatus,
                        d.nombre_departamento AS department,
                        m.nombre_municipio AS city
                    FROM affiliate a
                    LEFT JOIN affiliation_detail ind
                    ON ind.filed_number = a.filed_number
                    LEFT JOIN affiliate_mercantile dep
                    ON dep.filed_number = a.filed_number
                    LEFT JOIN tmp_departamentos d
                    ON
                    CASE
                        WHEN a.affiliation_type = 'Empleador'
                        THEN d.nombre_departamento = UPPER(dep.department)
                        ELSE d.nombre_departamento = UPPER(ind.department)
                    END
                    LEFT JOIN tmp_municipality m
                    ON
                        CASE
                            WHEN a.affiliation_type = 'Empleador'
                            THEN m.nombre_municipio = UPPER(dep.city_municipality)
                            ELSE m.nombre_municipio = UPPER(ind.municipality_employer)
                        END
                    WHERE
                     a.affiliation_date BETWEEN CAST(:startDate AS timestamp) AND CAST(:endDate AS timestamp)
                    AND a.affiliation_type IN :affiliationTypes
                    AND (:department IS NULL OR d.id_departamento = :department)
                    AND (:city IS NULL OR m.id_municipio = :city)
                    AND (:economicActivity IS NULL OR CAST(ea.class_risk||ea.code_ciiu||ea.additional_code AS INTEGER) = :economicActivity)
                    AND a.filed_number IS NOT NULL
                    """, nativeQuery = true)
    Page<OfficialReportDTO> findOfficialReportByEmployer(
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("affiliationTypes") List<String> affiliationTypes,
            @Param("department") Integer department,
            @Param("city") Integer city,
            @Param("economicActivity") Integer economicActivity,
            Pageable pageable);

    @Query("select a from Affiliation a where a.filedNumber = ?1")
    Optional<Affiliation> findByFiledNumber(String filedNumber);

    @Query(value =
            """
                    SELECT DISTINCT novelty_type FROM affiliate a
                    """, nativeQuery = true)
    List<String> findNoveltyTypeOption();

    List<Affiliation> findAllByIdentificationDocumentTypeAndIdentificationDocumentNumber(String documentType, String documentNumber);
    Optional<Affiliation> findFirstByIdentificationDocumentTypeAndIdentificationDocumentNumberOrderByFiledNumberDesc(String identificationDocumentType,
                           String identificationDocumentNumber);

    Optional<Affiliation> findByIdentificationDocumentNumber(String identificationDocumentNumber);
}