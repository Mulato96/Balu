package com.gal.afiliaciones.infrastructure.dao.repository;

import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DateInterviewWebRepository extends JpaRepository<DateInterviewWeb, Long>, JpaSpecificationExecutor<DateInterviewWeb> {

    @Query(value = """
            SELECT id_official as id, count(id_official) as totalInterviewWeb
            FROM date_interview_web
            GROUP by id_official
            ORDER by totalInterviewWeb ASC
            """, nativeQuery = true)
    List<InterviewsOfficialsView> findInterviewsOfficial();

    List<DateInterviewWeb> findByIdOfficial(Long idOfficial);

}
