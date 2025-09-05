package com.gal.afiliaciones.infrastructure.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotesDTO {


    private Long id;
    private String note;
    private Long idOfficial;
    private String filedNumberAffiliation;
    private LocalDateTime dateInterviewWed;
    private String stageManagement;
    private String title;
    private String nameOfficial;
}
