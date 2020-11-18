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

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * @author Christoph Strobl
 */
public class CodeGeneratorApplication {

	public static void main(String[] args) {

		String packageToScan = args[0];
		String targetDir = args[1];

		System.out.println("targetDir: " + targetDir);

		File outputDirectory = new File(targetDir);
		if (!outputDirectory.exists()) {
			outputDirectory.mkdir();
		}

		scanProcessAndWriteFiles(packageToScan, outputDirectory);
	}

	private static void scanProcessAndWriteFiles(String packageName, File outputDirectory) {

		PersistableEntityScanner scanner = new PersistableEntityScanner();
		List<Class<?>> types = scanner.scan(packageName);

		DataModelGenerator modelGenerator = new DataModelGenerator(new LinkedHashSet<>(types));
		DataModelFileWriter fileWriter = new DataModelFileWriter(modelGenerator.process());
		fileWriter.processFiles();
		try {
			fileWriter.writeTo(outputDirectory);
			fileWriter.writeSubstitution(outputDirectory);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
