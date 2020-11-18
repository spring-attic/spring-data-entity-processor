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

import java.io.IOException;

import edu.emory.mathcs.backport.java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.data.example.repo.Person;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class DataComponentProcessorUnitTests {

	@Test
	void x2() throws IOException {

		DataModelGenerator modelGenerator = new DataModelGenerator(Collections.singleton(Person.class));

		DataModelFileWriter fileWriter = new DataModelFileWriter(modelGenerator.process());
		fileWriter.processFiles();
		fileWriter.writeTo(null);
		fileWriter.writeSubstitution(null);
	}
}
