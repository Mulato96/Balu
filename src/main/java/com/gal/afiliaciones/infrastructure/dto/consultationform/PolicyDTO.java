package com.gal.afiliaciones.infrastructure.dto.consultationform;


import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDTO {

    private String policyNumber;
    private String validity;
    private String bonding;
    private LocalDate validityFrom;
    private LocalDate validityTo;
    private String policyEndDate;
    private String PolicyName;
    private String state;
}
