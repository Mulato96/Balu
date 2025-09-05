package com.gal.afiliaciones.application.service.novelty;

import com.gal.afiliaciones.domain.model.novelty.PermanentNovelty;

public interface PilaRetirementEventManagementService {

    void pilaRetirementEventManagement(PermanentNovelty novelty, boolean noveltyRetirementIncome);
    void independent(PermanentNovelty novelty);

}
