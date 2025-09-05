package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.config.ex.validationpreregister.ErrorCreateSequence;
import com.gal.afiliaciones.config.ex.validationpreregister.UserNotFoundInDataBase;
import com.gal.afiliaciones.domain.model.CodeValidCertificate;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.ICodeValidCertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.UserSpecifications;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CodeValidCertificationServiceImpl implements CodeValidCertificationService {

    private final ICodeValidCertificateRepository iCodeValidCertificateRepository;
    private final IUserPreRegisterRepository iUserPreRegisterRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyyy");

    @Override
    public String consultCode(String  numberDocument, String typeDocument) {

        Specification<UserMain> spec = UserSpecifications.hasDocumentTypeAndNumber(typeDocument, numberDocument);
        UserMain user = iUserPreRegisterRepository.findOne(spec).orElseThrow(() -> new UserNotFoundInDataBase(Constant.USER_NOT_FOUND_IN_DATA_BASE));
        return dataPersonal(user);
    }

    @Override
    public String consultCode(String numberDocument, String typeDocument, boolean isNotAfiliate) {

        LocalDateTime now = LocalDateTime.now();
        String sequence = String.valueOf(updateCode(sequence()));
        String dataPersonal = typeDocument
                .concat(now.format(formatter))
                .concat("N")
                .concat(numberDocument)
                .concat("A");

        String code = dataPersonal.concat(sequence);


        return code.length() == Constant.CERTIFICATE_VALIDATION_CODE_SIZE ? code : dataPersonal.concat(complete(sequence, code.length()));
    }

    private String dataPersonal(UserMain user){

        LocalDateTime now = LocalDateTime.now();
        String sequence = String.valueOf(updateCode(sequence()));
        String dataPersonal = user.getIdentificationType()
                .concat(now.format(formatter))
                .concat(user.getFirstName().substring(0,1))
                .concat(user.getIdentification())
                .concat(user.getSurname().substring(0,1));

        String code = dataPersonal.concat(sequence);


        return  code.length() == Constant.CERTIFICATE_VALIDATION_CODE_SIZE ? code : dataPersonal.concat(complete(sequence, code.length()));


    }

    private String complete(String code, int num){

        StringBuilder complete = new StringBuilder();
        num = (Constant.CERTIFICATE_VALIDATION_CODE_SIZE - num);
        num = num < Constant.CERTIFICATE_VALIDATION_CODE_SIZE ? num : 0;
        complete.append("0".repeat(Math.max(0, num)));
        return complete.toString().concat(code);

    }

    private CodeValidCertificate sequence(){
        String year = String.valueOf(LocalDateTime.now().getYear());
        Specification<CodeValidCertificate> spec = UserSpecifications.codeCertificated(year);
        Optional<CodeValidCertificate> validCertificate = iCodeValidCertificateRepository.findOne(spec);

        if(validCertificate.isEmpty()){
            resetSequence();
            validCertificate = iCodeValidCertificateRepository.findOne(spec);
            return validCertificate.orElseThrow(() -> new IllegalStateException(Constant.ERROR_SEQUENCE));
        }
        return validCertificate.orElseThrow(() -> new IllegalStateException(Constant.ERROR_SEQUENCE));
    }

    private int updateCode(CodeValidCertificate codeValidCertificate){

        String year = String.valueOf(LocalDateTime.now().getYear());

        if(!year.equals(codeValidCertificate.getStartSequence())){
            resetSequence();
            codeValidCertificate = sequence();
        }

        codeValidCertificate.setSequence(codeValidCertificate.getSequence() + 1);
        return iCodeValidCertificateRepository.save(codeValidCertificate).getSequence();
    }

    private void resetSequence(){
        try {
            iCodeValidCertificateRepository.save(new CodeValidCertificate(null, String.valueOf(LocalDateTime.now().getYear()), 0));
        }catch (Exception e){
            throw  new ErrorCreateSequence(Constant.ERROR_SEQUENCE);
        }
    }
}



