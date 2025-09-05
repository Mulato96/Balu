package com.gal.afiliaciones.application.service.employeeupdateinfo;

import com.gal.afiliaciones.infrastructure.dto.employeeupdateinfo.UpdateInfoEmployeeIndependentRequest;
import jakarta.mail.MessagingException;

import java.io.IOException;

public interface IUpdateInfoEmployeeInfoService {

    String updateInfoEmployeeIndependet(UpdateInfoEmployeeIndependentRequest request)  throws MessagingException, IOException;
}
