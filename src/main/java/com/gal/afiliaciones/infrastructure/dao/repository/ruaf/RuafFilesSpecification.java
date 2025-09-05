package com.gal.afiliaciones.infrastructure.dao.repository.ruaf;

import com.gal.afiliaciones.domain.model.noveltyruaf.RuafFiles;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafFilterDTO;
import com.gal.afiliaciones.infrastructure.dto.ruaf.RuafStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class RuafFilesSpecification {

    private RuafFilesSpecification() {}

    public static Specification<RuafFiles> findByFilter(RuafFilterDTO filter) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (filter != null) {
                if (filter.status() != null && !filter.status().name().isEmpty()) {
                    boolean isSuccessful = filter.status().equals(RuafStatus.COMPLETE);
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("isSuccessful"), isSuccessful));
                }

                if (filter.createdAt() != null && !filter.createdAt().isEmpty()) {
                    YearMonth generationPeriod = YearMonth.parse(filter.createdAt(), DateTimeFormatter.ofPattern("MM/yyyy"));
                    LocalDate startDate = LocalDate.parse(generationPeriod.atDay(1).toString());
                    LocalDate endDate = LocalDate.parse(generationPeriod.atEndOfMonth().toString());
                    predicate = criteriaBuilder.and(predicate, criteriaBuilder.between(root.get("createdAt"), startDate, endDate));
                }

                if (filter.reportType() != null && !filter.reportType().isEmpty()) {
                    predicate = criteriaBuilder.and(predicate, root.get("reportType").in(filter.reportType()));
                }
            }

            return predicate;
        };
    }

}
