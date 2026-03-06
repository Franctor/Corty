package com.corty.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MinAgeValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinAge {
    int value() default 18;
    String message() default "Debes ser mayor de edad para registrarte";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
