package com.gal.afiliaciones.application.service.positiva.impl;

import com.gal.afiliaciones.application.service.positiva.PositivaEmployerMercantileService;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.PositivaEmployerMercantileView;
import com.gal.afiliaciones.infrastructure.dao.repository.PositivaEmployerMercantileViewRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.PositivaEmployerMercantileSpecification;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.PositivaEmployerMercantileDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PositivaEmployerMercantileServiceImpl implements PositivaEmployerMercantileService {

    private final PositivaEmployerMercantileViewRepository repository;

    @Override
    public List<PositivaEmployerMercantileDTO> findEmployers(String idTipoDoc, String idEmpresa, Integer idSubEmpresa) {
        var spec = PositivaEmployerMercantileSpecification.filterBy(idTipoDoc, idEmpresa, idSubEmpresa);
        List<PositivaEmployerMercantileView> rows = repository.findAll(spec);
        return rows.stream().map(this::toDto).toList();
    }

    private PositivaEmployerMercantileDTO toDto(PositivaEmployerMercantileView v) {
        return PositivaEmployerMercantileDTO.builder()
                .idTipoDoc(v.getIdTipoDoc())
                .idEmpresa(v.getIdEmpresa())
                .subempresa(v.getSubempresa())
                .dvEmpresa(v.getDvEmpresa())
                .razonSocial(v.getRazonSocial())
                .idDepartamento(v.getIdDepartamento())
                .idMunicipio(v.getIdMunicipio())
                .idActEconomica(v.getIdActEconomica())
                .direccionEmpresa(v.getDireccionEmpresa())
                .telefonoEmpresa(v.getTelefonoEmpresa())
                .faxEmpresa(v.getFaxEmpresa())
                .emailEmpresa(v.getEmailEmpresa())
                .indZona(v.getIndZona())
                .transporteTrabajadores(v.getTransporteTrabajadores())
                .fechaRadicacion(v.getFechaRadicacion())
                .indAmbitoEmpresa(v.getIndAmbitoEmpresa())
                .estado(v.getEstado())
                .idTipoDocRepLegal(v.getIdTipoDocRepLegal())
                .idRepresentanteLegal(v.getIdRepresentanteLegal())
                .representanteLegal(v.getRepresentanteLegal())
                .fechaAfiliacionEfectiva(v.getFechaAfiliacionEfectiva())
                .origenEmpresa(v.getOrigenEmpresa())
                .idArp(v.getIdArp())
                .idTipoDocArl(v.getIdTipoDocArl())
                .nitArl(v.getNitArl())
                .fechaNotificacion(v.getFechaNotificacion())
                .affiliateMercantileId(v.getAffiliateMercantileId())
                .affiliateId(v.getAffiliateId())
                .filedNumber(v.getFiledNumber())
                .employerName(v.getEmployerName())
                .affiliateCompany(v.getAffiliateCompany())
                .registrationDate(v.getRegistrationDate())
                .fechaRetiroInactivacion(v.getFechaRetiroInactivacion())
                .idSubEmpresa(v.getIdSubEmpresa())
                .nombreEstado(v.getNombreEstado())
                .razonSocialSubempresa(v.getRazonSocialSubempresa())
                .build();
    }
}


