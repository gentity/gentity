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
import com.github.dbsjpagen.config.CollectionTableDto;
import com.sun.codemodel.JAnnotationUse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.GenerationType;
import com.github.dbsjpagen.config.ConfigurationDto;
import com.github.dbsjpagen.config.EntityTableDto;
import com.github.dbsjpagen.config.ExclusionDto;
import com.github.dbsjpagen.config.JoinTableDto;
import com.github.dbsjpagen.config.JoinedEntityTableDto;
import com.github.dbsjpagen.config.MappingConfigDto;
import com.github.dbsjpagen.config.RootEntityTableDto;
import com.github.dbsjpagen.config.SingleTableEntityDto;
import com.github.dbsjpagen.config.SingleTableHierarchyDto;
import com.github.dbsjpagen.config.TableConfigurationDto;
import com.github.dbsjpagen.config.TableFieldDto;
import com.github.dbsjpagen.config.XToOneRelationDto;
import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.dbsjpagen.dbsmodel.ForeignKeyColumnDto;
import com.github.dbsjpagen.dbsmodel.ForeignKeyDto;
import com.github.dbsjpagen.dbsmodel.ProjectDto;
import com.github.dbsjpagen.dbsmodel.SequenceDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import com.github.gentity.core.ChildTableRelation.Directionality;
import com.github.gentity.core.entities.EntityInfo;
import com.github.gentity.core.entities.JoinedRootEntityInfo;
import com.github.gentity.core.entities.JoinedSubEntityInfo;
import com.github.gentity.core.entities.PlainEntityInfo;
import com.github.gentity.core.entities.RootEntityInfo;
import com.github.gentity.core.entities.SingleTableRootEntityInfo;
import com.github.gentity.core.entities.SingleTableSubEntityInfo;
import com.github.gentity.core.fields.PlainTableFieldColumnSource;
import com.github.gentity.core.fields.SingleTableFieldColumnSource;
import com.github.gentity.core.fields.SingleTableRootFieldColumnSource;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.DatabaseModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.TableModel;
import com.github.gentity.core.model.dbs.DbsModelReader;
import com.github.gentity.core.model.dbs.Exclusions;
import javax.persistence.ForeignKey;
import java.util.function.Consumer;
import java.util.function.Function;


/**
 *
 * @author upachler
 */
public class SchemaModelImpl implements SchemaModel {
	
	private final MappingConfigDto cfg;
	private final ProjectDto project;
	
	
	private Set<String> excludedTables;
	private Set<String> excludedTableColumns;
	private Map<String, ConfigurationDto> tableConfigurations;
	private ConfigurationDto globalConfiguration;
	private Map<String, TableDto> tables;
	private Map<String, SequenceDto> sequences;
	private List<ChildTableRelation> childTableRelations;
	private List<JoinTableRelation> joinTableRelations;
	private Map<String, JoinTableRelation> manyToManyRelationsJoinTables;
	private Map<String, CollectionTableDecl> collectionTableDeclarations = new HashMap<>();
	private HashMap<SingleTableEntityDto, RootEntityTableDto> singleTableRootMap;
	private final List<EntityInfo> entityInfos = new ArrayList<>();
	private final DatabaseModel databaseSchemaModel;
	
	public SchemaModelImpl(MappingConfigDto cfg, ProjectDto project) {
		this.cfg = cfg;
		this.project = project;
		
		
		Set<String> mappedTables = new HashSet<>();
		Set<String> excludedTables = new HashSet<>();
		Map<String,Set<String>> excludedTableColumns = new HashMap<>();
		
		Exclusions exclusions = new Exclusions();
		for(ExclusionDto ex : cfg.getExclude()) {
			if(ex.getTable()!=null) {
				if(ex.getColumn() == null) {
					exclusions.addTable(ex.getTable());
				} else {
					exclusions.addTableColumn(ex.getTable(), ex.getColumn());
				}
			}
		}
		
		// TODO: abstract DbsModelReader out so that we finally are independent of the
		// DBS format
		databaseSchemaModel = new DbsModelReader(project, exclusions).read();
		
		// generate entity infos first that are declared in the mapping configuration file
		for(RootEntityTableDto et : cfg.getEntityTable()) {
			if(exclusions.isTableExcluded(et.getTable())) {
				throw new RuntimeException("configuration found for excluded table '"+et.getTable()+"'");
			}
			EntityInfo ei;
			if(et.getJoinedHierarchy()!= null) {
				ei = buildJoinedHierarchyEntityInfos(et, databaseSchemaModel);
			} else if(et.getSingleTableHierarchy() != null) {
				ei = buildSingleTableHierarchy(et, databaseSchemaModel);
			} else {
				TableModel table = databaseSchemaModel.getTable(et.getTable());
				ei = new PlainEntityInfo(table, new PlainTableFieldColumnSource(table, et), et);
				buildCollectionTableDecls(ei, et.getCollectionTable(), databaseSchemaModel);
			}
			entityInfos.add(ei);
		}
		
		
		joinTableRelations = cfg.getJoinTable().stream()
			.map(jt -> {
				JoinTableRelation.Kind kind = jt.isUnidirectional() ? JoinTableRelation.Kind.UNI_MANY_TO_MANY : JoinTableRelation.Kind.MANY_TO_MANY;
				return toJoinTableRelation(kind, jt);
			})
			.collect(Collectors.toCollection(ArrayList::new));
		initDeclaredOneToNRelations();
		
		// find table names that were mapped by configuration
		joinTableRelations.stream()
			.map(JoinTableRelation::getTable)
			.map(TableModel::getName)
			.forEach(mappedTables::add);
		mappedTables.addAll(collectionTableDeclarations.keySet());
		for(EntityInfo ei : entityInfos) {
			mappedTables.add(ei.getTable().getName());
			if(ei instanceof JoinedRootEntityInfo) {
				visitJoinedSubEntityInfos(((JoinedRootEntityInfo)ei).getChildren(), jsei -> mappedTables.add(jsei.getTable().getName()));
			}
		};
			
		// TODO: generate collection tables declared in configuration
		// NOTE: currently we're generating them while generating entity tables above..
		
		// TODO / FINISH: implement default table mappings not declared in configurations
		Set<TableModel> tablesToMap = new HashSet<>(databaseSchemaModel.getTables());
		tablesToMap.removeIf(t -> mappedTables.contains(t.getName()));
		
		// all tables with primary keys are mapped as root entities
		Set<TableModel> defaultEntityMappedTables = 
			tablesToMap.stream()
				.filter(t -> t.getPrimaryKey()!=null)
				.collect(Collectors.toSet());
		defaultEntityMappedTables
			.forEach(t -> {
				entityInfos.add(new PlainEntityInfo(t, new PlainTableFieldColumnSource(t), null));
			});
		tablesToMap.removeAll(defaultEntityMappedTables);
		
		// tables with exactly two foreign keys that cover all columns
		// are mapped as join tables
		Set<TableModel> defaultJoinTables = 
			tablesToMap.stream()
			.filter(table -> table.getForeignKeys().size()==2 && foreignKeysCoverAllColumns(table))
			.collect(Collectors.toSet());
		defaultJoinTables.forEach(table -> {
			ForeignKeyModel foreignKey1 = table.getForeignKeys().get(0);
			ForeignKeyModel foreignKey2 = table.getForeignKeys().get(1);
			
			joinTableRelations.add(new JoinTableRelation(JoinTableRelation.Kind.MANY_TO_MANY, table, foreignKey1, null, foreignKey2, null));
		});
		tablesToMap.removeAll(defaultJoinTables);
		
		// tables without a primary key but a foreign key become
		// collection tables (except for those mapped previously as join
		// tables, see above), if their first foreign key refers to a table
		// mapped as an entity
		Set<TableModel> defaultCollectionTables = new HashSet<>();
		for(TableModel table : tablesToMap) {
			if(table.getForeignKeys().isEmpty()) {
				// no foreign keys -> not a collection table
				continue;
			}
			
			// default collection tables use the first foreign key in declaration order
			ForeignKeyModel fk = table.getForeignKeys().get(0);
			EntityInfo einfo = findRootOrJoinedEntityOfTable(fk.getTargetTable());
			collectionTableDeclarations.put(table.getName(), new CollectionTableDecl(null, table, fk, einfo));

		}
		
		initDefaultOneToNRelations();
	}
	
	private EntityInfo findRootOrJoinedEntityOfTable(TableModel table) {
		return findRootOrJoinedEntityOfTableImpl(table, entityInfos);
	}
	
	private EntityInfo findRootOrJoinedEntityOfTableImpl(TableModel table, List<EntityInfo> eis) {
		for(EntityInfo ei : eis) {
			if(ei.getBaseTable() == table) {
				return ei;
			} else {
				EntityInfo result = findRootOrJoinedEntityOfTableImpl(table, ei.getChildren());
				if(result != null) {
					return result;
				}
			}
		}
		return null;
	}
	
	void visitJoinedSubEntityInfos(List<JoinedSubEntityInfo> entityInfos, Consumer<JoinedSubEntityInfo> consumer) {
		entityInfos.forEach(jsei -> consumer.accept(jsei));
		entityInfos.forEach(jsei -> visitJoinedSubEntityInfos(jsei.getChildren(), consumer));
	}
	
	private boolean foreignKeysCoverAllColumns(TableModel table) {
		Set<ColumnModel> coveredColumns = table.getForeignKeys().stream()
			.flatMap(fk -> fk.getColumnMappings().stream())
			.map(m -> m.getChildColumn())
			.collect(Collectors.toSet())
			;
		Set<ColumnModel> allColumns = new HashSet<>(table.getColumns());
		return allColumns.equals(coveredColumns);
	}
	
	private JoinedRootEntityInfo buildJoinedHierarchyEntityInfos(RootEntityTableDto rt, DatabaseModel dm) {
		TableModel rootTable = dm.getTables().stream()
			.filter(t -> t.getName().equals(rt.getTable()))
			.findAny()
			.orElseThrow(() -> new RuntimeException("root table '"+rt.getTable() + "' not found"));
		String dcolName = rt.getJoinedHierarchy().getDiscriminateBy().getColumn();
		ColumnModel dcol = rootTable.getColumns().findColumn(dcolName);
		if(dcol == null) {
			throw new RuntimeException("cannot find discriminator column '" + dcolName + "' declared for table '" + rootTable.getName() + "'");
		}
		
		String dval = rt.getJoinedHierarchy().getRoot().getDiscriminator();
		JoinedRootEntityInfo rootEInfo = new JoinedRootEntityInfo(rootTable, new PlainTableFieldColumnSource(rootTable, rt), null, dcol, dval, rt);
		buildCollectionTableDecls(rootEInfo, rt.getCollectionTable(), dm);
		
		buildJoinedHierarchySubentities(rootTable, rt, rootEInfo, rt.getJoinedHierarchy().getEntityTable(), dm);
		
		return rootEInfo;
	}

	private void buildCollectionTableDecls(EntityInfo einfo, List<CollectionTableDto> ctableDtos, DatabaseModel dm) {
		for(CollectionTableDto ctableDto : ctableDtos) {
			TableModel table = dm.getTable(ctableDto.getTable());
			if(table == null) {
				throw new RuntimeException("table '" + ctableDto.getTable() + "' declared in collection table tag not found in database model");
			}
			ForeignKeyModel fk = table.findForeignKey(ctableDto.getForeignKey());
			if(fk == null) {
				throw new RuntimeException("foreign key '" + ctableDto.getForeignKey() + "' not found in table '" + ctableDto.getTable() + "' declared in collection table tag");
			}
			collectionTableDeclarations.put(table.getName(), new CollectionTableDecl(ctableDto, table, fk, einfo));
		}
	}
	private void buildJoinedHierarchySubentities(TableModel rootTable, EntityTableDto parent, EntityInfo<JoinedSubEntityInfo> parentEntityInfo, List<JoinedEntityTableDto> subTables, DatabaseModel dm) {
		for(JoinedEntityTableDto subTable : subTables) {
			
			// use the foreign key to get to the supertable and its
			// corresponding superclass entity, and generate the
			// subclass entity from there
			TableModel table = dm.getTable(subTable.getTable());
			if(table == null) {
				throw new RuntimeException("declared join table '" + subTable.getTable() + "' not found");
			}
			ForeignKeyModel fk = table.findForeignKey(subTable.getForeignKey());
			if(!fk.getTargetTable().equals(parentEntityInfo.getTable())) {
				throw new RuntimeException(String.format("specified foreign key %s of table %s refers to table %s, but the supertable is %s", fk.getName(), subTable.getTable(), fk.getTargetTable().getName(), parent.getTable()));
			}
			EntityInfo einfo = new JoinedSubEntityInfo(table, rootTable, new PlainTableFieldColumnSource(table, subTable), parentEntityInfo, fk, subTable.getDiscriminator(), subTable);
			buildCollectionTableDecls(einfo, subTable.getCollectionTable(), dm);
			buildJoinedHierarchySubentities(rootTable, subTable, einfo, subTable.getEntityTable(), dm);
		}
	}
	
	private SingleTableRootEntityInfo buildSingleTableHierarchy(RootEntityTableDto rootEntity, DatabaseModel dm) {
		SingleTableHierarchyDto h = rootEntity.getSingleTableHierarchy();
		TableModel rootTable = dm.getTable(rootEntity.getTable());
		String dcolName = h.getDiscriminateBy().getColumn();
		ColumnModel dcol = rootTable.getColumns().findColumn(dcolName);
		if(dcol == null) {
			throw new RuntimeException("could not find discriminator column '" + dcolName + "' in table '" + rootTable.getName() + "'");
		}
		String dval = h.getRoot().getDiscriminator();
		checkEachFieldOnlyOnce(rootTable, h.getEntity());
		SingleTableRootEntityInfo einfo = new SingleTableRootEntityInfo(rootTable, new SingleTableRootFieldColumnSource(rootTable, rootEntity), null, dcol, dval, rootEntity);
		
		buildSingleTableChildEntities(rootTable, einfo, h.getEntity(), dm);
		buildCollectionTableDecls(einfo, rootEntity.getCollectionTable(), dm);
		return einfo;
	}
	
	private void buildSingleTableChildEntities(TableModel rootTable, EntityInfo parentEntityInfo, List<SingleTableEntityDto> entities, DatabaseModel dm) {
		for(SingleTableEntityDto entity : entities) {
			String dval = entity.getDiscriminator();
			EntityInfo einfo = new SingleTableSubEntityInfo(entity.getName(), rootTable, new SingleTableFieldColumnSource(rootTable, entity), parentEntityInfo, dval, entity);
			buildSingleTableChildEntities(rootTable, einfo, entity.getEntity(), dm);
			buildCollectionTableDecls(einfo, entity.getCollectionTable(), dm);
		}
	}
	
	private void fillPrimaryKeyJoinColumn(JAnnotationUse primaryKeyJoinColumnUse, ForeignKeyDto fk, ForeignKeyColumnDto fkCol) {
		primaryKeyJoinColumnUse
			.param("name", fkCol.getName())
			.param("referencedColumnName", fkCol.getPk())
			.annotationParam("foreignKey", ForeignKey.class)
				.param("name", fk.getName());
	}
	

	private void checkEachFieldOnlyOnce(TableModel table, List<SingleTableEntityDto> entities) {
		for (SingleTableEntityDto entity : entities) {
			Set<String> colMap = table.getColumns().stream()
				.map(c -> c.getName())
				.collect(Collectors.toSet());
			checkEachFieldOnlyOnceImpl(table, colMap, new HashSet<>(), entity.getEntity());
		}
	}
	
	private void checkEachFieldOnlyOnceImpl(TableModel table, Set<String> colSet, Set<String> usedColNames, List<SingleTableEntityDto> entities) {
		for(SingleTableEntityDto entity : entities) {
			for(TableFieldDto f : entity.getField()) {
				if(!colSet.contains(f.getColumn())) {
					throw new RuntimeException(String.format("Specified field column %s does not exist in table %s", f.getColumn(), table.getName()));
				}
				if(!usedColNames.add(f.getColumn())) {
					throw new RuntimeException(String.format("duplicate column name %s found in single table hierarchy of root table %s", f.getColumn(), table.getName()));
				}
			}
		
			checkEachFieldOnlyOnceImpl(table, colSet, usedColNames, entity.getEntity());
		}
	}
	
	@Override
	public boolean isColumnNullable(ColumnDto column) {
		return !"y".equals(column.getMandatory());
	}
	
	boolean isColumnPrimaryKey(TableModel table, String columnName) {
		ColumnModel col = table.getColumns().stream()
			.filter(c -> columnName.equals(c.getName()))
			.findAny()
			.get()
			;
		return isColumnPrimaryKey(table, col);
	}
	
	private boolean isSubclassTableInJoinedHierarchy(TableModel table) {
		return cfg.getEntityTable().stream()
			.filter(et -> et.getJoinedHierarchy() != null)
			.flatMap(et -> et.getJoinedHierarchy().getEntityTable().stream())
			.anyMatch(jt -> containsJoinedHierarchySubclassTable(jt, table.getName()));
	}
	
	private boolean containsJoinedHierarchySubclassTable(JoinedEntityTableDto jt, String tableName) {
		return jt.getTable().equals(tableName)
			|| jt.getEntityTable().stream()
				.anyMatch(subJt -> containsJoinedHierarchySubclassTable(subJt, tableName));
	}
	
	@Override
	public boolean isColumnPrimaryKey(TableModel table, ColumnModel column) {
		return Optional.ofNullable(table.getPrimaryKey())
			.map(pk -> pk.findColumn(column.getName()))
			.isPresent();
	}
	
	@Override
	public ConfigurationDto findClassOptions(String name) {
		if(tableConfigurations == null) {
			tableConfigurations = new HashMap<>(Stream.of(
				cfg.getJoinTable().stream(),
				cfg.getEntityTable().stream()
			)
			.flatMap(t -> t)
			.collect(Collectors.toMap(TableConfigurationDto::getTable, cfg -> cfg)));
			
			Consumer<EntityTableDto> addOp = et -> {
				tableConfigurations.put(et.getTable(), et);
				// FIXME: add recursive .foreach() here to add subtables
			};
		}
		
		if(globalConfiguration == null) {
			globalConfiguration = Optional.ofNullable(cfg.getConfiguration()).orElse(new ConfigurationDto());
		}
		
		return tableConfigurations.getOrDefault(name, globalConfiguration);
	}
	
	public boolean isTableExcluded(String tableName) {
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
	
	@Override
	public boolean isColumnExcluded(String tableName, String columnName) {
		if(excludedTableColumns == null) {
			excludedTableColumns = cfg.getExclude().stream()
				.filter(x -> x.getColumn() != null)
				.map(x -> toTableColumnKey(x.getTable(), x.getColumn()))
				.collect(Collectors.toSet());
		}
		return excludedTableColumns.contains(toTableColumnKey(tableName, columnName));
	}
	
	@Override
	public boolean isColumnIgnored(String tableName, String columnName) {
		TableModel table = findTable(tableName);
		return isColumnExcluded(tableName, columnName)
			|| isSubclassTableInJoinedHierarchy(table) && isColumnPrimaryKey(table, columnName)
			|| isDiscriminatorColumn(table, columnName)
			|| isForeignKeyColumn(table, columnName)
			;
	}
	
	private boolean isDiscriminatorColumn(TableModel table, String columnName) {
		// see if there is a hierarchy containing this table (root or subclass)
		// that has the given column name as discriminator
		
		return cfg.getEntityTable().stream()
			.filter(j -> j.getJoinedHierarchy()!=null)
			.filter(j -> j.getJoinedHierarchy().getDiscriminateBy().getColumn().equals(columnName))
			.anyMatch(j -> 
				j.getTable().equals(table.getName())
			)
			||
			cfg.getEntityTable().stream()
			.filter(h -> h.getSingleTableHierarchy()!= null)
			.filter(s -> s.getSingleTableHierarchy().getDiscriminateBy().getColumn().equals(columnName))
			.anyMatch(h -> 
				h.getTable().equals(table.getName())
			);
	}
	
	private boolean isForeignKeyColumn(TableModel table, String columName) {
		return table.getForeignKeys().stream()
			.anyMatch(fk -> 
				fk.getColumns().stream()
				.anyMatch(fkCol -> fkCol.getName().equals(columName))
			);
	}
	
	private boolean containsSupertableJoinRelation(JoinedEntityTableDto et, String tableName, String foreignKeyName) {
		if(et.getForeignKey().equals(foreignKeyName) && et.getTable().equals(tableName)) {
			return true;
		}
		return et.getEntityTable().stream()
			.anyMatch(subEt -> containsSupertableJoinRelation(subEt, tableName, foreignKeyName));
	}
	private boolean isSupertableJoinRelation(TableModel table, String foreignKeyName) {
		// check if table is part of a joined hierarchy's join relations
		// and if one of them contains the given table in its foreign key 
		// declaration
		return cfg.getEntityTable().stream()
			.filter(h -> h.getJoinedHierarchy()!= null)
			.flatMap(h -> h.getJoinedHierarchy().getEntityTable().stream())
			.anyMatch(et -> containsSupertableJoinRelation(et, table.getName(), foreignKeyName));
	}
	
	
	@Override
	public TableModel findTable(String name) {
		return databaseSchemaModel.getTable(name);
	}

	Set<SingleTableEntityDto> findSingleTableEntities(List<SingleTableEntityDto> sts) {
		Set<SingleTableEntityDto> set = new HashSet<>();
		for(SingleTableEntityDto st : sts) {
			set.add(st);
			set.addAll(findSingleTableEntities(st.getEntity()));
		}
		return set;
	}
	
	@Override
	public RootEntityTableDto findParentRootEntityTable(SingleTableEntityDto singleTableEntity) {
		if(singleTableRootMap == null) {
			singleTableRootMap = new HashMap<>();
			for(RootEntityTableDto et : cfg.getEntityTable()) {
				if(et.getSingleTableHierarchy() == null) {
					continue;
				}
				findSingleTableEntities(et.getSingleTableHierarchy().getEntity())
					.forEach(st -> singleTableRootMap.put(st, et));
			}
		}
		return singleTableRootMap.get(singleTableEntity);
	}
	
	@Override
	public SequenceDto getSequence(String sequenceName) {
		if(sequences == null) {
			sequences = project.getSchema().getSequence().stream()
				.collect(Collectors.toMap(SequenceDto::getName, s->s));
		}
		return sequences.get(sequenceName);
	}
	
	@Override
	public ForeignKeyModel findTableForeignKey(String tableName, String foreignKeyName) {
		return Optional.ofNullable(findTable(tableName))
			.map(table -> table.findForeignKey(foreignKeyName))
			.orElse(null);
	}
	
	private ChildTableRelation toChildTableRelation(ChildTableRelation.Kind kind, String tableName, String entityName, XToOneRelationDto xToMany) {
		if(isTableExcluded(tableName)) {
			throw new RuntimeException(String.format("table %s is excluded, cannot add relation for foreign key %s", tableName, xToMany.getForeignKey()));
		}
		
		TableModel table = Optional.ofNullable(findTable(tableName))
			.orElseThrow(()->new RuntimeException("table not found in relation: '" + tableName + "'"));
		ForeignKeyModel fk = toTableForeignKey(table.getName(), xToMany.getForeignKey());
		
		if(containsExcludedTableColumns(tableName, fk)) {
			throw new RuntimeException(String.format("foreign key %s of table %s contains excluded column(s)", fk.getName(), tableName));
		};

		return new ChildTableRelation(kind, table, fk, entityName, xToMany.getInverseEntity());
	}
	
	private boolean containsExcludedTableColumns(String tableName, ForeignKeyModel fk) {
		return fk.getColumns().stream()
			.anyMatch(col -> isColumnExcluded(tableName, col.getName()));
	}
	
	private boolean containsIgnoredTableColumns(String tableName, ForeignKeyModel fk) {
		return fk.getColumns().stream()
			.anyMatch(col -> isColumnIgnored(tableName, col.getName()));
	}
	
	private JoinTableRelation toJoinTableRelation(JoinTableRelation.Kind kind, JoinTableDto manyToMany) {
		TableModel table = Optional.ofNullable(databaseSchemaModel.getTable(manyToMany.getTable()))
			.orElseThrow(()->new RuntimeException("table not found in relation: '" + manyToMany.getTable() + "'"));
		
		String ownerFkName =  manyToMany.getOwnerRelation().getForeignKey();
		String ownerEntityName = manyToMany.getOwnerRelation().getOwningEntity();
		
		String inverseFkName = null;
		String inverseEntityName = null;
		if(manyToMany.getInverseRelation() != null) {
			inverseFkName =  manyToMany.getInverseRelation().getForeignKey();
			inverseEntityName =  manyToMany.getInverseRelation().getInverseEntity();
		}
		
		ForeignKeyModel ownerFk = toTableForeignKey(table.getName(), ownerFkName);
		
		// NOTE: We know that there must be exactly two relations in such a table
		// that are relevant to us. For the common case that there are exactly
		// two foreign keys in such a table, users can omit specifying the
		// inverse side, because we can pick that ourselves.
		if(inverseFkName == null) {
			if(table.getForeignKeys().size() != 2) {
				throw new RuntimeException(String.format("cannot determine inverse side foreign key for table %s: either specify explicitely or make sure that there are exactly two foreign keys specified in this table", table.getName()));
			}
			// find the other foreign key name
			inverseFkName = table.getForeignKeys().stream()
				.filter(fk -> !fk.getName().equals(ownerFk.getName()))
				.map(fk -> fk.getName())
				.findAny()
				.get();
		}
		
		ForeignKeyModel inverseFk = toTableForeignKey(table.getName(), inverseFkName);
		return new JoinTableRelation(kind, table, ownerFk, ownerEntityName, inverseFk, inverseEntityName);
	}
	
	
	@Override
	public List<ChildTableRelation> getChildTableRelations() {
		if(childTableRelations == null) {
			initOneToNRelations();
		}
		return childTableRelations;
	}
	
	List<ChildTableRelation> toChildTableRelations(String tableName, String entityName, List<XToOneRelationDto> mtos, List<XToOneRelationDto> otos) {
		return Stream.of(mtos.stream()
			.map(mto -> {
				ChildTableRelation.Kind kind = mto.isBidirectional() ? ChildTableRelation.Kind.MANY_TO_ONE : ChildTableRelation.Kind.UNI_MANY_TO_ONE;
				return toChildTableRelation(kind, tableName, entityName, mto);
			}),
			otos.stream()
			.map(oto -> {
				ChildTableRelation.Kind kind = oto.isBidirectional() ? ChildTableRelation.Kind.ONE_TO_ONE : ChildTableRelation.Kind.UNI_ONE_TO_ONE;
				return toChildTableRelation(kind, tableName, entityName, oto);
			})
		)
		.flatMap(s -> s)
		.collect(Collectors.toList());
	}
	
	private List<ChildTableRelation> toChildTableRelationsJoined(JoinedEntityTableDto et) {
		ArrayList<ChildTableRelation> rels = new ArrayList<>();
		rels.addAll(toChildTableRelations(et.getTable(), null, et.getManyToOne(), et.getOneToOne()));
		et.getEntityTable().stream()
			.map(jt -> toChildTableRelationsJoined(jt))
			.forEach(rels::addAll);
		
		return rels;
	}
	
	private List<ChildTableRelation> toChildTableRelationsSingleTable(String tableName, SingleTableEntityDto et) {
		ArrayList<ChildTableRelation> rels = new ArrayList<>();
		rels.addAll(toChildTableRelations(tableName, et.getName(), et.getManyToOne(), et.getOneToOne()));
		et.getEntity().stream()
			.map(jt -> toChildTableRelationsSingleTable(tableName, jt))
			.forEach(rels::addAll);
		
		return rels;
	}
	
	private void initOneToNRelations() {
		initDeclaredOneToNRelations();
		initDefaultOneToNRelations();
	}
	
	private void initDeclaredOneToNRelations() {
		// collect declared one-to-many et. al. relations
		childTableRelations = new ArrayList();
		
		for(RootEntityTableDto rt : cfg.getEntityTable()) {
			childTableRelations.addAll(toChildTableRelations(rt.getTable(), null, rt.getManyToOne(), rt.getOneToOne()));
			if(rt.getJoinedHierarchy() != null) {
				rt.getJoinedHierarchy().getEntityTable().stream()
					.map(jt -> toChildTableRelationsJoined(jt))
					.forEach(childTableRelations::addAll);
			} else if(rt.getSingleTableHierarchy() != null) {
				rt.getSingleTableHierarchy().getEntity().stream()
					.map(st -> toChildTableRelationsSingleTable(rt.getTable(), st))
					.forEach(childTableRelations::addAll);
			}
		}
	}
	
	private void initDefaultOneToNRelations() {
		// get all foreign key names involved in a declared one-to-many
		Set<String> otmFkNames = childTableRelations.stream()
			.map(otm -> otm.getForeignKey().getName())
			.collect(Collectors.toSet());
		// get all foreign key names invoved in a declared many-to-many
		Set<String> mtmFkNames = getJoinTableRelations().stream()
			.flatMap(mtm -> Stream.of(mtm.getOwnerForeignKey(), mtm.getInverseForeignKey()))
			.map(ForeignKeyModel::getName)
			.collect(Collectors.toSet());
		
		// auto-collect all foreign keys into a one-to-x relation which are not
		// involved into a declared relationship (one-to-many, many-to-many,
		// or a hierarchy
		for(TableModel t : databaseSchemaModel.getTables()) {
			if(isTableExcluded(t.getName())) {
				continue;
			}
			
			CollectionTableDecl collectionTable = getCollectionTableDeclaration(t.getName());
			
			Directionality defaultDirectionality = collectionTable == null
				?	ChildTableRelation.Directionality.BIDIRECTIONAL
				:	ChildTableRelation.Directionality.UNIDIRECTIONAL;
			
			for(ForeignKeyModel fk : t.getForeignKeys()) {
				if(otmFkNames.contains(fk.getName()) || mtmFkNames.contains(fk.getName())) {
					// foreign key already used in a child table or join table relationship,
					// possibly pre-declared
					continue;
				}
				if(isSupertableJoinRelation(t, fk.getName())) {
					// foreign key forms a relation to a table which is mapped
					// to a an entity representing a supertype to of the entity
					// to which this table is mapped
					continue;
				}
				if(containsExcludedTableColumns(t.getName(), fk)) {
					// excluding a foreign key column will exclude a depending
					// foreign key with it
					continue;
				}
				if(collectionTable!=null && collectionTable.getForeignKey().getName().equals(fk.getName())) {
					// the foreign key represents the relation binding a 
					// collection table to its entity
					continue;
				}
				childTableRelations.add(new ChildTableRelation(t, fk, defaultDirectionality));
			}
		}
	}

	@Override
	public List<JoinTableRelation> getJoinTableRelations() {
		return joinTableRelations;
	}
	
	@Override
	public boolean isJoinTable(String tableName) {
		if(manyToManyRelationsJoinTables == null) {
			manyToManyRelationsJoinTables = getJoinTableRelations().stream()
				.collect(Collectors.toMap(mtm->mtm.getTable().getName(), mtm->mtm));
		}
		return manyToManyRelationsJoinTables.containsKey(tableName);
	}

	@Override
	public boolean isCollectionTable(String tableName) {
		return getCollectionTableDeclaration(tableName) != null;
	}
	
	@Override
	public CollectionTableDecl getCollectionTableDeclaration(String tableName) {
		return collectionTableDeclarations.get(tableName);
	}
	
	@Override
	public GenerationType findPrimaryKeyColumnGenerationStrategy(TableModel table, ColumnModel column) {
		if(!isColumnPrimaryKey(table, column)) {
			return null;
		}
		if(column.getSequence() != null) {
			return GenerationType.SEQUENCE;
		} else if(column.isIdentityColumn()) {
			return GenerationType.IDENTITY;
		} else {
			return null;
		}
	}

	@Override
	public List<EntityInfo> getRootEntityDefinitions() {
		return entityInfos.stream()
			.filter(ei -> (ei instanceof RootEntityInfo) || (ei instanceof PlainEntityInfo))
			.collect(Collectors.toList());
	}
	
	@Override
	public String getTargetPackageName() {
		return cfg.getTargetPackageName();
	}

	@Override
	public String getDefaultExtends() {
		return Optional.ofNullable(cfg.getConfiguration())
			.map(ConfigurationDto::getExtends)
			.orElse(null);
	}

	@Override
	public List<String> getDefaultImplements() {
		return Optional.ofNullable(cfg.getConfiguration())
			.map(ConfigurationDto::getImplements)
			.orElse(null);
	}
	
	@Override
	public TableModel toTable(String name) {
		return Optional.ofNullable(findTable(name))
			.orElseThrow(() -> new RuntimeException("table '" + name + "' not found"));
	}
	
	@Override
	public ForeignKeyModel toTableForeignKey(String tableName, String foreignKeyName) {
		return Optional.ofNullable(findTableForeignKey(tableName, foreignKeyName))
			.orElseThrow(()->new RuntimeException("foreign key '" + foreignKeyName + "' not found for table '" + tableName + "'"));
	}

	@Override
	public <T> CollectionTableTravesal<T> collectionTableTravesalOf(Function<RootEntityTableDto, T> rootContextProvider, Function<JoinedEntityTableDto, T> joinedContextProvider, Function<SingleTableEntityDto, T> singleTableContextProvider) {
		return CollectionTableTravesal.of(cfg, rootContextProvider, joinedContextProvider, singleTableContextProvider);
	}
	
}
