package com.gal.afiliaciones.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "SOLICITUD_CORRECCION")
public class ContributionCorrection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_transaccion")
    private String transactionType;

    @OneToOne
    @JoinColumn(name = "request_reasons_id")
    private ReasonCollectionReturn requestReasons;

    @Column(name = "descripcion_requerimiento")
    private String requirementDescription;

    @Column(name = "periodo_pago_correcto")
    private String correctPaymentPeriod;

    @Column(name = "periodo_pago_incorrecto")
    private String incorrectPaymentPeriod;

    @Column(name = "tipo_documento_empleador_correcto")
    private String correctEmployerDocumentType;

    @Column(name = "numero_documento_empleador_correcto")
    private String correctEmployerDocumentNumber;

    @Column(name = "digito_verificacion_empleador_correcto")
    private String correctEmployerVerificationDigit;

    @Column(name = "razon_social_empleador_correcta")
    private String correctSocialReason;

    @Column(name = "tipo_documento_empleador_incorrecto")
    private String incorrectEmployerDocumentType;

    @Column(name = "numero_documento_empleador_incorrecto")
    private String incorrectEmployerDocumentNumber;

    @Column(name = "digito_verificacion_empleador_incorrecto")
    private String incorrectEmployerVerificationDigit;

    @Column(name = "razon_social_empleador_incorrecto")
    private String incorrectSocialReason;

    @Column(name = "primer_nombre_usuario_correcto")
    private String correctFirstNameUser;

    @Column(name = "segundo_nombre_usuario_correcto")
    private String correctSecondNameUser;

    @Column(name = "primer_apellido_usuario_correcto")
    private String correctFirstLastNameUser;

    @Column(name = "segundo_apellido_usuario_correcto")
    private String correctSecondLastNameUser;

    @Column(name = "numero_documento_usuario_correcto")
    private String correctDocumentNumberUser;

    @Column(name = "primer_nombre_usuario_incorrecto")
    private String incorrectFirstNameUser;

    @Column(name = "segundo_nombre_usuario_incorrecto")
    private String incorrectSecondNameUser;

    @Column(name = "primer_apellido_usuario_incorrecto")
    private String incorrectFirstLastNameUser;

    @Column(name = "segundo_apellido_usuario_incorrecto")
    private String incorrectSecondLastNameUser;

    @Column(name = "numero_documento_usuario_incorrecto")
    private String incorrectDocumentNumberUser;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private UserMain user;

    @Column(name = "date_create_request_correction")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreateRequest;

    @Column(name = "folder_id")
    private String folderId;

    @Column(name = "personal_data_processing_consent")
    private Boolean personalDataProcessingConsent;

    @Column(name = "email_notification_consent")
    private Boolean emailNotificationConsent;

    @Column(name = "numero_radicado")
    private String filedNumber;

    @Column (name = "request_status")
    private String requestStatus;

    @OneToMany(mappedBy = "requestCorrection")
    private List<ManageCorrectionRequest> manageCorrectionRequest;

    @Column(name = "numero_planilla")
    private Long payrollNumber;

    @ManyToOne
    @JoinColumn(name = "id_recaudo")
    private Collection collection;
}