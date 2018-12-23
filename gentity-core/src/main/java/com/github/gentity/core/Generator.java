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
import com.github.dbsjpagen.config.CollectionTableDto;
import com.github.gentity.core.fields.FieldColumnSource;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JExpr;
import com.sun.codemodel.JExpression;
import com.sun.codemodel.JFieldVar;
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
import com.github.dbsjpagen.config.ConfigurationDto;
import com.github.dbsjpagen.config.MappingConfigDto;
import com.github.dbsjpagen.dbsmodel.ProjectDto;
import static com.github.gentity.core.ChildTableRelation.Directionality.UNIDIRECTIONAL;
import static com.github.gentity.core.ChildTableRelation.Kind.ONE_TO_ONE;
import static com.github.gentity.core.ChildTableRelation.Kind.UNI_ONE_TO_ONE;
import java.util.EnumSet;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import static com.github.gentity.core.ChildTableRelation.Kind.MANY_TO_ONE;
import static com.github.gentity.core.ChildTableRelation.Kind.UNI_MANY_TO_ONE;
import com.github.gentity.core.entities.JoinedRootEntityInfo;
import com.github.gentity.core.entities.JoinedSubEntityInfo;
import com.github.gentity.core.entities.PlainEntityInfo;
import com.github.gentity.core.entities.SingleTableRootEntityInfo;
import com.github.gentity.core.entities.SingleTableSubEntityInfo;
import com.github.gentity.core.fields.FieldMapping;
import com.github.gentity.core.fields.PlainTableFieldColumnSource;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.ForeignKeyModel.Mapping;
import com.github.gentity.core.model.SequenceModel;
import com.github.gentity.core.model.TableModel;
import com.github.gentity.core.util.Tuple;
import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.persistence.Lob;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;


/**
 *
 * @author upachler
 */
public class Generator {
	
	private final SchemaModel sm;
	
	JCodeModel cm;
	Map<String, JDefinedClass> tablesToEntities;
	Map<JDefinedClass, EntityInfo> entities;
	Map<String, JDefinedClass> tablesToEmbeddables;
	
	private final EntityRefFactory LIST_ENTITY_REF_FACTORY = new EntityRefFactory() {
		@Override
		public JExpression createInitExpression() {
			return JExpr._new(cm._ref(ArrayList.class));
		}
		@Override
		public JClass getCollectionType(JDefinedClass elementType) {
			return cm.ref(List.class)
				.narrow(elementType);
		}
	};
	
	private final Pattern FK_COL_NAME_PATTERN = Pattern.compile("(.*)_(.+)");

	private NameProvider nameProvider;
	
	public Generator(MappingConfigDto cfg, ProjectDto project) {
		sm = new SchemaModelImpl(cfg, project);
	}
	
	
	JCodeModel generate() {
		if(cm == null) {
			cm = new JCodeModel();
		}
		JPackage p = cm._package(sm.getTargetPackageName());
		
		AccessorGenerator accessorGenerator = new AccessorGenerator(cm);
		nameProvider = new NameProvider();
		
		try {
			tablesToEntities = new HashMap<>();
			entities = new HashMap<>();
			tablesToEmbeddables = new HashMap<>();
			
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
			sm.collectionTableTravesalOf(
					rt -> findEntity(rt.getTable(), null),
					jt -> findEntity(jt.getTable(), null),
					st -> findEntity(sm.findParentRootEntityTable(st).getTable(), st.getName())
				)
				.traverse(ctx -> {
					try {
						JDefinedClass entityClass = ctx.getParentContext();
						genEmbeddable(entityClass, ctx.getElement());
					} catch (JClassAlreadyExistsException ex) {
						throw new RuntimeException(ex);
					}
				});
			
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
				entities.entrySet().stream()
					.map(Tuple::of)
					.map(t -> t.mapY(EntityInfo::getFieldColumnSource))
				,
				tablesToEmbeddables.entrySet().stream()
					.map(e -> Tuple.of(e.getValue(), e.getKey()))
					.map(t -> t.mapY(sm::toTable))
					.map(t -> t.mapY(x -> new PlainTableFieldColumnSource(x)))
			)
				.flatMap(t -> t)
				.forEach(t -> {
					FieldColumnSource src = t.y();
					JDefinedClass cls = t.x();

					src.getFieldMappings().stream()
						.filter((m) -> (!sm.isColumnIgnored(m.getTable().getName(), m.getColumn().getName())))
						.forEachOrdered((m) -> genColumn(cls, m));
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
				
			
		} catch(JClassAlreadyExistsException caex) {
			throw new RuntimeException(caex);
		}
		
		return cm;
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
	
	private JDefinedClass genEntityClass(JPackage p, String nameCandidate, JClass superClass, EntityInfo einfo) throws JClassAlreadyExistsException {
		JClass serializableClass = cm.ref(Serializable.class);
			
		JDefinedClass cls = p._class(JMod.PUBLIC, toClassName(p, nameCandidate));
		if(superClass!=null) {
			cls._extends(superClass);
		}
		cls.annotate(Entity.class);
		TableModel table = einfo.getTable();
		if(table != null) {
			cls.annotate(Table.class)
				.param("name", table.getName());
			tablesToEntities.put(table.getName(), cls);
		}
		entities.put(cls, einfo);
		
		cls._implements(serializableClass);
		return cls;
	}
	
	private JDefinedClass genEmbeddableClass(String nameCandidate, TableModel table, String foreignKeyName, JDefinedClass enclosingEntityClass) throws JClassAlreadyExistsException {
		JPackage p = cm._package(sm.getTargetPackageName());
		JClass serializableClass = cm.ref(Serializable.class);
			
		JDefinedClass cls = p._class(JMod.PUBLIC, toClassName(p, nameCandidate));
		cls.annotate(Embeddable.class);
		tablesToEmbeddables.put(table.getName(), cls);
		
		cls._implements(serializableClass);
		
		JFieldVar field = genCollectionFieldVar(enclosingEntityClass, cls);
		field.annotate(cm.ref(ElementCollection.class));
		JAnnotationUse ct = field.annotate(cm.ref(CollectionTable.class));
		ct.param("name", table.getName());
		
		List<ForeignKeyModel.Mapping> fkCols = table.findForeignKey(foreignKeyName).getColumnMappings();
		annotateJoinColumns(() -> ct.paramArray("joinColumns"), fkCols);
		return cls;
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
		TableModel table = m.getTable();
		ColumnModel column = m.getColumn();
		String fieldNameCanditate = m.getFieldName();
		
		JType colType = mapColumnType(table, column);
		
		EnumType etype = null;
		if(m.getEnumType() != null) {
			
			if(colType == cm._ref(java.lang.String.class)) {
				etype = EnumType.STRING;
			} else if(colType == cm.INT || colType == cm.LONG || colType == cm.BYTE || colType == cm.SHORT) {
				etype = EnumType.ORDINAL;
			} else {
				throw new RuntimeException(String.format("column %s field definition with enumType of %s must be mappable to a Java String or integral type", m.getColumn(), m.getEnumType()));
			}
			
			colType = cm.ref(m.getEnumType());
		}
		String fieldName = toFieldName(cls, fieldNameCanditate);
		JFieldVar field = cls.field(JMod.PROTECTED, colType, fieldName);
		
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
		
		if(isColumnByteArrayType(column)) {
			field.annotate(Lob.class);
		}
		// @Column or @JoinColumn
		
		// @Column
		JAnnotationUse columnAnnotation = field
			.annotate(Column.class);
		columnAnnotation
			.param("name", column.getName());
		if(colType.equals(cm._ref(String.class))) {
			columnAnnotation.param("length", column.getLength());
		}
		if(!column.isNullable() && !sm.isColumnPrimaryKey(table, column)) {
			columnAnnotation.param("nullable", false);
		}
	}
	
	private void genJoinedHierarchy(JoinedRootEntityInfo rootEInfo, JPackage pakkage) throws JClassAlreadyExistsException {
		
		TableModel rootTable = rootEInfo.getTable();
		JDefinedClass rootClass = genEntityClass(pakkage, rootTable.getName(), null, rootEInfo);
		rootClass.annotate(cm.ref(Inheritance.class))
			.param("strategy", InheritanceType.JOINED);
		
		genDiscriminatorColumnAnnotation(rootClass, rootTable, rootEInfo.getDiscriminatorColumn().getName());
		
		genJoinedHierarchySubentities(rootTable, pakkage, rootEInfo, rootClass);
	}
	
	private void genJoinedHierarchySubentities(TableModel rootTable, JPackage pakkage, EntityInfo<JoinedSubEntityInfo> parentEntityInfo, JClass superclassEntity) throws JClassAlreadyExistsException {
		for(JoinedSubEntityInfo einfo : parentEntityInfo.getChildren()) {
			
			ForeignKeyModel fk = einfo.getJoiningForeignKey();
			TableModel table = einfo.getTable();
			JDefinedClass subclassEntity = genEntityClass(pakkage, table.getName(), superclassEntity, einfo);
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
		
		genEntityClass(pakkage, table.getName(), null, einfo);
	}

	private void genSingleTableHierarchy(SingleTableRootEntityInfo einfo, JPackage pakkage) throws JClassAlreadyExistsException {
		JDefinedClass rootClass = genEntityClass(pakkage, einfo.getTable().getName(), null, einfo);
		rootClass.annotate(Inheritance.class)
			.param("strategy", InheritanceType.SINGLE_TABLE);
		genDiscriminatorColumnAnnotation(rootClass, einfo.getTable(), einfo.getDiscriminatorColumn().getName());
		
		genDiscriminatorValueAnnotation(rootClass, einfo.getDiscriminatorValue());
		
		genSingleTableChildEntities(einfo.getBaseTable(), einfo, rootClass, einfo.getChildren());
	}
	
	private void genSingleTableChildEntities(TableModel rootTable, EntityInfo<SingleTableSubEntityInfo> parentEntityInfo, JDefinedClass parentclass, List<SingleTableSubEntityInfo> entities) throws JClassAlreadyExistsException {
		for(SingleTableSubEntityInfo entity : parentEntityInfo.getChildren()) {
			JDefinedClass cls = genEntityClass(parentclass.getPackage(), entity.getName(), parentclass, entity);
			
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
	
	private void genEmbeddable(JDefinedClass entityClass, CollectionTableDto collectionTable) throws JClassAlreadyExistsException {
		TableModel table = sm.toTable(collectionTable.getTable());
		genEmbeddableClass(table.getName(), table, collectionTable.getForeignKey(), entityClass);
	}
	
	private JFieldVar genCollectionFieldVar(JDefinedClass cls, JDefinedClass elementType) {
		EntityRefFactory factory = findEntityRefFactory(cls);
		JClass fieldType = factory.getCollectionType(elementType);
		
		JFieldVar field = cls
			.field(JMod.PROTECTED, fieldType, toFieldName(cls, elementType.name()));

		field.init(factory.createInitExpression());
		return field;
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
		if(EnumSet.of(ONE_TO_ONE, UNI_ONE_TO_ONE).contains(otm.getKind())) {
			childField.annotate(OneToOne.class);
		} else {
			assert  EnumSet.of(MANY_TO_ONE, UNI_MANY_TO_ONE).contains(otm.getKind());
			childField.annotate(ManyToOne.class);
		}
		
		annotateJoinColumns(childField::annotate, () -> childField.annotate(JoinColumns.class).paramArray("value"), fkCols);
		
		// parent table side mapping, only for the two bidirectional mappings
		if(otm.getKind() == ONE_TO_ONE) {
			JFieldVar parentField = parentTableEntity.field(JMod.PROTECTED, childTableClass, toFieldName(parentTableEntity, otm.getTable().getName()));
			parentField.annotate(OneToOne.class)
				.param("mappedBy", childField.name());
		} else if(otm.getKind() == MANY_TO_ONE){
			JFieldVar field = genCollectionFieldVar(parentTableEntity, childTableClass);
			field.annotate(OneToMany.class)
				.param("mappedBy", childField.name());
		}
		
	}
	
	private void annotateJoinColumns(Function<Class<? extends Annotation>, JAnnotationUse> singularAnnotator, Supplier<JAnnotationArrayMember> pluralAnnotator, List<ForeignKeyModel.Mapping> fkCols){
		if(fkCols.size() == 1) {
			ColumnModel fkColumn = fkCols.get(0).getChildColumn();
			// @JoinColumn
			JAnnotationUse joinColumnAnnotation = singularAnnotator.apply(JoinColumn.class);
			joinColumnAnnotation.param("name", fkColumn.getName());
			if(fkColumn.isNullable()) {
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
		
		ownerField.annotate(ManyToMany.class);
		
		JAnnotationUse joinTableAnnotation = ownerField.annotate(JoinTable.class)
			.param("name", jtr.getTable().getName());
		
		JAnnotationArrayMember joinColumnsArray = joinTableAnnotation
			.paramArray("joinColumns");
		genJoinColumns(joinColumnsArray, jtr.getOwnerForeignKey());
		
		JAnnotationArrayMember inverseJoinColumnsArray = joinTableAnnotation
			.paramArray("inverseJoinColumns");
		genJoinColumns(inverseJoinColumnsArray, jtr.getInverseForeignKey());

		boolean isBidirectional = jtr.getKind() == JoinTableRelation.Kind.MANY_TO_MANY;
		if(isBidirectional) {
			JFieldVar inverseField = genCollectionFieldVar(inverseClass, ownerClass);

			inverseField.annotate(ManyToMany.class)
				.param("mappedBy", ownerField.name());
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
	
	private boolean isLob(ColumnModel column) {
		// NOTE: this may not always be the case, but for now it'll do
		return isColumnByteArrayType(column);
	}
	
	private boolean isColumnByteArrayType(ColumnModel column) {
		switch(column.getSqlType()) {
			case "blob":
			case "binary":
			case "varbinary":
			case "bytea":
				return true;
			default:
				return false;
		}
	}
	
	private JType mapColumnType(TableModel table, ColumnModel column) {
		String type = normalizeTypeName(column.getSqlType());
		
		JType jtype;
		switch(type) {
			case "char":
			case "character":
				if(Short.valueOf((short)1).equals(column.getLength())) {
					jtype = cm.ref(Character.class);
				} else {
					jtype = cm.ref(String.class);
				}
				break;
			case "varchar":
			case "character varying":
				jtype = cm.ref(String.class);
				break;
			case "bigint":
				jtype = cm.LONG;
				break;
			case "int":
			case "integer":
				jtype = cm.INT;
				break;
			case "bool":
			case "boolean":
				jtype = cm.BOOLEAN;
				break;
			case "float":
				jtype = cm.FLOAT;
				break;
			case "real":
			case "double":
			case "double precision":
				jtype = cm.DOUBLE;
				break;
			case "timestamp":
				jtype = cm.ref(Timestamp.class);
				break;
			case "date":
				jtype = cm.ref(LocalDate.class);
				break;
			case "datetime":
				jtype = cm.ref(LocalDateTime.class);
				break;
			default:
				if(isColumnByteArrayType(column)) {
					jtype = cm.ref(byte[].class);
					break;
				}
				throw new RuntimeException("no mapping found for SQL type '" + column.getSqlType() + "'");
		}
		
		if(column.isNullable() || (table.getPrimaryKey() != null && table.getPrimaryKey().contains(column))) {
			jtype = jtype.boxify();
		}
		
		return jtype;
	}

	
}
