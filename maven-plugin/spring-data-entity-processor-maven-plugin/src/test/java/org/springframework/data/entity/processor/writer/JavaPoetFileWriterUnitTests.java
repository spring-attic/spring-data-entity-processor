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
package org.springframework.data.entity.processor.writer;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.Types;
import org.springframework.data.Types.AccessorMethodsType;
import org.springframework.data.Types.AnnotationType;
import org.springframework.data.entity.processor.model.AnnotationInfo;
import org.springframework.data.entity.processor.model.ConstructorInfo;
import org.springframework.data.entity.processor.model.DomainTypes;
import org.springframework.data.entity.processor.model.PropertyInfo;
import org.springframework.data.entity.processor.model.TypeInfo;
import org.springframework.data.example.repo.Person;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class JavaPoetFileWriterUnitTests {

	JavaPoetFileWriter writer;

	@BeforeEach
	void beforeEach() {
		writer = new JavaPoetFileWriter();
	}

	@Test
	void newInstanceBlockForNoArgsCtor() {

		CodeBlock codeBlock = writer.newInstanceBlock(new ConstructorInfo(Types.NoArgsCtor.class));

		assertThat(codeBlock).isEqualTo(CodeBlock.of("org.springframework.data.mapping.model.ConfigurableTypeConstructor.<org.springframework.data.Types.NoArgsCtor>noArgsConstructor(org.springframework.data.Types.NoArgsCtor::new)"));
	}

	@Test
	void newInstanceBlockForCtorWithArgs() {

		CodeBlock codeBlock = writer.newInstanceBlock(new ConstructorInfo(Types.SingleCtor.class));

		System.out.println(codeBlock.toString());
		assertThat(codeBlock).isEqualTo(CodeBlock.of("org.springframework.data.mapping.model.ConfigurableTypeConstructor.<org.springframework.data.Types.SingleCtor>builder().args(\"stringValue\", \"longValue\").newInstanceFunction((args) -> new org.springframework.data.Types.SingleCtor((java.lang.String)args[0], (java.lang.Long)args[1]))"));
	}

	// TODO: do we really need to have this or are the fields form the target class sufficient
	@Test
	void superCallForRootType() {

		CodeBlock codeBlock = writer.superCallBlock(new TypeInfo(Types.DomainType.class));

		assertThat(codeBlock).isEqualTo(CodeBlock.of("super(org.springframework.data.Types.DomainType.class)"));
	}

	@Test
	void superCallForInheritedType() {

		CodeBlock codeBlock = writer.superCallBlock(new TypeInfo(Types.AbstractTypeImplementation.class));

		assertThat(codeBlock).isEqualTo(CodeBlock.of("super(org.springframework.data.Types.AbstractTypeImplementation.class, org.springframework.data.Types.AbstractTypeConfigurableTypeInformation.instance())"));
	}

	@Test
	void fieldDeclaration() {

		TypeInfo typeInfo = new TypeInfo(Types.SingleTypes.class);
		PropertyInfo propertyInfo = new PropertyInfo(typeInfo, "stringValue", String.class);

		CodeBlock codeBlock = writer.fieldVariableBlock(propertyInfo, new DomainTypes(Collections.singleton(typeInfo)));

		assertThat(codeBlock).isEqualTo(CodeBlock.of("org.springframework.data.mapping.model.Field<org.springframework.data.Types.SingleTypes,java.lang.String> stringValue = org.springframework.data.mapping.model.Field.type(\"stringValue\", org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class))"));
	}

	// ACCESSORS

	@Test
	void fieldAccessorSetter() {

		TypeInfo typeInfo = new TypeInfo(Types.AccessorMethodsType.class);
		PropertyInfo propertyInfo = new PropertyInfo(typeInfo, "justSetter", String.class);
		propertyInfo.setter(ReflectionUtils.findMethod(Types.AccessorMethodsType.class, "setJustSetter", String.class));

		CodeBlock codeBlock = writer.setterMethodBlock(propertyInfo);

		assertThat(codeBlock).isEqualTo(CodeBlock.of("justSetter.setter(org.springframework.data.Types.AccessorMethodsType::setJustSetter)"));
	}

	@Test
	void fieldAccessorGetter() {

		TypeInfo typeInfo = new TypeInfo(Types.AccessorMethodsType.class);
		PropertyInfo propertyInfo = new PropertyInfo(typeInfo, "justGetter", String.class);
		propertyInfo.getter(ReflectionUtils.findMethod(Types.AccessorMethodsType.class, "getJustGetter"));

		CodeBlock codeBlock = writer.getterMethodBlock(propertyInfo);

		assertThat(codeBlock).isEqualTo(CodeBlock.of("justGetter.getter(org.springframework.data.Types.AccessorMethodsType::getJustGetter)"));
	}

	@Test
	void fieldAccessorWither() {

		TypeInfo typeInfo = new TypeInfo(Types.AccessorMethodsType.class);
		PropertyInfo propertyInfo = new PropertyInfo(typeInfo, "getterAndWither", String.class);
		propertyInfo.wither(ReflectionUtils.findMethod(AccessorMethodsType.class, "withGetterAndWither", String.class));

		CodeBlock codeBlock = writer.witherMethodBlock(propertyInfo);

		assertThat(codeBlock).isEqualTo(CodeBlock.of("getterAndWither.wither(org.springframework.data.Types.AccessorMethodsType::withGetterAndWither)"));
	}

	// ANNOTATIONS

	@Test
	void annotationOnField() {

		TypeInfo typeInfo = new TypeInfo(Types.FieldAnnotation.class);
		PropertyInfo propertyInfo = new PropertyInfo(typeInfo, "singleAnnotationWith", String.class);
		AnnotationInfo annotationInfo = new AnnotationInfo(ReflectionUtils.findField(Types.FieldAnnotation.class, "singleAnnotationWith"), AnnotationType.class);
		propertyInfo.annotations(Collections.singleton(annotationInfo));

		CodeBlock codeBlock = writer.fieldAnnotationBlock(propertyInfo, annotationInfo);

		assertThat(codeBlock).isEqualTo(CodeBlock.of("singleAnnotationWith.annotation(" +
				"new org.springframework.data.Types.AnnotationType() {" +
				"public Class<? extends java.lang.annotation.Annotation> annotationType() {" +
				" return org.springframework.data.Types.AnnotationType.class; " +
				"}" +
				"public java.lang.String att() {" +
				" return \"custom-value\"; " +
				"}" +
				"})"));
	}

	@Test
	@Disabled
	void writeToConsole() {

		DataModelGenerator modelGenerator = new DataModelGenerator(Collections.singleton(Person.class));
		DomainTypes domainTypes = new DomainTypes(modelGenerator.process());

		JavaFile file = writer.computeFile(domainTypes.iterator().next(), domainTypes);
		System.out.println(file.toString());
	}
}
