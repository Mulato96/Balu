package com.gal.afiliaciones.config.converters;

import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;
import com.gal.afiliaciones.infrastructure.dto.novelty.NoveltyExportDTO;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

public class NoveltyAdapter {

    private NoveltyAdapter() {}

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static Function<PermanentNovelty, NoveltyExportDTO> entityToExport = (PermanentNovelty entity) -> {
        if (entity == null)
            return null;

        return NoveltyExportDTO.builder()
                .channel(entity.getChannel().getName())
                .date(formatter.format(entity.getRegistryDate()))
                .documentNumber(entity.getContributorIdentification())
                .nameContributor(entity.getNameOrCompanyName())
                .contributantIdentification(entity.getContributantIdentification())
                .contributantName(fullName(entity))
                .noveltyType(entity.getNoveltyType().getDescription())
                .state(entity.getStatus().getStatus())
                .causal(entity.getCausal().getCausal())
                .build();
    };

    private static String fullName(PermanentNovelty entity) {
        String firstName = entity.getContributantFirstName();
        String secondName = entity.getContributantSecondName();
        String surname = entity.getContributantSurname();
        String secondSurname = entity.getContributantSecondSurname();

        return (firstName != null ? firstName : "") + " " +
                (secondName != null ? secondName : "") + " " +
                (surname != null ? surname : "") + " " +
                (secondSurname != null ? secondSurname : "");
    }

}
