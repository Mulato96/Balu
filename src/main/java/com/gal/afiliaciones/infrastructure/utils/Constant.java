package com.gal.afiliaciones.infrastructure.utils;

import java.math.BigDecimal;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constant {
    public static final String NULL_FIELD = "Este campo no puede estar vacío";
    public static final String INVALID_DOCUMENT_TYPE = "El tipo de documento ingresado no es valido";
    public static final String INVALID_DOCUMENT_CONDITIONS = "Las condiciones para este tipo de documento no se cumplen";
    public static final String USER_AND_TYPE_ALREADY_EXISTS = "Ya existe un usuario con este tipo y número de documento.";
    public static final String EMAIL_ALREADY_EXISTS = "Este correo ya está en uso, por favor intenta de nuevo";
    public static final String PHONE1_ALREADY_EXISTS = "Ya existe un usuario con este teléfono.";
    public static final String USER_NOT_FOUND_IN_NATIONAL_REGISTRY = "El usuario no fue encontrado y la consulta al Registro Nacional no es aplicable.";
    public static final String USER_NOT_FOUND_IN_DATA_BASE = "El usuario no fue encontrado y la consulta a la base de datos no es aplicable.";
    public static final String ERROR_REGISTER_USER = "Error al registrar el usuario: ";
    public static final String ERROR_CONSULT_USER = "Error al consultar el usuario: ";
    public static final String VALIDATION_OTHER_SEX = "El campo Otro Género no puede tener más de 50 caracteres.";
    public static final String VALIDATION_ADDRESS = "La dirección no puede tener más de 200 caracteres.";
    public static final String VALIDATION_PHONE = "El número de teléfono debe tener un máximo de 15 dígitos.";
    public static final String PHONE = "El numero de telefono tiene un formato incorrecto";
    public static final String CC = "CC";
    public static final String CE = "CE";
    public static final String PA = "PA";
    public static final String NI = "NI";
    public static final String TI = "TI";
    public static final String CD = "CD";
    public static final String PE = "PE";
    public static final String RC = "RC";
    public static final String SC = "SC";
    public static final String PT = "PT";
    public static final String DV = "DV";
    public static final String N = "N";
    public static final String J = "J";
    public static final String VALIDATION_EMAIL = "El correo electrónico no puede tener más de 100 caracteres.";
    public static final String EXCEEDS_LENGTH = "El número excede la longitud máxima permitida.";
    public static final String PASSWORD_SIZE = "La contraseña debe contener al menos 8 caracteres";
    public static final String PASSWORD_NOT_CONTAIN_LETTERS_CAPITAL = "La contraseña debe contener al menos una letra mayúscula";
    public static final String PASSWORD_NOT_CONTAIN_LETTERS_LOWERCASE = "La contraseña debe contener al menos una letra minúscula";
    public static final String PASSWORD_NOT_CONTAIN_NUMBERS = "La contraseña debe contener al menos un número";
    public static final String PASSWORD_NOT_CONTAIN_CHARACTERISTICS_SPECIAL = "La contraseña debe contener al menos un carácter especial";
    public static final String PASSWORD_CHANGE_SUCCESSFUL = "Cambio de contraseña exitoso. Por favor, dirígete al inicio de sesión para acceder al portal con tu nueva contraseña";
    public static final String PASSWORD_NOT_CONTAIN_DATA_PERSONAL = "La contraseña no debe contener el nombre, apellido o número de documento";
    public static final String PASSWORD_NOT_CONTAIN_SEQUENCES = "Error, la contraseña no puede tener secuencias de números o letras de más de tres caracteres";
    public static final String USER_NOT_FOUND = "Usuario no existente. Lo sentimos, no estás en el sistema. Te invitamos a que te registres";
    public static final String ERROR_REGISTER_USER_KEYCLOAK = "No se pudo registrar el usuario en Keycloak";
    public static final String ERROR_UPDATE_USER_KEYCLOAK = "No se pudo actualizar el usuario en Keycloak";
    public static final String VALIDATION_DOCUMENT_NUMBER = "El número de documento no puede tener más de 16 caracteres.";
    public static final String USER_NOT_AFFILIATE = "El número de documento ingresado no se encuentra registrado en nuestra ARL, Puedes obtener el Certificado de No afiliado dando clic en el botón Descargar certificado";
    public static final String NUMBER_MAX_ATTEMPTS_FOR_DAY = "Haz excedido el máximo de intentos, inténtalo nuevamente dentro de las próximas 12 horas";
    public static final String CERTIFICATE_CODE_VALIDATION_MESSAGE = "No ha sido posible validar  tu identidad, por favor solicita la de actualización de tus datos a tu empleador, si eres independiente debes actualizar tus datos en el portal o puedes comunicarte con nuestros canales de atención";
    public static final String MESSAGE = "message";
    public static final String STATUS = "STATUS";
    public static final String CODE = "CODE";
    public static final String ERROR = "ERROR";
    public static final String LIMIT_ATTEMPTS_EXCEEDED = "LIMIT_ATTEMPTS_EXCEEDED";
    public static final String VALIDATION_CODE_INCORRECT = "El código de validación capturado no es el mismo enviado, por favor valida y vuelve a intentar.";
    public static final String VALIDATION_CODE_HAS_EXPIRED = "El código de validación ha expirado";
    public static final String VALIDATION_SUCCESSFUL = "Verificación exitosa";
    public static final String PLANTILLA_OTP = "plantilla-otp.html";
    public static final String USER = "Usuario";
    public static final String VALIDITY = "vigencia";
    public static final String UPDATE_PRE_REGISTER_SUCCESSFUL = "Estado pre registro actualizado correctamente";
    public static final String UPDATE_REGISTER_SUCCESSFUL = "Estado registro actualizado correctamente";
    public static final String UPDATE_ACTIVE_SUCCESSFUL = "Estado activo actualizado correctamente";
    public static final String UPDATE_INACTIVE_SUCCESSFUL = "Estado inactivo actualizado correctamente";
    public static final String AFFILATE_NOT_FOUND_DOCUMENT_TYPE = "Affiliate not found with document type ";
    public static final String AFFILATE_NOT_FOUND_CERTIFICATE = " and document number: ";
    public static final String TYPE_AFFILIATE_STUDENT = "Tipo estudiante";
    public static final String ERROR_SEQUENCE = "Error, Secuencia no encontrada";
    public static final String TYPE_NOT_AFFILLATE = "certificado no afiliado";
    public static final String TYPE_AFFILLATE_INDEPENDENT_WORKER = "certificado afiliaciones trabajador independiente";
    public static final String TYPE_AFFILLATE_DEPENDENT_WORKER = "certificado afiliaciones trabajador dependiente";
    public static final String TYPE_AFFILLATES = "certificado afiliaciones";
    public static final String TYPE_AFFILLATE_JUDICIAL_PROCESSES = "certificado afiliaciones procesos judiciales";
    public static final String TYPE_AFFILLATE_EMPLOYER_DOMESTIC = "Empleador Servicio Doméstico";
    public static final String TYPE_AFFILIATE_EMPLOYER = "certificado afiliaciones empleador";
    public static final String DATE_FORMAT_SHORT_LATIN = "dd/MM/yyyy";
    public static final String TYPE_AFFILIATE_EMPLOYER_OPS = "certificado afiliaciones trabajador ops";
    public static final String TYPE_CERTIFICATE_SINGLE_MEMBERSHIP = "certificado unico afiliacion";
    public static final String DATE_FORMAT_CERTIFICATE_EXPEDITION = "d 'días del mes de' MMMM 'del' yyyy";
    public static final String USER_NOT_AFFILIATE_CARD = "Señor documento, no presentas información en nuestra ARL";
    public static final String TYPE_CERTIFICATE_INDEPENDENT_VOLUNTEER = "certificado independiente voluntario";
    public static final String USER_NOT_AFFILIATED = "El usuario no cuenta con afiliacion activa!";
    public static final String ERROR_FIND_CONTRACT_EXTENSION = "La extensión del contrato no fue encontrado!";
    public static final String ERROR_FIND_CARD = "El carnet no fue encontrado!";
    public static final String AFFILIATE_NOT_FOUND = "La afiliacion no se encuentra";
    public static final String ERROR_CODE_VALIDATION_EXPIRED = "El código se ha vencido. Solicita un nuevo certificado";
    public static final String ERROR_CALCULATE_TIME_EXPIRED_CERTIFICATION = "Error calculando el tiempo de vigencia del certificado";
    public static final int CERTIFICATE_VALIDATION_CODE_SIZE = 26;
    public static final String CODE_ARL = "14-23";
    public static final String ERROR_SEND_EMAIL = "Error al enviar el email";
    public static final String UPDATE_INFO_EMPLOYEE_INDEPENDENT = "Actualización exitosa, La actualización de los datos se ha registrado correctamente";
    public static final String USER_NOT_AFFILIATE_ACTIVE = "El usuario no cuenta con afiliación activa";
    public static final String ERROR_AFFILIATE_EMPLOYER = "No encontramos tu afiliación como trabajador. Si eres empleador, no necesitas un carné de afiliación. Para descargar tu certificado de empleador, ingresa con tu usuario y contraseña";
    public static final String DOCUMENTS_NOT_FOUND_MESSAGE = "No se encontraron documentos asociados a la afiliación";
    public static final String ERROR_AGE = "El usuario con Tipo documento está fuera del rango de afiliación.Si es un error, contáctanos: link | TeléfonoARL";
    public static final String NO_INFORMATION = "Sin información";
    public static final String NO_RECORD_LABEL = "No registra";
    /*-----------------------Type Certificate------------------------------*/
    public static final String NIT = "nit";
    public static final String VALIDATOR_CODE = "validatorCode";
    public static final String TYPE_IDENTIFICATION = "typeIdentification";
    public static final String DOCUMENT_IDENTIFIER = "documentIdentifier";
    public static final String IDENTIFICATION_TYPE_NAME = "identificationType";
    public static final String IDENTIFICATION = "identification";
    public static final String COMPANY = "company";
    public static final String COMPANY_NAME = "companyName";
    public static final String NAME = "name";
    public static final String ECONOMY_ACTIVITY = "economyActivity";
    public static final String NAME_ACTIVITY = "nameActivity";
    public static final String MEMBERSHIP_DATE = "membershipDate";
    public static final String COVERAGE_DATE = "coverageDate";
    public static final String STATUS_FIELD = "status";
    public static final String RETIREMENT_DATE = "retirementDate";
    public static final String VINCULATION_TYPE = "vinculationType";
    public static final String RISK = "risk";
    public static final String CITY = "city";
    public static final String EXPEDITION_DATE = "expeditionDate";
    public static final String NAME_SIGNATURE_ARL = "nameSignatureARL";
    public static final String NAME_ARL_LABEL = "nameARL";
    public static final String ADDRESSED_TO_LABEL = "addressedTo";
    public static final String CITY_ARL = "Bogotá D.C";
    public static final String ADDRESSED_TO_DEFAULT = "Quien pueda interesar";
    public static final String COMPANY_MOTTO = "Lema de la empresa";
    public static final String COMPANY_NIT = "nitCompany";
    public static final String COMPANY_NUMBER = "numberCompany";
    public static final String NAME_CONTRACTOR = "nameContractor";
    public static final String CONTRACTOR_DOCUMENT_TYPE = "typeDocumentContract";
    public static final String CONTRACTOR_DOCUMENT_NUMBER = "numberDocumentContract";
    public static final String INIT_CONTRACT_DATE = "initContractDate";
    public static final String END_CONTRACT_DATE = "endContractDate";
    public static final String POSITION = "position";
    public static final String INACTIVATION_DATE = "inactivationDate";
    public static final String CERT_PARAM_DEPENDENT_WORKERS_NUMBER = "dependedWorkersNumber";
    public static final String CERT_PARAM_INDEPENDENT_WORKERS_NUMBER = "independentWorkerNumber";
    /*-----------------------LOGIN------------------------------*/
    public static final String PASSWORD_INCORRECT = "Por favor verifica la contraseña que ingresaste.";
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String TOKEN = "token";
    public static final String AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE_LABEL = "Content-Type";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String GRANT_TYPE = "grant_type";
    public static final String USER_INFO = "user_info";
    public static final String USER_NOT_FOUND_LOGIN = "Lo sentimos no estás dentro del sistema te invitamos a que te registres";
    public static final String USER_INACTIVE = "Usuario inactivo, por favor comunícate con el administrador del sistema";
    public static final int MAX_LOGIN_ATTEMPTS = 3;
    public static final Integer MAX_GENERATE_ATTEMPTS = 5;
    public static final String ERROR_CREATE_USER_KEYCLOAK = "Ocurrio un error mientras se estaba creando el usuario en keycloak";
    /*-----------------------ACTIVITY ECONOMIC------------------------------*/
    public static final String DESCRIPTION_NOT_FOUND_MESSAGE = "Descripcion no encontrada";
    public static final String CODE_CIIU_NOT_FOUND_MESSAGE = "Por favor, verifica tu RUT e intentalo nuevamente";
    public static final String CODE_CIIU_SHORTER_LENGTH = "Solo se admiten valores númericos de 4 a 7 caracteres";
    public static final String ACTIVITY_ECONOMIC_CODE_LENGTH = "La longitud del código debe ser de 7 digitos";
    public static final Long LENGTH_CODE_CIIU = 4L;
    public static final Long LENGTH_ECONOMIC_ACTIVITY_CODE = 7L;
    public static final String CODE_DESCRIPTION_NULL = "ingresa un mínimo de 4 caracteres";
    public static final String ID_ECONOMIC_SECTOR_EMPTY = "Id sector económico esta vacio";

    /*-----------------------Affiliation--------------------------------*/

    public static final String TEMPLANTE_EMAIL_REJECT_DOCUMENTS = "rejectAffiliation/template-email-reject-documents.html";
    public static final String TEMPLANTE_EMAIL_SING = "sing/template-email-sing.html";
    public static final String TEMPLANTE_EMAIL_WELCOME = "welcome/template-email-welcome.html";
    public static final String TEMPLATE_EMAIL_UPDATE_DEPENDENT = "update/template-email-update-dependent.html";
    public static final String TEMPLATE_EMAIL_UPDATE_EMPLOYER = "update/template-email-update-employer.html";
    public static final String TEMPLATE_EMAIL_UPDATE_MASSIVE = "update/proceso-autorizacion-masivo.html";
    public static final String TEMPLATE_EMAIL_RETIREMENT_WORKER = "retirement/template-retiro-trabajador.html";
    public static final String TEMPLATE_EMAIL_BULK_LOADING = "plantilla-carga-masiva-dependiente-independiente.html";
    public static final String TEMPLATE_EMAIL_HEADQUARTERS = "plantilla-sedes.html";
    public static final String AFFAIR_EMAIL_REJECT_DOCUMENTS = "Proceso de afiliación";
    public static final String EMAIL_ARL = "miarl@correo.com.co";
    public static final String ERROR_AFFILIATION = "La afiliacion se encuentra en un estado que no puede ser validado por el momento";
    public static final String ERROR_FIND_DOCUMENT_ALFRESCO = "No se encuentran los documentos en el repositorio";
    public static final String ERROR_DOCUMENTS_REJECT = "Se encontraron documentos que no se an revisado o fueron rechazados";
    public static final String AFFILIATION_SUBTYPE_TAXI_DRIVER = "Taxista";
    public static final String TI_DOCUMENT_TYPE_RESTRICTED = "De acuerdo con la resolución 2389 del año 2019, no se permite la afiliación de empleadores con tipo de documento Tarjeta de identidad";
    public static final String INVALID_PERSON_TYPE_DOCUMENT = "El documento no corresponde a ningun tipo de persona";
    public static final String INVALID_VERIFICATION_DIGIT = "El Dígito de verificación no corresponde o no aplica para el tipo de documento seleccionado. Verifica, el tipo y número de documento del empleador, intenta nuevamente, tienes 2 intentos más";

    public static final String REQUEST_DENIED_MERCANTILE = "template-email-confirmation-interview-web.html";
    public static final String INTERVIEW_WEB_MERCANTILE = "template-email-confirmation-interview-web.html";

    public static final String APPROVED_INTERVIEW_WEB_MERCANTILE = "template-emails/APPROVED_INTERVIEW_WEB_MERCANTILE.html";
    public static final String WELCOME_MERCANTILE = "template-emails/WELCOME_MERCANTILE.html";
    public static final String REMINDER_INTERVIEW_WEB_MERCANTILE_OFFICIAL = "template-emails/REMINDER_INTERVIEW_WEB_MERCANTILE_OFFICIAL.html";
    public static final String REMINDER_INTERVIEW_WEB_MERCANTILE_USER = "template-emails/REMINDER_INTERVIEW_WEB_MERCANTILE_USER.html";
    public static final String CONFIRMATION_INTERVIEW_WEB_MERCANTILE = "template-emails/CONFIRMATION_INTERVIEW_WEB_MERCANTILE.html";

    /*-----------------------ADD OPTION------------------------------*/
    public static final String MAXIMUM_ACTIVITIES_ALLOWED = "Ha excedido el numero de actividades permitidas en el sistema";
    public static final String REPORTS = "Reportes";
    public static final String NEWS = "Novedades";
    public static final String ADD_WORKER = "Ingresar trabajador";
    public static final String UPDATE_DATA = "Actualizar Datos";
    public static final String NORMALIZATIONS = "Normalizaciones";
    public static final String EMPLOYER_WORKER_WITHDRAWALS = "Retiros empleador / trabajador";
    public static final String CREATE_COMPANY = "Crear empresa";
    public static final String REPORT_WORKER = "Reporte de trabajadores";
    public static final String NORMALIZATIONS_MADE = "Normalizaciones realizadas";
    public static final String NORMALIZATIONS_SPECIAL = "Normalizaciones especiales";
    public static final String FAILED_AFFILIATION = "afiliaciones fallidas";
    public static final String GENERATE_CERTIFICATE = "Certificados generados";
    public static final String AFFILIATION_SUBTYPE_DOMESTIC_SERVICES = "Servicios Domesticos";
    public static final String AFFILIATION_STATUS_ACTIVE = "Activa";
    public static final String AFFILIATION_STATUS_INACTIVE = "Inactiva";
    public static final String STAGE_MANAGEMENT_DOCUMENTAL_REVIEW = "Revisión documental";
    public static final String INTERVIEW_WEB = "entrevista web";
    public static final String SING = "firma";
    public static final String REGULARIZATION = "regularizacion";
    public static final String SCHEDULING = "Agendamiento";
    public static final String SUSPENDED = "Suspendida";
    public static final String ACCEPT_AFFILIATION = "Afiliación completa";
    public static final String PENDING_COMPLETE_FORM = "Pendiente completar formulario";
    public static final String NOT_FOUND_INTERVIEW_WEB = "No se encontro la entrevista web";
    public static final Long USER_STATUS_ACTIVE = 1L;
    public static final Long EXTERNAL_USER_TYPE = 2L;

    /*-----------------------ADD OPTION------------------------------*/
    public static final String CANCEL_AFFILIATION_NOT_FOUND = "Verifica los datos e intentalo nuevamente";
    public static final String DATE_CANCEL_AFFILIATION = "El período de 24 horas permitido para la anulación ha expirado. Si deseas retirar al trabajador, debes hacerlo utilizando la opción:";
    public static final String WORKER_UNCONNECTED = "Trabajador no existe, verifica los datos e intenta nuevamente";

    /*-----------------------CONSECUTIVE AFFILIATION------------------------------*/
    public static final String PREFIX_REQUEST_AFFILIATION = "SOL";
    public static final String PREFIX_UPDATE_AFFILIATION = "ACT";
    public static final String PREFIX_WORKER_RETIREMENT = "RET";
    public static final Long ID_PROCESS_AFFILIATIONS = 1L;

    /*----------------------- FIXED AFFILIATION VALUES ------------------------------*/
    public static final Long PROCEDURE_TYPE_AFFILIATION = 1L;
    public static final Long PROCEDURE_TYPE_TRANSFER = 2L;
    public static final Long PROCEDURE_TYPE_END_AFFILIATION = 3L;
    public static final String NAME_LEGAL_NATURE_EMPLOYER = "Privada";
    public static final Long CODE_LEGAL_NATURE_EMPLOYER = 2L;
    public static final String NAME_CONTRIBUTOR_TYPE_EMPLOYER = "Empleador";
    public static final String CODE_CONTRIBUTOR_TYPE_EMPLOYER = "01";
    public static final String CODE_MAIN_ECONOMIC_ACTIVITY_DOMESTIC = "1970001"; //Servicio domestico
    public static final String ECONOMIC_ACTIVITY_DOMESTIC_2 = "3869201"; //Enfermeras
    public static final String ECONOMIC_ACTIVITY_DOMESTIC_3 = "1970002"; //Mayordomos
    public static final String ECONOMIC_ACTIVITY_DOMESTIC_4 = "3970001"; //Conductores
    public static final String CODE_MAIN_ECONOMIC_ACTIVITY_TAXI_DRIVER = "4492103";
    public static final String DESCRIPTION_ECONOMIC_ACTIVITY_TAXI_DRIVER = "Transporte de pasajeros, incluye el " +
            "transporte terrestre de pasajeros por sistemas de transporte urbano y suburbano, que abarca transporte " +
            "individual (taxis)";
    public static final String NAME_CONTRIBUTOR_TYPE_TAXI_DRIVER = "Independiente";

    /*-----------------------GENERATION CARD---------------------------------------*/
    public static final String ERROR_GENERATED_CARD = "Error generando el carned";

    public static final String EMAIL_SUBJECT_NAME = "affair";

    public static final String AFFILIATE_NOT_FOUND_MESSAGE = "Ten en cuenta. Se generará la afiliación de un trabajador independiente cuyo contratante no tiene afiliación activa en la ARL (Decreto 723 de 2013, que regula la afiliación al Sistema General de Riesgos Laborales para personas con contratos de prestación de servicios y trabajadores independientes de alto riesgo)";
    public static final String TYPE_AFFILLATE_INDEPENDENT = "Trabajador Independiente";
    public static final String SUBTYPE_AFFILIATE_INDEPENDENT_VOLUNTEER = "Voluntario";
    public static final String NAME_CONTRIBUTOR_TYPE_INDEPENDENT_COLUNTEER = "Independiente sin contrato (Voluntario)";
    public static final String CODE_CONTRIBUTOR_TYPE_INDEPENDENT = "02";
    public static final Long CODE_CONTRIBUTANT_TYPE_VOLUNTEER = 57L;
    public static final Long CODE_CONTRIBUTANT_TYPE_TAXI_DRIVER = 3L;
    public static final Long CODE_CONTRIBUTANT_TYPE_PROVISION_SERVICE = 59L;
    public static final Long CODE_CONTRIBUTANT_TYPE_COUNCILLOR = 35L;
    public static final Long CODE_CONTRIBUTANT_TYPE_EDIL = 60L;
    public static final Long CODE_CONTRIBUTANT_TYPE_DOMESTIC = 2L;
    public static final String CODE_CONTRIBUTANT_SUBTYPE_NOT_APPLY = "0";
    public static final String CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE = "11";
    public static final String CODE_CONTRIBUTANT_SUBTYPE_TAXI_DRIVE_WITHOUT_PENSION = "12";
    public static final Long CODE_CONTRIBUTANT_TYPE_DEPENDENT = 1L;
    public static final Long CODE_CONTRIBUTANT_TYPE_STUDENT = 23L;
    public static final Long CODE_CONTRIBUTANT_TYPE_APPRENTICE = 20L;
    public static final Long CODE_CONTRIBUTANT_TYPE_INDEPENDENT = 3L;

    /*-----------------------NIT special cases--------------------------------*/
    public static final String NIT_CONTRACT_VOLUNTEER = "800000003";
    public static final String DV_CONTRACT_VOLUNTEER = "4";
    public static final String COMPANY_NAME_CONTRACT_VOLUNTEER = "Afiliación voluntaria decreto 1563";
    public static final String NIT_CONTRACT_723 = "900000005";
    public static final String COMPANY_NAME_CONTRACT_723 = "Empresa Contratante 723";

    /*-----------------------SAT Independiente Volutario--------------------------------*/
    public static final String LEGAL_ENTITY = "J";
    public static final Long CONTRIBUTOR_TYPE_INDEPENDENT_SAT = 2L;
    public static final String CONTRIBUTOR_CLASS_INDEPENDENT = "I";
    public static final String URBAN_ZONE_CODE = "U";
    public static final Long INDEPENDENT_VOLUNTEER_CODE_SAT = 57L;
    public static final Long INDEPENDENT_SUBTYPE_CODE_SAT = 0L;

    /*-----------------------Formulario independientes--------------------------------*/
    public static final String CONTRACT_TYPE_ADMINISTRATIVE = "Administrativo";
    public static final String CONTRACT_TYPE_COMMERCIAL = "Comercial";
    public static final String CONTRACT_TYPE_CIVIL = "Civil";
    public static final String CONTRACT_PRIVATE = "Privado";
    public static final String CONTRACT_PUBLIC = "Público";
    public static final String WORKING_DAY_SINGLE = "Jornada única";
    public static final String WORKING_DAY_ROTATING = "Jornada rotativa";
    public static final String WORKING_DAY_SHIFTS = "Jornada turnos";
    public static final String WORKING_DAY_NO_SCHEDULE = "Sin horario definido";
    public static final String DOES_NOT_APPLY = "N/A";

    /*-----------------------Zona direccion--------------------------------*/
    public static final String RURAL_ZONE = "R";
    public static final String URBAN_ZONE = "U";

    /*-----------------------Mercantil--------------------------------*/
    public static final String SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE = "Actividades mercantiles";
    public static final String TYPE_AFFILLATE_EMPLOYER = "Empleador";

    /*-----------------------Prestacion de servicios--------------------------------*/
    public static final String SUBTYPE_AFFILIATE_INDEPENDENT_PROVISION_SERVICES = "Prestación de servicios";

    /*-----------------------Estilos observaciones--------------------------------*/
    public static final String STYLES_OBSERVATION = "<p class=\"text\" style=\"color: #393B38 !important; margin: 0;\" >";
    public static final String CLOSING_STYLES_OBSERVATION = "</p>";

    /*-----------------------Concejal Edil--------------------------------*/
    public static final String SUBTYPE_AFFILIATE_INDEPENDENT_COUNCILLOR = "Concejal edil";
    public static final String NIT_MAYORALTY_BOGOTA = "899999061";

    /*-----------------------CONSECUTIVE CERTIFICATE------------------------------*/
    public static final String PREFIX_CERTIFICATE = "CER";
    public static final String PREFIX_FORM = "FOR";
    public static final String CONSECUTIVE_DOCUMENT = "consecutivoDoc";

    public static final String TYPE_AFFILLATE_DEPENDENT = "Trabajador Dependiente";
    public static final Integer AGE_ADULT = 18;
    public static final String BONDING_TYPE_DEPENDENT = "Dependiente";
    public static final String BONDING_TYPE_INDEPENDENT = "Independiente";
    public static final String BONDING_TYPE_STUDENT = "Estudiante en práctica";
    public static final String BONDING_TYPE_STUDENT_DECREE = "Estudiante Decreto 055";
    public static final String BONDING_TYPE_APPRENTICE = "Aprendiz SENA";

    public static final String PREFIX_RETIREMENT = "RET";
    public static final String PREFIX_WORKER_DISPLACEMENT = "SND";
    public static final String RETIRED = "Retirado";
    public static final String CONCILIATION_NOT_COLLECTED = "Cotizantes liquidados y recaudados por igual valor";

    /*-----------------------NOVELTY TYPE------------------------------*/
    public static final String NOVELTY_TYPE_AFFILIATION = "Afiliación";
    public static final String NOVELTY_TYPE_RETIREMENT = "Retiro";
    public static final String NOVELTY_TYPE_WORKER_DISPLACEMENT = "Desplazamiento trabajador";
    public static final String POLICY_NOT_FOUND = "poliza no encontrada";
    public static final String EMPLOYEE = "Trabajador";


    /*-----------------------NOVELTY GENERAL------------------------------*/
    public static final String REMAINDER_RETIRE = "El trabajador se encuentra en estado pendiente por retirar";
    public static final String IMMEDIATE_RETIREMENT = "El trabajador se encuentra retirado";
    public static final String AFFILIATION = "Ingreso";
    public static final String CANCELLATION_SUCCESSFUL = "Anular afiliación";
    public static final String RETIRE_SUCCESSFUL = "Retiro";
    public static final String APPLIED = "Aplicado";
    public static final String NOT_APPLIED = "No Aplicado";
    public static final String AFFILIATION_SUCCESSFUL = "Ingreso - ING (aportante) Cotizante (afiliación)";
    public static final String CONTRACT_EXTENSION_SUCCESSFULL = "Prórroga contrato";
    public static final String CONTRACT_EXTENSION = "Se realiza extensión del contrato";



    /*-----------------------UPDATE PASSWORD------------------------------*/
    public static final String CURRENT_PASSWORD_INCORRECT = "La contraseña actual es incorrecta.";
    public static final String PASSWORD_AND_CONFIRM_DIFFERENT = "Las contraseñas no coinciden, inténtalo de nuevo.";
    public static final String PASSWORD_EQUALS_TO_CURRENT = "La nueva contraseña no puede ser igual a la contraseña actual.";
    public static final String TEMPORAL_PASSWORD_EXPIRED = "El tiempo para cambiar tu contraseña ha expirado. Puedes asignar una nueva ingresando (aquí).";

    public static final Long ID_ECONOMY_ACTIVITY_ONE = 190L;
    public static final Long ID_ECONOMY_ACTIVITY_TWO = 407L;
    public static final Long ID_ECONOMY_ACTIVITY_THREE = 709L;
    public static final Long ID_ECONOMY_ACTIVITY_FOUR = 1164L;
    public static final long INACTIVITY_THRESHOLD_HOURS = 72L;
    public static final long REMINDER_START_DAYS = 20L;
    public static final long REMINDER_END_DAYS = 30L;

    public static final String REMAINDER_AFFILIATION = "Solicitud de afiliación pendiente";
    public static final String USER_INACTIVE_PENDING_AFFILIATION = "Es necesario que actives tu cuenta. Te enviaremos un código a  tu correo electrónico y/o celular registrado, para que puedas continuar con tu afiliación";

    public static final BigDecimal PERCENTAGE_40 = new BigDecimal("0.4");

    public static final Long REQUEST_CHANNEL_PORTAL = 1L;

    public static final String PARAM_DAYS_FORCED_UPDATE_PASSWORD = "DaysForcedUpdatePassword";

    /*-----------------------EMAIL USER REGISTER-----------------------------*/
    public static final String TEMPLATE_EMAIL_WELCOME_REGISTER = "welcome/template-email-welcome-register.html";
    public static final String SUBJECT_EMAIL_WELCOME_REGISTER = "Portal ";

    /*-----------------------QR VALID DATE-----------------------------*/
    public static final long LIMIT_QR_CERTIFICATE_VALID = 30L;

    /*-----------------------MAIN OFFICE SPECIFICATION-----------------------------*/
    public static final String FIELD_OFFICE_MANAGER = "officeManager";
    public static final String FIELD_ID_DEPARTMENT = "idDepartment";
    public static final String FIELD_ID_CITY = "idCity";
    public static final String FIELD_ID_AFFILIATE = "idAffiliate";

    /*-----------------------CERTIFICATE SERVICE-----------------------------*/
    public static final String ECONOMIC_ACTIVITY_NOT_FOUND = "No se encontro la actividad enconomica";

    /*-----------------------MERCANTILE FORM-----------------------------*/
    public static final String FIELD_CLASE_RIESGO = "claseRiesgo";

    /*-----------------------USER SPECIFICATIONS-----------------------------*/
    public static final String FIELD_IDENTIFICATION = "identification";
    public static final String AFFILIATE_INDEPENDENT_RELATIONSHIP = "Afiliación Activa.  Este trabajador ya cuenta con vinculación como independiente. Si deseas continuar con la afiliación, debes registrar un nuevo contrato con una actividad económica distinta a la registrada anteriormente.";

    /*-----------------------NOVELTIES------------------------------*/
    public static final String NOVELTY_ING = "ING";
    public static final String NOVELTY_TRANSITIONAL = "Transitoria";
    public static final String NOVELTY_RET = "RET";
    public static final String NOVELTY_VSP = "VSP";
    public static final String NOVELTY_VCT = "VCT";
    public static final String NOVELTY_PENDING_STATUS = "Pendiente";
    public static final String NOVELTY_APPLY_STATUS = "Aplicado";
    public static final String NOVELTY_NOT_APPLY_STATUS = "No aplicado";
    public static final String NOVELTY_REVIEW_STATUS = "En revisión";
    public static final Long CAUSAL_PENDING = 1L;
    public static final Long CAUSAL_APPLY = 2L;
    public static final Long CAUSAL_NOT_APPLY_OFFICIAL = 42L;
    public static final Long CAUSAL_EMPLOYER_NOT_AFFILIATE = 3L;
    public static final Long CAUSAL_EMPLOYER_INACTIVE = 6L;
    public static final Long CAUSAL_NOT_DATE_NOVELTY = 31L;
    public static final Long CAUSAL_LESS_DATE = 4L;
    public static final Long CAUSAL_COMPENSATION_FUND = 27L;
    public static final Long CAUSAL_CONTRIBUTANT_DOMESTIC = 25L;
    public static final Long CAUSAL_CONTRIBUTANT_123 = 23L;
    public static final Long CAUSAL_MULTIPLE_CONTRACT_RISK_4_AND_5 = 33L;
    public static final Long CAUSAL_INDEPENDENT_RISK_4_AND_5 = 43L;
    public static final Long CAUSAL_EXIST_ECONOMIC_ACTIVTY = 44L;
    public static final String NOVELTY_VALUE_C = "C";
    public static final String MASCULINE_GENDER = "M";
    public static final Long COLOMBIAN_NATIONALITY = 1L;
    public static final Long OTHER_OCCUPATIONS_ID = 7736L;
    public static final Long TAXI_DRIVER_ID = 6056L;
    public static final Long COUNCILLOR_ID = 357L;
    public static final Long APPRENTICE_SENA_ID = 86L;
    public static final Long STUDENT_DECRE055_ID = 662L;
    public static final String FOSYGA_CODE_EPS = "EPS103";
    public static final String TEMPLATE_EMAIL_PILA_APPLY = "pila/template-novedad-pila-aplicada.html";
    public static final String TEMPLATE_EMAIL_PILA_NOT_APPLY = "pila/template-novedad-pila-no-aplicada.html";
    public static final String TEMPLATE_EMAIL_PILA_RETIREMENT_NOT_APPLY = "pila/template-notificacion-novedad-2.html";
    public static final String PREFIX_PERMANENT_NOVELTY = "NPILA";

    public static final Long REQUEST_CHANNEL_PILA = 4L;
    public static final String PASSWORD_EXPIRED = "Contrasena expirada";

    /*----------------------- TIPOS POLIZA -----------------------------*/

    public static final Long ID_EMPLOYER_POLICY = 1L;
    public static final Long ID_CONTRACTOR_POLICY = 2L;

    public static final String IDENTIFICATION_TYPE = "identificationType";
    public static final String PRE_REGISTER = "preregistro";
    /*----------------------- Novedades RUAF -----------------------------*/
    public static final String NOVELTY_RUAF_RETIREMENT_CODE = "R04";
    public static final Integer NOVELTY_RUAF_CAUSAL_PENSION = 1;
    public static final Integer NOVELTY_RUAF_CAUSAL_DISASSOCIATION = 2;
    public static final Integer NOVELTY_RUAF_CAUSAL_DEATH = 4;
    
    // Valores por defecto para campos obligatorios de dirección
    public static final Long DEFAULT_MAIN_STREET_ID = 1L;
    public static final Long DEFAULT_NUMBER_MAIN_STREET_ID = 1L;
    
    // Tolerancia para validación de IBC (diferencias de redondeo)
    public static final BigDecimal IBC_TOLERANCE = new BigDecimal("1.00");

    /*----------------------- Integraciones -----------------------------*/
    public static final String USER_AUD = "LinkTICUsr";
    public static final String MAQ_AUD = "LinkTICMaq";
    public static final Integer ID_COLOMBIA_COUNTRY = 170;
    public static final String ECONOMIC_ACTIVITY_DEFAULT = "1829902";
    public static final String EPS_DEFAULT = "EPS103";
    public static final int AFP_DEFAULT = 0;
    public static final String EMPLOYER_VOLUNTEER = "899999998";




    /*------------------------Certificados masivo---------------------------*/
    public static final String TEMPLATE_CERTIFICATE_MASSIVE = "template-emails/massive_affiliation_notification.html";
}