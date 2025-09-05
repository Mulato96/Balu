package com.gal.afiliaciones.domain.model.otp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
@Table(name = "otp_codes")
public class OtpCodeEntity {

    @Id
    @Column(name = "number_document")
    private String numberDocument;
    @Column(name = "otp")
    private String otp;
    @Column(name = "expiration")
    private Date expiration;

}
