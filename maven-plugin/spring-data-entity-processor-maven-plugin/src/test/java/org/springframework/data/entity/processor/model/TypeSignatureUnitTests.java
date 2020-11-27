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
package org.springframework.data.entity.processor.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.Types.ArrayTypes;
import org.springframework.data.Types.DomainType;
import org.springframework.data.Types.DomainTypeWithGenericSignature;
import org.springframework.data.Types.ListTypes;
import org.springframework.data.Types.MapTypes;
import org.springframework.data.Types.SingleTypes;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class TypeSignatureUnitTests {

	// Single Types

	@Test
	void computeObjectTypeSignature() {

		TypeSignature signature = TypeSignature.from(SingleTypes.class, "objectValue");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.lang.Object");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.SimpleConfiguredTypes.object()");
	}

	@Test
	void computeSimpleTypeSignature() {

		TypeSignature signature = TypeSignature.from(SingleTypes.class, "stringValue");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.lang.String");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class)");
	}

	@Test
	void computePrimitiveTypeSignature() {

		TypeSignature signature = TypeSignature.from(SingleTypes.class, "intValue");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.lang.Integer");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.Integer.class)");
	}

	@Test
	void computeDomainTypeSignature() {

		TypeSignature signature = TypeSignature.from(SingleTypes.class, "domainType");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("org.springframework.data.Types.DomainType");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString(MockDomainTypes.of(DomainType.class))).isEqualTo("org.springframework.data.Types.DomainTypeConfigurableTypeInformation.instance()");
	}

	// Arrays

	@Test
	void computeObjectTypeArrayTypeSignature() {

		TypeSignature signature = TypeSignature.from(ArrayTypes.class, "arrayOfObject");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.lang.Object[]");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.ArrayTypeInformation.arrayOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.object())");
	}

	@Test
	void computeSimpleTypeArrayTypeSignature() {

		TypeSignature signature = TypeSignature.from(ArrayTypes.class, "arrayOfString");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.lang.String[]");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.ArrayTypeInformation.arrayOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class))");
	}

	@Test
	void computeDomainTypeArrayTypeSignature() {

		TypeSignature signature = TypeSignature.from(ArrayTypes.class, "arrayOfDomainType");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("org.springframework.data.Types.DomainType[]");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString(MockDomainTypes.of(DomainType.class))).isEqualTo("org.springframework.data.mapping.model.ArrayTypeInformation.arrayOf(org.springframework.data.Types.DomainTypeConfigurableTypeInformation.instance())");
	}

	// Lists

	@Test
	void computeRawListTypeSignature() {

		TypeSignature signature = TypeSignature.from(ListTypes.class, "rawList");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.List<?>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.list()");
	}

	@Test
	void computeWildcardListTypeSignature() {

		TypeSignature signature = TypeSignature.from(ListTypes.class, "listOfWildcard");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.List<?>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.list()");
	}

	@Test
	void computeObjectTypeListTypeSignature() {

		TypeSignature signature = TypeSignature.from(ListTypes.class, "listOfObject");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.List<java.lang.Object>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.object())");
	}

	@Test
	void computeSimpleTypeListTypeSignature() {

		TypeSignature signature = TypeSignature.from(ListTypes.class, "listOfString");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.List<java.lang.String>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class))");
	}

	@Test
	void computeDomainTypeListTypeSignature() {

		TypeSignature signature = TypeSignature.from(ListTypes.class, "listOfDomainType");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.List<org.springframework.data.Types.DomainType>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString(MockDomainTypes.of(DomainType.class))).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.Types.DomainTypeConfigurableTypeInformation.instance())");
	}

	@Test
	void computeNestedWildcardTypeListTypeSignature() {

		TypeSignature signature = TypeSignature.from(ListTypes.class, "listOfListOfWildcard");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.List<java.util.List<?>>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.ListTypeInformation.list())");
	}

	@Test
	void computeNestedSimpleTypeListTypeSignature() {

		TypeSignature signature = TypeSignature.from(ListTypes.class, "listOfListOfString");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.List<java.util.List<java.lang.String>>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class)))");
	}

	@Test
	void computeNestedDomainTypeListTypeSignature() {

		TypeSignature signature = TypeSignature.from(ListTypes.class, "listOfListOfDomainType");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.List<java.util.List<org.springframework.data.Types.DomainType>>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString(MockDomainTypes.of(DomainType.class))).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.Types.DomainTypeConfigurableTypeInformation.instance()))");
	}

	// Maps

	@Test
	void computeRawMapTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "rawMap");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<?,?>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.map()");
	}

	@Test
	void computeWildcardMapTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "mapOfWildcard");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<?,?>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.map()");
	}

	@Test
	void computeObjectTypeMapTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "mapOfObject");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<java.lang.Object,java.lang.Object>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.object(),org.springframework.data.mapping.model.SimpleConfiguredTypes.object())");
	}

	@Test
	void computeSimpleTypeMapTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "mapOfString");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<java.lang.String,java.lang.String>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class))");
	}

	@Test
	void computeDomainTypeMapKeyTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "mapOfDomainTypeKey");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<org.springframework.data.Types.DomainType,java.lang.String>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString(MockDomainTypes.of(DomainType.class))).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.Types.DomainTypeConfigurableTypeInformation.instance(),org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class))");
	}

	@Test
	void computeDomainTypeMapValueTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "mapOfDomainTypeValue");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<java.lang.String,org.springframework.data.Types.DomainType>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString(MockDomainTypes.of(DomainType.class))).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.Types.DomainTypeConfigurableTypeInformation.instance())");
	}

	@Test
	void computeNestedSimpleTypeMapTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "mapOfListOfStringValueType");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<java.lang.String,java.util.List<java.lang.String>>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class)))");
	}

	@Test
	void computeNestedDomainTypeMapTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "mapOfListOfDomainValueType");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<java.lang.String,java.util.List<org.springframework.data.Types.DomainType>>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString(MockDomainTypes.of(DomainType.class))).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.Types.DomainTypeConfigurableTypeInformation.instance()))");
	}

	@Test
	void computeNestedMapTypeMapTypeSignature() {

		TypeSignature signature = TypeSignature.from(MapTypes.class, "mapOfMapValueType");

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("java.util.Map<java.lang.String,java.util.Map<java.lang.String,java.lang.String>>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class)))");
	}

	// Custom Generic Types

	@Test
	void computeSignatureForDomainType() {

		TypeSignature signature = TypeSignature.fromClass(DomainTypeWithGenericSignature.class);

		Assertions.assertThat(signature.getJavaSignatureString()).isEqualTo("org.springframework.data.Types.DomainTypeWithGenericSignature<?,?,?>");
		Assertions.assertThat(signature.getConfigurableTypeSignatureString()).isEqualTo("org.springframework.data.Types.DomainTypeWithGenericSignatureConfigurableTypeInformation.instance()");
	}
}
