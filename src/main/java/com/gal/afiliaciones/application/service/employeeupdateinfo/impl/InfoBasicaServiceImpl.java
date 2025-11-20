package com.gal.afiliaciones.application.service.employeeupdateinfo.impl;

import com.gal.afiliaciones.application.service.IUserRegisterService;
import com.gal.afiliaciones.application.service.employeeupdateinfo.InfoBasicaService;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.InfoBasicaRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.audit.PersonaUpdateTraceRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.projection.InfoBasicaProjection;
import com.gal.afiliaciones.infrastructure.dto.InfoBasicaDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateInfoBasicaRequest;
import com.gal.afiliaciones.domain.model.audit.PersonaUpdateTrace;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.UserPreRegisterDto;
import com.gal.afiliaciones.infrastructure.service.RegistraduriaUnifiedService;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.BeanUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.HashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfoBasicaServiceImpl implements InfoBasicaService {

    private final InfoBasicaRepository infoRepo;
    private final IUserPreRegisterRepository userRepo;
    private final PersonaUpdateTraceRepository traceRepo;
    private final IUserRegisterService userRegisterService;
    private final AffiliationDetailRepository affiliationDetailRepository;
    private final AffiliationDependentRepository affiliationDependentRepository;
    private final AffiliateRepository affiliateRepository;


    @Override
    public InfoBasicaDTO consultarInfoBasica(String documento) {
        InfoBasicaProjection v = infoRepo.findInfoBasicaByDocumento(documento)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No se encontró información para el documento: " + documento
                ));

        if (!isTipoPersona(v.getTipoDocumento())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten tipos de identificación de persona (no NIT)."
            );
        }

        InfoBasicaDTO dto = mapToDTO(v);
        boolean isRegistry = false;
        int codeWarning = 0;

        if (Constant.CC.equalsIgnoreCase(v.getTipoDocumento())) {
            try {
                UserDtoApiRegistry apiRegistry = userRegisterService.searchUserInNationalRegistry(v.getNumeroIdentificacion());

                if(apiRegistry.getIdStatus().equals("21")){
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Solo se permite actualizacion para persona con idStatus diferente a 21 (Fallecido)"
                    );
                }

                if (apiRegistry != null && apiRegistry.getFirstName() != null) {


                    UserPreRegisterDto userRegistryData = UserPreRegisterDto.builder()
                            .identificationType(v.getTipoDocumento())
                            .identification(v.getNumeroIdentificacion())
                            .firstName(apiRegistry.getFirstName())
                            .secondName(apiRegistry.getSecondName())
                            .surname(apiRegistry.getSurname())
                            .secondSurname(apiRegistry.getSecondSurname())
                            .dateBirth(apiRegistry.getDateBirth())
                            .phoneNumber("")
                            .build();

                    BeanUtils.copyProperties(apiRegistry, userRegistryData);
                    userRegistryData.setSex(RegistraduriaUnifiedService.mapGender(apiRegistry.getGender()));
                    userRegistryData.setUserFromRegistry(true);
                    userRegistryData.setStatusPreRegister(false);

                    isRegistry = true;

                    Map<String, Object> diffs = comparatorsLimited(v, userRegistryData);
                    dto = applyRegistryDifferences(dto, diffs);

                    if (!diffs.isEmpty()) {
                        codeWarning = 1;
                    }

                }else {
                    isRegistry = false;
                }
            } catch (Exception e) {
                isRegistry = false;
            }
        }

        return new InfoBasicaDTO(
                dto.tipoDocumento(),
                dto.numeroIdentificacion(),
                dto.primerNombre(),
                dto.segundoNombre(),
                dto.primerApellido(),
                dto.segundoApellido(),
                dto.fechaNacimiento(),
                dto.edad(),
                dto.nacionalidad(),
                dto.sexo(),
                dto.afp(),
                dto.eps(),
                dto.email(),
                dto.telefono1(),
                dto.telefono2(),
                dto.idDepartamento(),
                dto.idCiudad(),
                dto.direccionTexto(),
                dto.fechaNovedad(),
                dto.observaciones(),
                codeWarning,
                isRegistry
        );

    }


    @Override
    @Transactional
    public void actualizarInfoBasica(String documentoObjetivo,
                                     UpdateInfoBasicaRequest r,
                                     String documentoUsuarioLogueado) {

        validarRequestBasico(documentoObjetivo, r, documentoUsuarioLogueado);

        Optional<LocalDate> ingresoOpt = infoRepo.findLatestAffiliationDateByDoc(documentoObjetivo);
        if (r.fechaNacimiento() != null) {
            validarFechaNacimiento(r.fechaNacimiento(), ingresoOpt.orElse(null));
        }

        List<Affiliate> affiliates = affiliateRepository
                .findAllByDocumentTypeAndDocumentNumber(r.tipoDocumento(), documentoObjetivo);

        if (!affiliates.isEmpty()) {
            for (Affiliate aff : affiliates) {
                procesarAfiliado(aff, r, documentoObjetivo);
            }
        }

        registrarTraza(documentoObjetivo, documentoUsuarioLogueado, null);
    }


    private void procesarAfiliado(Affiliate aff,
                                  UpdateInfoBasicaRequest r,
                                  String documentoObjetivo) {

        if (Constant.TYPE_AFFILLATE_DEPENDENT.equals(aff.getAffiliationType())) {
            procesarDependiente(r, documentoObjetivo);
            return;
        }

        if (Constant.TYPE_AFFILLATE_INDEPENDENT.equals(aff.getAffiliationType())) {
            procesarIndependiente(r, documentoObjetivo);
        }
    }

    private void procesarDependiente(UpdateInfoBasicaRequest r, String documentoObjetivo) {
        affiliationDependentRepository.updateInfoBasicaForDependents(
                n(r.primerNombre()), n(r.segundoNombre()), n(r.primerApellido()), n(r.segundoApellido()),
                r.sexo(), r.fechaNacimiento(), nullSafeTrim(r.email()), nullSafeTrim(r.telefono1()),
                nullSafeTrim(r.telefono2()), nullSafeTrim(r.direccionTexto()), parseLongSafe(r.nacionalidad()),
                parseLongSafe(r.afp()), parseLongSafe(r.eps()), asLong(r.idDepartamento()), asLong(r.idCiudad()),
                r.tipoDocumento(), documentoObjetivo
        );
    }


    private void procesarIndependiente(UpdateInfoBasicaRequest r, String documentoObjetivo) {

        UserMain usuario = obtenerUsuario(documentoObjetivo);
        validarUsuarioParaActualizacion(usuario);

        actualizarCamposPersonales(usuario, r);
        actualizarCamposGenerales(usuario, r);
        actualizarObservaciones(documentoObjetivo, r);

        usuario.setLastUpdate(LocalDateTime.now());
        userRepo.save(usuario);

        procesarAfiliacionesIndependiente(r, documentoObjetivo);
    }

    private void procesarAfiliacionesIndependiente(UpdateInfoBasicaRequest r,
                                                   String documentoObjetivo) {

        affiliationDetailRepository.updateInfoBasicaForAffiliates(
                n(r.primerNombre()), n(r.segundoNombre()), n(r.primerApellido()), n(r.segundoApellido()),
                r.sexo(), r.fechaNacimiento(), nullSafeTrim(r.email()), nullSafeTrim(r.telefono1()),
                nullSafeTrim(r.telefono2()), nullSafeTrim(r.direccionTexto()), parseLongSafe(r.nacionalidad()),
                parseLongSafe(r.afp()), parseLongSafe(r.eps()), asLong(r.idDepartamento()), asLong(r.idCiudad()),
                r.tipoDocumento(), documentoObjetivo
        );
    }



    private void registrarTraza(String targetDoc, String actorDoc, String actorRoleHint) {
        TrazaInfo trazaInfo = obtenerInformacionTraza(actorDoc, actorRoleHint);

        PersonaUpdateTrace t = new PersonaUpdateTrace();
        t.setActorUserId(trazaInfo.actorUserId());
        t.setActorDoc(actorDoc != null ? actorDoc.trim() : null);
        t.setActorRoleId(trazaInfo.actorRoleId());
        t.setActorRoleName(trazaInfo.actorRoleName());
        t.setTargetUserDoc(targetDoc);
        traceRepo.save(t);
    }

    private TrazaInfo obtenerInformacionTraza(String actorDoc, String actorRoleHint) {
        if (actorDoc == null || actorDoc.isBlank()) {
            return new TrazaInfo(null, null, null);
        }

        Optional<UserMain> actorUserOpt = userRepo.findByDocumentoNormalizado(actorDoc);
        if (actorUserOpt.isEmpty()) {
            return new TrazaInfo(null, null, null);
        }

        Long userId = actorUserOpt.get().getId();
        InfoBasicaRepository.RolePair rolePair = buscarRolUsuario(userId, actorRoleHint);

        if (rolePair != null) {
            return new TrazaInfo(userId, rolePair.getId(), rolePair.getNombreRol());
        } else {
            String roleName = (actorRoleHint != null && !actorRoleHint.isBlank()) ?
                    actorRoleHint.trim() : null;
            return new TrazaInfo(userId, null, roleName);
        }
    }

    private InfoBasicaRepository.RolePair buscarRolUsuario(Long userId, String actorRoleHint) {
        if (actorRoleHint != null && !actorRoleHint.isBlank()) {
            Optional<InfoBasicaRepository.RolePair> specificRole =
                    infoRepo.findRoleByUserIdAndRoleName(userId, actorRoleHint);
            if (specificRole.isPresent()) {
                return specificRole.get();
            }
        }

        return infoRepo.findAnyRoleByUserId(userId).orElse(null);
    }

    private record TrazaInfo(Long actorUserId, Long actorRoleId, String actorRoleName) {}

    private void validarRequestBasico(String documentoObjetivo, UpdateInfoBasicaRequest r, String documentoUsuarioLogueado) {
        if (documentoObjetivo == null || documentoObjetivo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Documento objetivo vacío");
        }
        if (r == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body de actualización vacío");
        }
        if (documentoUsuarioLogueado != null &&
                normalizar(documentoUsuarioLogueado).equals(normalizar(documentoObjetivo))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se permite auto-actualización.");
        }
    }

    private UserMain obtenerUsuario(String documentoObjetivo) {
        return userRepo.findByDocumentoNormalizado(documentoObjetivo)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }


    private List<Affiliation> obtenerAffiliations(String tipoDocumento,String documentoObjetivo) {
        return affiliationDetailRepository.findByDocumentTypeAndNumber(tipoDocumento,documentoObjetivo);
    }

    private List<AffiliationDependent> obtenerAffiliationsDepent(String tipoDocumento,String documentoObjetivo) {
        return affiliationDependentRepository.findByDocumentTypeAndNumber(tipoDocumento,documentoObjetivo);
    }


    private void validarUsuarioParaActualizacion(UserMain u) {
        final String tipoDoc = n(u.getIdentificationType());

        if (!isTipoPersona(tipoDoc)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Solo se permiten tipos de identificación de persona (no NIT)."
            );
        }

        if (!isActivo(u)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El trabajador está inactivo; edición no permitida."
            );
        }
    }

    private void actualizarCamposPersonales(UserMain u, UpdateInfoBasicaRequest r) {
        if (r.primerNombre() != null)    u.setFirstName(n(r.primerNombre()));
        if (r.segundoNombre() != null)   u.setSecondName(n(r.segundoNombre()));
        if (r.primerApellido() != null)  u.setSurname(n(r.primerApellido()));
        if (r.segundoApellido() != null) u.setSecondSurname(n(r.segundoApellido()));
        if (r.sexo() != null)            u.setSex(r.sexo());
        if (r.fechaNacimiento() != null) u.setDateBirth(r.fechaNacimiento());
    }

    private void actualizarCamposGenerales(UserMain u, UpdateInfoBasicaRequest r) {
        actualizarCamposContacto(u, r);
        actualizarCamposEntidades(u, r);
        actualizarCamposDireccion(u, r);
    }

    private void actualizarCamposContacto(UserMain u, UpdateInfoBasicaRequest r) {
        if (r.email() != null)          u.setEmail(nullSafeTrim(r.email()));
        if (r.telefono1() != null)      u.setPhoneNumber(nullSafeTrim(r.telefono1()));
        if (r.telefono2() != null)      u.setPhoneNumber2(nullSafeTrim(r.telefono2()));
        if (r.direccionTexto() != null) u.setAddress(nullSafeTrim(r.direccionTexto()));
    }

    private void actualizarCamposEntidades(UserMain u, UpdateInfoBasicaRequest r) {
        if (r.nacionalidad() != null) u.setNationality(parseLongSafe(r.nacionalidad()));
        if (r.afp() != null)          u.setPensionFundAdministrator(parseLongSafe(r.afp()));
        if (r.eps() != null)          u.setHealthPromotingEntity(parseLongSafe(r.eps()));
    }

    private void actualizarCamposDireccion(UserMain u, UpdateInfoBasicaRequest r) {
        if (r.idDepartamento() != null)     u.setIdDepartment(asLong(r.idDepartamento()));
        if (r.idCiudad() != null)           u.setIdCity(asLong(r.idCiudad()));
    }


    private void actualizarObservaciones(String documentoObjetivo, UpdateInfoBasicaRequest r) {
        String obs = n(r.observaciones());
        if (obs != null && obs.length() >= 10) {
            infoRepo.updateAffiliateObservationByDoc(documentoObjetivo, obs);
        }
    }

    private void validarFechaNacimiento(LocalDate fechaNac, LocalDate fechaIngreso) {
        if (fechaNac == null) return;
        LocalDate hoy = LocalDate.now();

        if (fechaNac.isAfter(hoy)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento no puede ser futura");
        }
        if (fechaNac.isBefore(hoy.minusYears(90))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La diferencia entre fecha de nacimiento y hoy no puede ser mayor a 90 años");
        }
        if (fechaIngreso != null && fechaNac.isAfter(fechaIngreso)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de nacimiento no puede ser mayor a la fecha de ingreso");
        }
    }

    private boolean isTipoPersona(String tipo) {
        return tipo != null && !tipo.equalsIgnoreCase("NIT");
    }

    private boolean isActivo(UserMain u) {
        Boolean act = u.getStatusActive();
        return act == null || Boolean.TRUE.equals(act);
    }


    private String normalizar(String value) {
        if (value == null) return null;
        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }


    private String nullSafeTrim(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
    private Long parseLongSafe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.matches("\\d+") ? Long.valueOf(t) : null;
    }
    private Long asLong(Integer i) {
        return i == null ? null : i.longValue();
    }
    private static String n(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private InfoBasicaDTO mapToDTO(InfoBasicaProjection v) {
        return new InfoBasicaDTO(
                v.getTipoDocumento(), v.getNumeroIdentificacion(),
                v.getPrimerNombre(), v.getSegundoNombre(), v.getPrimerApellido(), v.getSegundoApellido(),
                v.getFechaNacimiento(), v.getEdad(), v.getNacionalidad(), v.getSexo(),
                v.getAfp(), v.getEps(), v.getEmail(), v.getTelefono1(), v.getTelefono2(),
                v.getIdDepartamento(), v.getIdCiudad(),v.getDireccionTexto(),v.getFechaNovedad(),v.getObservaciones(),
                0,
                false
        );
    }


    private Map<String, Object> comparatorsLimited(InfoBasicaProjection bd, UserPreRegisterDto reg) {
        Map<String, Object> diffs = new HashMap<>();

        addIfDifferent("primerNombre", normalizar(bd.getPrimerNombre()), normalizar(reg.getFirstName()), diffs);
        addIfDifferent("segundoNombre", normalizar(bd.getSegundoNombre()), normalizar(reg.getSecondName()), diffs);
        addIfDifferent("primerApellido", normalizar(bd.getPrimerApellido()), normalizar(reg.getSurname()), diffs);
        addIfDifferent("segundoApellido", normalizar(bd.getSegundoApellido()), normalizar(reg.getSecondSurname()), diffs);
        addIfDifferent("sexo", bd.getSexo(), reg.getSex(), diffs);
        addIfDifferent("fechaNacimiento", bd.getFechaNacimiento(), reg.getDateBirth(), diffs);

        return diffs;
    }

    private void addIfDifferent(String field, Object bdValue, Object regValue, Map<String, Object> map) {

        if (regValue == null) return;

        if (regValue instanceof String && ((String) regValue).trim().isEmpty()) return;

        if (!Objects.equals(bdValue, regValue)) {
            map.put(field, regValue);
        }
    }

    private InfoBasicaDTO applyRegistryDifferences(InfoBasicaDTO dto, Map<String, Object> diffs) {
        return new InfoBasicaDTO(
                dto.tipoDocumento(),
                dto.numeroIdentificacion(),
                (String) diffs.getOrDefault("primerNombre", dto.primerNombre()),
                (String) diffs.getOrDefault("segundoNombre", dto.segundoNombre()),
                (String) diffs.getOrDefault("primerApellido", dto.primerApellido()),
                (String) diffs.getOrDefault("segundoApellido", dto.segundoApellido()),
                (LocalDate) diffs.getOrDefault("fechaNacimiento", dto.fechaNacimiento()),
                dto.edad(),
                dto.nacionalidad(),
                (String) diffs.getOrDefault("sexo", dto.sexo()),
                dto.afp(),
                dto.eps(),
                dto.email(),
                dto.telefono1(),
                dto.telefono2(),
                dto.idDepartamento(),
                dto.idCiudad(),
                dto.direccionTexto(),
                dto.fechaNovedad(),
                dto.observaciones(),
                !diffs.isEmpty() ? 1 : dto.codeWarning(), // codeWarning
                dto.isRegistry()
        );
    }

}
