package com.gal.afiliaciones.application.service.officeremployerupdate.impl;

import com.gal.afiliaciones.application.service.officeremployerupdate.EmployerLookupService;
import com.gal.afiliaciones.infrastructure.dao.repository.updateEmployerData.OfficerAffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerBasicProjection;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.EmployerUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.LegalRepUpdateRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.employer.updateDataEmployer.LegalRepViewDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class EmployerLookupServiceImpl implements EmployerLookupService {

    private final OfficerAffiliateMercantileRepository mercRepo;

    public EmployerLookupServiceImpl(OfficerAffiliateMercantileRepository mercRepo) {
        this.mercRepo = mercRepo;
    }

    private static String nz(String s) { return s == null ? "" : s.trim(); }
    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
    private static void badRequest(String msg) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg); }

    private static void requireNotBlank(String value, String fieldLabel) {
        if (isBlank(value)) badRequest("El campo " + fieldLabel + " es obligatorio");
    }

    private static void requireAnyPhone(String p1, String p2) {
        if (isBlank(p1) && isBlank(p2)) badRequest("Debe informar al menos un teléfono (phone1 o phone2)");
    }

    private static void validateEmailFormat(String email) {
        if (!isBlank(email) && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            badRequest("El correo electrónico no tiene un formato válido");
        }
    }

    private static String normDocType(String v) {
        if (v == null) return null;
        String x = v.toUpperCase().replaceAll("[^A-Z0-9]", "");
        return x.isEmpty() ? null : x;
    }

    private static String normDocNumber(String v) {
        if (v == null) return null;
        String x = v.replaceAll("[^0-9]", "");
        return x.isEmpty() ? null : x;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EmployerBasicProjection> findBasic(String docType, String docNumber) {
        return mercRepo.findBasicByDoc(docType, docNumber);
    }

    @Override
    @Transactional
    public int updateBasic(EmployerUpdateDTO dto) {
        String keyType = normDocType(dto.getDocType());
        String keyNum = normDocNumber(dto.getDocNumber());
        if (isBlank(keyType) || isBlank(keyNum)) badRequest("docType y docNumber (llave de búsqueda) son obligatorios");

        requireNotBlank(dto.getBusinessName(), "businessName");
        requireNotBlank(dto.getDepartmentId(), "departmentId");
        requireNotBlank(dto.getCityId(), "cityId");
        requireNotBlank(dto.getAddressFull(), "addressFull");
        requireAnyPhone(dto.getPhone1(), dto.getPhone2());
        requireNotBlank(dto.getEmail(), "email");
        validateEmailFormat(dto.getEmail());

        String docTypeNew = normDocType(dto.getDocTypeNew());
        String business = nz(dto.getBusinessName());
        String depId = nz(dto.getDepartmentId());
        String cityId = nz(dto.getCityId());
        String addressFull = nz(dto.getAddressFull());
        String phone1 = nz(dto.getPhone1());
        String phone2 = nz(dto.getPhone2());
        String email = nz(dto.getEmail());

        return mercRepo.updateBasicByDoc(
                keyType, keyNum, docTypeNew, business, depId, cityId,
                addressFull, phone1, phone2, email
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<LegalRepViewDTO> findLegalRep(String docType, String docNumber) {
        return mercRepo.findLegalRepByDoc(docType, docNumber);
    }

    @Override
    @Transactional
    public int updateLegalRep(LegalRepUpdateRequestDTO dto) {
        String keyType = normDocType(dto.getDocType());
        String keyNum = normDocNumber(dto.getDocNumber());
        if (isBlank(keyType) || isBlank(keyNum)) badRequest("docType y docNumber (llave de búsqueda) son obligatorios");

        //if (isBlank(dto.getCauseCode())) badRequest("La causal es obligatoria");
        //if (isBlank(dto.getEventDate())) badRequest("La fecha de novedad es obligatoria");
       // if (dto.getObservations() == null || dto.getObservations().trim().length() < 10) {
          //  badRequest("Las observaciones deben tener al menos 10 caracteres");
        //}

        requireNotBlank(dto.getEpsId(), "epsId");
        requireNotBlank(dto.getAfpId(), "afpId");
        requireNotBlank(dto.getAddressFull(), "addressFull");
        requireAnyPhone(dto.getPhone1(), dto.getPhone2());
        requireNotBlank(dto.getEmail(), "email");
        validateEmailFormat(dto.getEmail());

        String epsId = nz(dto.getEpsId());
        String afpId = nz(dto.getAfpId());
        String addressFull = nz(dto.getAddressFull());
        String phone1 = nz(dto.getPhone1());
        String phone2 = nz(dto.getPhone2());
        String email = nz(dto.getEmail());

        return mercRepo.updateLegalRepByDoc(
                keyType, keyNum, epsId, afpId, addressFull, phone1, phone2, email
        );
    }
}
