package com.gal.afiliaciones.application.service.impl;

import com.gal.afiliaciones.application.service.CertificateNoAffiliateService;
import com.gal.afiliaciones.application.service.CodeValidCertificationService;
import com.gal.afiliaciones.application.service.filed.FiledService;
import com.gal.afiliaciones.domain.model.ArlInformation;
import com.gal.afiliaciones.domain.model.QrDocument;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IQrRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.arl.ArlInformationDao;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.consecutive.ConsecutiveRequestDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CertificateNoAffiliateServiceImpl implements CertificateNoAffiliateService {

    private final GenericWebClient genericWebClient;
    private final CodeValidCertificationService codeValidCertificationService;
    private final CertificateRepository certificateRepository;
    private final IQrRepository qrRepository;
    private final ArlInformationDao arlInformationDao;
    private final FiledService filedService;

    @Override
    public String validateNonAffiliateCertificate(String identification, String typeIdentification) {

        Certificate certificate =  new Certificate();

        Optional<Certificate> optionalCertifica = certificateRepository.findByNumberDocumentAndTypeDocument(typeIdentification, identification);

        if(optionalCertifica.isPresent()){

            certificate = optionalCertifica.get();

            if(validateDate(LocalDate.parse(certificate.getExpeditionDate()))){
                List<QrDocument> all = qrRepository.findByIdentificationNumberAndIssueDate(certificate.getValidatorCode(), LocalDate.parse(certificate.getExpeditionDate()));
                all.forEach(qrRepository::delete);
                certificateRepository.delete(certificate);
            }

            return generateNonAffiliateCertificate(certificate);
        }

        String codeValidation = codeValidCertificationService.consultCode(identification, typeIdentification, true);
        String dateNow = String.valueOf(LocalDate.now());

        certificate.setNumberDocument(identification);
        certificate.setTypeDocument(typeIdentification);
        certificate.setValidatorCode(codeValidation);
        certificate.setExpeditionDate(dateNow);
        certificate.setVinculationType(Constant.TYPE_NOT_AFFILLATE);
        certificate.setFiledNumber(filedService.getNextFiledNumberCertificate());
        certificateRepository.save(certificate);

        return generateNonAffiliateCertificate(certificate);
    }

    private String generateNonAffiliateCertificate(Certificate certificate){

        CertificateReportRequestDTO reportRequestDTO = new CertificateReportRequestDTO();
        reportRequestDTO.setReportName("certificado no afiliado");
        reportRequestDTO.setIdReport(null);

        LocalDate date = LocalDate.parse(certificate.getExpeditionDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'días del mes de' MMMM 'del' yyyy", new Locale("es", "ES"));
        String dateFormat = date.format(formatter);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("validatorCode", certificate.getValidatorCode());
        parameters.put("identification", certificate.getNumberDocument());
        parameters.put("identificationType", certificate.getTypeDocument());
        parameters.put("city", "Bogotá D.C.");
        parameters.put("expeditionDate", dateFormat);
        parameters.put("consecutivoDoc", certificate.getFiledNumber());
        parameters.put("nameSignatureARL", certificate.getNameSignatureARL() != null ? certificate.getNameSignatureARL() : "");

        List<ArlInformation> allArlInformation = arlInformationDao.findAllArlInformation();
        parameters.put(Constant.NAME_ARL_LABEL, allArlInformation.get(0).getName());
        parameters.put(Constant.NIT, allArlInformation.get(0).getNit());

        reportRequestDTO.setParameters(parameters);

        return genericWebClient.generateReportCertificate(reportRequestDTO);
    }

    private boolean validateDate(LocalDate date){

        return LocalDate.now().isAfter(date.plusMonths(1));
    }
}
