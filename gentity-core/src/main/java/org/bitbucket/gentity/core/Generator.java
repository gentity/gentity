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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.bitbucket.dbsjpagen.config.ConfigurationDto;
import org.bitbucket.dbsjpagen.config.ExclusionDto;
import org.bitbucket.dbsjpagen.config.ManyToManyDto;
import org.bitbucket.dbsjpagen.config.MappingConfigDto;
import org.bitbucket.dbsjpagen.config.OneToManyDto;
import org.bitbucket.dbsjpagen.config.TableConfigurationDto;
import org.bitbucket.dbsjpagen.dbsmodel.ColumnDto;
import org.bitbucket.dbsjpagen.dbsmodel.ForeignKeyColumnDto;
import org.bitbucket.dbsjpagen.dbsmodel.ForeignKeyDto;
import org.bitbucket.dbsjpagen.dbsmodel.IndexUniqueDto;
import org.bitbucket.dbsjpagen.dbsmodel.ProjectDto;
import org.bitbucket.dbsjpagen.dbsmodel.TableDto;

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

	private NameProvider nameProvider;
	private Set<String> excludedTables;
	private Set<String> excludedTableColumns;
	private Map<String, ConfigurationDto> tableConfigurations;
	private ConfigurationDto globalConfiguration;
	private Map<String, TableDto> tables;
	private HashMap<String, OneToManyRelation> tableColumnOneToManyRelations;
	private List<OneToManyRelation> oneToManyRelations;
	private Map<String, List<OneToManyRelation>> oneToManyRelationReferences;
	private List<ManyToManyRelation> manyToManyRelations;
	private Map<String, ManyToManyRelation> manyToManyRelationsJoinTables;
	
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
			JClass serializableClass = cm.ref(Serializable.class);
			
			tablesToEntities = new HashMap<>();
			
			// filter tables and generate empty entity classes
			for(TableDto table : project.getSchema().getTable()) {
				if(isTableExcluded(table.getName()) || isJoinTable(table.getName())) {
					continue;
				}
				JDefinedClass cls = p._class(JMod.PUBLIC, toClassName(p, table.getName()));
				tablesToEntities.put(table.getName(), cls);
				
				cls._implements(serializableClass);
			}
			
			// process table columns
			for(Map.Entry<String, JDefinedClass> e : tablesToEntities.entrySet()) {
				TableDto table = findTable(e.getKey());
				JDefinedClass cls = e.getValue();
				cls.annotate(Entity.class);
				cls.annotate(Table.class)
					.param("name", table.getName());
				
				table.getColumn().stream()
					.filter((column) -> (!isColumnExcluded(table.getName(), column.getName())))
					.forEachOrdered((c) -> genColumn(cls, table, c));
			}
			
			// add one-to-many relations
			for(OneToManyRelation otm : getOneToManyRelations()) {
				JDefinedClass cls = tablesToEntities.get(otm.getForeignKey().getToTable());
				
				genOneToMany(cls, otm);
			}
			
			// add many-to-manh relations
			for(ManyToManyRelation mtm : getManyToManyRelations()) {
				JDefinedClass ownerClass = tablesToEntities.get(mtm.getOwnerForeignKey().getToTable());
				JDefinedClass referencedClass = tablesToEntities.get(mtm.getReferencedForeignKey().getToTable());
				
				String fieldName = genManyToManyOwner(ownerClass, mtm);
				genManyToManyReferenced(referencedClass, mtm, fieldName);
			}
			
			// generate accessor methods
			tablesToEntities.values().forEach(accessorGenerator::generateAccessors);
			
		} catch(JClassAlreadyExistsException caex) {
			throw new RuntimeException(caex);
		}
		
		return cm;
	}
	
	private void genColumn(JDefinedClass cls, TableDto table, ColumnDto column) {
		
		String fieldName = toFieldName(cls, column.getName());
		
		OneToManyRelation owner = findOneToManyRelationOwner(table, column);
		
		JType colType;
		if(owner == null) {
			colType = mapColumnType(column);
		} else {
			colType = tablesToEntities.get(owner.getForeignKey().getToTable());
		}
		JFieldVar field = cls.field(JMod.PRIVATE, colType, fieldName);
		
		// @Id
		if(isColumnPrimaryKey(table, column)) {
			field.annotate(Id.class);
		}
		
		// @Column or @JoinColumn
		if(owner == null) {
			// @Column
			JAnnotationUse columnAnnotation = field
				.annotate(Column.class);
			columnAnnotation
				.param("name", column.getName());
			if(colType.equals(cm._ref(String.class))) {
				columnAnnotation.param("length", column.getLength());
			}
			if(!isColumnNullable(column)) {
				columnAnnotation.param("nullable", false);
			}
		} else {
			// @ManyToOne
			field.annotate(ManyToOne.class);
			
			List<ForeignKeyColumnDto> fkColumns = owner.getForeignKey().getFkColumn();
			if(fkColumns.size() == 1) {
				// @JoinColumn
				field
					.annotate(JoinColumn.class)
					.param("name", fkColumns.get(0).getName());
			} else {
				// @JoinColumns (plural!)
				JAnnotationArrayMember valueAnnotation = field
					.annotate(JoinColumns.class)
					.paramArray("value");
				genJoinColumns(valueAnnotation, fkColumns);
			}
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
	
	private JFieldVar genRelationFieldVar(JDefinedClass cls, String targetTableName) {
		EntityRefFactory factory = findEntityRefFactory(cls);
		JDefinedClass elementType = tablesToEntities.get(targetTableName);
		JClass fieldType = factory.getCollectionType(elementType);
		
		JFieldVar field = cls
			.field(JMod.PRIVATE, fieldType, toFieldName(cls, targetTableName));

		field.init(factory.createInitExpression());
		return field;
	}
	
	private void genOneToMany(JDefinedClass cls, OneToManyRelation otm) {
		JFieldVar field = genRelationFieldVar(cls, otm.getTable().getName());
		field.annotate(OneToMany.class);
	}
	
	private String genManyToManyOwner(JDefinedClass cls, ManyToManyRelation mtm) {
		JFieldVar field = genRelationFieldVar(cls, mtm.getReferencedForeignKey().getToTable());
		
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
	
	private void genManyToManyReferenced(JDefinedClass cls, ManyToManyRelation mtm, String mappedByFieldName) {
		JFieldVar field = genRelationFieldVar(cls, mtm.getOwnerForeignKey().getToTable());
		
		field.annotate(ManyToMany.class)
			.param("mappedBy", mappedByFieldName);
	}
	
	private boolean isColumnNullable(ColumnDto column) {
		return !"y".equals(column.getMandatory());
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
		return excludedTableColumns.contains(toTableColumnKey(tableName, columnName));
	}

	
	private String normalizeTypeName(String typeName) {
		return typeName.toLowerCase();
	}
	
	
	private JType mapColumnType(ColumnDto column) {
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
			case "bytea":
				jtype = cm.ref(Blob.class);
				break;
			case "timestamp":
				jtype = cm.ref(Timestamp.class);
				break;
			default:
				throw new RuntimeException("no mapping found for SQL type '" + column.getType() + "'");
		}
		
		if(isColumnNullable(column)) {
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
	
	private ForeignKeyDto findTableForeignKey(String tableName, String columnName) {
		return findTable(tableName).getFk().stream()
			.filter(fk -> columnName.equals(fk.getName()))
			.findAny()
			.orElse(null);
	}
	
	private ForeignKeyDto toTableForeignKey(String tableName, String foreignKeyName) {
		return Optional.of(findTableForeignKey(tableName, foreignKeyName))
			.orElseThrow(()->new RuntimeException("foreign key '" + foreignKeyName + "' not found for table '" + tableName + "'"));
	}
	
	private OneToManyRelation toOneToManyRelation(OneToManyDto oneToMany) {
		TableDto table = Optional.of(findTable(oneToMany.getTable()))
			.orElseThrow(()->new RuntimeException("table not found in oneToMany relation: '" + oneToMany.getTable() + "'"));
		ForeignKeyDto fk = toTableForeignKey(table.getName(), oneToMany.getOwnerRelation().getForeignKey());
		return new OneToManyRelation(table, fk);
	}
	
	private ManyToManyRelation toManyToManyRelation(ManyToManyDto manyToMany) {
		TableDto table = Optional.ofNullable(findTable(manyToMany.getTable()))
			.orElseThrow(()->new RuntimeException("table not found in manyToMany relation: '" + manyToMany.getTable() + "'"));
		// NOTE: We know that there must be exactly two  relations here
		
		String ownerFkName =  manyToMany.getOwnerRelation().getForeignKey();
		String referencedFkName =  manyToMany.getReferencedRelation().getForeignKey();
		ForeignKeyDto ownerFk = findTableForeignKey(table.getName(), ownerFkName);
		ForeignKeyDto referencedFk = findTableForeignKey(table.getName(), referencedFkName);
		return new ManyToManyRelation(table, ownerFk, referencedFk);
	}
	
	private List<OneToManyRelation> getOneToManyRelations() {
		if(oneToManyRelations == null) {
			oneToManyRelations = cfg.getOneToMany().stream()
				.map(this::toOneToManyRelation)
				.collect(Collectors.toCollection(ArrayList::new));
			
			Set<String> otmFkNames = oneToManyRelations.stream()
				.map(otm -> otm.getForeignKey().getName())
				.collect(Collectors.toSet());
			Set<String> mtmFkNames = getManyToManyRelations().stream()
				.flatMap(mtm -> Stream.of(mtm.getOwnerForeignKey(), mtm.getReferencedForeignKey()))
				.map(ForeignKeyDto::getName)
				.collect(Collectors.toSet());
			
			for(TableDto t : tables.values()) {
				t.getFk().stream()
					.filter(fk -> !otmFkNames.contains(fk.getName()))
					.filter(fk -> !mtmFkNames.contains(fk.getName()))
					.map(fk -> new OneToManyRelation(t, fk))
					.forEach(oneToManyRelations::add);
			}
		}
		return oneToManyRelations;
	}
	
	private List<ManyToManyRelation> getManyToManyRelations() {
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
	
	private OneToManyRelation findOneToManyRelationOwner(TableDto table, ColumnDto column) {
		if(tableColumnOneToManyRelations == null) {
			tableColumnOneToManyRelations = new HashMap<>(); 
			
			for(OneToManyRelation otm : getOneToManyRelations()) {
				for(ForeignKeyColumnDto fkColumn : otm.getForeignKey().getFkColumn()) {
					String key = toTableColumnKey(otm.getTable().getName(), fkColumn.getName());
					tableColumnOneToManyRelations.put(key, otm);
				}
			}
		}
		
		return tableColumnOneToManyRelations.get(toTableColumnKey(table.getName(), column.getName()));
	}

	private List<OneToManyRelation> findOneToManyRelationReferences(TableDto table) {
		if(oneToManyRelationReferences == null) {
			oneToManyRelationReferences = getOneToManyRelations().stream()
				.collect(Collectors.groupingBy((otm -> otm.getForeignKey().getToTable())));
		}
		return oneToManyRelationReferences.get(table.getName());
	}

}
