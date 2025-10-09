package com.gal.afiliaciones.application.service.updatedatesemployee;

import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.UpdateCoverageDateDTO;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.VinculacionDTO;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.VinculacionDetalleDTO;
import com.gal.afiliaciones.infrastructure.dto.updatedatesemployee.VinculacionQueryDTO;

import java.util.List;

public interface DateUpdateService {
    List<VinculacionDTO> consultLinks(VinculacionQueryDTO query);
    void updateDateCoverage(UpdateCoverageDateDTO updateDto);
    VinculacionDetalleDTO getLinksDetail(String tipo, Long id);
}
