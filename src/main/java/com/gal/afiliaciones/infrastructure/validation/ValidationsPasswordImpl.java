package com.gal.afiliaciones.infrastructure.validation;
import java.util.regex.Pattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class ValidationsPasswordImpl implements ConstraintValidator<ValidationPassword, String> {


    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if(password == null){
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (password.length() < 8) {
            context.buildConstraintViolationWithTemplate("").addConstraintViolation();
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            context.buildConstraintViolationWithTemplate("").addConstraintViolation();
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            context.buildConstraintViolationWithTemplate("").addConstraintViolation();
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            context.buildConstraintViolationWithTemplate("").addConstraintViolation();
            return false;
        }

        if(!Pattern.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*",password)){
            context.buildConstraintViolationWithTemplate("").addConstraintViolation();
            return false;
        }

        return true;
    }

}
