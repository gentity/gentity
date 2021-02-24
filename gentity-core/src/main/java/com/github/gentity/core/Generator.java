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

import com.github.gentity.core.entities.CollectionTableDecl;
import com.github.gentity.core.entities.EntityInfo;
import com.github.gentity.core.fields.FieldColumnSource;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.SequenceGenerators;
import javax.persistence.Table;
import com.github.gentity.core.config.dto.ConfigurationDto;
import com.github.gentity.core.config.dto.MappingConfigDto;
import com.github.gentity.ToManySide;
import com.github.gentity.ToOneSide;
import static com.github.gentity.core.Cardinality.*;
import static com.github.gentity.core.Directionality.*;
import static com.github.gentity.core.ChildTableRelation.Kind.*;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import static com.github.gentity.core.ChildTableRelation.Kind.MANY_TO_ONE;
import com.github.gentity.core.entities.JoinedRootEntityInfo;
import com.github.gentity.core.entities.JoinedSubEntityInfo;
import com.github.gentity.core.entities.MappingInfo;
import com.github.gentity.core.entities.PlainEntityInfo;
import com.github.gentity.core.entities.RootEntityInfo;
import com.github.gentity.core.entities.SingleTableRootEntityInfo;
import com.github.gentity.core.entities.SingleTableSubEntityInfo;
import com.github.gentity.core.entities.SubEntityInfo;
import com.github.gentity.core.fields.FieldMapping;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.ForeignKeyModel.Mapping;
import com.github.gentity.core.model.ModelReader;
import com.github.gentity.core.model.PrimaryKeyModel;
import com.github.gentity.core.model.SequenceModel;
import com.github.gentity.core.model.TableModel;
import com.github.gentity.core.util.Tuple;
import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassContainer;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JInvocation;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JVar;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.Lob;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.IdClass;
import javax.persistence.PrePersist;
import javax.persistence.PreRemove;


/**
 *
 * @author upachler
 */
public class Generator {
	
	private final SchemaModel sm;
	
	JCodeModel cm;
	List<JType> numericTypes;
	Map<String, JDefinedClass> tablesToEntities;
	Map<JDefinedClass, EntityInfo> entities;
	Map<String, JDefinedClass> tablesToEmbeddables;
	Map<JDefinedClass, CollectionTableDecl> embeddables;
	
	private static final String IDCLASS_NAME = "Id";
	private final EntityRefFactory LIST_ENTITY_REF_FACTORY = new EntityRefFactory() {
		@Override
		public JExpression createInitExpression() {
			return JExpr._new(cm._ref(ArrayList.class));
		}
		@Override
		public JClass getCollectionType(JType elementType) {
			return cm.ref(List.class)
				.narrow(elementType);
		}
	};
	
	private final Pattern FK_COL_NAME_PATTERN = Pattern.compile("(.*)_(.+)");

	private NameProvider nameProvider;
	private final MappingConfigDto cfg;
	private final ShellLogger logger;
	
	public Generator(MappingConfigDto cfg, ModelReader reader, ShellLogger logger) throws IOException {
		sm = new SchemaModelImpl(cfg, reader, logger);
		this.cfg = cfg;
		this.logger = logger;
	}
	
	private boolean isAutomaticBidirectionalUpdateEnabled() {
		return Optional.ofNullable(cfg.getConfiguration())
			.map(c -> c.isAutomaticBidirectionalUpdate())
			.orElse(true);
	}
	
	JCodeModel generate() {
		if(cm == null) {
			cm = new JCodeModel();
			// all subclasses of Number, as of Java 7 (and most likely all
			// future Java versions to come...
			numericTypes = Arrays.asList(
				cm.ref(AtomicInteger.class),
				cm.ref(AtomicLong.class),
				cm.ref(BigDecimal.class),
				cm.ref(BigInteger.class),
				cm.ref(Byte.class),
				cm.ref(Double.class),
				cm.ref(Float.class),
				cm.ref(Integer.class),
				cm.ref(Long.class),
				cm.ref(Short.class),
				cm.BYTE,
				cm.SHORT,
				cm.INT,
				cm.LONG,
				cm.FLOAT,
				cm.DOUBLE
			);
		}
		JPackage p = cm._package(sm.getTargetPackageName());
		
		AccessorGenerator accessorGenerator = new AccessorGenerator(cm, isAutomaticBidirectionalUpdateEnabled());
		nameProvider = new NameProvider();
		
		try {
			tablesToEntities = new HashMap<>();
			entities = new HashMap<>();
			tablesToEmbeddables = new HashMap<>();
			embeddables = new HashMap<>();
			
			// generate entities first that are part of a hierarchy
			for(EntityInfo et : sm.getRootEntityDefinitions()) {
				if(et instanceof JoinedRootEntityInfo) {
					genJoinedHierarchy((JoinedRootEntityInfo)et, p);
				} else if(et instanceof SingleTableRootEntityInfo) {
					genSingleTableHierarchy((SingleTableRootEntityInfo)et, p);
				} else {
					genPlainTable((PlainEntityInfo)et, p);
				}
			}
			
			// generate element collection embeddables
			genCollectionTableEmbeddables(sm.getRootEntityDefinitions());
			
			// for all entity-mapped tables, find entities where we need to
			// map sequences
			Set<String> mappedSequenceNames = new HashSet<>();
			for(Map.Entry<String, JDefinedClass> e : tablesToEntities.entrySet()) {
				String tableName = e.getKey();
				TableModel table = sm.toTable(tableName);
				
				JDefinedClass cls = e.getValue();
				if(table.getPrimaryKey() == null) {
					continue;	// no primary key -> no sequence
				}
				Map<String,SequenceModel> tableSequences = table.getPrimaryKey().stream()
					.filter(col -> col.getSequence() != null)
					.map(col -> col.getSequence())
					.filter(seq -> !mappedSequenceNames.contains(seq.getName()))
					.collect(Collectors.toMap(SequenceModel::getName, Function.identity()));
				
				mappedSequenceNames.addAll(tableSequences.keySet());
				if(tableSequences.size() == 1) {
					genSequence(cls.annotate(SequenceGenerator.class), tableSequences.values().iterator().next());
				} else if(tableSequences.size()>1) {
					JAnnotationArrayMember pa = cls.annotate(SequenceGenerators.class)
						.paramArray("value");
					
					for(SequenceModel s : tableSequences.values()) {
						JAnnotationUse au = pa.annotate(SequenceGenerator.class);
						genSequence(au, s);
					}
				}
			}
			
			// process table columns
			Stream.of(
				entities.entrySet().stream(),
				embeddables.entrySet().stream()
			)
				.flatMap(t -> t)
				.map(Tuple::of)
				.map(t -> t.mapY(MappingInfo::getFieldColumnSource))
				.forEach(t -> {
					FieldColumnSource src = t.y();
					JDefinedClass cls = t.x();

					filterBasicMappings(src.getFieldMappings())
						.forEach((m) -> genColumn(cls, m));
				});
			
			// add one-to-many relations
			for(ChildTableRelation ctr : sm.getChildTableRelations()) {
				
				genChildTableRelation(ctr);
			}
			
			// add many-to-many relations
			for(JoinTableRelation jtr : sm.getJoinTableRelations()) {
				JDefinedClass ownerClass = findEntity(jtr.getOwnerForeignKey().getTargetTable().getName(), jtr.getOwnerEntityName());
				JDefinedClass inverseClass = findEntity(jtr.getInverseForeignKey().getTargetTable().getName(), jtr.getInverseEntityName());
				
				genJoinTableRelation(ownerClass, inverseClass, jtr);
			}
			
			// generate accessors for embeddables
			tablesToEmbeddables.values().stream()
				.forEach(accessorGenerator::generateAccessors);
			
			// generate accessor methods, ordered by entity class hierarchies, roots first
			Set<JDefinedClass> processed = entities.keySet().stream()
				.filter(this::isClassHierarchyRoot)
				.collect(Collectors.toSet());
			processed.forEach(accessorGenerator::generateAccessors);
			
			Set<JDefinedClass> remaining = new HashSet<>(entities.keySet());
			remaining.removeAll(processed);
			
			while(!remaining.isEmpty()) {
				List<JDefinedClass> snapshot = new ArrayList<>(remaining);
				for(JDefinedClass cls : snapshot) {
					JClass ext = cls._extends();
					if(ext instanceof JDefinedClass && processed.contains((JDefinedClass)ext)) {
						remaining.remove(cls);
						processed.add(cls);
						accessorGenerator.generateAccessors(cls);
					}
				}
			}
				
			// generate IdClass anotations
			List<JDefinedClass> roots = entities.keySet().stream()
				.filter(this::isClassHierarchyRoot)
				.collect(Collectors.toList());
			
			List<JDefinedClass> generatedIdClasses = new ArrayList<>();
			for(JDefinedClass e : roots) {
				JClass idClass = genIdClassAnnotation(e, entities.get(e));
				if(idClass != null && idClass instanceof JDefinedClass) {
					generatedIdClasses.add((JDefinedClass)idClass);
				}
			}
			
			// generate class bodies for generated id classes - this needs
			// to be done after the IdClass annotation generation, because
			// empty id classes are also generated in the previous step that
			// we now depend on
			for(JDefinedClass i : generatedIdClasses) {
				genIdClassBody(i);
			}
			
		} catch(JClassAlreadyExistsException caex) {
			throw new RuntimeException(caex);
		}
		
		return cm;
	}
	
	private List<FieldMapping> filterBasicMappings(List<FieldMapping> mappings) {
		return mappings.stream()
			.filter((m) -> (!sm.isColumnIgnored(m.getTable().getName(), m.getColumn().getName())))
			.collect(Collectors.toList());
	}
	
	private void genCollectionTableEmbeddables(List<? extends EntityInfo> einfos) {
		for(EntityInfo<?> ei : einfos) {
			List<CollectionTableDecl> ctables = ei.getCollectionTables();
			
			if(!ctables.isEmpty()) {
				JDefinedClass cls = findEntity(ei);
				for(CollectionTableDecl c : ctables) {
					try {
						genEmbeddable(cls, c);
					} catch (JClassAlreadyExistsException ex) {
						throw new RuntimeException(ex);
					}
				}
			}
			genCollectionTableEmbeddables(ei.getChildren());
		}
	}
	
	boolean isClassHierarchyRoot(JDefinedClass cls) {
		// a class is a hierarchy root if its superclass..
		
		// it was not defined by this generator (not a JDefinedClass), or ...
		JClass ext = cls._extends();
		if(!(ext instanceof JDefinedClass)) {
			return true;
		}
		
		// ... has no @Entity annotation
		return !((JDefinedClass)ext).annotations().stream()
			.anyMatch(au -> au.getAnnotationClass().equals(cm.ref(Entity.class)));
	}
	
	private JDefinedClass genRootEntityClass(JPackage p, String nameCandidate, JDefinedClass superClassEntity, RootEntityInfo einfo) throws JClassAlreadyExistsException {
		JDefinedClass cls = genEntityClassImpl(p, nameCandidate, superClassEntity, einfo);
		
		return cls;
	}
	
	private JDefinedClass genSubEntityClass(JPackage p, String nameCandidate, JDefinedClass superClassEntity, SubEntityInfo einfo) throws JClassAlreadyExistsException {
		return genEntityClassImpl(p, nameCandidate, superClassEntity, einfo);
	}
	
	private JDefinedClass genMappedClassImpl(JPackage p, String nameCandidate, JDefinedClass superClassEntity, MappingInfo einfo) throws JClassAlreadyExistsException {
		JClass serializableClass = cm.ref(Serializable.class);
			
		JDefinedClass cls = p._class(JMod.PUBLIC, toClassName(p, nameCandidate));
		
		JClass effectiveSuperClass;
		if(einfo.getExtends() != null) {
			if(superClassEntity != null) {
				throw new IllegalArgumentException(String.format("class %s has a superclass entity %s in conflict with a declared superclass %s", cls.name(), superClassEntity.name(), einfo.getExtends()));
			}
			effectiveSuperClass = cm.ref(einfo.getExtends());
		} else if(sm.getDefaultExtends() != null && superClassEntity == null) {
			effectiveSuperClass = cm.ref(sm.getDefaultExtends());
		} else {
			effectiveSuperClass = superClassEntity;
		}
		
		if(effectiveSuperClass != null) {
			cls._extends(effectiveSuperClass);
		}
		
		List<String> effectiveSuperInterfaceNames;
		if(einfo.getImplements() != null) {
			effectiveSuperInterfaceNames = einfo.getImplements();
		} else {
			effectiveSuperInterfaceNames = sm.getDefaultImplements();
		}
		
		if(effectiveSuperInterfaceNames != null) {
			for(String ifname : effectiveSuperInterfaceNames) {
				cls._implements(cm.ref(ifname));
			}
		}
		
		cls._implements(serializableClass);
		return cls;
	}
	
	private JDefinedClass genEntityClassImpl(JPackage p, String nameCandidate, JDefinedClass superClassEntity, EntityInfo einfo) throws JClassAlreadyExistsException {
		JDefinedClass cls = genMappedClassImpl(p, nameCandidate, superClassEntity, einfo);
		
		cls.annotate(Entity.class);
		TableModel table = einfo.getTable();
		if(table != null) {
			cls.annotate(Table.class)
				.param("name", table.getName());
			tablesToEntities.put(table.getName(), cls);
		}
		entities.put(cls, einfo);
		
		return cls;
	}
	
	private void genEmbeddable(JDefinedClass enclosingEntityClass, CollectionTableDecl collectionTable) throws JClassAlreadyExistsException {
		TableModel table = collectionTable.getTable();
		ForeignKeyModel foreignKeyName = collectionTable.getForeignKey();
		JPackage p = cm._package(sm.getTargetPackageName());
		JClass serializableClass = cm.ref(Serializable.class);
		
		List<FieldMapping> mappings = filterBasicMappings(collectionTable.getFieldColumnSource().getFieldMappings());
		JType type;
		String fieldNameCandidate = null;
		boolean isSingleValueColumn = collectionTable.isBasicElementCollection();
		EnumType etype = null;
		if(isSingleValueColumn) {
			FieldMapping m = mappings.get(0);
			type = mapColumnType(m.getTable(), m.getColumn());
			if(m.getEnumType() != null) {
				etype = mapEnumType(type, m);
				type = cm.ref(m.getEnumType());
			}
			fieldNameCandidate = m.getFieldName();
		} else {
			JDefinedClass cls = genMappedClassImpl(p, table.getName(), null, collectionTable);
			cls.annotate(Embeddable.class);
			tablesToEmbeddables.put(table.getName(), cls);
			embeddables.put(cls, collectionTable);
		
			cls._implements(serializableClass);
			type = cls;
			fieldNameCandidate = cls.name();
		}
		
		JFieldVar field = genCollectionFieldVar(enclosingEntityClass, type, fieldNameCandidate);
		field.annotate(cm.ref(ElementCollection.class));
		JAnnotationUse ct = field.annotate(cm.ref(CollectionTable.class));
		ct.param("name", table.getName());
		if(isSingleValueColumn) {
			FieldMapping m = mappings.get(0);
			annotateBasicField(enclosingEntityClass, m, type, etype, field);
		}
		
		List<ForeignKeyModel.Mapping> fkCols = foreignKeyName.getColumnMappings();
		annotateJoinColumns(() -> ct.paramArray("joinColumns"), fkCols);
	}
	
	
	private void genSequence(JAnnotationUse au, SequenceModel sequence) {
		au.param("name", sequence.getName());
		Long start = sequence.getStartValue();
		boolean startIsDefault = (start == null || Long.valueOf(1).equals(start));
		if(!startIsDefault){
			au.param("start", start);
		}
	}
	
	private void genColumn(JDefinedClass cls, FieldMapping m) {
		String fieldNameCanditate = m.getFieldName();
		
		JType colType = mapColumnType(m.getTable(), m.getColumn());
		EnumType etype = null;
		if(m.getEnumType() != null) {
			etype = mapEnumType(colType, m);
			colType = cm.ref(m.getEnumType());
		}

		String fieldName = toFieldName(cls, fieldNameCanditate);
		JFieldVar field = cls.field(JMod.PROTECTED, colType, fieldName);
		
		annotateBasicField(cls, m, colType, etype, field);
	}
	
	private JClass genIdClassAnnotation(JDefinedClass cls, EntityInfo ei) throws JClassAlreadyExistsException {
		// IdClass can only be generated on root entities
		RootEntityInfo einfo = (RootEntityInfo)ei;
		
		String idClassFQCN = einfo.getIdClass();
		JClass idClass = null;
		if(idClassFQCN != null) {
			// if the 'idClass' attribute was set, assume that class to
			// exist and use it as IdClass
			idClass = cm.directClass(idClassFQCN);
		} else if(einfo.getTable().getPrimaryKey().size() > 1) {
			// if we have a composite primary key, but no 'idClass' was set
			// in the gentity file, we generate one
			JDefinedClass genIdClass = cls._class(JMod.PUBLIC | JMod.STATIC, IDCLASS_NAME);
			idClass = genIdClass;
		}
		
		if(idClass != null) {
			cls.annotate(IdClass.class)
				.param("value", idClass);
		}
		
		return idClass;
	}
	
	private void genIdClassBody(JDefinedClass genIdClass) {
		JDefinedClass cls = (JDefinedClass)genIdClass.outer();
		
		genIdClass._implements(Serializable.class);
		List<JFieldVar> idFields = new ArrayList<>();
		for(JFieldVar f : cls.fields().values()) {
			boolean hasIdAnnotation = f.annotations().stream()
				.anyMatch(a -> a.getAnnotationClass().equals(cm.ref(Id.class)));
			if(hasIdAnnotation) {

				boolean isAssociationField = f.annotations().stream()
					.map(a -> a.getAnnotationClass())
					.anyMatch(ac -> 
							ac.equals(cm.ref(OneToOne.class))
						||	ac.equals(cm.ref(OneToMany.class))
						||	ac.equals(cm.ref(ManyToOne.class))
						||	ac.equals(cm.ref(ManyToMany.class))
					);

				JType idFieldType;

				if(!isAssociationField) {
					idFieldType = f.type();
				} else {

					JDefinedClass targetClass;
					JClass fieldClass = (JClass)f.type();
					if(cm.ref(Map.class).isAssignableFrom(fieldClass)) {
						// Map has the target type as the second type argument
						targetClass = (JDefinedClass)fieldClass.getTypeParameters().get(1);
					} else if(cm.ref(Collection.class).isAssignableFrom((JClass)f.type())) {
						// the other collection types have it as the first type arg
						targetClass = (JDefinedClass)fieldClass.getTypeParameters().get(0);
					} else {
						targetClass = (JDefinedClass)f.type();
					}
					
					// find target's IdClass annotation, and deduce the
					// class that's declared - the @IdClass can be filled with
					// a generated id class or a custom defined one
					JClass targetIdClass = null;
					if(targetClass.annotations().stream()
						.anyMatch(a -> a.getAnnotationClass().equals(cm.ref(IdClass.class)))) {
						RootEntityInfo einfo = ((RootEntityInfo)entities.get(targetClass));
						if(einfo.getIdClass() != null) {
							targetIdClass = cm.ref(einfo.getIdClass());
						} else {
							targetIdClass = (JDefinedClass)Arrays.asList(targetClass.listClasses()).stream()
								.filter(c -> c.name().equals(IDCLASS_NAME))
								.findFirst()
								.get();
						}
					}

					if(targetIdClass != null) {
						idFieldType = targetIdClass;
					} else {
						// if we do not have an id class, we assume that
						// there is only one field representing the primary
						// key (i.e. one field only has the @Id annotation)
						List<JFieldVar> targetIdFields = targetClass.fields().values().stream()
							.filter(tf -> tf.annotations().stream()
								.anyMatch(a -> a.getAnnotationClass().equals(cm.ref(Id.class)))
							)
							.collect(Collectors.toList());
						assert targetIdFields.size() == 1;

						// the id field type in this case is the type of the
						// single id field in the target class
						idFieldType = targetIdFields.get(0).type();
					}
				}
				JFieldVar idField = genIdClass.field(JMod.PRIVATE, idFieldType, f.name());
				idFields.add(idField);
			}
		}

		// generate
		genIdClass.constructor(JMod.NONE);

		// generate field-initializing constructor
		genConstrutor(genIdClass, idFields);

		genEquals(genIdClass, idFields);
		genHashCode(genIdClass, idFields);
		genToString(genIdClass, idFields);
	}
	
	private JMethod genConstrutor(JDefinedClass cls, List<JFieldVar> fieldsToInitialze) {
		JMethod ctor = cls.constructor(JMod.PUBLIC);
		for(JFieldVar f : fieldsToInitialze) {
			JVar p = ctor.param(f.type(), f.name());
			ctor.body().assign(JExpr._this().ref(f), p);
		}
		return ctor;
	}
	
	private void genEquals(JDefinedClass cls, List<JFieldVar> equalsFields) {
		
		// mainly, the model for this was:
		// https://www.sitepoint.com/implement-javas-equals-method-correctly/
		
		JMethod m = cls.method(JMod.PUBLIC, cm.BOOLEAN, "equals");
		m.annotate(Override.class);
		JVar p = m.param(cm._ref(Object.class), "o");
		
		JBlock body = m.body();
		
		// if(o==this) return true;
		body._if(JExpr._this().eq(p))
			._then()._return(JExpr.TRUE);
		
		// if(o==null) return false
		body._if(JExpr._null().eq(p))
			._then()._return(JExpr.FALSE);
		
		// if(getClass() != o.getClass()) return false
		body._if(JExpr.invoke("getClass").ne(p.invoke("getClass")))
			._then()._return(JExpr.FALSE);
		
		if(equalsFields.isEmpty()) {
			// the unlikely case that there is nothing to compare, we''e done,
			// because we say that two empty same-class objects are equal.
			body._return(JExpr.TRUE);
		} else {
			// otherwise, do field-wise comparison..
			
			// cast to instance of class
			// <classname> other = (<classname>)o;
			JVar v = body.decl(cls, "other", JExpr.cast(cls, p));

			// now make Objects.equals() chain of comparison to return, like this:
			// return Objects.equals(field1, other.field1)
			//     && Objects.equals(field2, other.field2)
			//     ...
			//     ;
			JClass objCls = cm.ref(Objects.class);

			JExpression lhs = null;
			for(JFieldVar f : equalsFields) {
				JExpression rhs = 
					objCls.staticInvoke("equals")
						.arg(f)
						.arg(JExpr.ref(v, f));
				if(lhs == null) {
					lhs = rhs;
				} else {
					lhs = lhs.cand(rhs);
				}
			}
			body._return(lhs);
		}
	}
	
	private void genHashCode(JDefinedClass cls, List<JFieldVar> hashCodeFields) {
		// mainly, the model for this was:
		// https://www.sitepoint.com/how-to-implement-javas-hashcode-correctly/
		
		JMethod m = cls.method(JMod.PUBLIC, cm.INT, "hashCode");
		m.annotate(Override.class);
		JInvocation hashInvocation = cm.ref(Objects.class).staticInvoke("hash");
		for(JFieldVar f : hashCodeFields) {
			hashInvocation.arg(f);
		}
		m.body()._return(hashInvocation);
	}
	
	private void genToString(JDefinedClass cls, List<JFieldVar> toStringCodeFields) {
		JMethod m = cls.method(JMod.PUBLIC, cm.ref(String.class), "toString");
		m.annotate(Override.class);
		
		// generate class name to print
		String simpleName = "";
		JClassContainer container = cls.parentContainer();
		while(container instanceof JClass) {
			simpleName = ((JClass)container).name()+"$"+simpleName;
			container = container.parentContainer();
		}
		simpleName += cls.name();
		
		// generate toString() body, calling Objects.toString() on each field
		JClass objCls = cm.ref(Objects.class);
		JExpression exp = JExpr.lit(simpleName + "{");
		boolean first = true;
		for(JFieldVar f : toStringCodeFields) {
			String nextFieldString;
			if(!first) {
				nextFieldString = ",";
			} else {
				nextFieldString = "";
				first = false;
			}
			nextFieldString += f.name()+"=";
			exp = exp.plus(JExpr.lit(nextFieldString));
			exp = exp.plus(objCls.staticInvoke("toString").arg(f));
		}
		exp = exp.plus(JExpr.lit("}"));
		m.body()._return(exp);
	}
	
	private EnumType mapEnumType(JType colType, FieldMapping m) {
		if(m.getEnumType() != null) {
			
			if(colType == cm._ref(java.lang.String.class)) {
				return EnumType.STRING;
			} else if(colType == cm.INT || colType == cm.LONG || colType == cm.BYTE || colType == cm.SHORT) {
				return EnumType.ORDINAL;
			} else {
				throw new RuntimeException(String.format("column %s field definition with enumType of %s must be mappable to a Java String or integral type", m.getColumn(), m.getEnumType()));
			}
			
		} else {
			return null;
		}
		
	}
	
	private void annotateBasicField(JDefinedClass cls, FieldMapping m, JType colType, EnumType etype, JFieldVar field) {
		TableModel table = m.getTable();
		ColumnModel column = m.getColumn();
	
		if(etype != null) {
			field.annotate(Enumerated.class)
				.param("value", etype);
		}
		// @Id
		if(sm.isColumnPrimaryKey(table, column)) {
			field.annotate(Id.class);
			GenerationType strategy = sm.findPrimaryKeyColumnGenerationStrategy(table, column);
			if(strategy != null) {
				JAnnotationUse gv = field.annotate(GeneratedValue.class)
					.param("strategy", strategy);
				if(strategy == GenerationType.SEQUENCE) {
					gv.param("generator", column.getSequence().getName());
				}
			}
		}
		
		// FIXME: this is probably wong - it ignores what @Lob is for:
		// to indicate that the field should be persisted as a large object.
		// See the {@link Lob} documentation for details.
		// The question is, however, what a JPA implementation needs this for - 
		// most likely for generating a database schema, which is not really 
		// the gentity use case anyways, since we assume pleople generate
		// their database schema through modelling software.
		if(EnumSet.of(JDBCType.BINARY, JDBCType.VARBINARY, JDBCType.LONGVARBINARY, JDBCType.CLOB).contains(column.getType())) {
			field.annotate(Lob.class);
		}
		// @Column or @JoinColumn
		
		// @Column
		JAnnotationUse columnAnnotation = field
			.annotate(Column.class);
		columnAnnotation
			.param("name", column.getName());
		if(colType.equals(cm._ref(String.class))) {
			if(column.getLength()!=null) {
				columnAnnotation.param("length", column.getLength());
			}
		}
		if(numericTypes.contains(colType)) {
			if(column.getPrecision()!=null) {
				columnAnnotation.param("precision", column.getPrecision());
			}
			if(column.getScale()!=null) {
				columnAnnotation.param("scale", column.getScale());
			}
		}
		if(!column.isNullable() && !sm.isColumnPrimaryKey(table, column)) {
			columnAnnotation.param("nullable", false);
		}
	}
	
	private void genJoinedHierarchy(JoinedRootEntityInfo rootEInfo, JPackage pakkage) throws JClassAlreadyExistsException {
		
		TableModel rootTable = rootEInfo.getTable();
		JDefinedClass rootClass = genRootEntityClass(pakkage, rootTable.getName(), null, rootEInfo);
		rootClass.annotate(cm.ref(Inheritance.class))
			.param("strategy", InheritanceType.JOINED);
		
		genDiscriminatorColumnAnnotation(rootClass, rootTable, rootEInfo.getDiscriminatorColumn().getName());
		
		genJoinedHierarchySubentities(rootTable, pakkage, rootEInfo, rootClass);
	}
	
	private void genJoinedHierarchySubentities(TableModel rootTable, JPackage pakkage, EntityInfo<JoinedSubEntityInfo> parentEntityInfo, JDefinedClass superclassEntity) throws JClassAlreadyExistsException {
		for(JoinedSubEntityInfo einfo : parentEntityInfo.getChildren()) {
			
			ForeignKeyModel fk = einfo.getJoiningForeignKey();
			TableModel table = einfo.getTable();
			JDefinedClass subclassEntity = genSubEntityClass(pakkage, table.getName(), superclassEntity, einfo);
			genDiscriminatorValueAnnotation(subclassEntity, einfo.getDiscriminatorValue());

			if(fk.getColumnMappings().size()==1) {
				fillPrimaryKeyJoinColumn(subclassEntity.annotate(PrimaryKeyJoinColumn.class), fk, fk.getColumnMappings().iterator().next());
			} else {
				JAnnotationArrayMember arr = subclassEntity
					.annotate(PrimaryKeyJoinColumns.class)
					.paramArray("value");
				fk.getColumnMappings().forEach(
					fkCol -> fillPrimaryKeyJoinColumn(arr.annotate(PrimaryKeyJoinColumn.class), fk, fkCol)
				); 
			}
			
			genJoinedHierarchySubentities(rootTable, pakkage, einfo, subclassEntity);
		}
	}
	
	private void fillPrimaryKeyJoinColumn(JAnnotationUse primaryKeyJoinColumnUse, ForeignKeyModel fk, ForeignKeyModel.Mapping fkCol) {
		primaryKeyJoinColumnUse
			.param("name", fkCol.getChildColumn().getName())
			.param("referencedColumnName", fkCol.getParentColumn().getName())
			.annotationParam("foreignKey", ForeignKey.class)
				.param("name", fk.getName());
	}
	
	private void genDiscriminatorColumnAnnotation(JDefinedClass cls, TableModel table, String discriminatorColName) {
		ColumnModel rootdcol = table.getColumns().findColumn(discriminatorColName);
		if(rootdcol == null) {
			throw new RuntimeException("discrimiator column '" + discriminatorColName + "' not found");
		}
		JType rootdcolType = mapColumnType(table, rootdcol);

		JAnnotationUse au = cls.annotate(cm.ref(DiscriminatorColumn.class))
			.param("name", rootdcol.getName());
		if(rootdcolType instanceof JClass && ((JClass)rootdcolType).isAssignableFrom(cm.ref(String.class))) {
			au.param("length", rootdcol.getLength());
		}
	}
	
	private void genDiscriminatorValueAnnotation(JDefinedClass cls, String value) {
		cls.annotate(DiscriminatorValue.class)
			.param("value", value);
		
	}
	
	private void genPlainTable(PlainEntityInfo einfo, JPackage pakkage) throws JClassAlreadyExistsException {
		TableModel table = einfo.getTable();
		
		genRootEntityClass(pakkage, table.getName(), null, einfo);
	}

	private void genSingleTableHierarchy(SingleTableRootEntityInfo einfo, JPackage pakkage) throws JClassAlreadyExistsException {
		JDefinedClass rootClass = genRootEntityClass(pakkage, einfo.getTable().getName(), null, einfo);
		rootClass.annotate(Inheritance.class)
			.param("strategy", InheritanceType.SINGLE_TABLE);
		genDiscriminatorColumnAnnotation(rootClass, einfo.getTable(), einfo.getDiscriminatorColumn().getName());
		
		genDiscriminatorValueAnnotation(rootClass, einfo.getDiscriminatorValue());
		
		genSingleTableChildEntities(einfo.getBaseTable(), einfo, rootClass, einfo.getChildren());
	}
	
	private void genSingleTableChildEntities(TableModel rootTable, EntityInfo<SingleTableSubEntityInfo> parentEntityInfo, JDefinedClass parentclass, List<SingleTableSubEntityInfo> entities) throws JClassAlreadyExistsException {
		for(SingleTableSubEntityInfo entity : parentEntityInfo.getChildren()) {
			JDefinedClass cls = genSubEntityClass(parentclass.getPackage(), entity.getName(), parentclass, entity);
			
			genDiscriminatorValueAnnotation(cls, entity.getDiscriminatorValue());
			
			genSingleTableChildEntities(rootTable, entity, cls, entity.getChildren());
		}
	}
	
	private void genJoinColumns(JAnnotationArrayMember joinColumnsArray, ForeignKeyModel fkColumns) {
		for(Mapping fkColumn : fkColumns.getColumnMappings()) {
			joinColumnsArray
				.annotate(JoinColumn.class)
				.param("name", fkColumn.getChildColumn().getName())
				.param("referencedColumnName", fkColumn.getParentColumn().getName())
				;
		}
	}
	
	private EntityRefFactory findEntityRefFactory(JDefinedClass cls) {
		// for now, all we support is List<> with a created ArrayList<> instance
		return LIST_ENTITY_REF_FACTORY;
	}
	
	private JFieldVar genCollectionFieldVar(JDefinedClass cls, JDefinedClass elementType) {
		return genCollectionFieldVar(cls, elementType, null);
	}
	private JFieldVar genCollectionFieldVar(JDefinedClass cls, JType elementType, String nameCandidate) {
		EntityRefFactory factory = findEntityRefFactory(cls);
		JClass fieldType = factory.getCollectionType(elementType);
		
		if(nameCandidate == null) {
			nameCandidate = elementType.name();
		}
		JFieldVar field = cls
			.field(JMod.PROTECTED, fieldType, toFieldName(cls, nameCandidate));

		field.init(factory.createInitExpression());
		return field;
	}
	
	private JDefinedClass findEntity(EntityInfo einfo) {
		return entities.entrySet().stream()
			.filter(e -> e.getValue() == einfo)
			.findFirst()
			.get()
			.getKey();
	}
	
	private JDefinedClass findEntity(String tableName, String entityName) {
		if(entityName == null) {
			return tablesToEntities.get(tableName);
		} else {
			JDefinedClass cls = entities.entrySet().stream()
				.filter(e -> entityName.equals(e.getKey().name()))
				.filter(e -> tableName.equals(e.getValue().getBaseTable().getName()))
				.map(e -> e.getKey())
				.findAny()
				.orElseThrow(() -> new RuntimeException(String.format("specified entity %s in table scope %s not found", entityName, tableName)));
			return cls;
		}
	}
	private void genChildTableRelation(ChildTableRelation otm) {
		// find entity for child table side, requires special handling for
		// collection tables
		String childTableName = otm.getTable().getName();
		JDefinedClass childTableClass;
		CollectionTableDecl collectionTable = sm.getCollectionTableDeclaration(childTableName);
		if(collectionTable != null) {
			assert otm.getKind().getDirectionality() == UNIDIRECTIONAL: "collection tables may not contain bidirectional child table relations";
			childTableClass = tablesToEmbeddables.get(collectionTable.getTable().getName());
		} else {
			childTableClass = findEntity(childTableName, otm.getOwningEntityName());
		}
		// find entity representing parent table
		ForeignKeyModel ownerFk = otm.getForeignKey();
		JDefinedClass parentTableEntity = findEntity(ownerFk.getTargetTable().getName(), otm.getInverseEntityName());
		
		// child table side mapping
		String fieldName;
		List<ForeignKeyModel.Mapping> fkCols = ownerFk.getColumnMappings();
		if(fkCols.isEmpty()) {
			throw new RuntimeException(String.format("no columns defined in foreign key %s", ownerFk.getName()));
		}
		Matcher m = FK_COL_NAME_PATTERN.matcher(fkCols.get(0).getChildColumn().getName());
		if(fkCols.size()==1 && m.matches() && m.group(2).equals(fkCols.get(0).getParentColumn().getName())) {
			fieldName = toFieldName(childTableClass, m.group(1));
		} else {
			fieldName = toFieldName(childTableClass, parentTableEntity.name());
		}
		
		JFieldVar childField = childTableClass.field(JMod.PROTECTED, parentTableEntity, fieldName);
		
		// @ManyToOne / @OneToOne
		JAnnotationUse toManyAnno;
		if(otm.getKind().getFrom() == ONE) {
			toManyAnno = childField.annotate(OneToOne.class);
		} else {
			assert otm.getKind().getFrom() == MANY;
			toManyAnno = childField.annotate(ManyToOne.class);
		}
		genCascadeAnnotationParam(toManyAnno, otm.getOwnerCascadeTypes());
		
		// annotate with @Id if any column is part of a primary key if we're
		// not on a collection table embeddable
		PrimaryKeyModel pk = otm.getTable().getPrimaryKey();
		if(pk != null && collectionTable==null) {
			Set<String> pkColNames = pk.stream()
				.map(ColumnModel::getName)
				.collect(Collectors.toSet());

			if(otm.getForeignKey().getColumns().stream()
				.anyMatch(c -> pkColNames.contains(c.getName()))) {
				childField.annotate(Id.class);
			}
		}
		
		annotateJoinColumns(childField::annotate, () -> childField.annotate(JoinColumns.class).paramArray("value"), fkCols);
		
		// parent table side mapping, only for the two bidirectional mappings
		JFieldVar parentField = null;
		if(otm.getKind() == ONE_TO_ONE) {
			parentField = parentTableEntity.field(JMod.PROTECTED, childTableClass, toFieldName(parentTableEntity, otm.getTable().getName()));
			JAnnotationUse anno = parentField.annotate(OneToOne.class);
			anno.param("mappedBy", childField.name());
			genCascadeAnnotationParam(anno, otm.getInverseCascadeTypes());
		} else if(otm.getKind() == MANY_TO_ONE){
			parentField = genCollectionFieldVar(parentTableEntity, childTableClass);
			JAnnotationUse anno = parentField.annotate(OneToMany.class);
			anno.param("mappedBy", childField.name());
			genCascadeAnnotationParam(anno, otm.getInverseCascadeTypes());
		}
		
		if(isAutomaticBidirectionalUpdateEnabled()) {
			ChildTableRelation.Kind kind = otm.getKind();
			genRelationSideField(childTableClass, childField, kind.getTo(), parentTableEntity, parentField);
			
			if(kind.getDirectionality() == BIDIRECTIONAL) {
				genRelationSideField(parentTableEntity, parentField, kind.getFrom(), childTableClass, childField);
			}
		}

	}
	
	private void genCascadeAnnotationParam(JAnnotationUse anno, EnumSet<CascadeType> cascadeTypes) {
		if(!cascadeTypes.isEmpty()) {
			JAnnotationArrayMember cascadeArray = anno.paramArray("cascade");
			for(CascadeType ct : cascadeTypes) {
				cascadeArray.param(cm.ref(CascadeType.class).staticRef(ct.name()));
			}
		}
	}
	
	private void genRelationSideField(JDefinedClass entity, JFieldVar field, Cardinality toCardinality, JClass otherEntity, JFieldVar otherField) {
		JClass relationSideType;
		JInvocation initializer;
		
		// a single '$removed' boolean field needs to exist for each entity
		// with relation fields, including a @PreRemove and @PrePersist annotated method tracking
		// remove/persist/merge operations performed on this entity
		final String REMOVED_FIELD_NAME = "$removed";
		if(!entity.fields().containsKey(REMOVED_FIELD_NAME)) {
			JFieldVar removedF = entity.field(JMod.PRIVATE|JMod.TRANSIENT, boolean.class, REMOVED_FIELD_NAME);
			
			JMethod onPreRemoveM = entity.method(JMod.PRIVATE, void.class, "$onPreRemove");
			onPreRemoveM.annotate(PreRemove.class);
			onPreRemoveM.body()
				.assign(removedF, JExpr.TRUE);
			
			JMethod onPrePersistM = entity.method(JMod.PRIVATE, void.class, "$onPrePersist");
			onPrePersistM.annotate(PrePersist.class);
			onPrePersistM.body()
				.assign(removedF, JExpr.FALSE);
		}
		
		if(toCardinality == ONE) {
			relationSideType = cm.ref(ToOneSide.class).narrow(entity, (JClass)field.type());
			initializer = cm.ref(ToOneSide.class).staticInvoke("of")
			.arg(JExpr.direct("o -> o." + REMOVED_FIELD_NAME))
			.arg(JExpr.direct("o -> o." + field.name()))
			.arg(JExpr.direct("(o,m) -> o." + field.name() + " = m"));
		} else {
			assert toCardinality == MANY;
			relationSideType = cm.ref(ToManySide.class).narrow(entity, (JClass)field.type(), otherEntity);
			initializer = cm.ref(ToManySide.class).staticInvoke("of")
			.arg(JExpr.direct("o -> o." + REMOVED_FIELD_NAME))
			.arg(JExpr.direct("o -> o." + field.name()));
		}

		// for bidirectional relations, add argument connecting the
		// non-owning relation side
		if(otherField != null) {
			initializer
			.arg(otherEntity.staticRef(relationFieldName(otherField)));
		}

		entity.field(JMod.STATIC|JMod.FINAL, relationSideType, relationFieldName(field), initializer);
	}
	
	private String relationFieldName(JFieldVar field) {
		return "relationTo$" + field.name();
	}
	
	private void annotateJoinColumns(Function<Class<? extends Annotation>, JAnnotationUse> singularAnnotator, Supplier<JAnnotationArrayMember> pluralAnnotator, List<ForeignKeyModel.Mapping> fkCols){
		if(fkCols.size() == 1) {
			ColumnModel fkColumn = fkCols.get(0).getChildColumn();
			// @JoinColumn
			JAnnotationUse joinColumnAnnotation = singularAnnotator.apply(JoinColumn.class);
			joinColumnAnnotation.param("name", fkColumn.getName());
			if(!fkColumn.isNullable()) {
				joinColumnAnnotation.param("nullable", false);
			}
		} else {
			// @JoinColumns (plural!)
			annotateJoinColumns(pluralAnnotator, fkCols);
		}
	}

	private void annotateJoinColumns(Supplier<JAnnotationArrayMember> pluralAnnotator, List<ForeignKeyModel.Mapping> fkCols){
		// @JoinColumns (plural!)
		JAnnotationArrayMember joinColumnsArray = pluralAnnotator.get();
		for(ForeignKeyModel.Mapping fkColumn : fkCols) {
			joinColumnsArray
				.annotate(JoinColumn.class)
				.param("name", fkColumn.getChildColumn().getName())
				.param("referencedColumnName", fkColumn.getParentColumn().getName())
				;
		}
	}
	
	private void genJoinTableRelation(JDefinedClass ownerClass, JDefinedClass inverseClass, JoinTableRelation jtr) {
		JFieldVar ownerField = genCollectionFieldVar(ownerClass, inverseClass);
		
		JAnnotationUse onwerAnno = ownerField.annotate(ManyToMany.class);
		genCascadeAnnotationParam(onwerAnno, jtr.getOwnerCascadeTypes());
		
		JAnnotationUse joinTableAnnotation = ownerField.annotate(JoinTable.class)
			.param("name", jtr.getTable().getName());
		
		JAnnotationArrayMember joinColumnsArray = joinTableAnnotation
			.paramArray("joinColumns");
		genJoinColumns(joinColumnsArray, jtr.getOwnerForeignKey());
		
		JAnnotationArrayMember inverseJoinColumnsArray = joinTableAnnotation
			.paramArray("inverseJoinColumns");
		genJoinColumns(inverseJoinColumnsArray, jtr.getInverseForeignKey());

		JFieldVar inverseField = null;
		if(jtr.getKind().getDirectionality() == BIDIRECTIONAL) {
			inverseField = genCollectionFieldVar(inverseClass, ownerClass);

			JAnnotationUse inverseAnno = inverseField.annotate(ManyToMany.class);
			inverseAnno.param("mappedBy", ownerField.name());
			genCascadeAnnotationParam(inverseAnno, jtr.getInverseCascadeTypes());
		}
		
		if(isAutomaticBidirectionalUpdateEnabled()) {
			genRelationSideField(ownerClass, ownerField, jtr.getKind().getTo(), inverseClass, inverseField);
			if(jtr.getKind().getDirectionality() == BIDIRECTIONAL) {
				genRelationSideField(inverseClass, inverseField, jtr.getKind().getFrom(), ownerClass, ownerField);
			}
		}
	}
	
	
	private String toClassName(JPackage p, String name) {
		
		String baseName = nameProvider.javatizeName(name, true);
		
		ConfigurationDto cfg = sm.findClassOptions(name);
		return nameProvider.findNonexistingName(cfg.getClassNamePrefix() + baseName, cfg.getClassNameSuffix(), p::isDefined);
	}

	private String toFieldName(JDefinedClass cls, String name) {
		ConfigurationDto cfg = sm.findClassOptions(name);
		
		String baseName = nameProvider.javatizeName(name, false);
		String prefixedName = cfg.getFieldNamePrefix() + baseName;
		if(Character.isUpperCase(prefixedName.charAt(0))) {
			prefixedName = ""+Character.toLowerCase(prefixedName.charAt(0))+prefixedName.substring(1);
		}
		return nameProvider.findNonexistingName(prefixedName, cfg.getFieldNameSuffix(), cls.fields()::containsKey);
	}

	private String normalizeTypeName(String typeName) {
		return typeName.toLowerCase();
	}
	
	private JType mapColumnType(TableModel table, ColumnModel column) {
		JDBCType type = column.getType();
		
		JType jtype;
		switch(type) {
			case CHAR:
			case NCHAR:
				if(Short.valueOf((short)1).equals(column.getLength())) {
					jtype = cm.ref(Character.class);
				} else {
					jtype = cm.ref(String.class);
				}
				break;
			case VARCHAR:
			case NVARCHAR:
				jtype = cm.ref(String.class);
				break;
			case BIGINT:
				jtype = cm.LONG;
				break;
			case SMALLINT:
			case INTEGER:
				jtype = cm.INT;
				break;
			case BOOLEAN:
			case BIT:	// NOTE: not too sure on this one. Some DBs allow giving
				// BIT a length > 1, which makes this mapping funky. May consider
				// mapping BIT(2+) to boolean[] or something like that...
				jtype = cm.BOOLEAN;
				break;
			case FLOAT:
				jtype = cm.FLOAT;
				break;
			case REAL:
			case DOUBLE:
				jtype = cm.DOUBLE;
				break;
			case NUMERIC:
			case DECIMAL: {
				int p = column.getPrecision() != null
					?	column.getPrecision()
					:	0;
				int s = column.getScale() != null
					?	column.getScale()
					:	0;
				if(s == 0) {
					if(p<=9) {
						// fits into Integer.MAX_VALUE
						jtype = cm.INT;
					} else if(p<=18) {
						// fits into Long.MAX_VALUE
						jtype = cm.LONG;
					} else {
						// won't fit anywhere: 
						jtype = cm.ref(BigInteger.class);
					}
				} else {
					if(p<=7) {
						// 7 digits fit into float
						jtype = cm.FLOAT;
					} else if(p<=14) {
						// 14 digits it into double
						jtype = cm.DOUBLE;
					} else {
						// everything else is BidDecimal...
						jtype = cm.ref(BigDecimal.class);
					}
				}
			}	break;
			case TIMESTAMP_WITH_TIMEZONE:
				jtype = cm.ref(Timestamp.class);
				break;
			case DATE:
				jtype = cm.ref(LocalDate.class);
				break;
			case TIMESTAMP:
				jtype = cm.ref(LocalDateTime.class);
				break;
			case BLOB:
			case BINARY:
			case VARBINARY:
			case LONGVARBINARY:
				jtype = cm.ref(byte[].class);
				break;
			default:
				throw new RuntimeException("no mapping found for SQL type '" + column.getType() + "'");
		}
		
		if(column.isNullable() || (table.getPrimaryKey() != null && table.getPrimaryKey().contains(column))) {
			jtype = jtype.boxify();
		}
		
		return jtype;
	}

	
}
