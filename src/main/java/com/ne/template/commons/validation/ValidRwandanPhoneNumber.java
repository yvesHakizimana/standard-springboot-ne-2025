package com.ne.template.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = RwandanPhoneNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidRwandanPhoneNumber {
    String message() default "Invalid Rwandan phone number, must start with 072, 073, 078 or 079";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
