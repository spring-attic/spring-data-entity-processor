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

import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.example.annotations.Document;
import org.springframework.data.example.annotations.Field;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
@Document(collection = "persons")
public class Person {

	private @Id String id;

	@Field("first-name")
	private final String firstname;
	private final String lastname;
	private Address address;
	private List<Address> addressList;
	private Map<String, String> phoneNumbers;
	private Map<String, Email> emailAddresses;

	public Person(String firstname, String lastname) {
		this(null, firstname, lastname, null);
	}

	@PersistenceConstructor
	public Person(String id, String firstname, String lastname, Address address) {

		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.address = address;
	}

	public String getFirstname() {
		return firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getId() {
		return id;
	}

	public Person withId(String id) {
		return new Person(id, firstname, lastname, address);
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Address> getAddressList() {
		return addressList;
	}

	public void setAddressList(List<Address> addressList) {
		this.addressList = addressList;
	}

	public Map<String, String> getPhoneNumbers() {
		return phoneNumbers;
	}

	public void setPhoneNumbers(Map<String, String> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}

	public Map<String, Email> getEmailAddresses() {
		return emailAddresses;
	}

	public void setEmailAddresses(Map<String, Email> emailAddresses) {
		this.emailAddresses = emailAddresses;
	}
}
