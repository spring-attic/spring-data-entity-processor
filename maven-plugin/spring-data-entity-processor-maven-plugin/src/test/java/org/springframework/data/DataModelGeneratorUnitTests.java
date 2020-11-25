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

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.entity.processor.model.PropertyInfo;
import org.springframework.data.entity.processor.model.TypeInfo;
import org.springframework.data.entity.processor.writer.DataModelGenerator;
import org.springframework.data.example.repo.Person;
import org.springframework.data.mapping.model.ConfigurableTypeInformation;
import org.springframework.data.mapping.model.Field;
import org.springframework.data.mapping.model.ListTypeInformation;
import org.springframework.data.mapping.model.MapTypeInformation;
import org.springframework.data.mapping.model.SimpleConfiguredTypes;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class DataModelGeneratorUnitTests {


	@Test
	void xxx() {

		Set<TypeInfo> models = new DataModelGenerator(Collections.singleton(Person.class)).process();
		models.forEach(System.out::println);
	}

	@Test
	void detectsSimpleTypes() {

		TypeInfo typeInfo = new DataModelGenerator().computeTypeModel(JustSimpleTypes.class);

//		assertThat(typeModel.getProperty("stringValue")).satisfies(DataModelGeneratorUnitTests::isSimpleType);
//		assertThat(typeModel.getProperty("intValue")).satisfies(DataModelGeneratorUnitTests::isSimpleType);
//		assertThat(typeModel.getProperty("longValue")).satisfies(DataModelGeneratorUnitTests::isSimpleType);
//		assertThat(typeModel.getProperty("dateValue")).satisfies(DataModelGeneratorUnitTests::isSimpleType);
	}

	@Test
	void detectsListType() {

//		PropertyModel propertyModel = computeProperty(ListTypes.class, "listOfObject");
//		assertThat(propertyModel.isListType()).isTrue();
	}

	@Test
	void detectsListValueType() {

//		List<Object> listOfObject;
//		List<String> listOfString;
//		List<JustSimpleTypes> listOfComplexType;
//		List<InterfaceType> listOfInterface;
//		List<List<String>> listOfListOfString;
//		List<List<JustSimpleTypes>> listOfListOfComplexType;

//		assertThat(computeProperty(ListTypes.class, "listOfObject").asListModel().getListValueType()).isEqualTo(Object.class);
//		assertThat(computeProperty(ListTypes.class, "listOfString").asListModel().getListValueType()).isEqualTo(String.class);
//		assertThat(computeProperty(ListTypes.class, "listOfComplexType").asListModel().getListValueType()).isEqualTo(JustSimpleTypes.class);
//		assertThat(computeProperty(ListTypes.class, "listOfInterface").asListModel().getListValueType()).isEqualTo(InterfaceType.class);
//		assertThat(computeProperty(ListTypes.class, "listOfListOfString").asListModel().getListValueType()).isEqualTo(List.class);
//		assertThat(computeProperty(ListTypes.class, "listOfListOfComplexType").asListModel().getListValueType()).isEqualTo(List.class);
	}

//	PropertyModel computeProperty(Class<?> type, String property) {
//
//		Optional<PropertyModel> model = new DataModelGenerator().computeTypeModel(type).getProperty(property);
//		assertThat(model.isPresent());
//		return model.get();
//	}
//
//	private static void isSimpleType(Optional<PropertyInfo> it) {
//
//		it.isPresent();
//		assertThat(it.get().isSimpleType()).isTrue();
//	}

	static class JustSimpleTupesInfo extends ConfigurableTypeInformation<JustSimpleTypes> {

		JustSimpleTupesInfo() {
			super(JustSimpleTypes.class);

			Field.<JustSimpleTypes,java.lang.String> type("stringValue", SimpleConfiguredTypes.get(java.lang.String.class))
					.setter(JustSimpleTypes::setStringValue)
					.getter(JustSimpleTypes::getStringValue);


		}


	}

	static class ListTypesInfo extends ConfigurableTypeInformation<ListTypes> {

		ListTypesInfo() {
			super(ListTypes.class);

			Field.<ListTypes,List<String>> type("stringValue", ListTypeInformation.listOf(SimpleConfiguredTypes.get(String.class)))
					.setter(ListTypes::setListOfString)
					.getter(ListTypes::getListOfString);

			Field.<ListTypes,List<List<String>>> type("listOfString", ListTypeInformation.listOf(ListTypeInformation.listOf(SimpleConfiguredTypes.get(String.class))))
					.setter(ListTypes::setListOfListOfString)
					.getter(ListTypes::getListOfListOfString);

			Field.<ListTypes,List<List<String>>> type("listOfString", ListTypeInformation.listOf(ListTypeInformation.listOf(SimpleConfiguredTypes.get(String.class))))
					.setter(ListTypes::setListOfListOfString)
					.getter(ListTypes::getListOfListOfString);

		}


	}

	static class JustSimpleTypes {

		String stringValue;
		Integer intValue;
		Long longValue;

		Date dateValue;
		EnumType enumValue;

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}

		public Integer getIntValue() {
			return intValue;
		}

		public void setIntValue(Integer intValue) {
			this.intValue = intValue;
		}

		public Long getLongValue() {
			return longValue;
		}

		public void setLongValue(Long longValue) {
			this.longValue = longValue;
		}

		public Date getDateValue() {
			return dateValue;
		}

		public void setDateValue(Date dateValue) {
			this.dateValue = dateValue;
		}

		public EnumType getEnumValue() {
			return enumValue;
		}

		public void setEnumValue(EnumType enumValue) {
			this.enumValue = enumValue;
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
		List<List<JustSimpleTypes>> listOfListOfComplexType;

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

		public List<List<JustSimpleTypes>> getListOfListOfComplexType() {
			return listOfListOfComplexType;
		}

		public void setListOfListOfComplexType(List<List<JustSimpleTypes>> listOfListOfComplexType) {
			this.listOfListOfComplexType = listOfListOfComplexType;
		}

		public List getRawList() {
			return rawList;
		}

		public void setRawList(List rawList) {
			this.rawList = rawList;
		}
	}

	static class MapTypes {

		Map rawMap;
		Map<Object, Object> mapOfObject;
		Map<String, String> mapOfString;
		Map<String, JustSimpleTypes> mapOfComplexValueType;
		Map<JustSimpleTypes, JustSimpleTypes> mapOfComplexKeyValueType;
		Map<String, InterfaceType> mapOfInterfaceValueType;
		Map<String, List<String>> mapOfListOfStringValueType;
		Map<String, List<JustSimpleTypes>> mapOfListOfComplexValueType;

		public Map<Object, Object> getMapOfObject() {
			return mapOfObject;
		}

		public void setMapOfObject(Map<Object, Object> mapOfObject) {
			this.mapOfObject = mapOfObject;
		}

		public Map<String, String> getMapOfString() {
			return mapOfString;
		}

		public void setMapOfString(Map<String, String> mapOfString) {
			this.mapOfString = mapOfString;
		}

		public Map<String, JustSimpleTypes> getMapOfComplexValueType() {
			return mapOfComplexValueType;
		}

		public void setMapOfComplexValueType(Map<String, JustSimpleTypes> mapOfComplexValueType) {
			this.mapOfComplexValueType = mapOfComplexValueType;
		}

		public Map<JustSimpleTypes, JustSimpleTypes> getMapOfComplexKeyValueType() {
			return mapOfComplexKeyValueType;
		}

		public void setMapOfComplexKeyValueType(Map<JustSimpleTypes, JustSimpleTypes> mapOfComplexKeyValueType) {
			this.mapOfComplexKeyValueType = mapOfComplexKeyValueType;
		}

		public Map<String, InterfaceType> getMapOfInterfaceValueType() {
			return mapOfInterfaceValueType;
		}

		public void setMapOfInterfaceValueType(Map<String, InterfaceType> mapOfInterfaceValueType) {
			this.mapOfInterfaceValueType = mapOfInterfaceValueType;
		}

		public Map<String, List<String>> getMapOfListOfStringValueType() {
			return mapOfListOfStringValueType;
		}

		public void setMapOfListOfStringValueType(Map<String, List<String>> mapOfListOfStringValueType) {
			this.mapOfListOfStringValueType = mapOfListOfStringValueType;
		}

		public Map<String, List<JustSimpleTypes>> getMapOfListOfComplexValueType() {
			return mapOfListOfComplexValueType;
		}

		public void setMapOfListOfComplexValueType(Map<String, List<JustSimpleTypes>> mapOfListOfComplexValueType) {
			this.mapOfListOfComplexValueType = mapOfListOfComplexValueType;
		}

		public Map getRawMap() {
			return rawMap;
		}

		public void setRawMap(Map rawMap) {
			this.rawMap = rawMap;
		}
	}

	static class ListTypesTypeInfo extends ConfigurableTypeInformation<ListTypes> {

		public ListTypesTypeInfo() {
			super(ListTypes.class);

			Field<ListTypes, List> rawList = Field.<ListTypes, Map>type("rawList", MapTypeInformation.map());
			rawList.setter(ListTypes::setRawList);
			addField(rawList);

		}


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
