package com.gal.afiliaciones.infrastructure.dto.registraduria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentityCardConsultationRequestDTO {
    @NotBlank(message = "Document number is required")
    @Pattern(regexp = "^[0-9]{8,12}$", message = "Document number must have between 8 and 12 digits")
    private String documentNumber;
} 