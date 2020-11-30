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

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.entity.processor.PersistableEntityScanner;
import org.springframework.data.example.annotated.AnnotatedPerson;
import org.springframework.data.example.cyclic.LineItem;
import org.springframework.data.example.cyclic.Order;
import org.springframework.data.example.persistable.PersistablePerson;
import org.springframework.data.example.repo.Address;
import org.springframework.data.example.repo.Email;
import org.springframework.data.example.repo.GeoResultValueType;
import org.springframework.data.example.repo.ListValueType;
import org.springframework.data.example.repo.PageValueType;
import org.springframework.data.example.repo.Person;
import org.springframework.data.example.repo.SliceValueType;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class PersistableEntityScannerTests {

	private PersistableEntityScanner scanner;

	@BeforeEach
	void setUp() {
		scanner = new PersistableEntityScanner();
	}

	@Test
	void scansBasePackageForTypesUsedInRepositoryInterface() {

		assertThat(scanner.scan("org.springframework.data.example.repo"))
				.containsExactlyInAnyOrder(Person.class, Address.class, Email.class, GeoResultValueType.class, PageValueType.class, ListValueType.class, SliceValueType.class);
	}

	@Test
	void scansBasePackageForPersistentAnnotatedEntities() {

		assertThat(scanner.scan("org.springframework.data.example.annotated"))
				.containsExactlyInAnyOrder(AnnotatedPerson.class);
	}

	@Test
	void scansBasePackageForEntitiesExtendingPersistable() {

		assertThat(scanner.scan("org.springframework.data.example.persistable"))
				.containsExactlyInAnyOrder(PersistablePerson.class);
	}

	@Test
	void ignoresNotMatching() {

		assertThat(scanner.scan("org.springframework.data.example.ignored"))
				.isEmpty();
	}

	@Test
	void handlesCyclesInDomainModel() {

		assertThat(scanner.scan("org.springframework.data.example.cyclic"))
				.containsExactlyInAnyOrder(Order.class, LineItem.class);
	}
}
