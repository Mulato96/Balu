package com.gal.afiliaciones.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "request_collection_return")
public class RequestCollectionReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "request_type")
    private String requestType = "Refund Request";

    @OneToMany(mappedBy = "requestCollectionReturn", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<RequestReason> requestReasons;

    @Size(max = 200)
    @Column(name = "other_reasons")
    private String otherReasons;

    @Transient
    private int daysRemaining;

    @Column(name = "numero_planilla")
    private String payrollNumber;

    @Column(name = "periodo_de_pago")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate paymentPeriod;

    @ManyToOne
    @JoinColumn(name = "user_id")
    UserMain user;

    @NotBlank
    @Column(name = "account_holder_name")
    private String accountHolderName;

    @NotBlank
    private String bank;

    @NotBlank
    @Pattern(regexp = "^[0-9]+$")
    @Column(name = "account_number")
    private String accountNumber;
    @NotBlank
    @Column(name = "type_bank_account")
    private String typeBankAccount;
    @NotNull
    @Column(name = "personal_data_processing_consent")
    private Boolean personalDataProcessingConsent;
    @Column(name = "email_notification_consent")
    private Boolean emailNotificationConsent;
    @NotNull
    @Column(name = "date_create_request")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime dateCreateRequest;

    @Column(name = "folder_id")
    private String folderId;

    @Column (name = "request_status")
    private String requestStatus;

    @Column(name = "numero_radicado")
    private String filedNumber;

    @Column(name = "descripcion_requerimiento")
    private String description;

    @OneToMany(mappedBy = "requestCollectionReturn")
    private List<ManageReturnRequest> manageReturnRequest;

    @ManyToOne
    @JoinColumn(name = "id_recaudo")
    private Collection collection;
}
