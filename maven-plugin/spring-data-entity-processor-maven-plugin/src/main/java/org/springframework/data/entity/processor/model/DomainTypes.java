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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;


/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class DomainTypes implements Iterable<TypeInfo> {

	private final Set<TypeInfo> domainTypes;

	static DomainTypes empty() {
		return new DomainTypes();
	}

	public DomainTypes() {
		this(Collections.emptySet());
	}

	public DomainTypes(Set<TypeInfo> typeInfos) {
		this.domainTypes = new LinkedHashSet<>(typeInfos);
	}

	boolean containsDomainTypeModelForClass(Class<?> type) {
		return getDomainTypeModelForClass(type).isPresent();
	}

	Optional<TypeInfo> getDomainTypeModelForClass(Class<?> type) {
		return domainTypes.stream().filter(it -> it.getType().equals(type)).findFirst();
	}

	@Override
	public Iterator<TypeInfo> iterator() {
		return domainTypes.iterator();
	}
}
