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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class AnnotationModel {

	private final AnnotatedElement element;
	private final Class<? extends Annotation> annotation;
	private Map<String, Object> arguments;

	public AnnotationModel(AnnotatedElement element, Class<? extends Annotation> annotation) {

		this.element = element;
		this.annotation = annotation;
		this.arguments = resolveArguments(element, annotation);
	}

	public Class<? extends Annotation> getAnnotation() {
		return annotation;
	}

	public Map<String, Object> getArguments() {
		return arguments;
	}

	public static Map<String, Object> resolveArguments(AnnotatedElement element, Class<? extends Annotation> annotation) {
		return AnnotatedElementUtils.getMergedAnnotationAttributes(element, annotation);
	}

	public boolean matches(Class<? extends Annotation> type) {
		return annotation.equals(type);
	}

	@Override
	public String toString() {
		return "AnnotationModel{" +
				"element=" + element +
				", annotation=" + annotation +
				", arguments=" + arguments +
				'}';
	}

	public Method getMethod(String name) {
		return ReflectionUtils.findMethod(getAnnotation(), name);
	}
}
