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

import com.github.gentity.core.fields.FieldColumnSource;
import com.github.dbsjpagen.config.ChildTableRelationshipDto;
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
import java.sql.Blob;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.github.dbsjpagen.config.ExclusionDto;
import com.github.dbsjpagen.config.HierarchyDto;
import com.github.dbsjpagen.config.JoinRelationDto;
import com.github.dbsjpagen.config.JoinTableRelationshipDto;
import com.github.dbsjpagen.config.MappingConfigDto;
import com.github.dbsjpagen.config.RelationDto;
import com.github.dbsjpagen.config.SingleTableFieldDto;
import com.github.dbsjpagen.config.SingleTableRootEntityDto;
import com.github.dbsjpagen.config.SingleTableSubEntityDto;
import com.github.dbsjpagen.config.TableConfigurationDto;
import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.dbsjpagen.dbsmodel.ForeignKeyColumnDto;
import com.github.dbsjpagen.dbsmodel.ForeignKeyDto;
import com.github.dbsjpagen.dbsmodel.IndexUniqueDto;
import com.github.dbsjpagen.dbsmodel.ProjectDto;
import com.github.dbsjpagen.dbsmodel.SequenceDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import static com.github.gentity.core.ChildTableRelation.Kind.ONE_TO_ONE;
import static com.github.gentity.core.ChildTableRelation.Kind.UNI_ONE_TO_ONE;
import java.util.EnumSet;
import java.util.function.Function;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ForeignKey;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.PrimaryKeyJoinColumns;
import static com.github.gentity.core.ChildTableRelation.Kind.MANY_TO_ONE;
import static com.github.gentity.core.ChildTableRelation.Kind.UNI_MANY_TO_ONE;
import com.github.gentity.core.fields.PlainTableFieldColumnSource;
import com.github.gentity.core.fields.SingleTableFieldColumnSource;
import com.github.gentity.core.fields.SingleTableRootFieldColumnSource;
import java.time.LocalDate;
import java.time.LocalDateTime;


/**
 *
 * @author upachler
 */
public class Generator {
	
	private final MappingConfigDto cfg;
	private final ProjectDto project;
	JCodeModel cm;
	Map<String, JDefinedClass> tablesToEntities;
	Map<JDefinedClass, EntityInfo> entities;
	
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
	private Set<String> excludedTables;
	private Set<String> excludedTableColumns;
	private Map<String, ConfigurationDto> tableConfigurations;
	private ConfigurationDto globalConfiguration;
	private Map<String, TableDto> tables;
	private Map<String, SequenceDto> sequences;
	private List<ChildTableRelation> childTableRelations;
	private List<JoinTableRelation> joinTableRelations;
	private Map<String, JoinTableRelation> manyToManyRelationsJoinTables;
	
	public Generator(MappingConfigDto cfg, ProjectDto project) {
		this.cfg = cfg;
		this.project = project;
	}
	
	
	JCodeModel generate() {
		if(cm == null) {
			cm = new JCodeModel();
		}
		JPackage p = cm._package(cfg.getTargetPackageName());
		
		AccessorGenerator accessorGenerator = new AccessorGenerator(cm);
		nameProvider = new NameProvider();
		
		try {
			tablesToEntities = new HashMap<>();
			entities = new HashMap<>();
			
			// generate entities first that are part of a hierarchy
			for(HierarchyDto h : cfg.getHierarchy()) {
				
				if(h.getJoined() != null) {
					genJoinedHierarchy(h, p);
				} else if(h.getSingleTable() != null) {
					genSingleTableHierarchy(h, p);
				}
			}
			
			// filter tables and generate empty entity classes
			for(TableDto table : project.getSchema().getTable()) {
				if(tablesToEntities.containsKey(table.getName())){
					continue;	// skip table if we already generated an entity for it
				}
				if(isTableExcluded(table.getName()) || isJoinTable(table.getName())) {
					continue;
				}
				EntityInfo einfo = new EntityInfo(table, new PlainTableFieldColumnSource(table));
				genEntityClass(p, table.getName(), null, einfo);
			}
			
			// for all entity-mapped tables, find entities where we need to
			// map sequences
			Set<String> mappedSequenceNames = new HashSet<>();
			for(Map.Entry<String, JDefinedClass> e : tablesToEntities.entrySet()) {
				String tableName = e.getKey();
				JDefinedClass cls = e.getValue();
				TableDto table = findTable(tableName);
				List<String> tableSequenceNames = new ArrayList<>();
				for(ColumnDto col : table.getColumn()) {
					String seqName = findPrimaryKeyColumnGeneratorSequence(table, col);
					if(seqName != null && mappedSequenceNames.add(seqName)) {
						tableSequenceNames.add(seqName);
					}
				}
				
				if(tableSequenceNames.size() == 1) {
					genSequence(cls.annotate(SequenceGenerator.class), findSequence(tableSequenceNames.get(0)));
				} else if(tableSequenceNames.size()>1) {
					JAnnotationArrayMember pa = cls.annotate(SequenceGenerators.class)
						.paramArray("value");
					
					for(String n : tableSequenceNames) {
						JAnnotationUse au = pa.annotate(SequenceGenerator.class);
						genSequence(au, findSequence(n));
					}
				}
			}
			
			// process table columns
			for(Map.Entry<JDefinedClass, EntityInfo> e : entities.entrySet()) {
				FieldColumnSource src = e.getValue().getFieldColumnSource();
				JDefinedClass cls = e.getKey();
				
				src.getFieldMappings().stream()
					.filter((m) -> (!isColumnExcluded(m.getTable().getName(), m.getColumn().getName())))
					.forEachOrdered((m) -> genColumn(cls, m.getTable(), m.getColumn(), m.getFieldName()));
			}
			
			// add one-to-many relations
			for(ChildTableRelation ctr : getChildTableRelations()) {
				
				genChildTableRelation(ctr);
			}
			
			// add many-to-many relations
			for(JoinTableRelation jtr : getJoinTableRelations()) {
				JDefinedClass ownerClass = findEntity(jtr.getOwnerForeignKey().getToTable(), jtr.getOwnerEntityName());
				JDefinedClass inverseClass = findEntity(jtr.getInverseForeignKey().getToTable(), jtr.getInverseEntityName());
				
				genJoinTableRelation(ownerClass, inverseClass, jtr);
			}
			
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
	
	private JDefinedClass genEntityClass(JPackage p, String entityNameCandidate, JClass superClass, EntityInfo einfo) throws JClassAlreadyExistsException {
		JClass serializableClass = cm.ref(Serializable.class);
			
		JDefinedClass cls = p._class(JMod.PUBLIC, toClassName(p, entityNameCandidate));
		if(superClass!=null) {
			cls._extends(superClass);
		}
		cls.annotate(Entity.class);
		TableDto table = einfo.getTable();
		if(table != null) {
			cls.annotate(Table.class)
				.param("name", table.getName());
			tablesToEntities.put(table.getName(), cls);
		}
		entities.put(cls, einfo);
		
		cls._implements(serializableClass);
		return cls;
	}
	
	private void genSequence(JAnnotationUse au, SequenceDto sequence) {
		au.param("name", sequence.getName());
		Long start = sequence.getStart();
		boolean startIsDefault = (start == null || Long.valueOf(1).equals(start));
		if(!startIsDefault){
			au.param("start", start);
		}
	}
	
	private void genColumn(JDefinedClass cls, TableDto table, ColumnDto column, String fieldNameCanditate) {
		
		JType colType = mapColumnType(table, column);
		String fieldName = toFieldName(cls, fieldNameCanditate);
		JFieldVar field = cls.field(JMod.PROTECTED, colType, fieldName);
		
		// @Id
		if(isColumnPrimaryKey(table, column)) {
			field.annotate(Id.class);
			GenerationType strategy = findPrimaryKeyColumnGenerationStrategy(table, column);
			if(strategy != null) {
				JAnnotationUse gv = field.annotate(GeneratedValue.class)
					.param("strategy", strategy);
				if(strategy == GenerationType.SEQUENCE) {
					gv.param("generator", findPrimaryKeyColumnGeneratorSequence(table, column));
				}
			}
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
		if(!isColumnNullable(column) && !isColumnPrimaryKey(table, column)) {
			columnAnnotation.param("nullable", false);
		}
	}
	
	private void genJoinedHierarchy(HierarchyDto h, JPackage pakkage) throws JClassAlreadyExistsException {
		
		Map<String,JClass> hierarchyClasses = new HashMap<>();

		TableDto rootTable = project.getSchema().getTable().stream()
			.filter(t -> t.getName().equals(h.getJoined().getRoot().getTable()))
			.findAny()
			.orElseThrow(() -> new RuntimeException("root table '"+h.getJoined().getRoot().getTable() + "' not found"));
		EntityInfo rootEInfo = new EntityInfo(rootTable, new PlainTableFieldColumnSource(rootTable));
		JDefinedClass rootClass = genEntityClass(pakkage, rootTable.getName(), null, rootEInfo);
		hierarchyClasses.put(rootTable.getName(), rootClass);
		rootClass.annotate(cm.ref(Inheritance.class))
			.param("strategy", InheritanceType.JOINED);
		
		genDiscriminatorColumnAnnotation(rootClass, rootTable, h.getJoined().getDiscriminateBy().getColumn());

		Set<JoinRelationDto> unmappedRelations = new HashSet<>(h.getJoined().getJoinRelation());
		while(!unmappedRelations.isEmpty()) {
			// find a mappable join relation: We need a relation that
			// points to a direct supertable (table that is mapped
			// to an entity that will be a direct superclass of the
			// entity that will be generated from a candidate join
			// relation).
			JoinRelationDto mapableRelation = unmappedRelations.stream()
				.filter(jr -> {
					String superTableName = toTableForeignKey(jr.getTable(), jr.getForeignKey()).getToTable();
					return hierarchyClasses.containsKey(superTableName);
				})
				.findAny()
				.orElseThrow(() -> {
					String rels = unmappedRelations.stream()
						.map(r -> "("+r.getTable()+"|"+r.getForeignKey()+")")
						.collect(Collectors.joining(","));
					return new RuntimeException("no direct superclass table found for relation(s): " + rels);
				});

			unmappedRelations.remove(mapableRelation);

			// use the foreign key to get to the supertable and its
			// corresponding superclass entity, and generate the
			// subclass entity from there
			ForeignKeyDto fk = toTableForeignKey(mapableRelation.getTable(), mapableRelation.getForeignKey());
			JClass superclassEntity = hierarchyClasses.get(fk.getToTable());
			TableDto table = findTable(mapableRelation.getTable());
			EntityInfo einfo = new EntityInfo(table, rootTable, new PlainTableFieldColumnSource(table));
			JDefinedClass subclassEntity = genEntityClass(pakkage, table.getName(), superclassEntity, einfo);
			genDiscriminatorValueAnnotation(subclassEntity, mapableRelation.getDiscriminator());

			if(fk.getFkColumn().size()==1) {
				fillPrimaryKeyJoinColumn(subclassEntity.annotate(PrimaryKeyJoinColumn.class), fk, fk.getFkColumn().get(0));
			} else {
				JAnnotationArrayMember arr = subclassEntity
					.annotate(PrimaryKeyJoinColumns.class)
					.paramArray("value");
				fk.getFkColumn().forEach(
					fkCol -> fillPrimaryKeyJoinColumn(arr.annotate(PrimaryKeyJoinColumn.class), fk, fkCol)
				); 
			}
		}
	}
	
	private void fillPrimaryKeyJoinColumn(JAnnotationUse primaryKeyJoinColumnUse, ForeignKeyDto fk, ForeignKeyColumnDto fkCol) {
		primaryKeyJoinColumnUse
			.param("name", fkCol.getName())
			.param("referencedColumnName", fkCol.getPk())
			.annotationParam("foreignKey", ForeignKey.class)
				.param("name", fk.getName());
	}
	
	private void genDiscriminatorColumnAnnotation(JDefinedClass cls, TableDto table, String discriminatorColName) {
		ColumnDto rootdcol = table.getColumn().stream()
			.filter(c -> c.getName().equals(discriminatorColName))
			.findAny()
			.orElseThrow(() -> new RuntimeException("discrimiator column '" + discriminatorColName + "' not found"));
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
	
	private void checkEachFieldOnlyOnce(TableDto table, List<SingleTableSubEntityDto> entities) {
		for (SingleTableSubEntityDto entity : entities) {
			Set<String> colMap = table.getColumn().stream()
				.map(c -> c.getName())
				.collect(Collectors.toSet());
			checkEachFieldOnlyOnceImpl(table, colMap, new HashSet<>(), entity);
		}
	}
	
	private void checkEachFieldOnlyOnceImpl(TableDto table, Set<String> colSet, Set<String> usedColNames, SingleTableSubEntityDto entity) {
		for(SingleTableFieldDto f : entity.getField()) {
			if(!colSet.contains(f.getColumn())) {
				throw new RuntimeException(String.format("Specified field column %s does not exist in table %s", f.getColumn(), table.getName()));
			}
			if(!usedColNames.add(f.getColumn())) {
				throw new RuntimeException(String.format("duplicate column name %s found in single table hierarchy of root table %s", f.getColumn(), table.getName()));
			}
		}
		
		for(SingleTableSubEntityDto e : entity.getEntity()) {
			checkEachFieldOnlyOnceImpl(table, colSet, usedColNames, e);
		}
	}
	
	private void genSingleTableHierarchy(HierarchyDto h, JPackage pakkage) throws JClassAlreadyExistsException {
		
		SingleTableRootEntityDto rootEntity = h.getSingleTable().getEntity();
		TableDto rootTable = findTable(rootEntity.getTable());
		
		checkEachFieldOnlyOnce(rootTable, rootEntity.getEntity());
		EntityInfo einfo = new EntityInfo(rootTable, new SingleTableRootFieldColumnSource(rootTable, rootEntity));

		JDefinedClass rootClass = genEntityClass(pakkage, rootTable.getName(), null, einfo);
		rootClass.annotate(Inheritance.class)
			.param("strategy", InheritanceType.SINGLE_TABLE);
		genDiscriminatorColumnAnnotation(rootClass, rootTable, h.getSingleTable().getDiscriminateBy().getColumn());
		
		genDiscriminatorValueAnnotation(rootClass, rootEntity.getDiscriminator());
		
		genSingleTableChildEntities(rootTable, rootClass, rootEntity.getEntity());
	}
	
	private void genSingleTableChildEntities(TableDto rootTable, JDefinedClass parentclass, List<SingleTableSubEntityDto> entities) throws JClassAlreadyExistsException {
		for(SingleTableSubEntityDto entity : entities) {
			EntityInfo einfo = new EntityInfo(null, rootTable, new SingleTableFieldColumnSource(rootTable, entity));
			JDefinedClass cls = genEntityClass(parentclass.getPackage(), entity.getName(), parentclass, einfo);
			
			genDiscriminatorValueAnnotation(cls, entity.getDiscriminator());
			
			genSingleTableChildEntities(rootTable, cls, entity.getEntity());
		}
	}
	
	private void genJoinColumns(JAnnotationArrayMember joinColumnsArray, List<ForeignKeyColumnDto> fkColumns) {
		for(ForeignKeyColumnDto fkColumn : fkColumns) {
			joinColumnsArray
				.annotate(JoinColumn.class)
				.param("name", fkColumn.getName())
				.param("referencedColumnName", fkColumn.getPk())
				;
		}
	}
	
	private EntityRefFactory findEntityRefFactory(JDefinedClass cls) {
		// for now, all we support is List<> with a created ArrayList<> instance
		return LIST_ENTITY_REF_FACTORY;
	}
	
	private JFieldVar genRelationCollectionFieldVar(JDefinedClass cls, JDefinedClass elementType, String targetTableName) {
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
		JDefinedClass childTableEntity = findEntity(otm.getTable().getName(), otm.getOwningEntityName());
		ForeignKeyDto ownerFk = otm.getForeignKey();
		JDefinedClass parentTableEntity = findEntity(ownerFk.getToTable(), otm.getInverseEntityName());
		
		// child table side mapping
		String fieldName;
		List<ForeignKeyColumn> fkCols = findForeignKeyColumns(otm.getTable(), otm.getForeignKey());
		
		Matcher m = FK_COL_NAME_PATTERN.matcher(fkCols.get(0).getColumn().getName());
		if(fkCols.size()==1 && m.matches() && m.group(2).equals(fkCols.get(0).getPk().getName())) {
			fieldName = toFieldName(childTableEntity, m.group(1));
		} else {
			fieldName = toFieldName(childTableEntity, parentTableEntity.name());
		}
		
		JFieldVar childField = childTableEntity.field(JMod.PROTECTED, parentTableEntity, fieldName);
		
		// @ManyToOne / @OneToOne
		if(EnumSet.of(ONE_TO_ONE, UNI_ONE_TO_ONE).contains(otm.getKind())) {
			childField.annotate(OneToOne.class);
		} else {
			assert  EnumSet.of(MANY_TO_ONE, UNI_MANY_TO_ONE).contains(otm.getKind());
			childField.annotate(ManyToOne.class);
		}

		if(fkCols.size() == 1) {
			ColumnDto fkColumn = fkCols.get(0).getColumn();
			// @JoinColumn
			JAnnotationUse joinColumnAnnotation = childField.annotate(JoinColumn.class);
			joinColumnAnnotation.param("name", fkColumn.getName());
			if(!isColumnNullable(fkColumn)) {
				joinColumnAnnotation.param("nullable", false);
			}
		} else {
			// @JoinColumns (plural!)
			JAnnotationArrayMember joinColumnsArray = childField
				.annotate(JoinColumns.class)
				.paramArray("value");
			for(ForeignKeyColumn fkColumn : fkCols) {
				joinColumnsArray
					.annotate(JoinColumn.class)
					.param("name", fkColumn.getColumn().getName())
					.param("referencedColumnName", fkColumn.getPk().getName())
					;
			}
		}

		// parent table side mapping, only for the two bidirectional mappings
		if(otm.getKind() == ONE_TO_ONE) {
			JFieldVar parentField = parentTableEntity.field(JMod.PROTECTED, childTableEntity, toFieldName(parentTableEntity, otm.getTable().getName()));
			parentField.annotate(OneToOne.class)
				.param("mappedBy", childField.name());
		} else if(otm.getKind() == MANY_TO_ONE){
			JFieldVar field = genRelationCollectionFieldVar(parentTableEntity, childTableEntity, otm.getTable().getName());
			field.annotate(OneToMany.class)
				.param("mappedBy", childField.name());
		}
		
	}
	
	private void genJoinTableRelation(JDefinedClass ownerClass, JDefinedClass inverseClass, JoinTableRelation jtr) {
		JFieldVar ownerField = genRelationCollectionFieldVar(ownerClass, inverseClass, jtr.getInverseForeignKey().getToTable());
		
		ownerField.annotate(ManyToMany.class);
		
		JAnnotationUse joinTableAnnotation = ownerField.annotate(JoinTable.class)
			.param("name", jtr.getTable().getName());
		
		JAnnotationArrayMember joinColumnsArray = joinTableAnnotation
			.paramArray("joinColumns");
		genJoinColumns(joinColumnsArray, jtr.getOwnerForeignKey().getFkColumn());
		
		boolean isBidirectional = jtr.getKind() == JoinTableRelation.Kind.MANY_TO_MANY;
		if(isBidirectional) {
			JAnnotationArrayMember inverseJoinColumnsArray = joinTableAnnotation
				.paramArray("inverseJoinColumns");
			genJoinColumns(inverseJoinColumnsArray, jtr.getInverseForeignKey().getFkColumn());

			JFieldVar inverseField = genRelationCollectionFieldVar(inverseClass, ownerClass, jtr.getOwnerForeignKey().getToTable());

			inverseField.annotate(ManyToMany.class)
				.param("mappedBy", ownerField.name());
		}
	}
	
	private boolean isColumnNullable(ColumnDto column) {
		return !"y".equals(column.getMandatory());
	}
	
	private boolean isColumnPrimaryKey(TableDto table, String columnName) {
		ColumnDto col = table.getColumn().stream()
			.filter(c -> columnName.equals(c.getName()))
			.findAny()
			.get()
			;
		return isColumnPrimaryKey(table, col);
	}
	
	private boolean isSubclassTableInJoinedHierarchy(TableDto table) {
		return findSubclassTableJoinedHierarchy(table).isPresent();
	}
	
	private Optional<HierarchyDto> findSubclassTableJoinedHierarchy(TableDto table) {
		return cfg.getHierarchy().stream()
			.filter(h -> h.getJoined() != null)
			.filter(h -> h.getJoined().getJoinRelation().stream()
				.anyMatch(jr -> jr.getTable().equals(table.getName()))
			)
			.findAny();
	}
	
	private boolean isColumnPrimaryKey(TableDto table, ColumnDto column) {
		return table.getIndex().stream()
			.filter(idx -> IndexUniqueDto.PRIMARY_KEY == idx.getUnique())
			.flatMap(idx -> idx.getColumn().stream())
			.anyMatch(col -> col.getName().equals(column.getName()));
	}
	
	private String toClassName(JPackage p, String name) {
		
		String baseName = nameProvider.javatizeName(name, true);
		
		ConfigurationDto cfg = findClassOptions(name);
		return nameProvider.findNonexistingName(cfg.getClassNamePrefix() + baseName, cfg.getClassNameSuffix(), p::isDefined);
	}

	private String toFieldName(JDefinedClass cls, String name) {
		ConfigurationDto cfg = findClassOptions(name);
		
		String baseName = nameProvider.javatizeName(name, false);
		String prefixedName = cfg.getFieldNamePrefix() + baseName;
		if(Character.isUpperCase(prefixedName.charAt(0))) {
			prefixedName = ""+Character.toLowerCase(prefixedName.charAt(0))+prefixedName.substring(1);
		}
		return nameProvider.findNonexistingName(prefixedName, cfg.getFieldNameSuffix(), cls.fields()::containsKey);
	}
	
	private ConfigurationDto findClassOptions(String name) {
		if(tableConfigurations == null) {
			tableConfigurations = 
				cfg.getTable().stream()
				.collect(Collectors.toMap(TableConfigurationDto::getTable, cfg -> cfg));
		}
		
		if(globalConfiguration == null) {
			globalConfiguration = Optional.ofNullable(cfg.getConfiguration()).orElse(new ConfigurationDto());
		}
		
		return tableConfigurations.getOrDefault(name, globalConfiguration);
	}
	
	private boolean isTableExcluded(String tableName) {
		if(excludedTables == null) {
			excludedTables = cfg.getExclude().stream()
				.filter( x -> x.getColumn() == null)
				.map(ExclusionDto::getTable)
				.collect(Collectors.toSet());
		}
		return excludedTables.contains(tableName);
	}
	
	private String toTableColumnKey(String tableName, String columnName) {
		return tableName + '|' + columnName;
	}
	
	private boolean isColumnExcluded(String tableName, String columnName) {
		if(excludedTableColumns == null) {
			excludedTableColumns = cfg.getExclude().stream()
				.filter(x -> x.getColumn() != null)
				.map(x -> toTableColumnKey(x.getTable(), x.getColumn()))
				.collect(Collectors.toSet());
		}
		TableDto table = findTable(tableName);
		return excludedTableColumns.contains(toTableColumnKey(tableName, columnName))
			|| isSubclassTableInJoinedHierarchy(table) && isColumnPrimaryKey(table, columnName)
			|| isDiscriminatorColumn(table, columnName)
			|| isForeignKeyColumn(table, columnName)
			;
	}
	
	private boolean isDiscriminatorColumn(TableDto table, String columnName) {
		// see if there is a hierarchy containing this table (root or subclass)
		// that has the given column name as discriminator
		
		return cfg.getHierarchy().stream()
			.map(HierarchyDto::getJoined)
			.filter(j -> j!=null)
			.filter(j -> j.getDiscriminateBy().getColumn().equals(columnName))
			.anyMatch(j -> 
				j.getRoot().getTable().equals(table.getName())
			)
			||
			cfg.getHierarchy().stream()
			.filter(h -> h.getSingleTable()!= null)
			.map(h -> h.getSingleTable())
			.filter(s -> s.getDiscriminateBy().getColumn().equals(columnName))
			.anyMatch(h -> 
				h.getEntity().getTable().equals(table.getName())
			);
	}
	
	private boolean isForeignKeyColumn(TableDto table, String columName) {
		return table.getFk().stream()
			.anyMatch(fk -> 
				fk.getFkColumn().stream()
				.anyMatch(fkCol -> fkCol.getName().equals(columName))
			);
	}
	
	private boolean isSupertableJoinRelation(TableDto table, String foreignKeyName) {
		// check if table is part of a joined hierarchy's join relations
		// and if one of them contains the given table in its foreign key 
		// declaration
		return cfg.getHierarchy().stream()
			.filter(h -> h.getJoined() != null)
			.flatMap(h -> h.getJoined().getJoinRelation().stream())
			.filter(jr -> jr.getTable().equals(table.getName()))
			.anyMatch(jr -> jr.getForeignKey().equals(foreignKeyName));
	}
	
	private String normalizeTypeName(String typeName) {
		return typeName.toLowerCase();
	}
	
	
	private JType mapColumnType(TableDto table, ColumnDto column) {
		String type = normalizeTypeName(column.getType());
		
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
			case "blob":
			case "binary":
			case "varbinary":
			case "bytea":
				jtype = cm.ref(Blob.class);
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
				throw new RuntimeException("no mapping found for SQL type '" + column.getType() + "'");
		}
		
		if(isColumnNullable(column) || isColumnPrimaryKey(table, column)) {
			jtype = jtype.boxify();
		}
		
		return jtype;
	}
	
	private TableDto toTable(String name) {
		return Optional.ofNullable(findTable(name))
			.orElseThrow(() -> new RuntimeException("table '" + name + "' not found"));
	}
	
	private TableDto findTable(String name) {
		if(tables == null) {
			tables = project.getSchema().getTable().stream()
				.collect(Collectors.toMap(TableDto::getName, t->t));
		}
		return tables.get(name);
	}
	
	private SequenceDto findSequence(String sequenceName) {
		if(sequences == null) {
			sequences = project.getSchema().getSequence().stream()
				.collect(Collectors.toMap(SequenceDto::getName, s->s));
		}
		return sequences.get(sequenceName);
	}
	
	private ForeignKeyDto findTableForeignKey(String tableName, String foreignKeyName) {
		return findTable(tableName).getFk().stream()
			.filter(fk -> foreignKeyName.equals(fk.getName()))
			.findAny()
			.orElse(null);
	}
	
	private ForeignKeyDto toTableForeignKey(String tableName, String foreignKeyName) {
		return Optional.ofNullable(findTableForeignKey(tableName, foreignKeyName))
			.orElseThrow(()->new RuntimeException("foreign key '" + foreignKeyName + "' not found for table '" + tableName + "'"));
	}
	
	private ChildTableRelation toChildTableRelation(ChildTableRelation.Kind kind, ChildTableRelationshipDto oneToMany) {
		TableDto table = Optional.ofNullable(findTable(oneToMany.getTable()))
			.orElseThrow(()->new RuntimeException("table not found in relation: '" + oneToMany.getTable() + "'"));
		RelationDto orel = oneToMany.getOwnerRelation();
		ForeignKeyDto fk = toTableForeignKey(table.getName(), orel.getForeignKey());
		return new ChildTableRelation(kind, table, fk, orel.getOwningEntity(), orel.getInverseEntity());
	}
	
	private JoinTableRelation toJoinTableRelation(JoinTableRelation.Kind kind, JoinTableRelationshipDto manyToMany) {
		TableDto table = Optional.ofNullable(findTable(manyToMany.getTable()))
			.orElseThrow(()->new RuntimeException("table not found in relation: '" + manyToMany.getTable() + "'"));
		
		String ownerFkName =  manyToMany.getOwnerRelation().getForeignKey();
		String ownerEntityName = manyToMany.getOwnerRelation().getOwningEntity();
		
		String inverseFkName = null;
		String inverseEntityName = null;
		if(manyToMany.getReferencedRelation() != null) {
			inverseFkName =  manyToMany.getReferencedRelation().getForeignKey();
			inverseEntityName =  manyToMany.getReferencedRelation().getInverseEntity();
		}
		
		ForeignKeyDto ownerFk = toTableForeignKey(table.getName(), ownerFkName);
		
		// NOTE: We know that there must be exactly two relations in such a table
		// that are relevant to us. For the common case, that there are exactly
		// two foreign keys in such a table, we users can omit specifying the
		// inverse side, because we can pick that ourselves.
		if(inverseFkName == null) {
			if(table.getFk().size() != 2) {
				throw new RuntimeException(String.format("cannot determine inverse side foreign key for table %s: either specify explicitely or make sure that there are exactly two foreign keys specified in this talbe", table.getName()));
			}
			// find the other foreign key name
			inverseFkName = table.getFk().stream()
				.filter(fk -> !fk.getName().equals(ownerFk.getName()))
				.map(fk -> fk.getName())
				.findAny()
				.get();
		}
		
		ForeignKeyDto inverseFk = toTableForeignKey(table.getName(), inverseFkName);
		return new JoinTableRelation(kind, table, ownerFk, ownerEntityName, inverseFk, inverseEntityName);
	}
	
	
	private List<ChildTableRelation> getChildTableRelations() {
		if(childTableRelations == null) {
			initOneToNRelations();
		}
		return childTableRelations;
	}
	
	private void initOneToNRelations() {
		// collect declared one-to-many et. al. relations
		childTableRelations = Stream.of(
			cfg.getManyToOne().stream()
				.map(otm -> toChildTableRelation(ChildTableRelation.Kind.MANY_TO_ONE, otm)),
			cfg.getUniManyToOne().stream()
				.map(otm -> toChildTableRelation(ChildTableRelation.Kind.UNI_MANY_TO_ONE, otm)),
			cfg.getUniOneToOne().stream()
				.map(otm -> toChildTableRelation(ChildTableRelation.Kind.UNI_ONE_TO_ONE, otm))
			)
			.flatMap(Function.identity())
			.collect(Collectors.toCollection(ArrayList::new));

		// get all foreign key names involved in a declared one-to-many
		Set<String> otmFkNames = childTableRelations.stream()
			.map(otm -> otm.getForeignKey().getName())
			.collect(Collectors.toSet());
		// get all foreign key names invoved in a declared many-to-many
		Set<String> mtmFkNames = getJoinTableRelations().stream()
			.flatMap(mtm -> Stream.of(mtm.getOwnerForeignKey(), mtm.getInverseForeignKey()))
			.map(ForeignKeyDto::getName)
			.collect(Collectors.toSet());
		
		// auto-collect all foreign keys into a one-to-x relation which are not
		// involved into a declared relationship (one-to-many, many-to-many,
		// or a hierarchy
		for(TableDto t : tables.values()) {
			t.getFk().stream()
				.filter(fk -> !otmFkNames.contains(fk.getName()))
				.filter(fk -> !mtmFkNames.contains(fk.getName()))
				.filter(fk -> !isSupertableJoinRelation(t, fk.getName()))
				.map(fk -> new ChildTableRelation(t, fk))
				.forEach(childTableRelations::add);
		}
	}

	private List<JoinTableRelation> getJoinTableRelations() {
		if(joinTableRelations == null) {
			joinTableRelations = Stream.of(
				cfg.getManyToMany().stream()
					.map(jtr -> toJoinTableRelation(JoinTableRelation.Kind.MANY_TO_MANY, jtr)),
				cfg.getUniManyToMany().stream()
					.map(jtr -> toJoinTableRelation(JoinTableRelation.Kind.UNI_MANY_TO_MANY, jtr))
				)
				.flatMap(Function.identity())
				.collect(Collectors.toList());
		}
		return joinTableRelations;
	}
	
	private boolean isJoinTable(String tableName) {
		if(manyToManyRelationsJoinTables == null) {
			manyToManyRelationsJoinTables = getJoinTableRelations().stream()
				.collect(Collectors.toMap(mtm->mtm.getTable().getName(), mtm->mtm));
		}
		return manyToManyRelationsJoinTables.containsKey(tableName);
	}
	
	private List<ForeignKeyColumn> findForeignKeyColumns(TableDto table, ForeignKeyDto foreignKey) {
		TableDto parentTable = toTable(foreignKey.getToTable());
		Map<String, ColumnDto> childColumns = table.getColumn().stream()
			.collect(Collectors.toMap(ColumnDto::getName, col -> col));
		Map<String, ColumnDto> parentColumns = parentTable.getColumn().stream()
			.collect(Collectors.toMap(ColumnDto::getName, col -> col));
		return foreignKey.getFkColumn().stream()
			.map(fkcol -> new ForeignKeyColumn(parentColumns.get(fkcol.getPk()), childColumns.get(fkcol.getName())))
			.collect(Collectors.toList());
	}
	
	private GenerationType findPrimaryKeyColumnGenerationStrategy(TableDto table, ColumnDto column) {
		if(!isColumnPrimaryKey(table, column)) {
			return null;
		}
		if(column.getSequence() != null) {
			return GenerationType.SEQUENCE;
		} else if("y".equals(column.getAutoincrement())) {
			return GenerationType.IDENTITY;
		} else {
			return null;
		}
	}

	private String findPrimaryKeyColumnGeneratorSequence(TableDto table, ColumnDto column) {
		if(findPrimaryKeyColumnGenerationStrategy(table, column) == GenerationType.SEQUENCE) {
			return column.getSequence();
		} else {
			return null;
		}
	}

}
