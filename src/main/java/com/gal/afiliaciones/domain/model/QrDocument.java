package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class QrDocument {
    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String identificationNumber;
    private LocalDateTime issueDate;
    private String identificationType;
}
