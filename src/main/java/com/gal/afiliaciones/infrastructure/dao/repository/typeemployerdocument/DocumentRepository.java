package com.gal.afiliaciones.infrastructure.dao.repository.typeemployerdocument;

import com.gal.afiliaciones.domain.model.affiliate.typeemployerdocument.DocumentRequested;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<DocumentRequested, Long>, JpaSpecificationExecutor<DocumentRequested> {

    @Query(value =  "select d.* from document_Requested as d " +
                    " inner join subtypeemployer_document as sd on sd.document_id = d.id " +
                    " inner join sub_type_employer as ste on sd.subtypeemployer_id = ste.id where ste.id = :idSubtype", nativeQuery = true)
    List<DocumentRequested> findByIdSubTypeEmployer(@Param("idSubtype") Long id);

    @Query(value =  "select d.* from document_Requested as d " +
            " inner join subtypeemployer_document as sd on sd.document_id = d.id " +
            " inner join sub_type_employer as ste on sd.subtypeemployer_id = ste.id where d.requested = true and ste.id = :idSubtype", nativeQuery = true)
    List<DocumentRequested> findByIdSubTypeEmployerRequireTrue(@Param("idSubtype") Long id);

    @Query(value =  "select d.id as idDocument, d.name as nameDocument, d.requested as requestedDocument, " +
            " ste.id as idSubTypeEmployer, ste.name as nameSubTypeEmployer, te.id as idTypeEmployer, te.name as nameTypeEmployer  from document_Requested as d " +
            " inner join subtypeemployer_document as sd on sd.document_id = d.id " +
            " inner join sub_type_employer as ste on sd.subtypeemployer_id = ste.id" +
            " inner join type_employer as te on te.id = ste.typeemployer_id", nativeQuery = true)
    List<Object[]> allFind();
}
