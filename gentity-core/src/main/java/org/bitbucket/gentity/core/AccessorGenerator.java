/*
 * Copyright 2018 The Gentity Project. All rights reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bitbucket.gentity.core;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JVar;
import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 *
 * @author count
 */
class AccessorGenerator {
	
	private final JCodeModel cm;
	private static final String BUILDER_INSTANCE_NAME = "instance";
	private static final String BUILD_METHOD_NAME = "build";
	private static final String BUILDER_INTERFACE_NAME = "Builder";
	private static final String BUILDER_IMPL_CLASS_NAME = "BuilderImpl";
	private static final String BUILDER_FACTORY_METHOD_NAME = "builder";

	public AccessorGenerator(JCodeModel cm) {
		this.cm = cm;
	}
	
	private static class BuilderSet{
		final JDefinedClass builderInterface;
		final JDefinedClass builderImplClass;

		public BuilderSet(JDefinedClass builderInterface, JDefinedClass builderImplClass) {
			this.builderInterface = builderInterface;
			this.builderImplClass = builderImplClass;
		}
		
	}
	
	Optional<JAnnotationUse> annotationUseFor(JVar jvar, Class<? extends Annotation> annotation) {
		return jvar.annotations().stream()
			.filter(au -> {
				return au.getAnnotationClass()._package().name().equals(annotation.getPackage().getName())
					&& au.getAnnotationClass().name().equals(annotation.getSimpleName());
			})
			.findAny();
	}
	
	void generateAccessors(JDefinedClass cls) {
		
		JDefinedClass builderClass = genBuilderClass(cls);
				
		for(JFieldVar field : cls.fields().values()) {
			
			genGetter(cls, field);
			
			genSetter(cls, field);
			
			genBuilderMethod(builderClass, field);
		}
	}
	
	String initialUppercaseOf(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
	
	void genGetter(JDefinedClass cls, JFieldVar field) {
		cls.method(JMod.PUBLIC, field.type(), "get" + initialUppercaseOf(field.name()))
			.body()
			._return(field);
	}
	
	void genSetter(JDefinedClass cls, JFieldVar field) {
		JMethod m = cls.method(JMod.PUBLIC, cm.VOID, "set" + initialUppercaseOf(field.name()));
		JVar param = m.param(field.type(), field.name());
		m.body()
			.assign(JExpr._this().ref(field), param);
	}

	private void genBuilderMethod(JDefinedClass builderClass, JFieldVar field) {
		
		// add method to builder implementation
		JFieldVar builderInstanceField = builderClass.fields().get(BUILDER_INSTANCE_NAME);
		JMethod m = builderClass.method(JMod.PUBLIC, builderClass, field.name());
		JVar p = m.param(field.type(), field.name());
		JBlock body = m.body();
		body.assign(builderInstanceField.ref(field), p);
		body._return(JExpr._this());
		
	}

	private JDefinedClass genBuilderClass(JDefinedClass cls) {
		JDefinedClass builderClass;
		try {
			builderClass = cls._class(JMod.PUBLIC|JMod.STATIC, BUILDER_IMPL_CLASS_NAME);
		} catch (JClassAlreadyExistsException ex) {
			throw new RuntimeException(ex);
		}
		JFieldVar builderInstanceField = builderClass.field(JMod.PRIVATE|JMod.FINAL, cls, BUILDER_INSTANCE_NAME);
		builderInstanceField.init(JExpr._new(cls));
		builderClass.method(JMod.PUBLIC, cls, BUILD_METHOD_NAME)
			.body()
			._return(builderInstanceField);
		cls.method(JMod.PUBLIC|JMod.STATIC, builderClass, BUILDER_FACTORY_METHOD_NAME)
			.body()
			._return(JExpr._new(builderClass));
		
		return builderClass;
	}
	
}
