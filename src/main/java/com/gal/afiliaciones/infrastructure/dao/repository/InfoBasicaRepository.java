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
        )
        SELECT
          u.tipo_documento AS tipoDocumento,
          u.numero_identificacion AS numeroIdentificacion,
          u.primer_nombre AS primerNombre,
          u.segundo_nombre AS segundoNombre,
          u.primer_apellido AS primerApellido,
          u.segundo_apellido AS segundoApellido,
          u.date_birth AS fechaNacimiento,
          COALESCE(u.age, EXTRACT(YEAR FROM age(current_date, u.date_birth))::int) AS edad,
          u.nationality AS nacionalidad,
          u.sex AS sexo,
          u.afp AS afp,
          u.eps AS eps,
          u.email AS email,
          u.telefono AS telefono1,
          u.telefono_2 AS telefono2,
          u.id_department AS idDepartamento,
          u.id_city AS idCiudad,
          u.id_main_street AS idCallePrincipal,
          u.id_number_main_street AS numeroCallePrincipal,
          u.id_letter1_main_street AS letra1CallePrincipal,
          u.id_letter2_main_street AS letra2CallePrincipal,
          u.id_cardinal_point_main_street AS puntoCardinalCallePrincipal,
          u.bis AS bis,
          u.id_number1_second_street AS numero1Secundaria,
          u.id_number2_second_street AS numero2Secundaria,
          u.id_letter_second_street AS letraSecundaria,
          u.id_cardinal_point2 AS puntoCardinal2,
          u.id_horizontal_property1 AS ph1,  u.id_number_horizontal_property1 AS numPh1,
          u.id_horizontal_property2 AS ph2,  u.id_number_horizontal_property2 AS numPh2,
          u.id_horizontal_property3 AS ph3,  u.id_number_horizontal_property3 AS numPh3,
          u.id_horizontal_property4 AS ph4,  u.id_number_horizontal_property4 AS numPh4,
          u.address AS direccionTexto,
          u.id_cargo AS idCargo,
          current_date AS fechaNovedad,
          a.observation AS observaciones,
          nac.descripcion           AS nacionalidadNombre,
          afpCat.nombre_afp         AS afpNombre,
          epsCat.nombreeps          AS epsNombre,
          mun.nombre_municipio      AS ciudadNombre,
          dep.nombre_departamento   AS departamentoNombre
        FROM u
        LEFT JOIN a
          ON regexp_replace(a.document_number::text, '\\\\D', '', 'g')
           = regexp_replace(u.numero_identificacion::text, '\\\\D', '', 'g')
        LEFT JOIN public.tmp_nacionalidades    nac    ON nac.id::text             = u.nationality::text
        LEFT JOIN public.tmp_fondos_pensiones  afpCat ON afpCat.id_afp::text      = u.afp::text
        LEFT JOIN public.tmp_municipality      mun    ON mun.id_municipio::text   = u.id_city::text
        LEFT JOIN public.tmp_health            epsCat ON epsCat.id::text          = u.eps::text
        LEFT JOIN public.tmp_departamentos     dep    ON dep.id_departamento::text = u.id_department::text
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
