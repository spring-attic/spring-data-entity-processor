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

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.example.annotated.AnnotatedPerson;
import org.springframework.data.example.persitable.PersistablePerson;
import org.springframework.data.example.repo.Address;
import org.springframework.data.example.repo.Email;
import org.springframework.data.example.repo.Person;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class PersistableEntityScannerTests {

	@Test
	void scansBasePackageForTypes() {

		PersistableEntityScanner scanner = new PersistableEntityScanner();
		List<Class<?>> types = scanner.scan("org.springframework.data.example");

		Assertions.assertThat(types).containsExactlyInAnyOrder(AnnotatedPerson.class, PersistablePerson.class, Person.class, Address.class, Email.class);
	}
}
