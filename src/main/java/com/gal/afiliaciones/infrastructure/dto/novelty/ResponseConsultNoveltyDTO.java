package com.gal.afiliaciones.infrastructure.dto.novelty;

public interface ResponseConsultNoveltyDTO {

    Long getId();
    String getChannel();
    String getRegistryDate();
    String getContributorIdentificationType();
    String getContributorIdentification();
    String getNameOrCompanyName();
    String getContributantIdentificationType();
    String getContributantIdentification();
    String getContributantFirstName();
    String getContributantSecondName();
    String getContributantSurname();
    String getContributantSecondSurname();
    String getNoveltyType();
    String getStatus();
    String getCausal();

}
