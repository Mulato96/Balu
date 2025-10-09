package com.gal.afiliaciones.infrastructure.dao.repository.updateEmployerData;

import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerBasicProjection;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.LegalRepViewDTO;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface OfficerAffiliateMercantileRepository extends JpaRepository<AffiliateMercantile, Long> {

    String FIND_BASIC_QUERY = """
      SELECT
        COALESCE(CAST(m.id AS text), '')                           AS "employerId",
        COALESCE(CAST(m.type_document_identification AS text), '') AS "docType",
        COALESCE(CAST(m.number_identification AS text), '')        AS "docNumber",
        COALESCE(CAST(m.digit_verification_dv AS text), '')        AS "dv",
        COALESCE(CAST(m.business_name AS text), '')                AS "businessName",
        COALESCE(CAST(m.id_department AS text), '')                AS "departmentId",
        COALESCE(CAST(dep.nombre_departamento AS text), '')        AS "departmentName",
        COALESCE(CAST(m.id_city AS text), '')                      AS "cityId",
        COALESCE(CAST(city.nombre_municipio AS text), '')          AS "cityName",
        COALESCE(CAST(m.address AS text), '')                      AS "addressFull",
        COALESCE(CAST(m.phone_one AS text), '')                    AS "phone1",
        COALESCE(CAST(m.phone_two AS text), '')                    AS "phone2",
        COALESCE(CAST(m.email AS text), '')                        AS "email",
        'MERCANTIL'                                                AS "employerType"
      FROM public.affiliate_mercantile m
      LEFT JOIN public.tmp_departamentos dep ON m.id_department = dep.id_departamento
      LEFT JOIN public.tmp_municipality city ON m.id_city = city.id_municipio
      WHERE regexp_replace(upper(m.type_document_identification::text),'[^A-Z0-9]','','g')
              = regexp_replace(upper(:docType),'[^A-Z0-9]','','g')
        AND regexp_replace(m.number_identification::text,'[^0-9]','','g')
              = regexp_replace(:docNumber,'[^0-9]','','g')
      LIMIT 1
      """;

    String UPDATE_BASIC_QUERY = """
      UPDATE public.affiliate_mercantile m
      SET
        type_document_identification = COALESCE(NULLIF(regexp_replace(upper(:docTypeNew),'[^A-Z0-9]','','g'), ''), m.type_document_identification),
        business_name                = COALESCE(NULLIF(:businessName,''), m.business_name),
        id_department                = COALESCE(CAST(NULLIF(:departmentId,'') AS integer), m.id_department),
        id_city                      = COALESCE(CAST(NULLIF(:cityId,'') AS integer), m.id_city),
        address                      = COALESCE(NULLIF(:addressFull,''), m.address),
        phone_one                    = COALESCE(NULLIF(:phone1,''), m.phone_one),
        phone_two                    = COALESCE(NULLIF(:phone2,''), m.phone_two),
        email                        = COALESCE(NULLIF(:email,''), m.email)
      WHERE
        regexp_replace(upper(m.type_document_identification::text),'[^A-Z0-9]','','g')
          = regexp_replace(upper(:keyType),'[^A-Z0-9]','','g')
        AND regexp_replace(m.number_identification::text,'[^0-9]','','g')
          = regexp_replace(:keyNum,'[^0-9]','','g')
      """;

    String FIND_LEGAL_REP_QUERY = """
      SELECT
        'MERCANTIL' AS "employerType",
        COALESCE(CAST(m.type_document_identification AS text), '')       AS "docType",
        COALESCE(CAST(m.number_identification AS text), '')              AS "docNumber",
        COALESCE(CAST(m.type_document_person_responsible AS text), '')   AS "rlDocType",
        COALESCE(CAST(m.number_document_person_responsible AS text), '') AS "rlDocNumber",
        COALESCE(CAST(u.primer_nombre        AS text), '')               AS "rlFirstName",
        COALESCE(CAST(u.segundo_nombre       AS text), '')               AS "rlSecondName",
        COALESCE(CAST(u.primer_apellido      AS text), '')               AS "rlSurname",
        COALESCE(CAST(u.segundo_apellido     AS text), '')               AS "rlSecondSurname",
        COALESCE(CAST(u.date_birth           AS text), '')               AS "rlBirthDate",
        COALESCE(CAST(u.age                  AS text), '')               AS "rlAge",
        COALESCE(CAST(u.sex                  AS text), '')               AS "rlSex",
        COALESCE(CAST(u.nationality          AS text), '')               AS "rlNationality",
        COALESCE(CAST(nac.descripcion        AS text), '')               AS "rlNationalityName",
        COALESCE(CAST(m.eps                  AS text), '')               AS "epsId",
        COALESCE(CAST(eps.nombreeps          AS text), '')               AS "epsName",
        COALESCE(CAST(m.afp                  AS text), '')               AS "afpId",
        COALESCE(CAST(afp.nombre_afp         AS text), '')               AS "afpName",
        COALESCE(CAST(m.address_legal_representative   AS text), '')     AS "addressFull",
        COALESCE(CAST(m.phone_one_legal_representative AS text), '')     AS "phone1",
        COALESCE(CAST(m.phone_two_legal_representative AS text), '')     AS "phone2",
        COALESCE(CAST(m.email                AS text), '')               AS "email"
      FROM public.affiliate_mercantile m
      LEFT JOIN public.usuario u
        ON regexp_replace(upper(u.tipo_documento::text),'[^A-Z0-9]','','g')
             = regexp_replace(upper(m.type_document_person_responsible::text),'[^A-Z0-9]','','g')
       AND regexp_replace(u.numero_identificacion::text,'[^0-9]','','g')
             = regexp_replace(m.number_document_person_responsible::text,'[^0-9]','','g')
      LEFT JOIN public.tmp_nacionalidades nac ON CAST(u.nationality AS bigint) = nac.id
      LEFT JOIN public.tmp_fondos_pensiones afp ON m.afp = afp.id_afp
      LEFT JOIN public.tmp_health eps ON m.eps = eps.id
      WHERE regexp_replace(upper(m.type_document_identification::text),'[^A-Z0-9]','','g')
              = regexp_replace(upper(:docType),'[^A-Z0-9]','','g')
        AND regexp_replace(m.number_identification::text,'[^0-9]','','g')
              = regexp_replace(:docNumber,'[^0-9]','','g')
      LIMIT 1
      """;

    String UPDATE_LEGAL_REP_QUERY = """
      UPDATE public.affiliate_mercantile m
      SET
        eps  = COALESCE(
                 CAST(NULLIF(regexp_replace(:epsId, '[^0-9]', '', 'g'), '') AS bigint),
                 m.eps
               ),
        afp  = COALESCE(
                 CAST(NULLIF(regexp_replace(:afpId, '[^0-9]', '', 'g'), '') AS bigint),
                 m.afp
               ),
        address_legal_representative   = COALESCE(NULLIF(:addressFull,''), m.address_legal_representative),
        phone_one_legal_representative = COALESCE(NULLIF(:phone1,''),      m.phone_one_legal_representative),
        phone_two_legal_representative = COALESCE(NULLIF(:phone2,''),      m.phone_two_legal_representative),
        email                          = COALESCE(NULLIF(:email,''),       m.email)
      WHERE
        regexp_replace(upper(m.type_document_identification::text),'[^A-Z0-9]','','g')
          = regexp_replace(upper(:docType),'[^A-Z0-9]','','g')
        AND regexp_replace(m.number_identification::text,'[^0-9]','','g')
          = regexp_replace(:docNumber,'[^0-9]','','g')
      """;

    /**
     * Consulta datos básicos del empleador mercantil incluyendo información de catálogos.
     * Normaliza tipo y número de documento antes de comparar (elimina caracteres especiales).
     *
     * @param docType Tipo de documento de identificación (CC, NIT, etc)
     * @param docNumber Número de identificación
     * @return Datos básicos de la empresa con nombres descriptivos de departamento y ciudad
     */
    @Query(value = FIND_BASIC_QUERY, nativeQuery = true)
    Optional<EmployerBasicProjection> findBasicByDoc(@Param("docType") String docType,
                                                     @Param("docNumber") String docNumber);

    /**
     * Actualiza datos básicos de un empleador mercantil.
     * Solo actualiza los campos que no sean vacíos, manteniendo valores actuales para campos vacíos.
     *
     * @param keyType Tipo de documento para identificar el registro
     * @param keyNum Número de documento para identificar el registro
     * @param docTypeNew Nuevo tipo de documento (opcional, mantiene actual si es vacío)
     * @param businessName Razón social
     * @param departmentId ID del departamento
     * @param cityId ID de la ciudad
     * @param addressFull Dirección completa
     * @param phone1 Teléfono principal
     * @param phone2 Teléfono secundario (opcional)
     * @param email Correo electrónico
     * @return Número de registros actualizados (0 o 1)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = UPDATE_BASIC_QUERY, nativeQuery = true)
    int updateBasicByDoc(
            @Param("keyType") String keyType,
            @Param("keyNum") String keyNum,
            @Param("docTypeNew") String docTypeNew,
            @Param("businessName") String businessName,
            @Param("departmentId") String departmentId,
            @Param("cityId") String cityId,
            @Param("addressFull") String addressFull,
            @Param("phone1") String phone1,
            @Param("phone2") String phone2,
            @Param("email") String email
    );

    /**
     * Consulta datos del representante legal incluyendo información de catálogos.
     * Realiza JOIN con usuario, nacionalidad, EPS y AFP para obtener nombres descriptivos.
     *
     * @param docType Tipo de documento del empleador
     * @param docNumber Número de documento del empleador
     * @return Datos del representante legal con nombres de EPS, AFP y nacionalidad
     */
    @Query(value = FIND_LEGAL_REP_QUERY, nativeQuery = true)
    Optional<LegalRepViewDTO> findLegalRepByDoc(@Param("docType") String docType,
                                                @Param("docNumber") String docNumber);

    /**
     * Actualiza datos editables del representante legal de un empleador mercantil.
     * Solo actualiza EPS, AFP, dirección, teléfonos y email.
     * Los datos personales del RL (nombres, fecha nacimiento, etc) no son editables.
     *
     * @param docType Tipo de documento del empleador
     * @param docNumber Número de documento del empleador
     * @param epsId ID de la EPS
     * @param afpId ID del fondo de pensiones
     * @param addressFull Dirección del representante legal
     * @param phone1 Teléfono principal
     * @param phone2 Teléfono secundario (opcional)
     * @param email Correo electrónico
     * @return Número de registros actualizados (0 o 1)
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = UPDATE_LEGAL_REP_QUERY, nativeQuery = true)
    int updateLegalRepByDoc(
            @Param("docType") String docType,
            @Param("docNumber") String docNumber,
            @Param("epsId") String epsId,
            @Param("afpId") String afpId,
            @Param("addressFull") String addressFull,
            @Param("phone1") String phone1,
            @Param("phone2") String phone2,
            @Param("email") String email
    );

}