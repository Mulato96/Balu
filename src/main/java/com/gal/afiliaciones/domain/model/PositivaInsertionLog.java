package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "positiva_insertion_log")
public class PositivaInsertionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "operation")
    private String operation;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "document_number")
    private String documentNumber;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "affiliation_type")
    private String affiliationType;

    @Column(name = "affiliation_subtype")
    private String affiliationSubtype;

    @Column(name = "filed_number")
    private String filedNumber;

    @Column(name = "response_body")
    private String responseBody;

    @Column(name = "result_code")
    private Integer resultCode;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}


