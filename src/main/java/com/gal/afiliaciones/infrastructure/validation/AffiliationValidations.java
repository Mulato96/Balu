package com.gal.afiliaciones.infrastructure.validation;

import com.gal.afiliaciones.config.ex.affiliation.ErrorAffiliationProvisionService;
import com.gal.afiliaciones.infrastructure.utils.Constant;

public class AffiliationValidations {

    private static final String INCORRECT_ARL = "La ARL envíada es incorrecta";

    public static void validateArl(String arl, boolean is723) {
        if (is723) {
            if (arl != null && arl.equals(Constant.CODE_ARL)) {
                throw new ErrorAffiliationProvisionService(INCORRECT_ARL);
            }
        } else {
            if (!arl.equals(Constant.CODE_ARL)) {
                throw new ErrorAffiliationProvisionService(INCORRECT_ARL);
            }
        }
    }

}
