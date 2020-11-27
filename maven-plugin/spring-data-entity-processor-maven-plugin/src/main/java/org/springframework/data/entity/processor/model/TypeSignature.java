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

import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class TypeSignature {

	private static final String TYPE_POSTFIX = "ConfigurableTypeInformation";

	private final ResolvableType resolvableType;
	private final @Nullable Field field;

	public TypeSignature(ResolvableType resolvableType, @Nullable Field field) {

		this.resolvableType = resolvableType;
		this.field = field;
	}

	public static TypeSignature fromField(Field field) {
		return new TypeSignature(ResolvableType.forField(field), field);
	}

	public static TypeSignature fromClass(Class<?> type) {
		return new TypeSignature(ResolvableType.forClass(type), null);
	}

	public static TypeSignature from(Class<?> owner, String field) {
		return fromField(ReflectionUtils.findField(owner, field));
	}

	public String getPackageName() {
		return resolvableType.resolve().getPackage().getName();
	}

	public String getSimpleName() {
		return resolvableType.resolve().getSimpleName();
	}

	public String getCanonicalConfigurableTypeName() {
		return resolvableType.resolve().getCanonicalName() + TYPE_POSTFIX;
	}

	public String getCanonicalSuperConfigurableTypeName() {

		if (resolvableType.resolve().getSuperclass() != null) {
			return resolvableType.resolve().getSuperclass().getCanonicalName() + TYPE_POSTFIX;
		}
		return null;
	}

	public String getSimpleConfigurableTypeName() {
		return getSimpleName() + TYPE_POSTFIX;
	}

	public String getJavaSignatureString() {
		return getJavaSignature(resolvableType);
	}

	public String getConfigurableTypeSignatureString(DomainTypes domainTypes) {
		return initSignatureFrom(resolvableType, domainTypes);
	}

	public String getConfigurableTypeSignatureString() {
		return getConfigurableTypeSignatureString(new DomainTypes());
	}

	String getJavaSignature(ResolvableType type) {

		if (type.isArray()) {
			return type.getComponentType().resolve().getCanonicalName() + "[]";
		}
		if (type.resolve() == null) {
			return "?";
		}
		if (type.getType() instanceof TypeVariable) {
			TypeVariable<?> variable = (TypeVariable<?>) type.getType();
			return variable.getTypeName();
		}
		if (type.hasGenerics()) {
			String tmp = type.resolve().getCanonicalName() + '<';
			List<String> args = new ArrayList<>();
			for (ResolvableType arg : type.getGenerics()) {
				args.add(getJavaSignature(arg));
			}
			tmp += StringUtils.collectionToDelimitedString(args, ",") + '>';
			return tmp;
		}
		return ClassUtils.resolvePrimitiveIfNecessary(type.resolve()).getCanonicalName();
	}

	String initSignatureFrom(ResolvableType type, DomainTypes domainTypes) {

		if (domainTypes.containsDomainTypeModelForClass(type.resolve()) || field == null) {
			return type.resolve().getCanonicalName() + TYPE_POSTFIX + ".instance()";
		}

		if (type.isArray()) {

			String format = "org.springframework.data.mapping.model.ArrayTypeInformation.arrayOf(%s)";
			return String.format(format, initSignatureFrom(ResolvableType.forRawClass(type.resolve().getComponentType()), domainTypes));
		}

		if (type.resolve() == null) {
			return "?";
		}

		if (type.resolve() == Object.class) {
			return "org.springframework.data.mapping.model.SimpleConfiguredTypes.object()";
		}

		if (ClassUtils.isAssignable(List.class, type.resolve())) {

			if (!type.hasGenerics() || type.hasUnresolvableGenerics()) {
				return "org.springframework.data.mapping.model.ListTypeInformation.list()";
			}

			String format = "org.springframework.data.mapping.model.ListTypeInformation.listOf(%s)";
			return String.format(format, initSignatureFrom(type.getGeneric(0), domainTypes));
		}
		if (ClassUtils.isAssignable(Map.class, type.resolve())) {

			if (!type.hasGenerics() || type.hasUnresolvableGenerics()) {
				return "org.springframework.data.mapping.model.MapTypeInformation.map()";
			}

			String format = "org.springframework.data.mapping.model.MapTypeInformation.mapOf(%s,%s)";
			return String.format(format, initSignatureFrom(type.getGeneric(0), domainTypes), initSignatureFrom(type.getGeneric(1), domainTypes));
		}

		if (type.getType() instanceof TypeVariable) {
			TypeVariable<?> variable = (TypeVariable<?>) type.getType();
			return variable.getTypeName();
		}
		if (type.hasGenerics()) {
			String tmp = type.resolve().getCanonicalName() + '<';
			List<String> args = new ArrayList<>();
			for (ResolvableType arg : type.getGenerics()) {
				args.add(initSignatureFrom(arg, domainTypes));
			}
			tmp += StringUtils.collectionToDelimitedString(args, ", ") + '>';
			return tmp;
		}

		Class<?> resolved = ClassUtils.resolvePrimitiveIfNecessary(type.resolve());

		return String.format("org.springframework.data.mapping.model.SimpleConfiguredTypes.get(%s.class)", resolved.getCanonicalName());
	}
}
