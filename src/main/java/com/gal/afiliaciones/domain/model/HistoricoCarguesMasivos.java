package com.gal.afiliaciones.domain.model;

import jakarta.persistence.*;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "historico_cargues_masivos")
public class HistoricoCarguesMasivos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_cargue")
    private LocalDateTime fechaCargue;

    @Column(name = "estado")
    private String estado;

    @ManyToOne
    @JoinColumn(name = "id_empleador", referencedColumnName = "id_affiliate")
    private Affiliate empleador;

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Column(name = "cantidad_registros")
    private Integer cantidadRegistros;

    @Column(name = "cantidad_errores")
    private Integer cantidadErrores;

    @Column(name = "usuario_cargue")
    private String usuarioCargue;

    @Column(name = "archivo_cargado")
    private byte[] archivoCargado;

    @Column(name = "archivo_errores")
    private byte[] archivoErrores;
}