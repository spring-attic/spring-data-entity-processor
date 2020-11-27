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
package org.springframework.data.entity.processor.writer;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.squareup.javapoet.WildcardTypeName;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.entity.processor.model.AnnotationInfo;
import org.springframework.data.entity.processor.model.ConstructorInfo;
import org.springframework.data.entity.processor.model.DomainTypes;
import org.springframework.data.entity.processor.model.ParameterInfo;
import org.springframework.data.entity.processor.model.PropertyInfo;
import org.springframework.data.entity.processor.model.TypeInfo;
import org.springframework.data.mapping.model.ConfigurableTypeConstructor;
import org.springframework.data.mapping.model.ConfigurableTypeInformation;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class DataModelFileWriter {

	DomainTypes domainTypes;
	Map<Class<?>, JavaFileBuilder> fileBuilders;

	public DataModelFileWriter(Set<TypeInfo> domainTypes) {
		this(new DomainTypes(domainTypes));
	}

	public DataModelFileWriter(DomainTypes domainTypes) {

		this.domainTypes = domainTypes;
		this.fileBuilders = new LinkedHashMap<>();
	}

	public void processFiles() {

		for (TypeInfo typeInfo : domainTypes) {
			process(typeInfo);
		}
	}

	public void writeTo(@Nullable File directory) throws IOException {
		for (JavaFileBuilder fileBuilder : fileBuilders.values()) {

			JavaFile file = fileBuilder.build();
			if (directory == null) {
				System.out.println(file.toString());
			} else {
				file.writeTo(directory);
			}
		}
	}

	public void writeSubstitution(@Nullable File directory) throws IOException {

		TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);

		ParameterizedTypeName mapKeyType = ParameterizedTypeName.get(ClassName.get(Class.class), wildcard);
		ParameterizedTypeName mapValueType = ParameterizedTypeName.get(ClassName.get(ClassTypeInformation.class), wildcard);

		ParameterizedTypeName cacheType = ParameterizedTypeName.get(ClassName.get(Map.class), mapKeyType, mapValueType);

		Builder fromMethod = MethodSpec.methodBuilder("from")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addAnnotation(ClassName.get("com.oracle.svm.core.annotate", "Substitute"))
				.addTypeVariable(TypeVariableName.get("S"))
				.returns(ParameterizedTypeName.get(ClassName.get(ClassTypeInformation.class), TypeVariableName.get("S")))
				.addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("S")), "type");

		for (JavaFileBuilder fileBuilder : fileBuilders.values()) {

			fromMethod.beginControlFlow("if(type == " + fileBuilder.typeInfo.getTypeName() + ".class)");
			fromMethod.addStatement("return (org.springframework.data.util.ClassTypeInformation<S>) " + fileBuilder.generatedTypeName + ".instance()");
			fromMethod.endControlFlow();
		}

		fromMethod.addStatement("return (org.springframework.data.util.ClassTypeInformation<S>) cache.computeIfAbsent(type, org.springframework.data.util.ClassTypeInformation::new)");

		TypeSpec typeSpec = TypeSpec.classBuilder("Target_ClassTypeInformation")
				.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("com.oracle.svm.core.annotate", "TargetClass"))
						.addMember("className", "$S", "org.springframework.data.util.ClassTypeInformation")
						.addMember("onlyWith", "{ $T.class }", ClassName.get("org.springframework.graalvm.substitutions", "OnlyIfPresent"))
						.build())
				.addField(FieldSpec.builder(cacheType, "cache", Modifier.PRIVATE, Modifier.STATIC).addAnnotation(ClassName.get("com.oracle.svm.core.annotate", "Alias")).build())
				.addMethod(fromMethod.build())
				.build();
		JavaFile file = JavaFile.builder("org.springframework.data.util", typeSpec).build();

		if (directory == null) {
			System.out.println(file.toString());
		} else {
			file.writeTo(directory);
		}
	}

	void process(TypeInfo typeInfo) {

		JavaFileBuilder fileBuilder = getFileBuilder(typeInfo);
		fileBuilders.put(typeInfo.getType(), fileBuilder);

		addPersistenceConstructor(typeInfo, fileBuilder);
		addFields(typeInfo, fileBuilder);
	}


	void addPersistenceConstructor(TypeInfo typeInfo, JavaFileBuilder fileBuilder) {

		String newInstanceStatement = ConfigurableTypeConstructor.class.getTypeName() + ".<" + typeInfo.getTypeName() + ">";
		ConstructorInfo constructor = typeInfo.getConstructor();

		if (constructor.isNoArgConstructor()) {
			newInstanceStatement += "noArgsConstructor(" + typeInfo.getTypeName() + "::new)";
			fileBuilder.newDomainTypeInstance(newInstanceStatement);
			return;
		}

		newInstanceStatement += "builder()";

		{
			List<String> args = new ArrayList<>();
			List<String> ctor = new ArrayList<>();

			int i = 0;
			for (ParameterInfo parameter : constructor.getParameterList()) {
				args.add(String.format("\"%s\"", parameter.getName()));
				ctor.add(String.format("(%s)args[%s]", parameter.getType().getName(), i));
				i++;
			}

			newInstanceStatement += String.format(".args(%s)", StringUtils.collectionToCommaDelimitedString(args));
			newInstanceStatement += String.format(".newInstanceFunction((args) -> new %s(%s))", typeInfo.getTypeName(), StringUtils.collectionToCommaDelimitedString(ctor));
		}

		fileBuilder.newDomainTypeInstance(newInstanceStatement);
	}

	private void addFields(TypeInfo typeInfo, JavaFileBuilder fileBuilder) {

		for (PropertyInfo propertyInfo : typeInfo) {
			computeFieldStatement(propertyInfo).forEach(fileBuilder::fieldStatement);
		}
	}

	List<String> computeFieldStatement(PropertyInfo propertyInfo) {

		List<String> statements = new ArrayList<>();

		String variableName = propertyInfo.getName();
		String typeArgs = String.format("%s,%s", propertyInfo.getOwnerTypeName(), propertyInfo.getTypeSignature().getJavaSignatureString());
		String declaration = String.format("org.springframework.data.mapping.model.Field<%s>", typeArgs);
		String fieldInit = "org.springframework.data.mapping.model.Field.type";
		String cti = computeTypeInfoInit(propertyInfo);
		String typeInit = String.format("(\"%s\",%s)", propertyInfo.getName(), cti);

		String variable = declaration + " " + variableName + " = " + fieldInit + typeInit;

		statements.add(variable);

		if (propertyInfo.hasGetter()) {
			statements.add(variableName + "." + getterMethod(propertyInfo));
		}
		if (propertyInfo.hasSetter()) {
			statements.add(variableName + "." + setterMethod(propertyInfo));
		}
		if (propertyInfo.hasWither()) {
			statements.add(variableName + "." + witherMethod(propertyInfo));
		}

		for (AnnotationInfo annotation : propertyInfo.getAnnotations()) {
			if (annotation.matches(Id.class)) {
				statements.add(variableName + ".annotatedWithAtId()");
			} else {
				statements.add(variableName + ".annotation(" + annotationFrom(annotation) + ")");
			}
		}

		statements.add(String.format("addField(%s)", variableName));
		return statements;
	}

	public String computeTypeInfoInit(PropertyInfo info) {
		return info.getTypeSignature().getConfigurableTypeSignatureString(domainTypes);
	}


	private String annotationFrom(AnnotationInfo model) {

		List<String> methods = new ArrayList<>();
		methods.add(String.format("public Class<? extends java.lang.annotation.Annotation> annotationType() { return %s.class; }", model.getAnnotation().getName()));

		for (Entry<String, Object> entry : model.getArguments().entrySet()) {

			Method method = model.getMethod(entry.getKey());
			Object value = null;
			if (BeanUtils.isSimpleProperty(entry.getValue().getClass())) {

				if (entry.getValue() instanceof Enum) {
					value = entry.getValue().getClass().getTypeName() + "." + entry.getValue();
				} else if (entry.getValue() instanceof String) {
					value = ("\"" + entry.getValue() + "\"");
				} else {
					value = entry.getValue();
				}
			} else if (entry.getValue() instanceof String) {
				value = ("\"" + entry.getValue() + "\"");
			} else {
				value = entry.getValue();
			}
			methods.add(String.format("public %s %s() { return %s; }", method.getReturnType().getTypeName(), entry.getKey(), value));
		}

		return String.format("new %s() {%s}", model.getAnnotation().getName(), StringUtils.collectionToDelimitedString(methods, "\n"));
	}

	private String getterMethod(PropertyInfo property) {
		return String.format("getter(%s::%s)", property.getOwnerTypeName(), property.getGetter().getName());
	}

	private String setterMethod(PropertyInfo property) {
		return String.format("setter(%s::%s)", property.getOwnerTypeName(), property.getSetter().getName());
	}

	private String witherMethod(PropertyInfo property) {
		return String.format("wither(%s::%s)", property.getOwnerTypeName(), property.getWither().getName());
	}

	JavaFileBuilder getFileBuilder(TypeInfo type) {
		return fileBuilders.computeIfAbsent(type.getType(), it -> new JavaFileBuilder(type));
	}


	static class JavaFileBuilder {

		private static final String TYPE_POSTFIX = "ConfigurableTypeInformation";

		private TypeInfo typeInfo;
		private String packageName;
		private String generatedTypeName;
		private String simpleTypeName;

		private String initSuper;
		private String newDomainTypeInstanceStatement;
		private List<String> fieldStatements;
		private Map<String, String> methodStatements;
		private Map<String, String> annotationStatements;

		JavaFileBuilder(TypeInfo typeInfo) {

			this.typeInfo = typeInfo;
			this.generatedTypeName = computeGeneratedTypeName(typeInfo.getType());
			this.packageName = typeInfo.getType().getPackage().getName();
			this.simpleTypeName = generatedTypeName.replace(packageName + ".", "");
			this.fieldStatements = new ArrayList<>();
			this.methodStatements = new LinkedHashMap<>();
			this.annotationStatements = new LinkedHashMap<>();

			superCall();
		}

		JavaFileBuilder superCall() {

			Class<?> type = typeInfo.getType();
			if (type.getSuperclass() == Object.class || type.getSuperclass() == null) {
				return superCall(String.format("super(%s.class)", type.getName()));
			}

			return superCall(String.format("super(%s.class, %s.instance())", type.getName(), computeGeneratedTypeName(type.getSuperclass())));
		}


		JavaFileBuilder superCall(String statement) {

			this.initSuper = statement;
			return this;
		}

		JavaFileBuilder newDomainTypeInstance(String statement) {

			this.newDomainTypeInstanceStatement = statement;
			return this;
		}

		JavaFileBuilder fieldStatement(String statement) {

			this.fieldStatements.add(statement);
			return this;
		}

		JavaFileBuilder methodStatement(String methodName, String statement) {

			this.methodStatements.put(methodName, statement);
			return this;
		}

		JavaFile build() {

			ClassName className = ClassName.get(packageName, simpleTypeName);

			TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(simpleTypeName)
					.addModifiers(Modifier.PUBLIC, Modifier.FINAL)
					.superclass(ParameterizedTypeName.get(ClassName.get(ConfigurableTypeInformation.class),
							ClassName.get(typeInfo.getType())));

			// SINGLETON INSTANCE
			{
				FieldSpec instance = FieldSpec.builder(className, "INSTANCE")
						.addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
						.initializer("new $T()", className)
						.build();

				typeSpecBuilder.addField(instance);

				MethodSpec instanceMethod = MethodSpec.methodBuilder("instance")
						.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
						.returns(className)
						.addStatement("return INSTANCE")
						.build();

				typeSpecBuilder.addMethod(instanceMethod);
			}

			// CONSTRUCTOR
			{
				MethodSpec.Builder constructorMethod = MethodSpec.constructorBuilder()
						.addModifiers(Modifier.PUBLIC);

				// SUPER CALL
				{
					constructorMethod.addStatement(initSuper);
				}

				// PERSISTENCE CONSTRUCTOR
				{
					constructorMethod.addComment("PERSISTENCE CONSTRUCTOR");
					constructorMethod.addStatement("setConstructor(" + newDomainTypeInstanceStatement + ")");
				}

				// FIELD INIT
				{
					constructorMethod.addComment("FIELDS");
					for (String statement : fieldStatements) {
						constructorMethod.addStatement(statement);
					}
				}

				typeSpecBuilder.addMethod(constructorMethod.build());
			}

			// METHODS
			{

			}

			// ANNOTATIONS
			{

			}

			JavaFile.Builder file = JavaFile.builder(packageName, typeSpecBuilder.build());
			return file.build();
		}


		private static String computeGeneratedTypeName(Class<?> baseType) {
			try {
				return baseType.getTypeName() + TYPE_POSTFIX;
			} catch (Exception e) {
				System.out.println("baseType: " + baseType);
				e.printStackTrace();
			}
			return "";
		}
	}
}
