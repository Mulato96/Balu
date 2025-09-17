package com.gal.afiliaciones.application.service.actualizacionfechas;

import com.gal.afiliaciones.infrastructure.dto.actualizacionfechas.UpdateCoverageDateDto;
import com.gal.afiliaciones.infrastructure.dto.actualizacionfechas.VinculacionDto;
import com.gal.afiliaciones.infrastructure.dto.actualizacionfechas.VinculacionQueryDto;

import java.util.List;

public interface DateUpdateService {
    List<VinculacionDto> consultarVinculaciones(VinculacionQueryDto query);
    void actualizarFechaCobertura(UpdateCoverageDateDto updateDto);
}
