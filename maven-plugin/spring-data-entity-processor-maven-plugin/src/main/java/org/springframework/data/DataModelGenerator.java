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

	private final Set<TypeModel> typeModels;

	public DataModelGenerator(Set<Class<?>> domainTypes) {

		this.domainTypes = domainTypes;
		this.processed = new LinkedHashSet<>();
		this.typeModels = new LinkedHashSet<>();
	}

	public DataModelGenerator() {
		this(Collections.emptySet());
	}


	Set<TypeModel> process() {

		for (Class<?> domainType : domainTypes) {
			computeTypeModel(domainType);
		}

		return typeModels;
	}

	TypeModel computeTypeModel(Class<?> domainType) {

		{
			Optional<TypeModel> typeModel = typeModelFor(domainType);
			if (typeModel.isPresent()) {
				return typeModel.get();
			}
		}

		TypeModel typeModel = new TypeModel(domainType);
		typeModels.add(typeModel);

		typeModel.setConstructor(new ConstructorModel(domainType));
		computeAndAddPropertyModels(typeModel);
		Set<AnnotationModel> annotations = computeAnnotation(domainType);
		typeModel.annotations(annotations);

		return typeModel;
	}

	void computeAndAddPropertyModels(TypeModel owner) {

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

	void addPropertyModel(TypeModel owner, ClassTypeInformation<?> ownerTypeInformation, Property property) {

		PropertyModel propertyModel = new PropertyModel(owner, property.getName(), property.getType());

		TypeInformation<?> fieldTypeInformation = ownerTypeInformation.getProperty(property.getName());
		if (isSimpleType(fieldTypeInformation)) {
			propertyModel = propertyModel.simpleType();
		} else if (fieldTypeInformation.isMap()) {

			TypeInformation<?> keyType = fieldTypeInformation.getComponentType();
			TypeInformation<?> valueType = fieldTypeInformation.getMapValueType();

			if (isSimpleType(keyType)) {
				if (isSimpleType(valueType)) {
					propertyModel = propertyModel.mapOf(keyType.getType(), valueType.getType());
				} else {
					propertyModel = propertyModel.mapOf(keyType.getType(), computeTypeModel(valueType.getType()));
				}
			} else {
				if (isSimpleType(valueType)) {
					propertyModel = propertyModel.mapOf(computeTypeModel(keyType.getType()), valueType.getType());
				} else {
					propertyModel = propertyModel.mapOf(computeTypeModel(keyType.getType()), computeTypeModel(valueType.getType()));
				}
			}
		} else if (fieldTypeInformation.isCollectionLike()) {

			TypeInformation<?> valueType = fieldTypeInformation.getActualType().getActualType();
			if (isSimpleType(valueType)) {
				propertyModel = propertyModel.listOf(valueType.getType());
			} else {
				propertyModel = propertyModel.listOf(computeTypeModel(valueType.getType()));
			}
		} else {

			TypeModel typeModel = computeTypeModel(property.getType());
			propertyModel = propertyModel.domainType(typeModel);
		}

		if (property.getGetter().isPresent()) {
			propertyModel = propertyModel.getter(property.getGetter().get());
		}
		if (property.getSetter().isPresent()) {
			propertyModel = propertyModel.setter(property.getSetter().get());
		}
		if (property.getWither().isPresent()) {
			propertyModel = propertyModel.wither(property.getWither().get());
		}

		if (property.isFieldBacked()) {

			Set<AnnotationModel> annotations = computeAnnotation(property.getField().get());
			propertyModel.annotations(annotations);
		}

		owner.addProperty(propertyModel);
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

	Optional<TypeModel> typeModelFor(Class<?> type) {
		return typeModels.stream().filter(it -> it.getType().equals(type)).findFirst();
	}

	private boolean isSimpleType(TypeInformation<?> typeInformation) {
		return (BeanUtils.isSimpleValueType(typeInformation.getType()) || typeInformation.getType() == String.class);
	}
}
