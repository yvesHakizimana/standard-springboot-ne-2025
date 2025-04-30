package com.ne.template.commons.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;
import java.util.List;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {
    private List<String> acceptedValues;
    private String allowedValues;
    private  boolean ignoreCase;

    @Override
    public void initialize(ValidEnum constraintAnnotation){
        Class<? extends Enum<?>> enumClass = constraintAnnotation.enumClass();
        this.ignoreCase = constraintAnnotation.ignoreCase();

        acceptedValues = Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toList();

        allowedValues = String.join(", ", acceptedValues);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty())
            return false;

        boolean isValid = ignoreCase ? acceptedValues.contains(value.toLowerCase()) : acceptedValues.contains(value);

        if(!isValid){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    context.getDefaultConstraintMessageTemplate().replace("{allowedValues}", allowedValues)
            ).addConstraintViolation();

        }
        return isValid;
    }
}