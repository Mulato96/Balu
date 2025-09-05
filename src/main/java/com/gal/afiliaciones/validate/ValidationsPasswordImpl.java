package com.gal.afiliaciones.validate;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import com.gal.afiliaciones.infrastructure.utils.Constant;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class ValidationsPasswordImpl implements ConstraintValidator<ValidationPassword, String> {


    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {

        if(password == null){
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (validateSequences(password)) {
            context.buildConstraintViolationWithTemplate(Constant.PASSWORD_NOT_CONTAIN_SEQUENCES).addConstraintViolation();
            return false;
        }

        if (password.length() < 8) {
            context.buildConstraintViolationWithTemplate(Constant.PASSWORD_SIZE).addConstraintViolation();
            return false;
        }

        if (!password.matches(".*[a-z].*")) {
            context.buildConstraintViolationWithTemplate(Constant.PASSWORD_NOT_CONTAIN_LETTERS_LOWERCASE).addConstraintViolation();
            return false;
        }

        if (!password.matches(".*[A-Z].*")) {
            context.buildConstraintViolationWithTemplate(Constant.PASSWORD_NOT_CONTAIN_LETTERS_CAPITAL).addConstraintViolation();
            return false;
        }

        if (!password.matches(".*\\d.*")) {
            context.buildConstraintViolationWithTemplate(Constant.PASSWORD_NOT_CONTAIN_NUMBERS).addConstraintViolation();
            return false;
        }

        if(!Pattern.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*",password)){
            context.buildConstraintViolationWithTemplate(Constant.PASSWORD_NOT_CONTAIN_CHARACTERISTICS_SPECIAL).addConstraintViolation();
            return false;
        }

        return true;
    }

    private boolean validateSequences(String password){
        return IntStream.range(0, password.length()-3)
                .anyMatch(i -> password.charAt(i + 1) == password.charAt(i) + 1 && password.charAt(i + 2) == password.charAt(i + 1) + 1 && password.charAt(i + 3) == password.charAt(i + 2) + 1);
    }

}
