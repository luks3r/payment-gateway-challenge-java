package com.checkout.payment.gateway.api.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(FIELD)
@Retention(RUNTIME)
@Constraint(validatedBy = AllowedCurrencyValidator.class)
public @interface AllowedCurrency {
  String message() default "Unsupported currency";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};

  String[] value() default {};
}
