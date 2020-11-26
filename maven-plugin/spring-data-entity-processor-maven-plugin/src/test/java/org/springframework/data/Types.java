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

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mapping.model.ConfigurableTypeInformation;
import org.springframework.data.mapping.model.Field;
import org.springframework.data.mapping.model.ListTypeInformation;
import org.springframework.data.mapping.model.MapTypeInformation;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public interface Types {

	class DomainType {

	}

	class DomainTypeWithGenericSignature<T1, T2, T3> {

		T1 t1Value;
		T2 t2Value;
		T3 t3Value;
	}

	class SingleTypes {

		Object objectValue;
		String stringValue;
		Integer intValue;
		Long longValue;

		Date dateValue;
		EnumType enumValue;
		DomainType domainType;


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

		public Object getObjectValue() {
			return objectValue;
		}

		public void setObjectValue(Object objectValue) {
			this.objectValue = objectValue;
		}
	}

	enum EnumType {
		E1, E2
	}

	class ArrayTypes {

		Object[] arrayOfObject;
		int[] arrayOfInt;
		String[] arrayOfString;

		EnumType[] arrayOfEnum;
		DomainType[] arrayOfDomainType;

		Object[][] multiDimensionalArray;
	}

	class ListTypes {

		List rawList;
		List<?> listOfWildcard;
		List<Object> listOfObject;
		List<String> listOfString;
		List<DomainType> listOfDomainType;
		List<InterfaceType> listOfInterface;
		List<List<?>> listOfListOfWildcard;
		List<List<String>> listOfListOfString;
		List<List<DomainType>> listOfListOfDomainType;

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

		public List<DomainType> getListOfDomainType() {
			return listOfDomainType;
		}

		public void setListOfDomainType(List<DomainType> listOfDomainType) {
			this.listOfDomainType = listOfDomainType;
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

		public List<List<DomainType>> getListOfListOfDomainType() {
			return listOfListOfDomainType;
		}

		public void setListOfListOfDomainType(List<List<DomainType>> listOfListOfDomainType) {
			this.listOfListOfDomainType = listOfListOfDomainType;
		}

		public List getRawList() {
			return rawList;
		}

		public void setRawList(List rawList) {
			this.rawList = rawList;
		}

		public List<?> getListOfWildcard() {
			return listOfWildcard;
		}

		public void setListOfWildcard(List<?> listOfWildcard) {
			this.listOfWildcard = listOfWildcard;
		}
	}

	class ListTypesTemplate extends ConfigurableTypeInformation<ListTypes> {

		ListTypesTemplate() {
			super(ListTypes.class);

			Field<ListTypes, List<?>> field = Field.type("", ListTypeInformation.list());
			Field<ListTypes, List<?>> field2 = Field.type("", ListTypeInformation.list());
			field2.getter(ListTypes::getRawList);
		}
	}

	class MapTypes {

		Map rawMap;
		Map<?, ?> mapOfWildcard;
		Map<Object, Object> mapOfObject;
		Map<String, String> mapOfString;
		Map<DomainType, String> mapOfDomainTypeKey;
		Map<String, DomainType> mapOfDomainTypeValue;
		Map<String, InterfaceType> mapOfInterfaceValueType;
		Map<String, List<String>> mapOfListOfStringValueType;
		Map<String, List<DomainType>> mapOfListOfDomainValueType;
		Map<String, Map<String, String>> mapOfMapValueType;

		public Map<String, Map<String, String>> getMapOfMapValueType() {
			return mapOfMapValueType;
		}

		public void setMapOfMapValueType(Map<String, Map<String, String>> mapOfMapValueType) {
			this.mapOfMapValueType = mapOfMapValueType;
		}

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

		public Map<?, ?> getMapOfWildcard() {
			return mapOfWildcard;
		}

		public void setMapOfWildcard(Map<?, ?> mapOfWildcard) {
			this.mapOfWildcard = mapOfWildcard;
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

		public Map<DomainType, String> getMapOfDomainTypeKey() {
			return mapOfDomainTypeKey;
		}

		public void setMapOfDomainTypeKey(Map<DomainType, String> mapOfDomainTypeKey) {
			this.mapOfDomainTypeKey = mapOfDomainTypeKey;
		}

		public Map<String, DomainType> getMapOfDomainTypeValue() {
			return mapOfDomainTypeValue;
		}

		public void setMapOfDomainTypeValue(Map<String, DomainType> mapOfDomainTypeValue) {
			this.mapOfDomainTypeValue = mapOfDomainTypeValue;
		}

		public Map<String, List<DomainType>> getMapOfListOfDomainValueType() {
			return mapOfListOfDomainValueType;
		}

		public void setMapOfListOfDomainValueType(Map<String, List<DomainType>> mapOfListOfDomainValueType) {
			this.mapOfListOfDomainValueType = mapOfListOfDomainValueType;
		}

		public Map getRawMap() {
			return rawMap;
		}

		public void setRawMap(Map rawMap) {
			this.rawMap = rawMap;
		}
	}

	class ListTypesTypeInfo extends ConfigurableTypeInformation<ListTypes> {

		public ListTypesTypeInfo() {
			super(ListTypes.class);

			Field<ListTypes, List> rawList = Field.<ListTypes, Map>type("rawList", MapTypeInformation.map());
			rawList.setter(ListTypes::setRawList);
			addField(rawList);
		}
	}

	class CyclicType {

		CyclicType parent;
		List<CyclicType> listOfCyclicType;
		Map<String, CyclicType> mapOfCyclicType;
	}

	abstract class AbstractType {

		String stringValueInAbstractType;
	}

	class AbstractTypeImplementation extends AbstractType {

		Long longValueInAbstractTypeImplementation;
	}

	class LimitedAccess {

		String noSetter;
		String noGetter;
		String withWither;
	}

	class NoArgsCtor {

	}

	class SingleCtor {

		public SingleCtor(String stringValue, Long longValue) {

		}
	}

	class ProtectedCtor {

		protected ProtectedCtor(String stringValue, Long longValue) {

		}
	}

	class MultipleCtors {

		MultipleCtors(String singleArg) {

		}

		MultipleCtors(String multiArg1, Long multiArg2) {

		}
	}

	class PreferredCtors {

		@PersistenceConstructor
		PreferredCtors(String singleArg) {

		}

		PreferredCtors(String multiArg1, Long multiArg2) {

		}
	}

	class FieldAnnotation {

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
	@interface AnnotationType {

		String att() default "default-value";
	}

	@Target({ElementType.TYPE, ElementType.FIELD})
	@interface AnnotationContainer {

		AnnotationType[] value() default {};
	}
}
