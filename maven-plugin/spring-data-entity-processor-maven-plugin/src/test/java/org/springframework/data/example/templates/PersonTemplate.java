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
package org.springframework.data.example.templates;

import java.util.List;

import org.springframework.data.example.repo.Address;
import org.springframework.data.example.repo.Person;
import org.springframework.data.mapping.model.ConfigurableTypeConstructor;
import org.springframework.data.mapping.model.ConfigurableTypeInformation;
import org.springframework.data.mapping.model.Field;
import org.springframework.data.mapping.model.ListTypeInformation;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class PersonTemplate extends ConfigurableTypeInformation<Person> {

	public PersonTemplate() {
		super(Person.class);

		setConstructor(ConfigurableTypeConstructor.<org.springframework.data.example.repo.Person>builder().args("arg0", "arg1", "arg2", "arg3").newInstanceFunction((args) -> new org.springframework.data.example.repo.Person((java.lang.String) args[0], (java.lang.String) args[1], (java.lang.String) args[2], (org.springframework.data.example.repo.Address) args[3])));
		setConstructor(ConfigurableTypeConstructor.<org.springframework.data.example.repo.Person>builder().args("arg0", "arg1", "arg2", "arg3").newInstanceFunction((args) -> new org.springframework.data.example.repo.Person((java.lang.String) args[0], (java.lang.String) args[1], (java.lang.String) args[2], (org.springframework.data.example.repo.Address) args[3])));

		Field.<Person, List<Address>>type("addressList", ListTypeInformation.listOf(new AddressTypeInformation())).getter(Person::getAddressList).setter(Person::setAddressList);

		addField(Field.<org.springframework.data.example.repo.Person, java.util.List<org.springframework.data.example.repo.Address>>type("addressList", org.springframework.data.mapping.model.ListTypeInformation.listOf(AddressTypeInformation.instance())).getter(org.springframework.data.example.repo.Person::getAddressList).setter(org.springframework.data.example.repo.Person::setAddressList));

//		Field.<> type("phoneNumbers", MapTypeInformation.mapOf(SimpleConfiguredTypes.get(String.class), SimpleConfiguredTypes.get(String.class)))
//				.getter(Person::getPhoneNumbers).setter(Person::setPhoneNumbers);

//		addField(Field.<org.springframework.data.example.repo.Person, java.util.Map<java.lang.String,java.lang.String>>type("phoneNumbers", org.springframework.data.mapping.model.MapTypeInformation.mapOf(SimpleConfiguredTypes.get(java.lang.String.class),SimpleConfiguredTypes.get(java.lang.String.class))).getter(org.springframework.data.example.repo.Person::getPhoneNumbers).setter(org.springframework.data.example.repo.Person::setPhoneNumbers));
//
//		addField(Field.<org.springframework.data.example.repo.Person, java.util.Map<java.lang.String,org.springframework.data.example.repo.Email>>type("emailAddresses", org.springframework.data.mapping.model.MapTypeInformation.mapOf(SimpleConfiguredTypes.get(java.lang.String.class),org.springframework.data.example.repo.EmailConfigurableTypeInformation.instance())).getter(org.springframework.data.example.repo.Person::getEmailAddresses).setter(org.springframework.data.example.repo.Person::setEmailAddresses));

		Field.<Person, Address>type("address", new AddressTypeInformation()).setter(Person::setAddress).getter(Person::getAddress);
	}


	static class AddressTypeInformation extends ConfigurableTypeInformation<Address> {

		public AddressTypeInformation() {
			super(Address.class);
		}

		static AddressTypeInformation instance() {
			return new AddressTypeInformation();
		}
	}
}
