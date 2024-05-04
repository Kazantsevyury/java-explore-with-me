package ru.practicum.yandex.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class IPv4Validator implements ConstraintValidator<ValidIPv4, String> {

    @Override
    public void initialize(ValidIPv4 constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        String ipV4Regex = "^(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})$";
        return s.matches(ipV4Regex);
    }
}
