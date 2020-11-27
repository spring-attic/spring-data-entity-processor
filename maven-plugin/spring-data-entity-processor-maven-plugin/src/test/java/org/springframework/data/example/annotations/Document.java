package org.springframework.data.example.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.core.annotation.AliasFor;
import org.springframework.data.annotation.Persistent;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
@Persistent
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Document {

	@AliasFor("collection")
	String value() default "";

	@AliasFor("value")
	String collection() default "";
}
