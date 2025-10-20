package com.gal.afiliaciones.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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

    @jakarta.persistence.Lob
    @Column(name = "archivo_cargado", columnDefinition = "BLOB")
    private byte[] archivoCargado;

    @jakarta.persistence.Lob
    @Column(name = "archivo_errores", columnDefinition = "BLOB")
    private byte[] archivoErrores;
}