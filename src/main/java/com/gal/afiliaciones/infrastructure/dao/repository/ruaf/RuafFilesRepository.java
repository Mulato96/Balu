package com.gal.afiliaciones.infrastructure.dao.repository.ruaf;

import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface RuafFilesRepository extends JpaRepository<RuafFiles, Long>, JpaSpecificationExecutor<RuafFiles> {

    Optional<RuafFiles> findByFileNameAndIsSuccessful(String fileName, Boolean isSuccessful);

    @Query(value = """
                    select
                     DISTINCT on (typeDocument, numberDocument, numberDocumentAffiliate, typeDocumentAffiliate)
                     TRIM(COALESCE(u.tipo_documento),'') as typeDocument,
                     TRIM(COALESCE(u.numero_identificacion,'')) as numberDocument,
                     TRIM(COALESCE(u.primer_apellido,'')) as firstSurname,
                     TRIM(COALESCE(u.segundo_apellido,'')) as secondSurname,
                     TRIM(COALESCE(u.primer_nombre,'')) as firstName,
                     TRIM(COALESCE(u.segundo_nombre,'')) as secondName,
                     TRIM(COALESCE(
                         case
                             when a.affiliation_status = 'Activa'
                             then '1'
                             else '2'
                         end
                     ,'')) as statusAffiliation,
                     TRIM(COALESCE(a.nit_company,'')) as numberDocumentAffiliate,
                     TRIM(COALESCE(
                         case
                             when a.affiliation_type = 'Empleador'
                                 then (select am.type_document_identification from affiliate_mercantile am where am.filed_number = a.filed_number)
                             when a.affiliation_type = 'Trabajador Dependiente'
                                 then (select ad.identification_type from affiliation_dependent ad where ad.filed_number = a.filed_number)
                             else (select ad.identification_document_type from affiliation_detail ad where ad.filed_number = a.filed_number)
                         end
                     ,'')) as typeDocumentAffiliate,
                     TRIM(COALESCE(a.company,'')) as nameCompany,
                     TRIM(COALESCE(
                         case
                             when
                                 (select
                                     (count(s) > 0)
                                 from stages_collection s
                                 where s.contributor_identification_type = u.tipo_documento
                                 and s.contributor_identification_number = u.numero_identificacion
                                 and s.stage in ('Coactiva','Persuasiva'))
                             then '2'
                             else '1'
                         end
                     ,'')) as arrears
                 from usuario u
                 inner join affiliate a on a.document_number  = u.numero_identificacion
                """, nativeQuery = true)
    Set<UsersInArrears> findUsersInArrears(Collection<String> stages);

}