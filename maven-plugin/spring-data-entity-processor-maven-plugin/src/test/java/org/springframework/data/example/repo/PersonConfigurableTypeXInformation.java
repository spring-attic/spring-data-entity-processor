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
package org.springframework.data.example.repo;

import java.lang.annotation.Annotation;

import org.springframework.data.mapping.model.ConfigurableTypeConstructor;
import org.springframework.data.mapping.model.ConfigurableTypeInformation;
import org.springframework.data.mapping.model.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public final class PersonConfigurableTypeXInformation extends ConfigurableTypeInformation<Person> {

	private static final PersonConfigurableTypeXInformation INSTANCE = new PersonConfigurableTypeXInformation();

	public PersonConfigurableTypeXInformation() {
		super(org.springframework.data.example.repo.Person.class);

	}

	public static PersonConfigurableTypeXInformation instance() {
		return INSTANCE;
	}
}
