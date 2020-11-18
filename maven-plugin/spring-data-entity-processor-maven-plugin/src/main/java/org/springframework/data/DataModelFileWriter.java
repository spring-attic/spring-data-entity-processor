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
import org.springframework.data.PropertyModel.ListPropertyModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mapping.model.DomainTypeConstructor;
import org.springframework.data.mapping.model.DomainTypeInformation;
import org.springframework.data.mapping.model.Field;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class DataModelFileWriter {

	DomainTypes domainTypes;
	Map<Class<?>, JavaFileBuilder> fileBuilders;

	public DataModelFileWriter(Set<TypeModel> domainTypes) {
		this(new DomainTypes(domainTypes));
	}

	public DataModelFileWriter(DomainTypes domainTypes) {

		this.domainTypes = domainTypes;
		this.fileBuilders = new LinkedHashMap<>();
	}

	void processFiles() {

		for (TypeModel typeModel : domainTypes) {
			process(typeModel);
		}
	}

	void writeTo(@Nullable File directory) throws IOException {
		for (JavaFileBuilder fileBuilder : fileBuilders.values()) {

			JavaFile file = fileBuilder.build();
			if (directory == null) {
				System.out.println(file.toString());
			} else {
				file.writeTo(directory);
			}
		}
	}

	void writeSubstitution(@Nullable File directory) throws IOException {

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

		fromMethod.addStatement("System.out.println(\"hello from substitution!\")");

		for (JavaFileBuilder fileBuilder : fileBuilders.values()) {

			fromMethod.beginControlFlow("if(type == " + fileBuilder.typeModel.getTypeName() + ".class)");
			fromMethod.addStatement("return (org.springframework.data.util.ClassTypeInformation<S>) " + fileBuilder.generatedTypeName + ".instance()");
			fromMethod.endControlFlow();
		}

		fromMethod.addStatement("return (org.springframework.data.util.ClassTypeInformation<S>) cache.computeIfAbsent(type, org.springframework.data.util.ClassTypeInformation::new)");

		TypeSpec typeSpec = TypeSpec.classBuilder("Target_ClassTypeInformation")
				.addModifiers(Modifier.PUBLIC)
				.addAnnotation(AnnotationSpec.builder(ClassName.get("com.oracle.svm.core.annotate", "TargetClass"))
						.addMember("className", "$S", "org.springframework.data.util.ClassTypeInformation")
//						.addMember("onlyWith", "{ $T.class }", ClassName.get("org.springframework.graalvm.substitutions", "OnlyIfPresent"))
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

	void process(TypeModel typeModel) {

		JavaFileBuilder fileBuilder = getFileBuilder(typeModel);
		fileBuilders.put(typeModel.getType(), fileBuilder);

		addPersistenceConstructor(typeModel, fileBuilder);
		addFields(typeModel, fileBuilder);
	}


	void addPersistenceConstructor(TypeModel typeModel, JavaFileBuilder fileBuilder) {

		String newInstanceStatement = DomainTypeConstructor.class.getTypeName() + ".<" + typeModel.getTypeName() + ">";
		ConstructorModel constructor = typeModel.getConstructor();

		if (constructor.isNoArgConstructor()) {
			newInstanceStatement += "noArgsConstructor(" + typeModel.getTypeName() + "::new)";
			fileBuilder.newDomainTypeInstance(newInstanceStatement);
			return;
		}

		newInstanceStatement += "builder()";

		{
			List<String> args = new ArrayList<>();
			List<String> ctor = new ArrayList<>();

			int i = 0;
			for (ParameterModel parameter : constructor.getParameterList()) {
				args.add(String.format("\"%s\"", parameter.getName()));
				ctor.add(String.format("(%s)args[%s]", parameter.getType().getName(), i));
				i++;
			}

			newInstanceStatement += String.format(".args(%s)", StringUtils.collectionToCommaDelimitedString(args));
			newInstanceStatement += String.format(".newInstanceFunction((args) -> new %s(%s))", typeModel.getTypeName(), StringUtils.collectionToCommaDelimitedString(ctor));
		}

		fileBuilder.newDomainTypeInstance(newInstanceStatement);
	}

	private void addFields(TypeModel typeModel, JavaFileBuilder fileBuilder) {

		for (PropertyModel propertyModel : typeModel) {
			addField(propertyModel, fileBuilder);
		}
	}

	private void addField(PropertyModel property, JavaFileBuilder fileBuilder) {

		String statement = "";
//		TypeInformation<?> fieldTypeInformation = owner.getProperty(property.getName());

		if (property.isSimpleType) {
			boolean appendName = true;
			statement = "$T.<" + property.getOwnerTypeName() + ">";
			if (property.getType() == Long.class) {
				statement += "int64";
			} else if (property.getType() == Integer.class) {
				statement += "int32";
			} else if (property.getType() == String.class) {
				statement += "string";
			} else {
				appendName = false;
				Class<?> type = ClassUtils.resolvePrimitiveIfNecessary(property.getType());
				statement = "((Field<" + type.getTypeName() + "," + property.getOwnerTypeName() + ">)$1T.type(\"" + property.getName() + "\", new org.springframework.data.mapping.model.DomainTypeInformation(" + type.getTypeName() + ".class)))";
			}
			if (appendName) {
				statement += "(\"" + property.getName() + "\")";
			}
		} else if (property.isMapType()) {

		} else if (property.isListType()) {

			String template = "$T.<%s, %s<%s>>type(\"%s\", org.springframework.data.mapping.model.ListTypeInformation.listOf(%s))";

			ListPropertyModel listPorperty = (ListPropertyModel) property;
			String domainTypeInformation = "";
			if (listPorperty.isSimpleValueType()) {
				domainTypeInformation = "new DomainTypeInformation(" + listPorperty.getListValueType().getTypeName() + ".class)";
			} else {
				JavaFileBuilder fieldBuilder = getFileBuilder(listPorperty.getListValueTypeModel());
				domainTypeInformation = fieldBuilder.generatedTypeName + ".instance()";
			}

			statement = String.format(template, property.getOwnerTypeName(), listPorperty.getType().getTypeName(), listPorperty.getListValueType().getTypeName(), property.getName(), domainTypeInformation);
		} else {
			JavaFileBuilder fieldBuilder = getFileBuilder(domainTypes.getDomainTypeModelForClass(property.getType()).get());
			statement = "$T.<" + property.getOwnerTypeName() + ", " + property.getType().getName() + ">type(\"" + property.getName() + "\", " + fieldBuilder.generatedTypeName + ".instance())";
		}

		if (property.hasGetter()) {
			statement += getterMethod(property);
		}
		if (property.hasSetter()) {
			statement += setterMethod(property);
		}
		if (property.hasWither()) {
			statement += witherMethod(property);
		}

		for (AnnotationModel annotation : property.getAnnotations()) {
			if (annotation.matches(Id.class)) {
				statement += ".annotatedWithAtId()";
			} else {
				statement += ".annotation(" + annotationFrom(annotation) + ")";
			}
		}

		fileBuilder.fieldStatement(statement);
	}

	private String annotationFrom(AnnotationModel model) {

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

	private String getterMethod(PropertyModel property) {
		return String.format(".getter(%s::%s)", property.getOwnerTypeName(), property.getGetter().getName());
	}

	private String setterMethod(PropertyModel property) {
		return String.format(".setter(%s::%s)", property.getOwnerTypeName(), property.getSetter().getName());
	}

	private String witherMethod(PropertyModel property) {
		return String.format(".wither(%s::%s)", property.getOwnerTypeName(), property.getWither().getName());
	}

	JavaFileBuilder getFileBuilder(TypeModel type) {
		return fileBuilders.computeIfAbsent(type.getType(), it -> new JavaFileBuilder(type));
	}


	static class JavaFileBuilder {

		private static final String TYPE_POSTFIX = "DomainTypeInformation";

		private TypeModel typeModel;
		private String packageName;
		private String generatedTypeName;
		private String simpleTypeName;

		private String initSuper;
		private String newDomainTypeInstanceStatement;
		private List<String> fieldStatements;
		private Map<String, String> methodStatements;
		private Map<String, String> annotationStatements;

		JavaFileBuilder(TypeModel typeModel) {

			this.typeModel = typeModel;
			this.generatedTypeName = computeGeneratedTypeName(typeModel.getType());
			this.packageName = typeModel.getType().getPackage().getName();
			this.simpleTypeName = generatedTypeName.replace(packageName + ".", "");
			this.fieldStatements = new ArrayList<>();
			this.methodStatements = new LinkedHashMap<>();
			this.annotationStatements = new LinkedHashMap<>();

			superCall();
		}

		JavaFileBuilder superCall() {

			Class<?> type = typeModel.getType();
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
					.superclass(ParameterizedTypeName.get(ClassName.get(DomainTypeInformation.class),
							ClassName.get(typeModel.getType())));

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
					constructorMethod.addStatement("setConstructor(" + newDomainTypeInstanceStatement + ")");
				}

				// FIELD INIT
				{
					for (String statement : fieldStatements) {
						constructorMethod.addStatement("addField(" + statement + ")", Field.class);
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
