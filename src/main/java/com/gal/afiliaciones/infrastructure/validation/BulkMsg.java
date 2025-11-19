package com.gal.afiliaciones.infrastructure.validation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BulkMsg {

    FIELD_REQUIRED("bulk.field.required"),
    INVALID_FORMAT("bulk.format.invalid"),
    INVALID_LENGTH_EXACT("bulk.length.exact"),
    INVALID_CHARACTERS("bulk.characters.invalid"),
    DATE_FORMAT_INVALID("bulk.date.format.invalid"),
    DATE_TOO_OLD("bulk.date.too.old"),
    AGE_BELOW_MIN_CC("bulk.age.cc.min"),
    PHONE_LENGTH("bulk.phone.length"),
    PHONE_AREA_CODE_INVALID("bulk.phone.area.invalid"),
    COVERAGE_BEFORE_TODAY("bulk.coverage.before.today"),
    COVERAGE_AFTER_WINDOW("bulk.coverage.after.window"),
    SALARY_NON_NUMERIC("bulk.salary.nan"),
    SALARY_BELOW_MIN("bulk.salary.min"),
    SALARY_ABOVE_MAX("bulk.salary.max"),
    IBC_OUT_OF_RANGE("bulk.ibc.out.of.range"),
    IBC_CANNOT_COMPUTE("bulk.ibc.cannot.compute"),
    ECON_CODE_NOT_FOUND("bulk.economic.code.notfound"),
    ECON_CODE_NOT_ASSOCIATED("bulk.economic.code.notassociated"),
    DOC_DUPLICATE_FILE("bulk.doc.duplicate.file"),
    DOC_DUPLICATE_DB("bulk.doc.duplicate.db"),
    EMAIL_DUPLICATE_FILE("bulk.email.duplicate.file"),
    EMAIL_DUPLICATE_DB("bulk.email.duplicate.db"),
    DOC_TYPE_TI_NOT_ALLOWED("bulk.doc.type.ti.not.allowed"),
    EMPLOYER_MISMATCH("bulk.employer.mismatch"),
    ADDRESS_INVALID_CHARS("bulk.address.invalid.chars"),
    CONTRACT_LT_MONTH("bulk.contract.lt.month"),
    COVERAGE_BEFORE_CONTRACT("bulk.coverage.before.contract"),
    MUNICIPALITY_NOT_IN_DEPARTMENT("bulk.municipality.not.in.department"),
    INVALID_LENGTH_MIN("bulk.length.min"),
    INVALID_CHARACTERS_NO_SPACES("bulk.characters.no.spaces"),
    COVERAGE_BEFORE_TOMORROW("bulk.coverage.before.tomorrow"),
    DATE_FORMAT_STRICT("bulk.date.format.strict"),
    BIRTH_DATE_FORMAT_INVALID("bulk.birth.date.format.invalid"),
    BIRTH_DATE_TOO_OLD("bulk.birth.date.too.old"),
    BIRTH_DATE_CC_UNDERAGE("bulk.birth.date.cc.underage"),
    BIRTH_DATE_UNDERAGE("bulk.birth.date.underage"),
    COVERAGE_DATE_FORMAT_INVALID("bulk.coverage.date.format.invalid"),
    CONTRACT_END_DATE_TOO_FAR("bulk.contract.end.date.too.far"),
    CONTRACT_NATURE_INVALID("bulk.contract.nature.invalid"),
    TRANSPORT_SUPPLY_INVALID("bulk.transport.supply.invalid"),
    CONTRACT_TYPE_INVALID("bulk.contract.type.invalid"),
    PHONE_FORMAT_INVALID("bulk.phone.format.invalid"),
    PHONE_PREFIX_INVALID("bulk.phone.prefix.invalid");

    private final String key;
}


