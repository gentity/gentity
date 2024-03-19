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
package com.github.gentity.core;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JType;
import com.sun.codemodel.JVar;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

/**
 *
 * @author count
 */
class AccessorGenerator {
	
	private final JCodeModel cm;
	private final boolean mutualUpdateEnabled;
	private static final String BUILDER_INSTANCE_NAME = "instance";
	private static final String BUILD_METHOD_NAME = "build";
	private static final String INIT_INSTANCE_METHOD_NAME = "_initInstance";
	private static final String BUILD_WITH_ID_METHOD_NAME = "buildWithId";
	private static final String BUILDER_IMPL_CLASS_NAME = "Builder";
	private static final String BUILDER_FACTORY_METHOD_NAME = "builder";
	
	private final Set<JType> entityAccociationAnnotationsSet;
	private final Set<JType> collectionAnnotationsSet;
	
	enum FieldKind {
		NON_RELATIONAL,
		ENTITY_ASSOCIATION,
		COLLECTION
	};
	
	public AccessorGenerator(JCodeModel cm, boolean mutualUpdateEnabled) {
		this.cm = cm;
		this.mutualUpdateEnabled = mutualUpdateEnabled;
		
		// set of annotations marking an entity relation field
		entityAccociationAnnotationsSet = Arrays.asList(
			OneToOne.class, OneToMany.class, ManyToOne.class, ManyToMany.class
		).stream()
			.map(cm::_ref)
			.collect(Collectors.toSet());
		
		// set of annotations marking a collection field
		collectionAnnotationsSet = Collections.singleton(cm._ref(ElementCollection.class));
	}
	
	private boolean equalsClass(JClass jclass, Class clazz) {
		return jclass._package().name().equals(clazz.getPackage().getName())
			&& jclass.name().equals(clazz.getSimpleName());
	}
	
	Optional<JAnnotationUse> annotationUseFor(JVar jvar, Class<? extends Annotation> annotation) {
		return jvar.annotations().stream()
			.filter(au -> equalsClass(au.getAnnotationClass(), annotation))
			.findAny();
	}
	
	private FieldKind determineFieldKind(JFieldVar jvar) {
		Set<JClass> annotationClasses = jvar.annotations().stream()
			.map(au -> au.getAnnotationClass())
			.collect(Collectors.toSet());
		
		if(annotationClasses.removeAll(collectionAnnotationsSet)) {
			return FieldKind.COLLECTION;
		}
		if(annotationClasses.removeAll(entityAccociationAnnotationsSet)) {
			return FieldKind.ENTITY_ASSOCIATION;
		}
		return FieldKind.NON_RELATIONAL;
	}
	
	void generateAccessors(JDefinedClass cls) {
		
		JDefinedClass builderClass = genBuilderClass(cls);
		
		List<JFieldVar> fixedPrimaryKeyFields = new ArrayList<>();
		
		for(JFieldVar field : cls.fields().values()) {
			if(excludeFieldFromAccessors(field)) {
				continue;
			}
			FieldKind fkind = determineFieldKind(field);
			if(!mutualUpdateEnabled || fkind == FieldKind.NON_RELATIONAL || fkind == FieldKind.COLLECTION) {
				genGetter(cls, field);

				if(!isGeneratedValueColumnField(field)) {
					if(annotationUseFor(field, Id.class).isPresent()) {
						// we collect fields mapped to fixed (non-generated) primary keys
						// so we can create constructor args from them later
						fixedPrimaryKeyFields.add(field);
					} else {
						genSetter(cls, field);
					}
				}
			} else {
				// we only generate the association setter for the owning
				// side
				genAssociationGetter(cls, field);
				if(isOwningSideOfSingleValuedAssociation(field)) {
					if(annotationUseFor(field, Id.class).isPresent()) {
						fixedPrimaryKeyFields.add(field);
					} else {
						genSingleValuedAssociationSetter(cls, field);
					}
				}
			}
		}
		
		genConstructor(cls, fixedPrimaryKeyFields);
		
		JMethod initInstanceMethod = generateBuilderBody(cls, builderClass);
		
		genCreateBuilderMethod(cls, builderClass, initInstanceMethod, fixedPrimaryKeyFields);
	}
	
	private boolean isOwningSideOfSingleValuedAssociation(JFieldVar field) {
		JAnnotationUse au = getEntityAssociationAnnotationUse(field);
		// we only generate the association getter for the owning
		// side
		boolean owningSide = null == au.getAnnotationMembers().get("mappedBy");
		boolean multiValued = cm.ref(Collection.class).isAssignableFrom((JClass)field.type());
		return owningSide && !multiValued;
	}
		
	private JAnnotationUse getEntityAssociationAnnotationUse(JFieldVar field) {
		for(JAnnotationUse au : field.annotations()) {
			
			if(entityAccociationAnnotationsSet.contains(au.getAnnotationClass())) {
				return au;
			}
		}
		throw new RuntimeException("No entity association found, but there must be one...");
	}
	
	private JDefinedClass findEntityBaseClass(JDefinedClass cls) {
		// find direct base entity class for class, if any
		return Optional.of(cls._extends())
			.filter(c -> c instanceof JDefinedClass)
			.map(c -> (JDefinedClass)c)
			.filter(c -> c.annotations().stream().anyMatch(au -> au.getAnnotationClass().equals(cm.ref(Entity.class))))
			.orElse(null);
	}
	
	private JMethod generateBuilderBody(JDefinedClass cls, JDefinedClass builderClass) {
		
		JMethod initInstanceMethod = builderClass.method(JMod.PROTECTED, cls, INIT_INSTANCE_METHOD_NAME);
		JVar initInstanceParam = initInstanceMethod.param(cls, BUILDER_INSTANCE_NAME);
		
		generateBuilderBodyImpl(cls, builderClass, initInstanceMethod);
		
		initInstanceMethod.body()._return(initInstanceParam);
		
		return initInstanceMethod;
	}
	
	private void generateBuilderBodyImpl(JDefinedClass cls, JDefinedClass builderClass, JMethod initInstanceMethod) {
		JBlock initInstanceBody = initInstanceMethod.body();
		JVar initInstanceParam = initInstanceMethod.params().get(0);
		
		for(JFieldVar field : cls.fields().values()) {
			if(excludeFieldFromAccessors(field) || annotationUseFor(field, Id.class).isPresent()) {
				continue;
			}
			
			JFieldVar builderField = builderClass.field(JMod.PRIVATE, field.type(), field.name());

			genBuilderMethod(builderClass, field, (body,param) -> {
				body.assign(JExpr._this().ref(builderField), param);
			});
			
			FieldKind fkind = determineFieldKind(field);
			if(!mutualUpdateEnabled || fkind!=FieldKind.ENTITY_ASSOCIATION) {
				initInstanceBody.assign(initInstanceParam.ref(field), JExpr._this().ref(builderField));
			} else if (isOwningSideOfSingleValuedAssociation(field)){
				initInstanceBody.directStatement(BUILDER_INSTANCE_NAME + ".relationTo$" + field.name() + ".set("+BUILDER_INSTANCE_NAME+", this." + builderField.name() + ");");
			}
		}
		
		cls = findEntityBaseClass(cls);
		if(cls != null) {
			generateBuilderBodyImpl(cls, builderClass, initInstanceMethod);
		}
	}
		
	
	private String initialUppercaseOf(String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}
	
	void genGetter(JDefinedClass cls, JFieldVar field) {
		genGetterImpl(cls, field, field);
	}
	
	void genAssociationGetter(JDefinedClass cls, JFieldVar field) {
		JExpression expr = JExpr.direct("relationTo$" + field.name() + ".get(this)");
		genGetterImpl(cls, field, expr);
	}
	
	void genGetterImpl(JDefinedClass cls, JFieldVar field, JExpression returnExpr) {
		cls.method(JMod.PUBLIC, field.type(), "get" + initialUppercaseOf(field.name()))
			.body()
			._return(returnExpr);
	}
	
	void genSetterImpl(JDefinedClass cls, JFieldVar field, BiConsumer<JBlock,JVar> visitor) {
		JMethod m = cls.method(JMod.PUBLIC, cm.VOID, "set" + initialUppercaseOf(field.name()));
		JVar param = m.param(field.type(), field.name());
		visitor.accept(m.body(), param);
	}
	
	void genSetter(JDefinedClass cls, JFieldVar field) {
		genSetterImpl(cls, field, (body,param) -> {
			// body simply sets the field
			body.assign(JExpr._this().ref(field), param);
		});
	}
	
	void genSingleValuedAssociationSetter(JDefinedClass cls, JFieldVar field) {
		genSetterImpl(cls, field, (body,param) -> {
			body.directStatement("relationTo$" + field.name() + ".set(this, " + param.name() + ");");
		});
	}
	
	private JMethod superconstructor(JDefinedClass cls) {
		JClass supercls = cls._extends();
		if(!(supercls instanceof JDefinedClass)) {
			// we cannot look inside classes that we didn't define, therefore
			// we simply assume they have a no-args constructor
			return null;
		}
		
		JDefinedClass _super = (JDefinedClass)supercls;
		
		// we only defined one constructor, so we expect that one
		return _super.constructors().next();
	}
	
	private void genConstructor(JDefinedClass cls, List<JFieldVar> fieldsToInitialize) {
		JMethod ctor = cls.constructor(JMod.PUBLIC);
		
		JBlock body = ctor.body();
		JMethod sctor = superconstructor(cls);
		if(sctor != null) {
			if(!sctor.params().isEmpty()) {
				// call super constructor and pass on params that we collect
				// for it in this constructor
				JInvocation superInvocation = JExpr.invoke("super");
				body.add(superInvocation);
				
				for(JVar v : sctor.params()) {
					JVar param = ctor.param(v.type(), v.name());
					superInvocation.arg(param);
				}
			}
		}
		for(JFieldVar f : fieldsToInitialize) {
			JVar param = ctor.param(f.type(), f.name());
			String relationToFieldName = "relationTo$"+f.name();
			if(cls.fields().containsKey(relationToFieldName)) {
				body.directStatement(relationToFieldName + ".set(this, " + param.name() + ");");
			} else {
				body.assign(JExpr._this().ref(f), param);
			}
		}
		if(!ctor.params().isEmpty()) {
			// in case we just generated a constructor with arguments, also
			// generate one without arguments that is protected to meet JPA's
			// requirement to have a public or private no-arg constructor
			cls.constructor(JMod.PROTECTED);
		}
	}
	
	private void genBuilderMethod(JDefinedClass builderClass, JFieldVar field, BiConsumer<JBlock, JVar> assignmentImplementor) {
		
		// add method to builder implementation
		JMethod m = builderClass.method(JMod.PUBLIC, builderClass, field.name());
		JVar p = m.param(field.type(), field.name());
		JBlock body = m.body();
		assignmentImplementor.accept(body, p);
		body._return(JExpr._this());
		
	}
	
	private void genCreateBuilderMethod(JDefinedClass cls, JDefinedClass builderClass, JMethod initMethod, List<JFieldVar> mandatoryInitializedFields) {
		JMethod builderMethod = builderClass.method(JMod.PUBLIC, cls, "dummy");
		List<JVar> params = new ArrayList<>();
		for(JFieldVar field : mandatoryInitializedFields) {
			JVar param = builderMethod.param(field.type(), field.name());
			params.add(param);
		}
		
		JMethod sctor = superconstructor(cls);
		if(sctor != null) {
			for(JVar sparam: sctor.params()) {
				JVar param = builderMethod.param(sparam.type(), sparam.name());
				params.add(param);
			}
		}
		
		if(builderMethod.params().isEmpty()) {
			builderMethod.name(BUILD_METHOD_NAME);
		} else {
			builderMethod.name(BUILD_WITH_ID_METHOD_NAME);
		}
		
		JInvocation newexp = JExpr._new(cls);
		for(JVar p : params) {
			newexp.arg(p);
		}
		builderMethod
			.body()
			._return(JExpr._this().invoke(initMethod).arg(newexp));
	}
	
	private JDefinedClass genBuilderClass(JDefinedClass cls) {
		JDefinedClass builderClass;
		try {
			builderClass = cls._class(JMod.PUBLIC|JMod.STATIC, BUILDER_IMPL_CLASS_NAME);
		} catch (JClassAlreadyExistsException ex) {
			throw new RuntimeException(ex);
		}
		builderClass.field(JMod.PRIVATE, cls, BUILDER_INSTANCE_NAME);
		Optional<JDefinedClass> entitySuperclass = Optional.ofNullable(cls._extends())
			.filter(c -> c instanceof JDefinedClass)
			.map(JDefinedClass.class::cast)
			.filter(c -> c.annotations().stream().anyMatch(au -> au.getAnnotationClass().equals(cm.ref(Entity.class))))
			;
		
		if(entitySuperclass.isPresent()) {
			Iterable<JDefinedClass> nestedClassesIt = () -> entitySuperclass.get().classes();
			Optional<JDefinedClass> builderSuperClass = StreamSupport.stream(nestedClassesIt.spliterator(), false)
				.filter(nc -> BUILDER_IMPL_CLASS_NAME.equals(nc.name()))
				.findAny();
			
			if(builderSuperClass.isPresent()) {
				builderClass._extends(builderSuperClass.get());
			}
		}
		cls.method(JMod.PUBLIC|JMod.STATIC, builderClass, BUILDER_FACTORY_METHOD_NAME)
			.body()
			._return(JExpr._new(builderClass));
		
		return builderClass;
	}

	private boolean isGeneratedValueColumnField(JFieldVar field) {
		return annotationUseFor(field, GeneratedValue.class)
			.isPresent();
	}
	
	private boolean excludeFieldFromAccessors(JFieldVar field) {
		return 0 != (field.mods().getValue() & (JMod.STATIC | JMod.TRANSIENT));
	}
}
