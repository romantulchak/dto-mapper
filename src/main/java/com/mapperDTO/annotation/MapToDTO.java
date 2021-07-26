package com.mapperDTO.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface MapToDTO {
   Class<?>[] mapClass();
   String assistedField() default "";
   //TODO: добавити відповідне поле в Entity
}
