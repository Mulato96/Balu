package com.gal.afiliaciones.application.service.employeeupdateinfo.impl;

import com.gal.afiliaciones.application.service.employeeupdateinfo.InfoBasicaService;
import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.InfoBasicaRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.audit.PersonaUpdateTraceRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.projection.InfoBasicaProjection;
import com.gal.afiliaciones.infrastructure.dto.InfoBasicaDTO;
import com.gal.afiliaciones.infrastructure.dto.UpdateInfoBasicaRequest;
import com.gal.afiliaciones.domain.model.audit.PersonaUpdateTrace;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.gal.afiliaciones.application.service.audit.PersonaUpdateTraceService;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class InfoBasicaServiceImpl implements InfoBasicaService {

    private final InfoBasicaRepository infoRepo;
    private final IUserPreRegisterRepository userRepo;
    private final PersonaUpdateTraceService traceService;
    private final PersonaUpdateTraceRepository traceRepo;

    public InfoBasicaServiceImpl(InfoBasicaRepository infoRepo,
                                 IUserPreRegisterRepository userRepo,
                                 PersonaUpdateTraceService traceService,
                                 PersonaUpdateTraceRepository traceRepo) {
        this.infoRepo = infoRepo;
        this.userRepo = userRepo;
        this.traceService = traceService;
        this.traceRepo = traceRepo;
    }

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

        return mapToDTO(v);
    }

    @Override
    @Transactional
    public void actualizarInfoBasica(String documentoObjetivo,
                                     UpdateInfoBasicaRequest r,
                                     String documentoUsuarioLogueado) {

        validarRequestBasico(documentoObjetivo, r, documentoUsuarioLogueado);

        UserMain u = obtenerUsuario(documentoObjetivo);
        validarUsuarioParaActualizacion(u);

        final boolean esCC = "CC".equalsIgnoreCase(n(u.getIdentificationType()));

        actualizarCamposPersonales(u, r, esCC, documentoObjetivo);
        actualizarCamposGenerales(u, r);
        actualizarObservaciones(documentoObjetivo, r);

        u.setLastUpdate(java.time.LocalDateTime.now());
        userRepo.saveAndFlush(u);
        registrarTraza(documentoObjetivo, documentoUsuarioLogueado, null);
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

    private void actualizarCamposPersonales(UserMain u, UpdateInfoBasicaRequest r, boolean esCC, String documentoObjetivo) {
        if (esCC) {
            return; // No se actualizan campos personales para CC
        }

        if (r.primerNombre() != null)    u.setFirstName(n(r.primerNombre()));
        if (r.segundoNombre() != null)   u.setSecondName(n(r.segundoNombre()));
        if (r.primerApellido() != null)  u.setSurname(n(r.primerApellido()));
        if (r.segundoApellido() != null) u.setSecondSurname(n(r.segundoApellido()));
        if (r.sexo() != null)            u.setSex(r.sexo());

        if (r.fechaNacimiento() != null) {
            LocalDate nueva = r.fechaNacimiento();
            Optional<LocalDate> ingresoOpt = infoRepo.findLatestAffiliationDateByDoc(documentoObjetivo);
            validarFechaNacimiento(nueva, ingresoOpt.orElse(null));
            u.setDateBirth(nueva);
        }
    }

    private void actualizarCamposGenerales(UserMain u, UpdateInfoBasicaRequest r) {
        actualizarCamposContacto(u, r);
        actualizarCamposEntidades(u, r);
        actualizarCamposDireccion(u, r);
        actualizarCamposPropiedadesHorizontales(u, r);

        if (r.idCargo() != null) u.setPosition(r.idCargo());
    }

    private void actualizarCamposContacto(UserMain u, UpdateInfoBasicaRequest r) {
        if (r.email() != null)          u.setEmail(nullSafeTrim(r.email()));
        if (r.telefono1() != null)      u.setPhoneNumber(nullSafeTrim(r.telefono1()));
        if (r.telefono2() != null)      u.setPhoneNumber2(nullSafeTrim(r.telefono2()));
        if (r.direccionTexto() != null) u.setAddress(nullSafeTrim(r.direccionTexto()));
        if (r.bis() != null)            u.setIsBis(Boolean.TRUE.equals(r.bis()));
    }

    private void actualizarCamposEntidades(UserMain u, UpdateInfoBasicaRequest r) {
        if (r.nacionalidad() != null) u.setNationality(parseLongSafe(r.nacionalidad()));
        if (r.afp() != null)          u.setPensionFundAdministrator(parseLongSafe(r.afp()));
        if (r.eps() != null)          u.setHealthPromotingEntity(parseLongSafe(r.eps()));
    }

    private void actualizarCamposDireccion(UserMain u, UpdateInfoBasicaRequest r) {
        if (r.idDepartamento() != null)     u.setIdDepartment(asLong(r.idDepartamento()));
        if (r.idCiudad() != null)           u.setIdCity(asLong(r.idCiudad()));

        if (r.idCallePrincipal() != null)         u.setIdMainStreet(parseLongSafe(r.idCallePrincipal()));
        if (r.numeroCallePrincipal() != null)     u.setIdNumberMainStreet(parseLongSafe(r.numeroCallePrincipal()));
        if (r.letra1CallePrincipal() != null)     u.setIdLetter1MainStreet(parseLongSafe(r.letra1CallePrincipal()));
        if (r.letra2CallePrincipal() != null)     u.setIdLetter2MainStreet(parseLongSafe(r.letra2CallePrincipal()));
        if (r.puntoCardinalCallePrincipal() != null) u.setIdCardinalPointMainStreet(parseLongSafe(r.puntoCardinalCallePrincipal()));

        if (r.numero1Secundaria() != null)  u.setIdNum1SecondStreet(parseLongSafe(r.numero1Secundaria()));
        if (r.numero2Secundaria() != null)  u.setIdNum2SecondStreet(parseLongSafe(r.numero2Secundaria()));
        if (r.letraSecundaria() != null)    u.setIdLetterSecondStreet(parseLongSafe(r.letraSecundaria()));
        if (r.puntoCardinal2() != null)     u.setIdCardinalPoint2(parseLongSafe(r.puntoCardinal2()));
    }

    private void actualizarCamposPropiedadesHorizontales(UserMain u, UpdateInfoBasicaRequest r) {
        if (r.ph1() != null)    u.setIdHorizontalProperty1(parseLongSafe(r.ph1()));
        if (r.numPh1() != null) u.setIdNumHorizontalProperty1(parseLongSafe(r.numPh1()));
        if (r.ph2() != null)    u.setIdHorizontalProperty2(parseLongSafe(r.ph2()));
        if (r.numPh2() != null) u.setIdNumHorizontalProperty2(parseLongSafe(r.numPh2()));
        if (r.ph3() != null)    u.setIdHorizontalProperty3(parseLongSafe(r.ph3()));
        if (r.numPh3() != null) u.setIdNumHorizontalProperty3(parseLongSafe(r.numPh3()));
        if (r.ph4() != null)    u.setIdHorizontalProperty4(parseLongSafe(r.ph4()));
        if (r.numPh4() != null) u.setIdNumHorizontalProperty4(parseLongSafe(r.numPh4()));
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

    private String normalizar(String s) {
        return s == null ? "" : s.replaceAll("\\D", "");
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
                v.getIdDepartamento(), v.getIdCiudad(),
                v.getIdCallePrincipal(), v.getNumeroCallePrincipal(),
                v.getLetra1CallePrincipal(), v.getLetra2CallePrincipal(), v.getPuntoCardinalCallePrincipal(), v.getBis(),
                v.getNumero1Secundaria(), v.getNumero2Secundaria(), v.getLetraSecundaria(), v.getPuntoCardinal2(),
                v.getPh1(), v.getNumPh1(), v.getPh2(), v.getNumPh2(), v.getPh3(), v.getNumPh3(), v.getPh4(), v.getNumPh4(),
                v.getDireccionTexto(), v.getIdCargo(), v.getFechaNovedad(), v.getObservaciones(),
                v.getNacionalidadNombre(),
                v.getAfpNombre(),
                v.getEpsNombre(),
                v.getCiudadNombre(),
                v.getDepartamentoNombre()
        );
    }
}