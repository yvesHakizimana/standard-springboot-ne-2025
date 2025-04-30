package com.ne.template.commons.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.FIELD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = EnumValidator.class)
public @interface ValidEnum {
    String message() default "Invalid value. Valid values are : {allowedValues}";

    Class<?>[] groups() default {};

    Class<? extends Enum<?>> enumClass();

    Class<? extends Payload>[] payload() default {};

    boolean ignoreCase() default false;
}