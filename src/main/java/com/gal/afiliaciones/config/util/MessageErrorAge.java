package com.gal.afiliaciones.config.util;

import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.Data;
import org.springframework.stereotype.Service;

@Data
@Service
public class MessageErrorAge {

    private final ArlInformationDao arlInformationDao;
    private String messageError;

    public MessageErrorAge(ArlInformationDao arlInformationDao){
        this.arlInformationDao = arlInformationDao;
    }

    public String messageError(String type, String document){

        ArlInformation arlInformation = arlInformationDao.findAllArlInformation().get(0);

        String url = (arlInformation.getWebsite() != null && !arlInformation.getWebsite().isEmpty()) ?  arlInformation.getWebsite() : "";
        String phone = (arlInformation.getPhoneNumber() != null && !arlInformation.getPhoneNumber().isEmpty()) ?  arlInformation.getPhoneNumber() : "";

          messageError= Constant.ERROR_AGE
                  .replace("Tipo", type)
                  .replace("documento", document)
                  .replace("link", url)
                  .replace("TeléfonoARL", phone);
          return messageError;
    }
}
