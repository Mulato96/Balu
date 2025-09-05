package com.gal.afiliaciones.validate;


import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NumberLengthValidator implements ConstraintValidator<NumberLength, Long> {
    private int max;

    @Override
    public void initialize(NumberLength constraintAnnotation) {
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Long value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return String.valueOf(value).length() <= max;
    }
}
