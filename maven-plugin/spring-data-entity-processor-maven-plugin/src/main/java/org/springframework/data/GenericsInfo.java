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

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.core.ResolvableType;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class GenericsInfo {

	private final ResolvableType resolvableType;

	public GenericsInfo(ResolvableType resolvableType) {
		this.resolvableType = resolvableType;
	}

	String getSignature() {
		return getSignature(resolvableType);
	}

	public ResolvableType getResolvableType() {
		return resolvableType;
	}

	String getSignature(ResolvableType type) {

		if (type.isArray()) {
			return type.getComponentType() + "[]";
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
				args.add(getSignature(arg));
			}
			tmp += StringUtils.collectionToDelimitedString(args, ", ") + '>';
			return tmp;
		}
		return ClassUtils.resolvePrimitiveIfNecessary(type.resolve()).getCanonicalName();
	}
}
