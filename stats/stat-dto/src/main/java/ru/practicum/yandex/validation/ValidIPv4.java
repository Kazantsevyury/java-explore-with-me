package ru.practicum.yandex.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
@Constraint(validatedBy = IPv4Validator.class)
public @interface ValidIPv4 {

    String message() default "Wrong IP format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
