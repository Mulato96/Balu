package com.gal.afiliaciones.application.service.usernotification;

import com.gal.afiliaciones.infrastructure.dto.usernotification.UserNotificationDTO;

import java.util.List;

public interface UserNotificationService {

    List<UserNotificationDTO> findAllAffiliatedUser();

}
