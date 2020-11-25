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
package org.springframework.data.entity.processor;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.data.annotation.Persistent;
import org.springframework.data.domain.Persistable;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.AbstractRepositoryMetadata;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class PersistableEntityScanner {

	Set<Class<?>> seen = new HashSet<>();

	public List<Class<?>> scan(String packageName) {

		Set<Class<?>> types = new LinkedHashSet<>();

		return collectTypes(packageName, types,
				te -> !Modifier.isAbstract(te.getModifiers()) && !Modifier.isStatic(te.getModifiers()));
	}

	private List<Class<?>> collectTypes(String packageName, Set<Class<?>> types, Predicate<Class<?>> typeSelectionCondition) {

		ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		seen.addAll(types);

		List<Class<?>> domainTypes = new ArrayList<>();

		try {

			String packagePath = "/" + packageName.replace(".", "/") + "/";
			Resource[] resources = resolver.getResources("classpath*:" + packagePath + "**/*.class");

			for (Resource resource : resources) {
				String path = resource.getURL().toString().replace(".class", "");
				String name = ClassUtils.convertResourcePathToClassName(path.substring(path.indexOf(packagePath) + 1));
				if (ClassUtils.isPresent(name, null)) {

					Class<?> type = ClassUtils.resolveClassName(name, null);

					if (!seen.contains(type)) {
						if (AnnotationUtils.findAnnotation(type, Persistent.class) != null || ClassUtils.isAssignable(Persistable.class, type)) {
							domainTypes.addAll(collectTypes(type));
						} else if (ClassUtils.isAssignable(Repository.class, type)) {

							System.out.println("repo found: " + type);
							domainTypes.addAll(collectTypesForRepositiory(type));
						}
					}
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Cannot locate resources in package: " + packageName, e);
		}

		return domainTypes;
	}

	private Set<Class<?>> collectTypesForRepositiory(Class<?> repositoryInterface) {

		RepositoryMetadata metadata = AbstractRepositoryMetadata.getMetadata(repositoryInterface);

		Set<Class<?>> types = new LinkedHashSet<>(collectTypes(metadata.getDomainType()));
		ReflectionUtils.doWithMethods(repositoryInterface, method -> {

			TypeInformation<?> methodTypeInfo = metadata.getReturnType(method);
			if (methodTypeInfo.isCollectionLike()) {
				if (!seen.contains(methodTypeInfo.getComponentType().getType())) {
					types.addAll(collectTypes(methodTypeInfo.getComponentType().getType()));
				}
			} else if (methodTypeInfo.isMap()) {
				if (!seen.contains(methodTypeInfo.getComponentType().getType())) {
					types.addAll(collectTypes(methodTypeInfo.getComponentType().getType()));
				}
				if (!seen.contains(methodTypeInfo.getMapValueType().getType())) {
					types.addAll(collectTypes(methodTypeInfo.getMapValueType().getType()));
				}
			} else {
				// TODO: generic signature???
				if (ClassUtils.isPrimitiveOrWrapper(methodTypeInfo.getActualType().getType())) {
					return;
				}
				if (!seen.contains(methodTypeInfo.getActualType().getType())) {
					types.addAll(collectTypes(methodTypeInfo.getActualType().getType()));
				}
			}
		});

		return types;
	}

	private Set<Class<?>> collectTypes(Class<?> domainType) {

		if (seen.contains(domainType) || domainType.getName().startsWith("java") || ClassUtils.isPrimitiveOrWrapper(domainType) || domainType.isInterface()) {
			return Collections.emptySet();
		}

		seen.add(domainType);

		Set<Class<?>> types = new LinkedHashSet<>();
		types.add(domainType);

		ClassTypeInformation<?> typeInformation = ClassTypeInformation.from(domainType);

		ReflectionUtils.doWithFields(domainType, field -> {

			TypeInformation<?> fieldTypeInfo = typeInformation.getProperty(field.getName());

			if (fieldTypeInfo.isCollectionLike()) {
				if (!seen.contains(fieldTypeInfo.getComponentType().getType())) {
					types.addAll(collectTypes(fieldTypeInfo.getComponentType().getType()));
				}
			} else if (fieldTypeInfo.isMap()) {
				if (!seen.contains(fieldTypeInfo.getComponentType().getType())) {
					types.addAll(collectTypes(fieldTypeInfo.getComponentType().getType()));
				}
				if (!seen.contains(fieldTypeInfo.getMapValueType())) {
					types.addAll(collectTypes(fieldTypeInfo.getMapValueType().getType()));
				}
			} else {
				if (!seen.contains(fieldTypeInfo.getActualType().getType())) {
					types.addAll(collectTypes(fieldTypeInfo.getActualType().getType()));
				}
			}
		});

		return types;
	}
}
