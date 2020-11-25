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
package org.springframework.data.entity.processor.model;

import java.lang.reflect.Parameter;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;
import org.springframework.data.mapping.PreferredConstructor;
import org.springframework.data.mapping.model.PreferredConstructorDiscoverer;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class TypeInfo implements Iterable<PropertyInfo> {

	private final Class<?> type;
	private final Set<AnnotationInfo> annotations;
	private final Set<PropertyInfo> properties;
	private ConstructorInfo constructor;

	public TypeInfo(Class<?> type) {

		this.type = type;
		this.annotations = new LinkedHashSet<>();
		this.properties = new LinkedHashSet<>();
	}

	List<Parameter> getConstructorArgs() {

		PreferredConstructor constructor = PreferredConstructorDiscoverer.discover(type);
		return constructor != null ? constructor.getParameters() : Collections.emptyList();
	}

	public Set<AnnotationInfo> getAnnotations() {
		return annotations;
	}

	public TypeInfo annotations(Set<AnnotationInfo> annotations) {
		this.annotations.addAll(annotations);
		return this;
	}

	public void setConstructor(ConstructorInfo constructor) {
		this.constructor = constructor;
	}

	public TypeInfo addProperty(PropertyInfo propertyInfo) {

		this.properties.add(propertyInfo);
		return this;
	}

	public Class<?> getType() {
		return type;
	}

	public String getTypeName() {
		return type.getName();
	}


	public ConstructorInfo getConstructor() {
		return constructor;
	}

	@Override
	public String toString() {
		return "TypeModel{" +
				"type=" + type +
				", annotations=" + annotations +
				", properties=" + properties +
				", constructor=" + constructor +
				'}';
	}

	@Override
	public Iterator<PropertyInfo> iterator() {
		return this.properties.iterator();
	}
}
