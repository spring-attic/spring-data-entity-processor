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

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class PropertyModel {

	private final Class<?> type;
	private final TypeModel owner;
	private TypeModel typeModel;
	private Set<AnnotationModel> annotations;

	private final String name;

	private Method getter;
	private Method setter;
	private Method wither;

	boolean isSimpleType;
	boolean isListType;
	boolean isMapType;


	public PropertyModel(TypeModel owner, String name, Class<?> type) {

		this.owner = owner;
		this.name = name;
		this.type = type;
	}

	PropertyModel simpleType() {
		this.isSimpleType = true;
		return this;
	}

	public String getOwnerTypeName() {
		return getOwner().getTypeName();
	}

	public Class<?> getOwnerType() {
		return getOwner().getType();
	}

	public TypeModel getOwner() {
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

	public PropertyModel getter(Method method) {

		this.getter = method;
		return this;
	}

	public PropertyModel setter(Method method) {

		this.setter = method;
		return this;
	}

	public PropertyModel wither(Method method) {

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

	ListPropertyModel listOf(Class<?> type) {
		return new ListPropertyModel(this).listValueType(type);
	}

	ListPropertyModel listOf(TypeModel model) {
		return new ListPropertyModel(this).listValueType(model);
	}

	MapPropertyModel mapOf(TypeModel key, TypeModel value) {
		return new MapPropertyModel(this).mapKeyType(key).mapValueType(value);
	}

	MapPropertyModel mapOf(Class<?> key, TypeModel value) {
		return new MapPropertyModel(this).mapKeyType(key).mapValueType(value);
	}

	MapPropertyModel mapOf(TypeModel key, Class<?> value) {
		return new MapPropertyModel(this).mapKeyType(key).mapValueType(value);
	}

	MapPropertyModel mapOf(Class<?> key, Class<?> value) {
		return new MapPropertyModel(this).mapKeyType(key).mapValueType(value);
	}

	DomainTypePropertyModel domainType(TypeModel typeModel) {
		return new DomainTypePropertyModel(this).type(typeModel);
	}

	public void annotations(Set<AnnotationModel> annotations) {
		this.annotations = annotations;
	}

	@Override
	public String toString() {
		return "PropertyModel(" + getName() + "){" +
				"type=" + type +
				", typeModel=" + typeModel +
				", annotations=" + annotations +
				'}';
	}

	public Set<AnnotationModel> getAnnotations() {
		return this.annotations;
	}


	static class ListPropertyModel extends PropertyModel {

		private Class<?> listValueType;
		private TypeModel listValueTypeModel;

		public ListPropertyModel(PropertyModel delegate) {
			super(delegate.getOwner(), delegate.getName(), delegate.getType());
		}

		public ListPropertyModel listValueType(Class<?> model) {
			this.listValueType = model;
			return this;
		}

		public ListPropertyModel listValueType(TypeModel model) {

			listValueType(model.getType());
			this.listValueTypeModel = model;
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
					", listValueTypeModel=" + listValueTypeModel +
					'}';
		}

		public boolean isSimpleValueType() {
			return listValueTypeModel == null;
		}

		public Class<?> getListValueType() {
			return listValueType;
		}

		public TypeModel getListValueTypeModel() {
			return listValueTypeModel;
		}
	}

	static class DomainTypePropertyModel extends PropertyModel {

		private TypeModel typeModel;

		public DomainTypePropertyModel(PropertyModel delegate) {
			super(delegate.getOwner(), delegate.getName(), delegate.getType());
		}

		public DomainTypePropertyModel type(TypeModel typeModel) {

			this.typeModel = typeModel;
			return this;
		}

		@Override
		public String toString() {
			return "DomainTypePropertyModel(" + getName() + "){" +
					"typeModel=" + typeModel +
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

	static class MapPropertyModel extends PropertyModel {

		private Class<?> mapKeyType;
		private TypeModel mapKeyTypeModel;
		private Class<?> mapValueType;
		private TypeModel mapValueTypeModel;

		public MapPropertyModel(PropertyModel delegate) {
			super(delegate.getOwner(), delegate.getName(), delegate.getType());
		}


		public MapPropertyModel mapValueType(Class<?> valueType) {

			this.mapValueType = valueType;
			return this;
		}

		public MapPropertyModel mapValueType(TypeModel model) {

			mapValueType(model.getType());
			this.mapValueTypeModel = model;
			return this;
		}

		public MapPropertyModel mapKeyType(Class<?> keyType) {
			this.mapKeyType = keyType;
			return this;
		}

		public MapPropertyModel mapKeyType(TypeModel model) {

			this.mapValueType = model.getType();
			this.mapValueTypeModel = model;
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
					", mapKeyTypeModel=" + mapKeyTypeModel +
					", mapValueType=" + mapValueType +
					", mapValueTypeModel=" + mapValueTypeModel +
					'}';
		}
	}
}
