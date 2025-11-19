package com.gal.afiliaciones.infrastructure.validation;

import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.Map;

@Component
public class BulkMessageService {

    private final Map<BulkMsg, String> messages = new EnumMap<>(BulkMsg.class);

    public BulkMessageService() {
        messages.put(BulkMsg.FIELD_REQUIRED, "El campo es obligatorio");
        messages.put(BulkMsg.INVALID_FORMAT, "Formato inválido para tipo {0}");
        messages.put(BulkMsg.INVALID_LENGTH_EXACT, "Debe tener exactamente {0} dígitos (tiene {1})");
        messages.put(BulkMsg.INVALID_CHARACTERS, "Contiene caracteres no permitidos (solo letras, espacios y tildes)");
        messages.put(BulkMsg.DATE_FORMAT_INVALID, "Formato de fecha inválido (use yyyy/MM/dd)");
        messages.put(BulkMsg.DATE_TOO_OLD, "Fecha de nacimiento no válida (muy antigua)");
        messages.put(BulkMsg.AGE_BELOW_MIN_CC, "El trabajador debe ser mayor de 18 años para tipo CC (tiene {0} años)");
        messages.put(BulkMsg.PHONE_LENGTH, "Debe tener exactamente 10 dígitos (tiene {0})");
        messages.put(BulkMsg.PHONE_AREA_CODE_INVALID, "Código de área inválido (debe iniciar con: 601, 602, 300-324, 350-351)");
        messages.put(BulkMsg.COVERAGE_BEFORE_TODAY, "La fecha debe ser mayor o igual a hoy");
        messages.put(BulkMsg.COVERAGE_AFTER_WINDOW, "La fecha no puede ser mayor a 30 días desde hoy");
        messages.put(BulkMsg.SALARY_NON_NUMERIC, "Debe ser un valor numérico válido");
        messages.put(BulkMsg.SALARY_BELOW_MIN, "Debe ser mayor o igual al SMMLV ({0})");
        messages.put(BulkMsg.SALARY_ABOVE_MAX, "No puede superar 25 veces el SMMLV (máximo: {0})");
        messages.put(BulkMsg.IBC_OUT_OF_RANGE, "El IBC calculado (40% del valor mensual) debe estar entre el SMMLV y 25 veces el SMMLV. El valor mensual del contrato debe ser al menos 2.5 veces el SMMLV");
        messages.put(BulkMsg.IBC_CANNOT_COMPUTE, "No se pudo calcular el IBC a partir del valor mensual");
        messages.put(BulkMsg.ECON_CODE_NOT_FOUND, "Código de actividad económica no existe en el catálogo");
        messages.put(BulkMsg.ECON_CODE_NOT_ASSOCIATED, "La actividad económica no está registrada para este empleador");
        messages.put(BulkMsg.DOC_DUPLICATE_FILE, "Número de documento duplicado en el archivo");
        messages.put(BulkMsg.DOC_DUPLICATE_DB, "Este trabajador ya está afiliado a este empleador");
        messages.put(BulkMsg.EMAIL_DUPLICATE_FILE, "Correo electrónico duplicado en el archivo");
        messages.put(BulkMsg.EMAIL_DUPLICATE_DB, "Este correo electrónico ya está registrado en el sistema");
        messages.put(BulkMsg.DOC_TYPE_TI_NOT_ALLOWED, "El tipo de documento TI (Tarjeta de Identidad) no está permitido para trabajadores independientes");
        // Optional keys; kept here if later used
        messages.put(BulkMsg.EMPLOYER_MISMATCH, "El documento del empleador no coincide con el seleccionado (esperado: {0} {1})");
        messages.put(BulkMsg.ADDRESS_INVALID_CHARS, "Los caracteres ingresados en el campo Dirección son incorrectos. Puedes utilizar los símbolos # y - como separadores");
        messages.put(BulkMsg.CONTRACT_LT_MONTH, "El contrato debe ser mayor a un mes, por favor valida e intenta nuevamente");
        messages.put(BulkMsg.COVERAGE_BEFORE_CONTRACT, "La fecha inicio cobertura no puede ser menor a la fecha inicio contrato");
        messages.put(BulkMsg.MUNICIPALITY_NOT_IN_DEPARTMENT, "El municipio no pertenece al departamento seleccionado");
        messages.put(BulkMsg.INVALID_LENGTH_MIN, "Debe tener mínimo {0} caracteres (tiene {1})");
        messages.put(BulkMsg.INVALID_CHARACTERS_NO_SPACES, "Solo se permiten letras y caracteres con tilde (sin espacios ni números)");
        messages.put(BulkMsg.COVERAGE_BEFORE_TOMORROW, "La fecha inicio cobertura debe ser a partir de mañana (no puede ser hoy ni días anteriores)");
        messages.put(BulkMsg.DATE_FORMAT_STRICT, "El formato de fecha es incorrecto. Debe ingresar la fecha exactamente en formato yyyy/MM/dd (año con 4 dígitos, mes con 2 dígitos, día con 2 dígitos). Ejemplo válido: 2024/12/25. No se aceptan otros formatos como dd/MM/yyyy, yyyy-MM-dd, o fechas abreviadas");
        messages.put(BulkMsg.BIRTH_DATE_FORMAT_INVALID, "El formato de la fecha de nacimiento es incorrecto. Debe ingresar la fecha en uno de los siguientes formatos: yyyy/MM/dd, dd/MM/yyyy, yyyy-MM-dd, dd-MM-yyyy. Ejemplos válidos: 1990/12/25, 25/12/1990, 1990-12-25. Verifique que el día, mes y año sean válidos");
        messages.put(BulkMsg.BIRTH_DATE_TOO_OLD, "La fecha de nacimiento es demasiado antigua. No se permiten fechas anteriores al 2 de enero de 1900. Verifique que haya ingresado correctamente el año de nacimiento");
        messages.put(BulkMsg.BIRTH_DATE_CC_UNDERAGE, "Para el tipo de documento CC (Cédula de Ciudadanía), la persona debe ser mayor de edad (18 años o más). La edad actual es {0} años. Verifique la fecha de nacimiento o use un tipo de documento apropiado para menores de edad");
        messages.put(BulkMsg.BIRTH_DATE_UNDERAGE, "La edad mínima permitida es {1} años. La edad actual es {0} años. Verifique la fecha de nacimiento ingresada");
        messages.put(BulkMsg.COVERAGE_DATE_FORMAT_INVALID, "El formato de la fecha de inicio de cobertura es incorrecto. Debe ingresar la fecha en uno de los siguientes formatos: yyyy/MM/dd, dd/MM/yyyy, yyyy-MM-dd, dd-MM-yyyy. Ejemplos válidos: 2024/12/25, 25/12/2024, 2024-12-25. Verifique que el día, mes y año sean válidos");
        messages.put(BulkMsg.CONTRACT_END_DATE_TOO_FAR, "La fecha de terminación del contrato no puede ser superior a 4 años desde la fecha actual. La fecha máxima permitida es {0}. La fecha ingresada ({1}) excede este límite. Por favor, ajuste la fecha de terminación del contrato para que esté dentro del rango permitido");
        messages.put(BulkMsg.CONTRACT_NATURE_INVALID, "El valor ingresado para NATURALEZA DEL CONTRATO no es válido. Solo se permiten los valores '1' o '2'. El valor ingresado ('{0}') no es aceptado. Por favor, corrija el valor ingresando únicamente '1' o '2' según corresponda al tipo de naturaleza del contrato");
        messages.put(BulkMsg.TRANSPORT_SUPPLY_INVALID, "El valor ingresado para SUMINISTRA TRANSPORTE no es válido. Solo se permiten los valores 'S' (Sí) o 'N' (No). El valor ingresado ('{0}') no es aceptado. Por favor, corrija el valor ingresando únicamente 'S' si suministra transporte o 'N' si no suministra transporte");
        messages.put(BulkMsg.CONTRACT_TYPE_INVALID, "El valor ingresado para TIPO DE CONTRATO no es válido. Solo se permiten los valores '1', '2' o '3'. El valor ingresado ('{0}') no es aceptado. Por favor, corrija el valor ingresando únicamente '1', '2' o '3' según corresponda al tipo de contrato");
        messages.put(BulkMsg.PHONE_FORMAT_INVALID, "El formato del número de teléfono es incorrecto. Debe contener exactamente 10 dígitos sin espacios, guiones, paréntesis ni código de país. El número ingresado ('{0}') tiene {1} caracteres. Ejemplo válido: 3001234567");
        messages.put(BulkMsg.PHONE_PREFIX_INVALID, "El prefijo del número de teléfono ('{0}') no es válido. Los primeros 3 dígitos deben corresponder a un operador móvil válido en Colombia. Prefijos válidos incluyen: 300-305, 310-324, 350-351, 601-608. Por favor, verifique el número de teléfono");
    }

    public String get(BulkMsg key, Object... args) {
        String template = messages.get(key);
        if (template == null || template.isBlank()) {
            return null; // Do not return unreadable keys
        }
        return (args == null || args.length == 0) ? template : MessageFormat.format(template, args);
    }
}

