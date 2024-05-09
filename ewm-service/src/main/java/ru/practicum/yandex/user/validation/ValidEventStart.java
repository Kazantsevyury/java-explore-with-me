package ru.practicum.yandex.user.validation;


import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = LocalDateTimeValidator.class)
public @interface ValidEventStart {

    String message() default "Invalid event date.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
