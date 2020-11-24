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

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.data.DataModelFileWriter.JavaFileBuilder;
import org.springframework.data.DataModelGeneratorUnitTests.InterfaceType;
import org.springframework.data.mapping.model.ConfigurableTypeInformation;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class DataModelFileWriterUnitTest {

	@Test
	void computeSimpleTypeInfo() {

		String init = typeInitString(JustSimpleTypes.class, "stringValue", String.class);
		assertThat(init).isEqualTo("org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class)");
	}

	@Test
	void computePrimitiveTypeInfo() {

		String init = typeInitString(JustSimpleTypes.class, "intValue", int.class);
		assertThat(init).isEqualTo("org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.Integer.class)");
	}

	@Test
	void computeRawListTypeInfo() {

		String init = typeInitString(ListTypes.class, "rawList", List.class);
		assertThat(init).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.list()");
	}

	@Test
	void computeSimpleTypeListTypeInfo() {

		String init = typeInitString(ListTypes.class, "listOfString", List.class);
		assertThat(init).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class))");
	}

	@Test
	void computeSimpleTypeMapTypeInfo() {

		String init = typeInitString(MapTypes.class, "mapOfString", List.class);
		assertThat(init).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class))");
	}

	@Test
	void computeNestedSimpleTypeMapTypeInfo() {

		String init = typeInitString(MapTypes.class, "mapOfListOfStringValueType", List.class);
		assertThat(init).isEqualTo("org.springframework.data.mapping.model.MapTypeInformation.mapOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class)))");
	}

	@Test
	void computeNestedSimpleTypeListTypeInfo() {

		String init = typeInitString(ListTypes.class, "listOfListOfString", List.class);
		assertThat(init).isEqualTo("org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class)))");
	}

	@Test
	void simpleFieldString() {

		TypeInfo owner = new TypeInfo(JustSimpleTypes.class);
		PropertyInfo propertyInfo = new PropertyInfo(owner, "stringValue", String.class);


		JavaFileBuilder fileBuilder = new JavaFileBuilder(owner);
		List<String> statement = new DataModelFileWriter(Collections.singleton(owner)).computeFieldStatement(propertyInfo, fileBuilder);
		statement.forEach(System.out::println);

//		assertThat(statement).isEqualTo("$T.<org.springframework.data.DataModelFileWriterUnitTest.JustSimpleTypes,java.lang.String>type(\"stringValue\",org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class))");
	}

	private String typeInitString(Class<?> owner, String property, Class<?> propertyType) {

		TypeInfo ownerInfo = new TypeInfo(owner);
		return new DataModelFileWriter(Collections.singleton(ownerInfo)).computeTypeInfoInit(new PropertyInfo(ownerInfo, property, propertyType));
	}

	static class JustSimpleTypes {

		String stringValue;
		int intValue;
		Long longValue;

		Date dateValue;
		DataModelGeneratorUnitTests.EnumType enumValue;
	}

	static class JustSimpleTypesInfo extends ConfigurableTypeInformation<JustSimpleTypes> {

		JustSimpleTypesInfo() {
			super(JustSimpleTypes.class);

			org.springframework.data.mapping.model.Field<org.springframework.data.DataModelFileWriterUnitTest.JustSimpleTypes,java.lang.String> stringValue = org.springframework.data.mapping.model.Field.type("stringValue",org.springframework.data.mapping.model.SimpleConfiguredTypes.get(java.lang.String.class));
			addField(stringValue);

		}
	}

	enum EnumType {
		E1, E2
	}

	static class ListTypes {

		List rawList;
		List<Object> listOfObject;
		List<String> listOfString;
		List<JustSimpleTypes> listOfComplexType;
		List<InterfaceType> listOfInterface;
		List<List<String>> listOfListOfString;
		List<List<DataModelGeneratorUnitTests.JustSimpleTypes>> listOfListOfComplexType;

		public List<Object> getListOfObject() {
			return listOfObject;
		}

		public void setListOfObject(List<Object> listOfObject) {
			this.listOfObject = listOfObject;
		}

		public List<String> getListOfString() {
			return listOfString;
		}

		public void setListOfString(List<String> listOfString) {
			this.listOfString = listOfString;
		}

		public List<JustSimpleTypes> getListOfComplexType() {
			return listOfComplexType;
		}

		public void setListOfComplexType(List<JustSimpleTypes> listOfComplexType) {
			this.listOfComplexType = listOfComplexType;
		}

		public List<InterfaceType> getListOfInterface() {
			return listOfInterface;
		}

		public void setListOfInterface(List<InterfaceType> listOfInterface) {
			this.listOfInterface = listOfInterface;
		}

		public List<List<String>> getListOfListOfString() {
			return listOfListOfString;
		}

		public void setListOfListOfString(List<List<String>> listOfListOfString) {
			this.listOfListOfString = listOfListOfString;
		}

		public List<List<DataModelGeneratorUnitTests.JustSimpleTypes>> getListOfListOfComplexType() {
			return listOfListOfComplexType;
		}

		public void setListOfListOfComplexType(List<List<DataModelGeneratorUnitTests.JustSimpleTypes>> listOfListOfComplexType) {
			this.listOfListOfComplexType = listOfListOfComplexType;
		}
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
}
