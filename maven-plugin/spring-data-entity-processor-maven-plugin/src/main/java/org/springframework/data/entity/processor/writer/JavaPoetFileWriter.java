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
package org.springframework.data.entity.processor.writer;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.squareup.javapoet.*;
import com.squareup.javapoet.CodeBlock.Builder;
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
import org.springframework.data.mapping.model.Field;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.lang.Nullable;

/**
 * @author Christoph Strobl
 * @since 2020/11
 */
public class JavaPoetFileWriter implements ConfigurableTypeWriter, GraalVmConfigurationWriter {

	@Override
	public void writeConfigurableTypes(DomainTypes domainTypes, @Nullable File targetDirectory) throws IOException {

		for (TypeInfo typeInfo : domainTypes) {

			JavaFile file = typeInfoToConfigurableTypeInformation(typeInfo, domainTypes);
			if (targetDirectory == null) {
				System.out.println(file.toString());
			} else {
				file.writeTo(targetDirectory);
			}
		}
	}

	@Override
	public void writeGraalVmConfiguration(DomainTypes domainTypes, File targetDirectory) throws IOException {

		TypeName wildcard = WildcardTypeName.subtypeOf(Object.class);

		ParameterizedTypeName mapKeyType = ParameterizedTypeName.get(ClassName.get(Class.class), wildcard);
		ParameterizedTypeName mapValueType = ParameterizedTypeName.get(ClassName.get(ClassTypeInformation.class), wildcard);

		ParameterizedTypeName cacheType = ParameterizedTypeName.get(ClassName.get(Map.class), mapKeyType, mapValueType);

		MethodSpec.Builder fromMethod = MethodSpec.methodBuilder("from")
				.addModifiers(Modifier.PUBLIC, Modifier.STATIC)
				.addAnnotation(ClassName.get("com.oracle.svm.core.annotate", "Substitute"))
				.addTypeVariable(TypeVariableName.get("S"))
				.returns(ParameterizedTypeName.get(ClassName.get(ClassTypeInformation.class), TypeVariableName.get("S")))
				.addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("S")), "type");

		for (TypeInfo typeInfo : domainTypes) {

			fromMethod.beginControlFlow("if(type == " + typeInfo.getType().getCanonicalName() + ".class)");
			fromMethod.addStatement("return ($T<S>) " + typeInfo.getSignature().getCanonicalConfigurableTypeName() + ".instance()", org.springframework.data.util.ClassTypeInformation.class);
			fromMethod.endControlFlow();
		}

		fromMethod.addStatement("return ($T<S>) cache.computeIfAbsent(type, $T::new)", org.springframework.data.util.ClassTypeInformation.class, org.springframework.data.util.ClassTypeInformation.class);

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

		if (targetDirectory == null) {
			System.out.println(file.toString());
		} else {
			file.writeTo(targetDirectory);
		}
	}

	JavaFile typeInfoToConfigurableTypeInformation(TypeInfo typeInfo, DomainTypes domainTypes) {

		TypeSpec typeSpec = computeTypeSpec(typeInfo, domainTypes);
		return JavaFile.builder(typeInfo.getSignature().getPackageName(), typeSpec).build();
	}

	TypeSpec computeTypeSpec(TypeInfo typeInfo, DomainTypes domainTypes) {

		ClassName className = ClassName.get(typeInfo.getSignature().getPackageName(), typeInfo.getSignature().getSimpleConfigurableTypeName());

		TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className)
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
				constructorMethod.addStatement(superCallBlock(typeInfo));
				constructorMethod.addCode("\n");
			}

			// ANNOTATIONS
			{
				constructorMethod.addComment("ANNOTATIONS");
				for (AnnotationInfo annotation : typeInfo.getAnnotations()) {
					constructorMethod.addStatement("addAnnotation($L)", newAnnotationBlock(annotation));
				}
				constructorMethod.addCode("\n");
			}

			// PERSISTENCE CONSTRUCTOR
			{
				constructorMethod.addComment("PERSISTENCE CONSTRUCTOR");
				constructorMethod.addStatement(CodeBlock.builder()
						.add("setConstructor(")
						.add(newInstanceBlock(typeInfo.getConstructor()))
						.add(")")
						.build());
				constructorMethod.addCode("\n");
			}

			// FIELD INIT
			{
				constructorMethod.addComment("FIELDS");
				constructorMethod.addCode("\n");

				for (PropertyInfo propertyInfo : typeInfo) {
					constructorMethod.addComment("$L $L", propertyInfo.getTypeSignature().getJavaSignatureString(), propertyInfo.getName());
					constructorMethod.addCode(fieldBlock(propertyInfo, domainTypes));
					constructorMethod.addStatement("addField($L)", propertyInfo.getName());
					constructorMethod.addCode("\n");
				}
			}

			typeSpecBuilder.addMethod(constructorMethod.build());
		}

		return typeSpecBuilder.build();
	}

	CodeBlock superCallBlock(TypeInfo typeInfo) {

		Class<?> type = typeInfo.getType();
		if (type.getSuperclass() == Object.class || type.getSuperclass() == null) {
			return CodeBlock.of("super($T.class)", type);
		}

		return CodeBlock.of("super($T.class, $L.instance())", type, typeInfo.getSignature().getCanonicalSuperConfigurableTypeName());
	}


	CodeBlock newInstanceBlock(ConstructorInfo ctorInfo) {

		Builder builder = CodeBlock.builder();
		{
			builder.add("$T.<$T>", ConfigurableTypeConstructor.class, ctorInfo.getType());
			if (ctorInfo.isNoArgConstructor()) {
				builder.add("noArgsConstructor($T::new)", ctorInfo.getType());
			} else {

				builder.add("builder()");
				Builder args = CodeBlock.builder();
				{
					args.add(".args(");
					args.add(ctorInfo.getParameterList().stream().map(it -> "\"" + it.getName() + "\"").collect(Collectors.joining(", ")));
					args.add(")");
				}

				Builder fkt = CodeBlock.builder();
				{
					fkt.add(".newInstanceFunction((args) -> new $T(", ctorInfo.getType());
					for (int i = 0; i < ctorInfo.getParameterList().size(); i++) {

						ParameterInfo parameter = ctorInfo.getParameterList().get(i);
						Builder ctorArg = CodeBlock.builder();
						ctorArg.add("($T)args[$L]", parameter.getType(), i);
						if (i != ctorInfo.getParameterList().size() - 1) {
							ctorArg.add(", ");
						}
						fkt.add(ctorArg.build());
					}
					fkt.add("))");
				}
				builder.add(args.build());
				builder.add(fkt.build());
			}
		}
		return builder.build();
	}


	CodeBlock fieldBlock(PropertyInfo propertyInfo, DomainTypes domainTypes) {
		Builder builder = CodeBlock.builder();

		builder.addStatement(fieldVariableBlock(propertyInfo, domainTypes));
		if (propertyInfo.hasAccessorMethods()) {
			builder.add(accessorMethodsBlock(propertyInfo));
		}
		builder.add(fieldAnnotations(propertyInfo));
		return builder.build();
	}

	CodeBlock fieldVariableBlock(PropertyInfo propertyInfo, DomainTypes domainTypes) {

		return CodeBlock.builder()
				.add("$T<$T,$L>", Field.class, propertyInfo.getOwner().getType(), propertyInfo.getTypeSignature().getJavaSignatureString())
				.add(" $L = ", propertyInfo.getName())
				.add("$T.type($S, $L)", Field.class, propertyInfo.getName(), propertyInfo.getTypeSignature().getConfigurableTypeSignatureString(domainTypes))
				.build();
	}

	CodeBlock accessorMethodsBlock(PropertyInfo propertyInfo) {

		Builder accessorMethods = CodeBlock.builder();
		if (propertyInfo.hasGetter()) {
			accessorMethods.addStatement(getterMethodBlock(propertyInfo));
		}
		if (propertyInfo.hasSetter()) {
			accessorMethods.addStatement(setterMethodBlock(propertyInfo));
		}
		if (propertyInfo.hasWither()) {
			accessorMethods.addStatement(witherMethodBlock(propertyInfo));
		}
		return accessorMethods.build();
	}

	CodeBlock getterMethodBlock(PropertyInfo propertyInfo) {

		return CodeBlock.builder()
				.add("$L.getter($T::$L)", propertyInfo.getName(), propertyInfo.getOwner().getType(), propertyInfo.getGetter().getName())
				.build();
	}

	CodeBlock setterMethodBlock(PropertyInfo propertyInfo) {

		return CodeBlock.builder()
				.add("$L.setter($T::$L)", propertyInfo.getName(), propertyInfo.getOwner().getType(), propertyInfo.getSetter().getName())
				.build();
	}

	CodeBlock witherMethodBlock(PropertyInfo propertyInfo) {

		return CodeBlock.builder()
				.add("$L.wither($T::$L)", propertyInfo.getName(), propertyInfo.getOwner().getType(), propertyInfo.getWither().getName())
				.build();
	}

	CodeBlock fieldAnnotations(PropertyInfo propertyInfo) {

		Builder annotations = CodeBlock.builder();
		for (AnnotationInfo annotation : propertyInfo.getAnnotations()) {
			annotations.addStatement(fieldAnnotationBlock(propertyInfo, annotation));
		}
		return annotations.build();
	}

	CodeBlock fieldAnnotationBlock(PropertyInfo propertyInfo, AnnotationInfo annotationInfo) {

		if (annotationInfo.matches(Id.class)) {
			return CodeBlock.of("$L.annotatedWithAtId()", propertyInfo.getName());
		}

		return CodeBlock.builder()
				.add("$L.annotation(", propertyInfo.getName())
				.add(newAnnotationBlock(annotationInfo))
				.add(")")
				.build();
	}

	CodeBlock newAnnotationBlock(AnnotationInfo annotationInfo) {
		return CodeBlock.builder()
				.add("new $T() {", annotationInfo.getAnnotation())
				.add(annotationValuesBlock(annotationInfo))
				.add("}")
				.build();
	}

	CodeBlock annotationValuesBlock(AnnotationInfo annotationInfo) {

		Builder annotation = CodeBlock.builder();
		annotation.add("public Class<? extends $T> annotationType() { return $T.class; }", Annotation.class, annotationInfo.getAnnotation());
		for (Entry<String, Object> entry : annotationInfo.getArguments().entrySet()) {

			Object value;
			Method method = annotationInfo.getMethod(entry.getKey());
			if (BeanUtils.isSimpleProperty(entry.getValue().getClass())) {

				if (entry.getValue() instanceof Enum) {
					value = entry.getValue().getClass().getSimpleName() + "." + entry.getValue();
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

			annotation.add("public $T $L() { return $L; }", method.getReturnType(), entry.getKey(), value);
		}

		return annotation.build();
	}
}
