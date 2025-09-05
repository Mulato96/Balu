package com.gal.afiliaciones.infrastructure.dao.repository.affiliationtaxidriverindependent.impl;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AffiliationTaxiDriverIndependentDAOImpl implements AffiliationTaxiDriverIndependentDAO {

    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository affiliationTaxiDriverIndependentRepository;

    @Override
    public Affiliation createAffiliation(Affiliation affiliation) {
        return affiliationTaxiDriverIndependentRepository.save(affiliation);
    }

    @Override
    public List<UserMain> findPreloadedData(String identificationType, String identification) {
        List<UserMain> response = new ArrayList<>();
        // Simulamos la recuperación de los datos adicionales del usuario, aunque no se guarden.
        Optional<UserMain> users = iUserPreRegisterRepository.findByIdentificationTypeAndIdentification(
                identificationType, identification);

        // Si no se encuentran usuarios, se agrega un mockUser como ejemplo.
        if (users.isEmpty()) {
            UserMain mockUser = new UserMain();
            mockUser.setIdentificationType("NI");
            mockUser.setIdentification("9987883");
            mockUser.setVerificationDigit(8);
            mockUser.setCompanyName("Planeta Digital");
            mockUser.setStatusPreRegister(true);
            mockUser.setFirstName("Carlos");
            mockUser.setSecondName("N/A");
            mockUser.setSurname("Ramos");
            mockUser.setSecondSurname("N/A");
            mockUser.setEmail("Carlos_R@gmail.com");

            response.add(mockUser);
        }else{
            response.add(users.get());
        }

        return response;
    }


    @Override
    public Optional<Affiliation> updateAffiliation(Affiliation affiliation) {
        return Optional.of(affiliationTaxiDriverIndependentRepository.save(affiliation));
    }

    @Override
    public Optional<Affiliation> findAffiliationById(Long id) {
        return affiliationTaxiDriverIndependentRepository.findById(id);
    }

}
