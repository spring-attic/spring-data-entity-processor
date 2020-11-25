/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.mapping.model.SimpleConfiguredTypes;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class PropertyInfo {

	private final String name;
	private final Class<?> type;
	private final TypeInfo owner;

	private Field field;
	private TypeSignature signature;
	private Set<AnnotationModel> annotations;

	private Method getter;
	private Method setter;
	private Method wither;

	boolean isSimpleType;
	boolean isListType;
	boolean isMapType;


	public PropertyInfo(TypeInfo owner, String name, Class<?> type) {

		this.owner = owner;
		this.name = name;
		this.type = type;

		setField(ReflectionUtils.findField(owner.getType(), name));
		this.isListType = ClassUtils.isAssignable(List.class, type);
		this.isMapType = ClassUtils.isAssignable(Map.class, type);
		this.isSimpleType = SimpleConfiguredTypes.isKownSimpleConfiguredType(type);
	}

	PropertyInfo simpleType() {
		this.isSimpleType = true;
		return this;
	}

	public TypeSignature getTypeSignature() {
		return signature;
	}

	public void setField(Field field) {

		this.field = field;
		signature = TypeSignature.fromField(field);
	}

	public String getOwnerTypeName() {
		return getOwner().getType().getCanonicalName();
	}

	public Class<?> getOwnerType() {
		return getOwner().getType();
	}

	public TypeInfo getOwner() {
		return owner;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isSimpleType() {
		return isSimpleType;
	}

	public boolean isListType() {
		return isListType;
	}

	public boolean isMapType() {
		return isMapType;
	}

	public PropertyInfo getter(Method method) {

		this.getter = method;
		return this;
	}

	public PropertyInfo setter(Method method) {

		this.setter = method;
		return this;
	}

	public PropertyInfo wither(Method method) {

		this.wither = method;
		return this;
	}

	public Method getGetter() {
		return getter;
	}

	public Method getSetter() {
		return setter;
	}

	public Method getWither() {
		return wither;
	}

	boolean hasGetter() {
		return getter != null;
	}

	boolean hasSetter() {
		return setter != null;
	}

	boolean hasWither() {
		return wither != null;
	}

	ListPropertyInfo listOf(Class<?> type) {
		return new ListPropertyInfo(this).listValueType(type);
	}

	ListPropertyInfo listOf(TypeInfo model) {
		return new ListPropertyInfo(this).listValueType(model);
	}

	MapPropertyInfo mapOf(TypeInfo key, TypeInfo value) {
		return new MapPropertyInfo(this).mapKeyType(key).mapValueType(value);
	}

	MapPropertyInfo mapOf(Class<?> key, TypeInfo value) {
		return new MapPropertyInfo(this).mapKeyType(key).mapValueType(value);
	}

	MapPropertyInfo mapOf(TypeInfo key, Class<?> value) {
		return new MapPropertyInfo(this).mapKeyType(key).mapValueType(value);
	}

	MapPropertyInfo mapOf(Class<?> key, Class<?> value) {
		return new MapPropertyInfo(this).mapKeyType(key).mapValueType(value);
	}

	DomainTypePropertyInfo domainType(TypeInfo typeInfo) {
		return new DomainTypePropertyInfo(this).type(typeInfo);
	}

	public void annotations(Set<AnnotationModel> annotations) {
		this.annotations = annotations;
	}

	@Override
	public String toString() {
		return "PropertyModel(" + getName() + "){" +
				"type=" + type +
				", annotations=" + annotations +
				'}';
	}

	public Set<AnnotationModel> getAnnotations() {
		return this.annotations;
	}


	static class ListPropertyInfo extends PropertyInfo {

		private Class<?> listValueType;
		private TypeInfo listValueTypeInfo;

		public ListPropertyInfo(PropertyInfo delegate) {
			super(delegate.getOwner(), delegate.getName(), delegate.getType());
		}

		public ListPropertyInfo listValueType(Class<?> model) {
			this.listValueType = model;
			return this;
		}

		public ListPropertyInfo listValueType(TypeInfo model) {

			listValueType(model.getType());
			this.listValueTypeInfo = model;
			return this;
		}

		@Override
		public boolean isListType() {
			return true;
		}

		@Override
		public String toString() {
			return "ListPropertyModel(" + getName() + "){" +
					"listValueType=" + listValueType +
					", listValueTypeModel=" + listValueTypeInfo +
					'}';
		}

		public boolean isSimpleValueType() {
			return listValueTypeInfo == null;
		}

		public Class<?> getListValueType() {
			return listValueType;
		}

		public TypeInfo getListValueTypeInfo() {
			return listValueTypeInfo;
		}
	}

	static class DomainTypePropertyInfo extends PropertyInfo {

		private TypeInfo typeInfo;

		public DomainTypePropertyInfo(PropertyInfo delegate) {
			super(delegate.getOwner(), delegate.getName(), delegate.getType());
		}

		public DomainTypePropertyInfo type(TypeInfo typeInfo) {

			this.typeInfo = typeInfo;
			return this;
		}

		@Override
		public String toString() {
			return "DomainTypePropertyModel(" + getName() + "){" +
					"typeModel=" + typeInfo +
					'}';
		}

		@Override
		public boolean isSimpleType() {
			return false;
		}

		@Override
		public boolean isListType() {
			return false;
		}

		@Override
		public boolean isMapType() {
			return false;
		}
	}

	static class MapPropertyInfo extends PropertyInfo {

		private Class<?> mapKeyType;
		private TypeInfo mapKeyTypeInfo;
		private Class<?> mapValueType;
		private TypeInfo mapValueTypeInfo;

		public MapPropertyInfo(PropertyInfo delegate) {
			super(delegate.getOwner(), delegate.getName(), delegate.getType());
		}


		public MapPropertyInfo mapValueType(Class<?> valueType) {

			this.mapValueType = valueType;
			return this;
		}

		public MapPropertyInfo mapValueType(TypeInfo model) {

			mapValueType(model.getType());
			this.mapValueTypeInfo = model;
			return this;
		}

		public MapPropertyInfo mapKeyType(Class<?> keyType) {
			this.mapKeyType = keyType;
			return this;
		}

		public MapPropertyInfo mapKeyType(TypeInfo model) {

			this.mapValueType = model.getType();
			this.mapValueTypeInfo = model;
			return this;
		}

		public Class<?> getMapKeyType() {
			return mapKeyType;
		}

		public Class<?> getMapValueType() {
			return mapValueType;
		}

		@Override
		public boolean isListType() {
			return false;
		}

		@Override
		public boolean isMapType() {
			return true;
		}

		@Override
		public boolean isSimpleType() {
			return false;
		}

		@Override
		public String toString() {
			return "MapPropertyModel(" + getName() + "){" +
					"mapKeyType=" + mapKeyType +
					", mapKeyTypeModel=" + mapKeyTypeInfo +
					", mapValueType=" + mapValueType +
					", mapValueTypeModel=" + mapValueTypeInfo +
					'}';
		}
	}
}
