/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.example.repo.Person;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class DataModelGeneratorUnitTests {

	@Test
	void xxx() {

		Set<TypeModel> models = new DataModelGenerator(Collections.singleton(Person.class)).process();
		models.forEach(System.out::println);
	}

	static class JustSimpleTypes {

		String stringValue;
		Integer intValue;
		Long longValue;

		Date dateValue;
		EnumType enumValue;
	}

	enum EnumType {
		E1, E2
	}

	static class ListTypes {

		List<Object> listOfObject;
		List<String> listOfString;
		List<JustSimpleTypes> listOfComplexType;
		List<InterfaceType> listOfInterface;
		List<List<String>> listOfListOfString;
		List<List<JustSimpleTypes>> listOfListOfComplexType;
	}

	static class MapTypes {

		Map<Object, Object> mapOfObject;
		Map<String, String> mapOfString;
		Map<String, JustSimpleTypes> mapOfComplexValueType;
		Map<JustSimpleTypes, JustSimpleTypes> mapOfComplexKeyValueType;
		Map<String, InterfaceType> mapOfInterfaceValueType;
		Map<String, List<String>> mapOfListOfStringValueType;
		Map<String, List<JustSimpleTypes>> mapOfListOfComplexValueType;
	}

	static class CyclicType {

		CyclicType parent;
		List<CyclicType> listOfCyclicType;
		Map<String, CyclicType> mapOfCyclicType;
	}

	static abstract class AbstractType {

		String stringValueInAbstractType;
	}

	static class AbstractTypeImplementation extends AbstractType {

		Long longValueInAbstractTypeImplementation;
	}

	static class LimitedAccess {

		String noSetter;
		String noGetter;
		String withWither;
	}

	static class NoArgsCtor {

	}

	static class SingleCtor {

		SingleCtor(String stringValue, Long longValue) {

		}
	}

	static class MultipleCtors {

		MultipleCtors(String singleArg) {

		}

		MultipleCtors(String multiArg1, Long multiArg2) {

		}
	}

	static class PreferredCtors {

		@PersistenceConstructor
		PreferredCtors(String singleArg) {

		}

		PreferredCtors(String multiArg1, Long multiArg2) {

		}
	}

	static class FieldAnnotation {

		@AnnotationType
		String singleAnnotation;

		@AnnotationType
		@AnnotationType
		String repeatedAnnotation;

		@AnnotationContainer(value = @AnnotationType)
		String containerAnnotation;
	}


	interface InterfaceType {

	}


	@Target({ElementType.TYPE, ElementType.FIELD})
	@Repeatable(AnnotationContainer.class)
	static @interface AnnotationType {

		String att() default "default-value";
	}

	@Target({ElementType.TYPE, ElementType.FIELD})
	static @interface AnnotationContainer {
		AnnotationType[] value() default {};
	}
}
