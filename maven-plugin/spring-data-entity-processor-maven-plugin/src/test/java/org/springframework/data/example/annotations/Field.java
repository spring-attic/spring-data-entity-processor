package org.springframework.data.example.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Field {

	@AliasFor("name")
	String value() default "";

	@AliasFor("value")
	String name() default "";

	FieldTypeEnum fieldType() default FieldTypeEnum.OBJECT;
}
