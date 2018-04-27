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
import java.util.Arrays;
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
import com.github.dbsjpagen.config.ManyToManyDto;
import com.github.dbsjpagen.config.MappingConfigDto;
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
	private List<JoinTableRelation> manyToManyRelations;
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
			
			// generate entities first that are part of a hierarchy
			for(HierarchyDto h : cfg.getHierarchy()) {
				
				if(h.getJoined() != null) {
					genJoinedHierarchy(h, p);
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
				genEntityClass(p, table);
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
			for(Map.Entry<String, JDefinedClass> e : tablesToEntities.entrySet()) {
				TableDto table = findTable(e.getKey());
				JDefinedClass cls = e.getValue();
				
				table.getColumn().stream()
					.filter((column) -> (!isColumnExcluded(table.getName(), column.getName())))
					.forEachOrdered((c) -> genColumn(cls, table, c));
			}
			
			// add one-to-many relations
			for(ChildTableRelation otm : getChildTableRelations()) {
				
				genOneToX(otm);
			}
			
			// add many-to-many relations
			for(JoinTableRelation mtm : getManyToManyRelations()) {
				JDefinedClass ownerClass = tablesToEntities.get(mtm.getOwnerForeignKey().getToTable());
				JDefinedClass referencedClass = tablesToEntities.get(mtm.getReferencedForeignKey().getToTable());
				
				String fieldName = genManyToManyOwner(ownerClass, mtm);
				genManyToManyReferenced(referencedClass, mtm, fieldName);
			}
			
			// generate accessor methods, ordered by entity class hierarchies, roots first
			Set<JDefinedClass> processed = tablesToEntities.values()
				.stream()
				.filter(this::isClassHierarchyRoot)
				.collect(Collectors.toSet());
			processed.forEach(accessorGenerator::generateAccessors);
			
			Set<JDefinedClass> remaining = new HashSet<>(tablesToEntities.values());
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
	
	private JDefinedClass genEntityClass(JPackage p, TableDto table) throws JClassAlreadyExistsException {
		return genEntityClass(p, table, null);
	}
	
	private JDefinedClass genEntityClass(JPackage p, TableDto table, JClass superClass) throws JClassAlreadyExistsException {
		JClass serializableClass = cm.ref(Serializable.class);
			
		JDefinedClass cls = p._class(JMod.PUBLIC, toClassName(p, table.getName()));
		if(superClass!=null) {
			cls._extends(superClass);
		}
		cls.annotate(Entity.class);
		cls.annotate(Table.class)
			.param("name", table.getName());
		tablesToEntities.put(table.getName(), cls);

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
	
	private void genColumn(JDefinedClass cls, TableDto table, ColumnDto column) {
		
		JType colType = mapColumnType(table, column);
		String fieldName = toFieldName(cls, column.getName());
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
		JDefinedClass rootClass = genEntityClass(pakkage, rootTable);
		hierarchyClasses.put(rootTable.getName(), rootClass);
		rootClass.annotate(cm.ref(Inheritance.class))
			.param("strategy", InheritanceType.JOINED);
		String rootDColName = h.getJoined().getDiscriminateBy().getColumn();
		ColumnDto rootdcol = rootTable.getColumn().stream()
			.filter(c -> c.getName().equals(rootDColName))
			.findAny()
			.orElseThrow(() -> new RuntimeException("discrimiator column '" + rootDColName + "' not found"));
		JType rootdcolType = mapColumnType(rootTable, rootdcol);

		JAnnotationUse au = rootClass.annotate(cm.ref(DiscriminatorColumn.class))
			.param("name", rootdcol.getName());
		if(rootdcolType instanceof JClass && ((JClass)rootdcolType).isAssignableFrom(cm.ref(String.class))) {
			au.param("length", rootdcol.getLength());
		}

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
			JDefinedClass subclassEntity = genEntityClass(pakkage, findTable(mapableRelation.getTable()), superclassEntity);
			subclassEntity.annotate(DiscriminatorValue.class)
				.param("value", mapableRelation.getDiscriminator());

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
	
	private JFieldVar genRelationCollectionFieldVar(JDefinedClass cls, String targetTableName) {
		EntityRefFactory factory = findEntityRefFactory(cls);
		JDefinedClass elementType = tablesToEntities.get(targetTableName);
		JClass fieldType = factory.getCollectionType(elementType);
		
		JFieldVar field = cls
			.field(JMod.PROTECTED, fieldType, toFieldName(cls, targetTableName));

		field.init(factory.createInitExpression());
		return field;
	}
	
	private void genOneToX(ChildTableRelation otm) {
		JDefinedClass childTableEntity = tablesToEntities.get(otm.getTable().getName());
		ForeignKeyDto ownerFk = otm.getForeignKey();
		JDefinedClass parentTableEntity = tablesToEntities.get(ownerFk.getToTable());
		
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
			genOneToOneReferenced(parentTableEntity, childTableEntity, otm.getTable().getName(), otm.getTable().getName());
			JFieldVar parentField = parentTableEntity.field(JMod.PROTECTED, childTableEntity, toFieldName(parentTableEntity, otm.getTable().getName()));
			parentField.annotate(OneToOne.class)
				.param("mappedBy", childField.name());
		} else if(otm.getKind() == MANY_TO_ONE){
			JFieldVar field = genRelationCollectionFieldVar(parentTableEntity, otm.getTable().getName());
			field.annotate(OneToMany.class)
				.param("mappedBy", childField.name());
		}
		
	}
	
	private String genManyToManyOwner(JDefinedClass cls, JoinTableRelation mtm) {
		JFieldVar field = genRelationCollectionFieldVar(cls, mtm.getReferencedForeignKey().getToTable());
		
		field.annotate(ManyToMany.class);
		
		JAnnotationUse joinTableAnnotation = field.annotate(JoinTable.class)
			.param("name", mtm.getTable().getName());
		
		JAnnotationArrayMember joinColumnsArray = joinTableAnnotation
			.paramArray("joinColumns");
		genJoinColumns(joinColumnsArray, mtm.getOwnerForeignKey().getFkColumn());
		
		JAnnotationArrayMember inverseJoinColumnsArray = joinTableAnnotation
			.paramArray("inverseJoinColumns");
		genJoinColumns(inverseJoinColumnsArray, mtm.getReferencedForeignKey().getFkColumn());
		return field.name();
	}
	
	private void genManyToManyReferenced(JDefinedClass cls, JoinTableRelation mtm, String mappedByFieldName) {
		JFieldVar field = genRelationCollectionFieldVar(cls, mtm.getOwnerForeignKey().getToTable());
		
		field.annotate(ManyToMany.class)
			.param("mappedBy", mappedByFieldName);
	}
	
	private void genOneToOneReferenced(JDefinedClass cls, JDefinedClass targetEntityClass, String sourceTableName, String mappedByFieldName) {
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
			.filter(h -> h.getJoined().getJoinRelation().stream()
				.anyMatch(j -> j.getTable().equals(table.getName()))
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
		String baseName = nameProvider.javatizeName(name, false);
		
		ConfigurationDto cfg = findClassOptions(name);
		return nameProvider.findNonexistingName(cfg.getFieldNamePrefix() + baseName, cfg.getFieldNameSuffix(), cls.fields()::containsKey);
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
			.filter(h -> h.getJoined() != null)
			.filter(h -> h.getJoined().getDiscriminateBy().getColumn().equals(columnName))
			.anyMatch(h -> 
				h.getJoined().getRoot().getTable().equals(table.getName())
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
		return Optional.of(findTableForeignKey(tableName, foreignKeyName))
			.orElseThrow(()->new RuntimeException("foreign key '" + foreignKeyName + "' not found for table '" + tableName + "'"));
	}
	
	private ChildTableRelation toChildTableRelation(ChildTableRelation.Kind kind, ChildTableRelationshipDto oneToMany) {
		TableDto table = Optional.of(findTable(oneToMany.getTable()))
			.orElseThrow(()->new RuntimeException("table not found in oneToMany relation: '" + oneToMany.getTable() + "'"));
		ForeignKeyDto fk = toTableForeignKey(table.getName(), oneToMany.getOwnerRelation().getForeignKey());
		return new ChildTableRelation(kind, table, fk);
	}
	
	private JoinTableRelation toManyToManyRelation(ManyToManyDto manyToMany) {
		TableDto table = Optional.ofNullable(findTable(manyToMany.getTable()))
			.orElseThrow(()->new RuntimeException("table not found in manyToMany relation: '" + manyToMany.getTable() + "'"));
		// NOTE: We know that there must be exactly two  relations here
		
		String ownerFkName =  manyToMany.getOwnerRelation().getForeignKey();
		String referencedFkName =  manyToMany.getReferencedRelation().getForeignKey();
		ForeignKeyDto ownerFk = findTableForeignKey(table.getName(), ownerFkName);
		ForeignKeyDto referencedFk = findTableForeignKey(table.getName(), referencedFkName);
		return new JoinTableRelation(JoinTableRelation.Kind.MANY_TO_MANY, table, ownerFk, referencedFk);
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
		Set<String> mtmFkNames = getManyToManyRelations().stream()
			.flatMap(mtm -> Stream.of(mtm.getOwnerForeignKey(), mtm.getReferencedForeignKey()))
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

	private boolean isUniqueIndexPresent(TableDto table, String... columnNameArray) {
		// check if a uniqe index is present in the given table that has the same set of columns
		// (column names) as the given columnNameArray
		Set<String> columnNames = new HashSet<>(Arrays.asList(columnNameArray));
		return table.getIndex().stream()
			.filter(idx -> !(!IndexUniqueDto.UNIQUE.equals(idx.getUnique())))
			.map((idx) -> idx.getColumn().stream()
				.map(ColumnDto::getName)
				.collect(Collectors.toSet()))
			.anyMatch((indexColumnNames) -> (columnNames.equals(indexColumnNames)));
	}
	
	private List<JoinTableRelation> getManyToManyRelations() {
		if(manyToManyRelations == null) {
			manyToManyRelations = cfg.getManyToMany().stream()
				.map(this::toManyToManyRelation)
				.collect(Collectors.toList());
		}
		return manyToManyRelations;
	}
	
	private boolean isJoinTable(String tableName) {
		if(manyToManyRelationsJoinTables == null) {
			manyToManyRelationsJoinTables = getManyToManyRelations().stream()
				.collect(Collectors.toMap(mtm->mtm.getTable().getName(), mtm->mtm));
		}
		return manyToManyRelationsJoinTables.containsKey(tableName);
	}
	
	private Map<String,ChildTableRelation> createTableColumnOneToXRelations(List<ChildTableRelation> oneToXRelations) {
		Map<String,ChildTableRelation> tableColumnOneToXRelations = new HashMap<>(); 

		for(ChildTableRelation otm : oneToXRelations) {
			for(ForeignKeyColumnDto fkColumn : otm.getForeignKey().getFkColumn()) {
				String key = toTableColumnKey(otm.getTable().getName(), fkColumn.getName());
				tableColumnOneToXRelations.put(key, otm);
			}
		}
		return tableColumnOneToXRelations;
	}
	
	
	private List<ForeignKeyColumn> findForeignKeyColumns(TableDto table, ForeignKeyDto foreignKey) {
		TableDto parentTable = findTable(foreignKey.getToTable());
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
