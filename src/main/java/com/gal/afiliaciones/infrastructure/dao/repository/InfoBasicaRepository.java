package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.projection.InfoBasicaProjection;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface InfoBasicaRepository extends JpaRepository<UserMain, Long> {

    @Query(value = """
            WITH u AS (
                  SELECT u.*
                  FROM public.usuario u
                  WHERE regexp_replace(u.numero_identificacion::text, '\\\\D', '', 'g')
                        = regexp_replace(CAST(:documento AS text), '\\\\D', '', 'g')
                  LIMIT 1
                ),
                a AS (
                  SELECT DISTINCT ON (regexp_replace(a.document_number::text, '\\\\D', '', 'g')) a.*
                  FROM public.affiliate a
                  WHERE regexp_replace(a.document_number::text, '\\\\D', '', 'g')
                        = regexp_replace(CAST(:documento AS text), '\\\\D', '', 'g')
                    AND a.affiliation_status IS NOT NULL
                    AND a.affiliation_date   IS NOT NULL
                  ORDER BY regexp_replace(a.document_number::text, '\\\\D', '', 'g'),
                           a.affiliation_date DESC NULLS LAST
                ),
                ad AS (
                  SELECT ad.*
                  FROM public.affiliation_dependent ad
                  INNER JOIN a ON ad.id_affiliate = a.id_affiliate
                  WHERE a.affiliation_type = 'Trabajador Dependiente'
                  LIMIT 1
                ),
                am AS (
                  SELECT am.*
                  FROM public.affiliate_mercantile am
                  INNER JOIN a ON am.id_affiliate = a.id_affiliate
                  WHERE a.affiliation_type = 'Empleador Mercantil'
                  LIMIT 1
                )
                SELECT
                  COALESCE(u.tipo_documento, ad.identification_type, am.type_document_identification, a.document_type) AS tipoDocumento,
                  COALESCE(u.numero_identificacion, ad.identification_number, am.number_identification, a.document_number) AS numeroIdentificacion,
                  COALESCE(u.primer_nombre, ad.first_name, '') AS primerNombre,
                  COALESCE(u.segundo_nombre, ad.second_name, '') AS segundoNombre,
                  COALESCE(u.primer_apellido, ad.surname, '') AS primerApellido,
                  COALESCE(u.segundo_apellido, ad.second_surname, '') AS segundoApellido,
                  COALESCE(u.date_birth, ad.date_of_birth) AS fechaNacimiento,
                  COALESCE(u.age, ad.age, EXTRACT(YEAR FROM age(current_date, COALESCE(u.date_birth, ad.date_of_birth)))::int) AS edad,
                  COALESCE(nac.id, null) AS nacionalidad,
                  COALESCE(u.sex, ad.gender, '') AS sexo,
                  COALESCE(epsCat.id, null) AS eps,
                  COALESCE(afpCat.id_afp, null) AS afp,
                  COALESCE(dep.id_departamento, null) AS idDepartamento,
                  COALESCE(mun.id_municipio, null) AS idCiudad,
                  COALESCE(u.address, ad.address, am.address, '') AS direccionTexto,
                  COALESCE(u.telefono, ad.phone1, am.phone_one, '') AS telefono1,
                  COALESCE(u.telefono_2, ad.phone2, am.phone_two, '') AS telefono2,
                  COALESCE(u.email, ad.email, am.email, '') AS email,
                  current_date AS fechaNovedad,
                  COALESCE(a.observation, '') AS observaciones
                FROM u
                FULL OUTER JOIN a
                  ON regexp_replace(a.document_number::text, '\\\\D', '', 'g')
                   = regexp_replace(u.numero_identificacion::text, '\\\\D', '', 'g')
                LEFT JOIN ad ON a.id_affiliate = ad.id_affiliate
                LEFT JOIN am ON a.id_affiliate = am.id_affiliate
                LEFT JOIN public.tmp_nacionalidades    nac    ON nac.id::text = COALESCE(u.nationality::text, ad.nationality::text)
                LEFT JOIN public.tmp_fondos_pensiones  afpCat ON afpCat.id_afp::text = COALESCE(u.afp::text, ad.pension_fund_administrator::text, am.afp::text)
                LEFT JOIN public.tmp_municipality      mun    ON mun.id_municipio::text = COALESCE(u.id_city::text, ad.id_city_municipality::text, am.id_city::text)
                LEFT JOIN public.tmp_health            epsCat ON epsCat.id::text = COALESCE(u.eps::text, ad.health_promoting_entity::text, am.eps::text)
                LEFT JOIN public.tmp_departamentos     dep    ON dep.id_departamento::text = COALESCE(u.id_department::text, ad.id_department::text, am.id_department::text)
                WHERE u.numero_identificacion IS NOT NULL\s
                   OR a.document_number IS NOT NULL
        """, nativeQuery = true)
    Optional<InfoBasicaProjection> findInfoBasicaByDocumento(@Param("documento") String documento);

    @Query(value = """
        SELECT a.affiliation_date::date
        FROM public.affiliate a
        WHERE regexp_replace(a.document_number::text, '\\\\D', '', 'g') =
              regexp_replace(CAST(:documento AS text), '\\\\D', '', 'g')
          AND a.affiliation_status IS NOT NULL
          AND a.affiliation_date   IS NOT NULL
        ORDER BY a.affiliation_date DESC NULLS LAST
        LIMIT 1
        """, nativeQuery = true)
    Optional<java.time.LocalDate> findLatestAffiliationDateByDoc(@Param("documento") String documento);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE public.affiliate a
           SET observation = :obs
         WHERE regexp_replace(a.document_number::text, '\\\\D','','g') =
               regexp_replace(CAST(:doc AS text), '\\\\D','','g')
           AND a.affiliation_status IS NOT NULL
           AND a.affiliation_date   IS NOT NULL
           AND a.affiliation_date = (
               SELECT max(b.affiliation_date)
               FROM public.affiliate b
               WHERE regexp_replace(b.document_number::text, '\\\\D','','g') =
                     regexp_replace(CAST(:doc AS text), '\\\\D','','g')
                 AND b.affiliation_status IS NOT NULL
                 AND b.affiliation_date   IS NOT NULL
           )
        """, nativeQuery = true)
    int updateAffiliateObservationByDoc(@Param("doc") String documento,
                                        @Param("obs") String observacion);

    interface RolePair {
        Long getId();
        String getNombreRol();
    }
    @Query(value = """
        SELECT r.id   AS id,
               r.nombre_rol AS nombreRol
        FROM public.usuario_rol ur
        JOIN public.role r ON r.id = ur.rol_id
        WHERE ur.usuario_id = :userId
          AND lower(r.nombre_rol) = lower(:roleName)
        LIMIT 1
        """, nativeQuery = true)
    Optional<RolePair> findRoleByUserIdAndRoleName(@Param("userId") Long userId,
                                                   @Param("roleName") String roleName);

    @Query(value = """
        SELECT r.id   AS id,
               r.nombre_rol AS nombreRol
        FROM public.usuario_rol ur
        JOIN public.role r ON r.id = ur.rol_id
        WHERE ur.usuario_id = :userId
        LIMIT 1
        """, nativeQuery = true)
    Optional<RolePair> findAnyRoleByUserId(@Param("userId") Long userId);
}
