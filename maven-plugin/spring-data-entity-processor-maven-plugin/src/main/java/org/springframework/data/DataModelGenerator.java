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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.data.mapping.model.Property;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class DataModelGenerator {

	private final Set<Class<?>> domainTypes;
	private final Set<Class<?>> processed;

	private final Set<TypeInfo> typeInfos;

	public DataModelGenerator(Set<Class<?>> domainTypes) {

		this.domainTypes = domainTypes;
		this.processed = new LinkedHashSet<>();
		this.typeInfos = new LinkedHashSet<>();
	}

	public DataModelGenerator() {
		this(Collections.emptySet());
	}


	Set<TypeInfo> process() {

		for (Class<?> domainType : domainTypes) {
			computeTypeModel(domainType);
		}

		return typeInfos;
	}

	TypeInfo computeTypeModel(Class<?> domainType) {

		{
			Optional<TypeInfo> typeModel = typeModelFor(domainType);
			if (typeModel.isPresent()) {
				return typeModel.get();
			}
		}

		TypeInfo typeInfo = new TypeInfo(domainType);
		typeInfos.add(typeInfo);

		typeInfo.setConstructor(new ConstructorInfo(domainType));
		computeAndAddPropertyModels(typeInfo);
		Set<AnnotationModel> annotations = computeAnnotation(domainType);
		typeInfo.annotations(annotations);

		return typeInfo;
	}

	void computeAndAddPropertyModels(TypeInfo owner) {

		PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(owner.getType());
		ClassTypeInformation<?> ownerTypeInformation = ClassTypeInformation.from(owner.getType());

		for (PropertyDescriptor descriptor : descriptors) {

			Field field = ReflectionUtils.findField(owner.getType(), descriptor.getName());

			Property property = field != null ? Property.of(ownerTypeInformation, field, descriptor) : Property.of(ownerTypeInformation, descriptor);
			if (isTransientProperty(property)) {
				continue;
			}

			addPropertyModel(owner, ownerTypeInformation, property);
		}
	}

	void addPropertyModel(TypeInfo owner, ClassTypeInformation<?> ownerTypeInformation, Property property) {

		PropertyInfo propertyInfo = new PropertyInfo(owner, property.getName(), property.getType());

		TypeInformation<?> fieldTypeInformation = ownerTypeInformation.getProperty(property.getName());
		if (isSimpleType(fieldTypeInformation)) {
			propertyInfo = propertyInfo.simpleType();
		} else if (fieldTypeInformation.isMap()) {

			TypeInformation<?> keyType = fieldTypeInformation.getComponentType();
			TypeInformation<?> valueType = fieldTypeInformation.getMapValueType();

			if (isSimpleType(keyType)) {
				if (isSimpleType(valueType)) {
					propertyInfo = propertyInfo.mapOf(keyType.getType(), valueType.getType());
				} else {
					propertyInfo = propertyInfo.mapOf(keyType.getType(), computeTypeModel(valueType.getType()));
				}
			} else {
				if (isSimpleType(valueType)) {
					propertyInfo = propertyInfo.mapOf(computeTypeModel(keyType.getType()), valueType.getType());
				} else {
					propertyInfo = propertyInfo.mapOf(computeTypeModel(keyType.getType()), computeTypeModel(valueType.getType()));
				}
			}
		} else if (fieldTypeInformation.isCollectionLike()) {

			TypeInformation<?> valueType = fieldTypeInformation.getActualType().getActualType();
			if (isSimpleType(valueType)) {
				propertyInfo = propertyInfo.listOf(valueType.getType());
			} else {
				propertyInfo = propertyInfo.listOf(computeTypeModel(valueType.getType()));
			}
		} else {

			TypeInfo typeInfo = computeTypeModel(property.getType());
			propertyInfo = propertyInfo.domainType(typeInfo);
		}

		if (property.getGetter().isPresent()) {
			propertyInfo = propertyInfo.getter(property.getGetter().get());
		}
		if (property.getSetter().isPresent()) {
			propertyInfo = propertyInfo.setter(property.getSetter().get());
		}
		if (property.getWither().isPresent()) {
			propertyInfo = propertyInfo.wither(property.getWither().get());
		}

		if (property.isFieldBacked()) {

			Set<AnnotationModel> annotations = computeAnnotation(property.getField().get());
			propertyInfo.annotations(annotations);

			propertyInfo.setField(property.getField().get());
		}

		owner.addProperty(propertyInfo);
	}

	private Set<AnnotationModel> computeAnnotation(AnnotatedElement element) {

		if (ObjectUtils.isEmpty(element.getAnnotations())) {
			return Collections.emptySet();
		}

		Set<AnnotationModel> annotations = new LinkedHashSet<>();
		for (Annotation annotation : element.getAnnotations()) {
			annotations.add(new AnnotationModel(element, annotation.annotationType()));
		}
		return annotations;
	}

	private boolean isTransientProperty(Property property) {

		if (property.getName().equals("class") || (!property.hasAccessor() && !property.getWither().isPresent())) {
			return true;
		}
		return false;
	}

	Optional<TypeInfo> typeModelFor(Class<?> type) {
		return typeInfos.stream().filter(it -> it.getType().equals(type)).findFirst();
	}

	private boolean isSimpleType(TypeInformation<?> typeInformation) {
		return (BeanUtils.isSimpleValueType(typeInformation.getType()) || typeInformation.getType() == String.class);
	}
}
