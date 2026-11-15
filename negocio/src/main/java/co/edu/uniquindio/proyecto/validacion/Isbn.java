package co.edu.uniquindio.proyecto.validacion;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * El valor debe ser un ISBN-10 o ISBN-13 con dígito de control correcto. Se admiten guiones y espacios.
 * Un valor nulo o vacío se considera válido (para exigirlo se combina con {@code @NotBlank}).
 */
@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Isbn {

    String message() default "El ISBN no es válido: debe tener 10 o 13 dígitos y un dígito de control correcto";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
