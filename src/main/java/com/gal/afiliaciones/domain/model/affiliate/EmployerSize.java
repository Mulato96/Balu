package com.gal.afiliaciones.domain.model.affiliate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "tamanio_empleador")
public class EmployerSize {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "min_number_trabajadores")
    private Integer minNumberWorker;

    @Column(name = "max_number_trabajadores")
    private Integer maxNumberWorker;

    @Column(name = "descripcion")
    private String description;

}
