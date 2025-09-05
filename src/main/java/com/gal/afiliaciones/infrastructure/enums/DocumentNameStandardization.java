package com.gal.afiliaciones.infrastructure.enums;

import lombok.Getter;

@Getter
public enum DocumentNameStandardization {
    AC("Acta de constitución"),
    AFP("Afiliación o certificación AFP"),
    AN("Acta de nombramiento (Edificio)"),
    APN("Acta de posesión del Notario (a)"),
    APR("Acta de posesión del Representante Legal"),
    CCC("Certificado de la Cámara de Comercio con matrícula mercantil renovada"),
    CDS("Certificado de supersolidaria"),
    CEC("Certificado de existencia del contrato entre Madres Comunitarioas e ICBF o contrato vigente"),
    CEN("Certificado de funcionamiento del ente territorial competente"),
    CIS("Certificado de inscripción a la secretaría de salud y/o rehabilitación"),
    CME("Certificación del Ministerio de educación"),
    CMJ("Certificado de funcionamiento del Ministerio del Interior y Justicia"),
    CMR("Certificado de la Cámara de Comercio con matrícula mercantil renovada de cada una de las partes"),
    CRL("Certificado de existencia y representación legal (Edificio)"),
    CS("Contrato sindical"),
    CSF("Certificación de la Superintendencia Financiera"),
    CSS("Certificación de la Superintendencia de Salud"),
    DAS("Documento que acredite subcontratación"),
    DC("Documento consorcial"),
    DI("Documento de identidad"),
    DIN("Documento de identidad del Notario (a)"),
    DRL("Documento de identidad del Representante Legal"),
    EPO("Examen Preocupacional"),
    EPS("Afiliación o certificación EPS"),
    FA("Formulario Afiliación"),
    FI("Firma"),
    HMT("Habilitación del Ministerio de Transporte"),
    IM("Inscripción en el registro sindical"),
    LFT("Licencia de funcionamiento del ente territorial competente"),
    LSV("Licencia de superintendencia de vigilancia y seguridad privada"),
    PA("Permiso de la Arquidiócesis"),
    PJ("Personería Jurídica"),
    PJJ("Personería Jurídica junta de acción comunal"),
    PJM("Personería jurídica del Ministerio de Educación Nacional"),
    PTP("Permiso de operación de transporte turístico (Transporte de pasajeros)"),
    RAM("Registro ante el Ministerio de Trabajo"),
    RCE("Resolución de constitución como entidad estatal"),
    RMS("Resolución del Ministerio de Salud"),
    RMT("Resolución del Ministerio de Trabajo"),
    RO("RUT del consorcio y cada una de las partes que lo conforman"),
    RT("RUT"),
    SL("Solicitud de legalización"),
    TC("Tarjeta de control"),
    TM("Título minero o contrato de operación minera"),
    TPV("Tarjeta de propiedad del vehículo"),
    AE("Agencias de empleo Intermediarios laborales y de selección de personal a terceros"),
    AFA("Asociaciones, fundaciones, corporaciones, agremiaciones, etc"),
    AFC("Asociaciones, fundaciones, corporaciones"),
    AG("Agremiaciones"),
    CAC("Cooperativas de ahorro y crédito"),
    CAT("Cooperativas / precooperativas trabajo asociado"),
    CCF("Cámaras de comercio / cajas de compensación familiar"),
    CI("Comunidades indígenas"),
    CM("Comunidades de minorías"),
    CMA("Cooperativas multiactivas"),
    CP("Cooperativas y precooperativas"),
    CPH("Conjuntos cerrados, centros comerciales o propiedad horizontal"),
    CR("Comunidades religiosas"),
    CRC("Comunidades religiosas católicas"),
    CRN("Comunidades religiosas no católicas"),
    CTE("Cooperativas /precooperativas trabajo asociado especializadas (Transporte, vigilancia y seguridad privada)"),
    CUT("Consorcio unión temporal"),
    CVB("Cuerpos de bomberos voluntarios y oficiales"),
    EA("Empleadores de acarreos"),
    EE("Empresas estatales"),
    EJ("Empresas jurídicas"),
    EJT("Empresa jurídica de taxi"),
    ENF("Empresas estatales, notarías y fiducias"),
    ENT("Empresa persona natural de taxi"),
    EP("Empresas Mineras"),
    EPL("Empleador profesión liberal (abogado, médico, ingeniero, etc)"),
    ER("Empresas de radiodiagnóstico"),
    EST("Empresas de servicios temporales - EST o Agencias de empleo"),
    ESP("Empresas de servicios temporales - EST empresas de suministro de personal"),
    ETC("Empleador de transporte de carga y/o de pasajeros"),
    ETF("Empresas de transporte fluvial"),
    EVP("Empresas de vigilancia privada"),
    FD("Fiducias"),
    FP("Fincas productivas"),
    IE("Instituciones de educación formal, no formal y superior"),
    IEF("Instituciones de educación formal"),
    IES("Instituciones de educación superior"),
    INF("Instituciones de educación no formal"),
    JAL("Juntas de acción comunal"),
    MC("Madres comunitarias"),
    NOT("Notarías"),
    ONG("ONG - Organizaciones No Gubernamentales"),
    OUT("Empresas de outsourcing (Aseo, suministro de alimentación, vigilancia y mantenimiento)"),
    PN("Personas naturales tipo NIT"),
    PNC("Persona natural transporte de carga"),
    SAS("Sociedades por acciones simplificada SAS"),
    SI("Sindicatos - contrato sindical"),
    TCP("Empresas jurídicas de transporte de pasajeros y de carga"),
    TPE("Transporte público especial");



    private final String description;

    DocumentNameStandardization(String description) {
        this.description = description;
    }

    public static String findByDescription(String description){
        for(DocumentNameStandardization field : DocumentNameStandardization.values()){
            if(field.getDescription().equals(description))
                return field.name();
        }

        return "";
    }
}
