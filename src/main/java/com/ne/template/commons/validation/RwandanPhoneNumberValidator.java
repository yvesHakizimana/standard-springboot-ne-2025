package com.ne.template.commons.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class RwandanPhoneNumberValidator implements ConstraintValidator<ValidRwandanPhoneNumber, String> {

    private static final String RWANDAN_PHONE_NUMBER_REGEX = "^(072|073|078|079)\\d{7}$";
    private static final Pattern PATTERN = Pattern.compile(RWANDAN_PHONE_NUMBER_REGEX);

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {

        if (value == null || value.isEmpty())
            return false;

        String cleanedNumber = value.replace("[\\s-]", "");

        return PATTERN.matcher(cleanedNumber).matches();
    }
}