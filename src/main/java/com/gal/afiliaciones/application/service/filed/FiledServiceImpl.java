package com.gal.afiliaciones.application.service.filed;

import com.gal.afiliaciones.application.service.consecutive.ConsecutiveService;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dto.consecutive.ConsecutiveRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FiledServiceImpl implements FiledService {

    private final AffiliateRepository sequenceRepository;
    private final ConsecutiveService consecutiveService;

    private static final String PREFIX = "%s%09d";

    @Override
    public String getNextFiledNumberAffiliation(){
        ConsecutiveRequestDTO request = new ConsecutiveRequestDTO();
        request.setPrefix(Constant.PREFIX_REQUEST_AFFILIATION);
        request.setProcessId(Constant.ID_PROCESS_AFFILIATIONS);
        String prefixFiled = consecutiveService.getConsecutive(request);

        long nextNumber = sequenceRepository.nextFiledNumberAffiliation();

        return String.format(PREFIX, prefixFiled, nextNumber);

    }

    @Override
    public String getNextFiledNumberUpdateAffiliation(){
        ConsecutiveRequestDTO request = new ConsecutiveRequestDTO();
        request.setPrefix(Constant.PREFIX_UPDATE_AFFILIATION);
        request.setProcessId(Constant.ID_PROCESS_AFFILIATIONS);
        String prefixFiled = consecutiveService.getConsecutive(request);

        long nextNumber = sequenceRepository.nextFiledNumberUpdateAffiliation();

        return String.format(PREFIX, prefixFiled, nextNumber);

    }

    @Override
    public String getNextFiledNumberForm(){
        ConsecutiveRequestDTO request = new ConsecutiveRequestDTO();
        request.setPrefix(Constant.PREFIX_FORM);
        request.setProcessId(Constant.ID_PROCESS_AFFILIATIONS);
        String prefixFiled = consecutiveService.getConsecutive(request);

        long nextNumber = sequenceRepository.nextFiledNumberForm();

        return String.format(PREFIX, prefixFiled, nextNumber);
    }


    @Override
    public String getNextFiledNumberCertificate(){
        ConsecutiveRequestDTO request = new ConsecutiveRequestDTO();
        request.setPrefix(Constant.PREFIX_CERTIFICATE);
        request.setProcessId(Constant.ID_PROCESS_AFFILIATIONS);
        String prefixFiled = consecutiveService.getConsecutive(request);

        long nextNumber = sequenceRepository.nextFiledNumberCertificate();

        return String.format(PREFIX, prefixFiled, nextNumber);
    }

    @Override
    public String getNextFiledNumberRetirementReason() {
        ConsecutiveRequestDTO request = new ConsecutiveRequestDTO();
        request.setPrefix(Constant.PREFIX_RETIREMENT);
        request.setProcessId(Constant.ID_PROCESS_AFFILIATIONS);

        String prefixFiled = consecutiveService.getConsecutive(request);

        long nextNumber = sequenceRepository.nextFiledNumberRetirementReason();

        return String.format(PREFIX, prefixFiled, nextNumber);
    }

    @Override
    public String getNextFiledNumberPermanentNovelty(){
        ConsecutiveRequestDTO request = new ConsecutiveRequestDTO();
        request.setPrefix(Constant.PREFIX_PERMANENT_NOVELTY);
        request.setProcessId(Constant.ID_PROCESS_AFFILIATIONS);
        String prefixFiled = consecutiveService.getConsecutive(request);

        long nextNumber = sequenceRepository.nextFiledNumberNovelty();
        return String.format(PREFIX, prefixFiled, nextNumber);
    }

}
