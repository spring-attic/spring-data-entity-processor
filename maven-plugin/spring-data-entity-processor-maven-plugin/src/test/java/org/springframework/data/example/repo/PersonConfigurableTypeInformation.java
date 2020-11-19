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

import org.springframework.data.mapping.model.ConfigurableTypeInformation;
import org.springframework.data.mapping.model.Field;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class PersonConfigurableTypeInformation  extends ConfigurableTypeInformation<Person> {

	private static final PersonConfigurableTypeInformation INSTANCE = new PersonConfigurableTypeInformation();

	public PersonConfigurableTypeInformation() {
		super(org.springframework.data.example.repo.Person.class);
		setConstructor(org.springframework.data.mapping.model.ConfigurableTypeConstructor.<org.springframework.data.example.repo.Person>builder().args("id","firstname","lastname","address").newInstanceFunction((args) -> new org.springframework.data.example.repo.Person((java.lang.String)args[0],(java.lang.String)args[1],(java.lang.String)args[2],(org.springframework.data.example.repo.Address)args[3])));
//		addField(Field.<org.springframework.data.example.repo.Person, org.springframework.data.example.repo.Address>type("address", org.springframework.data.example.repo.AddressConfigurableTypeInformation.instance()).getter(org.springframework.data.example.repo.Person::getAddress).setter(org.springframework.data.example.repo.Person::setAddress));
//		addField(Field.<org.springframework.data.example.repo.Person, java.util.List<org.springframework.data.example.repo.Address>>type("addressList", org.springframework.data.mapping.model.ListTypeInformation.listOf(org.springframework.data.example.repo.AddressConfigurableTypeInformation.instance())).getter(org.springframework.data.example.repo.Person::getAddressList).setter(org.springframework.data.example.repo.Person::setAddressList));
		addField(Field.<org.springframework.data.example.repo.Person,java.lang.String>simpleField("firstname", java.lang.String.class).getter(org.springframework.data.example.repo.Person::getFirstname).annotation(new org.springframework.data.mongodb.core.mapping.Field() {public Class<? extends java.lang.annotation.Annotation> annotationType() { return org.springframework.data.mongodb.core.mapping.Field.class; }
			public java.lang.String name() { return "first-name"; }
			public int order() { return 2147483647; }
			public org.springframework.data.mongodb.core.mapping.FieldType targetType() { return org.springframework.data.mongodb.core.mapping.FieldType.IMPLICIT; }
			public java.lang.String value() { return "first-name"; }}));
		addField(Field.<org.springframework.data.example.repo.Person,java.lang.String>simpleField("id", java.lang.String.class).getter(org.springframework.data.example.repo.Person::getId).setter(org.springframework.data.example.repo.Person::setId).wither(org.springframework.data.example.repo.Person::withId).annotatedWithAtId());
		addField(Field.<org.springframework.data.example.repo.Person,java.lang.String>simpleField("lastname", java.lang.String.class).getter(org.springframework.data.example.repo.Person::getLastname));
	}

	public static PersonConfigurableTypeInformation instance() {
		return INSTANCE;
	}
}
