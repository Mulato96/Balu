package com.gal.afiliaciones.application.service.employeeupdateinfo;

import com.gal.afiliaciones.infrastructure.dto.InfoBasicaDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateInfoBasicaRequest;

public interface InfoBasicaService {
    InfoBasicaDTO consultarInfoBasica(String documento);
    void actualizarInfoBasica(String documentoObjetivo,
                              UpdateInfoBasicaRequest request,
                              String documentoUsuarioLogueado);
}
