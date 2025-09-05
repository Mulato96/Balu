package com.gal.afiliaciones.domain.model;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tmp_history_options")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class HistoryOptions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

}
