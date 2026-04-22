package com.corty.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
import java.time.Period;

public class MinAgeValidator implements ConstraintValidator<MinAge, LocalDate> {

    private int minAge;

    @Override
    public void initialize(MinAge annotation) {
        this.minAge = annotation.value();
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        final boolean valid;
        if (birthDate == null) {
            valid = true;
        } else {
            valid = Period.between(birthDate, LocalDate.now()).getYears() >= minAge;
        }
        return valid;
    }
}