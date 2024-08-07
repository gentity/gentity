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

import com.github.gentity.core.config.dto.CascadeTypeDto;
import com.github.gentity.core.entities.CollectionTableDecl;
import com.github.gentity.core.config.dto.CollectionTableDto;
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
import jakarta.persistence.GenerationType;
import com.github.gentity.core.config.dto.ConfigurationDto;
import com.github.gentity.core.config.dto.EntityTableDto;
import com.github.gentity.core.config.dto.ExclusionDto;
import com.github.gentity.core.config.dto.GlobalConfigurationDto;
import com.github.gentity.core.config.dto.InverseTargetRelationDto;
import com.github.gentity.core.config.dto.JoinTableDto;
import com.github.gentity.core.config.dto.JoinedEntityTableDto;
import com.github.gentity.core.config.dto.MappingConfigDto;
import com.github.gentity.core.config.dto.OwnerTargetRelationDto;
import com.github.gentity.core.config.dto.RootEntityTableDto;
import com.github.gentity.core.config.dto.SingleTableEntityDto;
import com.github.gentity.core.config.dto.SingleTableHierarchyDto;
import com.github.gentity.core.config.dto.TableConfigurationDto;
import com.github.gentity.core.config.dto.TableFieldDto;
import com.github.gentity.core.config.dto.XToOneRelationDto;
import com.github.gentity.core.model.dbs.dto.ColumnDto;
import com.github.gentity.core.model.dbs.dto.ForeignKeyColumnDto;
import com.github.gentity.core.model.dbs.dto.ForeignKeyDto;
import com.github.gentity.core.model.dbs.dto.TableDto;
import com.github.gentity.core.entities.EntityInfo;
import com.github.gentity.core.entities.JoinedRootEntityInfo;
import com.github.gentity.core.entities.JoinedSubEntityInfo;
import com.github.gentity.core.entities.PlainEntityInfo;
import com.github.gentity.core.entities.HierarchyRootEntityInfo;
import com.github.gentity.core.entities.RootEntityInfo;
import com.github.gentity.core.entities.SingleTableRootEntityInfo;
import com.github.gentity.core.entities.SingleTableSubEntityInfo;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.DatabaseModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.ModelReader;
import com.github.gentity.core.model.TableColumnGroup;
import com.github.gentity.core.model.TableModel;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import jakarta.persistence.ForeignKey;
import java.util.function.Consumer;
import java.util.function.Function;
import jakarta.persistence.CascadeType;


/**
 *
 * @author upachler
 */
public class SchemaModelImpl implements SchemaModel {
	
	private final MappingConfigDto cfg;
	private final ShellLogger logger;
	
	
	private Set<String> excludedTables;
	private Set<String> excludedTableColumns;
	private Map<String, ConfigurationDto> tableConfigurations;
	private ConfigurationDto globalConfiguration;
	private Map<String, TableDto> tables;
	private List<ChildTableRelation> childTableRelations;
	private List<JoinTableRelation> joinTableRelations;
	private Map<String, JoinTableRelation> manyToManyRelationsJoinTables;
	private Map<String, CollectionTableDecl> collectionTableDeclarations = new HashMap<>();
	private HashMap<SingleTableEntityDto, RootEntityTableDto> singleTableRootMap;
	private final List<RootEntityInfo> entityInfos = new ArrayList<>();
	private final DatabaseModel databaseModel;
	
	private List<RootEntityTableDto> entityTables;
	private List<JoinTableDto> joinTables;
	private List<ExclusionDto> exclusions;
	
	public SchemaModelImpl(MappingConfigDto cfg, ModelReader reader, ShellLogger logger) throws IOException {
		this.cfg = cfg;
		this.logger = logger;
		
		Set<String> mappedTables = new HashSet<>();
		Set<String> excludedTables = new HashSet<>();
		Map<String,Set<String>> excludedTableColumns = new HashMap<>();
		
		Exclusions exclusions = new Exclusions();
		for(ExclusionDto ex : getExclusions()) {
			if(ex.getTable()!=null) {
				if(ex.getColumn() == null) {
					exclusions.addTable(ex.getTable());
				} else {
					exclusions.addTableColumn(ex.getTable(), ex.getColumn());
				}
			}
		}
		
		databaseModel = reader.read(exclusions);
		
		// generate entity infos first that are declared in the mapping configuration file
		for(RootEntityTableDto et : getEntityTables()) {
			if(exclusions.isTableExcluded(et.getTable())) {
				throw new RuntimeException("configuration found for excluded table '"+et.getTable()+"'");
			}
			RootEntityInfo ei;
			if(et.getJoinedHierarchy()!= null) {
				ei = buildJoinedHierarchyEntityInfos(et, databaseModel);
			} else if(et.getSingleTableHierarchy() != null) {
				ei = buildSingleTableHierarchy(et, databaseModel);
			} else {
				TableModel table = databaseModel.getTable(et.getTable());
				ei = new PlainEntityInfo(table, et);
				buildCollectionTableDecls(ei, et.getCollectionTable(), databaseModel);
			}
			entityInfos.add(ei);
		}
		
		
		joinTableRelations = getJoinTables().stream()
			.map(this::toJoinTableRelation)
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
		}
			
		// TODO: generate collection tables declared in configuration
		// NOTE: currently we're generating them while generating entity tables above..
		
		// TODO / FINISH: implement default table mappings not declared in configurations
		Set<TableModel> tablesToMap = new HashSet<>(databaseModel.getTables());
		tablesToMap.removeIf(t -> mappedTables.contains(t.getName()));
		
		// all tables with primary keys are mapped as root entities
		Set<TableModel> defaultEntityMappedTables = 
			tablesToMap.stream()
				.filter(t -> t.getPrimaryKey()!=null)
				.collect(Collectors.toSet());
		defaultEntityMappedTables
			.forEach(t -> {
				entityInfos.add(new PlainEntityInfo(t, null));
			});
		tablesToMap.removeAll(defaultEntityMappedTables);
		
		// tables with exactly two foreign keys that cover all columns
		// are mapped as join tables
		Set<TableModel> defaultJoinTables = 
			tablesToMap.stream()
			.filter(table -> table.getForeignKeys().size()==2 && foreignKeysCoverAllColumns(table))
			.collect(Collectors.toSet());
		defaultJoinTables.forEach(table -> {
			List<ForeignKeyModel> fks = table.getForeignKeys();
			fks = sortedFks(table, fks.get(0), fks.get(1));
			
			joinTableRelations.add(new JoinTableRelation(JoinTableRelation.Kind.MANY_TO_MANY, table, fks.get(0), null, fks.get(1), null));
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
			
			if(einfo == null) {
				// no entity for target table: this can't be a collection
				// table, because collections live within entities...
				continue;
			}
			collectionTableDeclarations.put(table.getName(), new CollectionTableDecl(null, table, fk, einfo));

		}
		
		initDefaultOneToNRelations();
		
	}
	
	private <T> List<T> filterAssignableClass(List<?> list, Class<T> clazz) {
		return list.stream()
			.filter(clazz::isInstance)
			.map(clazz::cast)
			.collect(Collectors.toList());
	}
	
	private List<RootEntityTableDto> getEntityTables() {
		if(entityTables == null) {
			entityTables = filterAssignableClass(cfg.getExcludeOrJoinTableOrEntityTable(), RootEntityTableDto.class);
		}
		return entityTables;
	}
	
	private List<JoinTableDto> getJoinTables() {
		if(joinTables == null) {
			joinTables = filterAssignableClass(cfg.getExcludeOrJoinTableOrEntityTable(), JoinTableDto.class);
		}
		return joinTables;
	}
	
	private List<ExclusionDto> getExclusions() {
		if(exclusions == null) {
			exclusions = filterAssignableClass(cfg.getExcludeOrJoinTableOrEntityTable(), ExclusionDto.class);
		}
		return exclusions;
	}
	
	List<ForeignKeyModel> sortedFks(TableModel table, ForeignKeyModel... fks) {
		List<ForeignKeyModel> fkList = new ArrayList(Arrays.asList(fks));
		Collections.sort(fkList, (fk1,fk2) -> lowestColumnIndex(table, fk1.getColumns()) - lowestColumnIndex(table, fk2.getColumns()));
		return fkList;
	}
	
	private int lowestColumnIndex(TableModel table, TableColumnGroup<ColumnModel> tcg) {
		TableColumnGroup<ColumnModel> cols = table.getColumns();
		return tcg.stream()
			.map(cols::indexOf)
			.min(Integer::compare)
			.get()
			;
	}
	
	private EntityInfo findRootOrJoinedEntityOfTable(TableModel table) {
		return findRootOrJoinedEntityOfTableImpl(table, entityInfos);
	}
	
	private EntityInfo findRootOrJoinedEntityOfTableImpl(TableModel table, List<RootEntityInfo> eis) {
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
	
	private void visitJoinedSubEntityInfos(List<JoinedSubEntityInfo> entityInfos, Consumer<JoinedSubEntityInfo> consumer) {
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
		String dcolName = rt.getJoinedHierarchy().getRootEntity().getDiscriminatorColumn();
		ColumnModel dcol = rootTable.getColumns().findColumn(dcolName);
		if(dcol == null) {
			throw new RuntimeException("cannot find discriminator column '" + dcolName + "' declared for table '" + rootTable.getName() + "'");
		}
		
		String dval = rt.getJoinedHierarchy().getRootEntity().getDiscriminator();
		JoinedRootEntityInfo rootEInfo = new JoinedRootEntityInfo(rootTable, dcol, dval, rt);
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
			ForeignKeyModel fk;
			if(ctableDto.getForeignKey() != null) {
				fk = table.findForeignKey(ctableDto.getForeignKey());
			} else {
				// use first foreign key (in declaration order)
				fk = table.getForeignKeys().get(0);
			}
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
			EntityInfo einfo = new JoinedSubEntityInfo(table, rootTable, parentEntityInfo, fk, subTable);
			buildCollectionTableDecls(einfo, subTable.getCollectionTable(), dm);
			buildJoinedHierarchySubentities(rootTable, subTable, einfo, subTable.getEntityTable(), dm);
		}
	}
	
	private SingleTableRootEntityInfo buildSingleTableHierarchy(RootEntityTableDto rootEntity, DatabaseModel dm) {
		SingleTableHierarchyDto h = rootEntity.getSingleTableHierarchy();
		TableModel rootTable = dm.getTable(rootEntity.getTable());
		String dcolName = h.getRootEntity().getDiscriminatorColumn();
		ColumnModel dcol = rootTable.getColumns().findColumn(dcolName);
		if(dcol == null) {
			throw new RuntimeException("could not find discriminator column '" + dcolName + "' in table '" + rootTable.getName() + "'");
		}
		String dval = h.getRootEntity().getDiscriminator();
		checkEachFieldOnlyOnce(rootTable, h.getEntity());
		SingleTableRootEntityInfo einfo = new SingleTableRootEntityInfo(rootTable, dcol, dval, rootEntity);
		
		buildSingleTableChildEntities(rootTable, einfo, h.getEntity(), dm);
		buildCollectionTableDecls(einfo, rootEntity.getCollectionTable(), dm);
		return einfo;
	}
	
	private void buildSingleTableChildEntities(TableModel rootTable, EntityInfo parentEntityInfo, List<SingleTableEntityDto> entities, DatabaseModel dm) {
		for(SingleTableEntityDto entity : entities) {
			String dval = entity.getDiscriminator();
			EntityInfo einfo = new SingleTableSubEntityInfo(entity.getName(), rootTable, parentEntityInfo, entity);
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
		return getEntityTables().stream()
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
				getJoinTables().stream(),
				getEntityTables().stream()
			)
			.flatMap(t -> t)
			.collect(Collectors.toMap(TableConfigurationDto::getTable, cfg -> cfg)));
			
			Consumer<EntityTableDto> addOp = et -> {
				tableConfigurations.put(et.getTable(), et);
				// FIXME: add recursive .foreach() here to add subtables
			};
		}
		
		if(globalConfiguration == null) {
			globalConfiguration = Optional.ofNullable(cfg.getConfiguration()).orElse(new GlobalConfigurationDto());
		}
		
		return tableConfigurations.getOrDefault(name, globalConfiguration);
	}
	
	public boolean isTableExcluded(String tableName) {
		if(excludedTables == null) {
			excludedTables = getExclusions().stream()
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
			excludedTableColumns = getExclusions().stream()
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
		
		return getEntityTables().stream()
			.filter(j -> j.getJoinedHierarchy()!=null)
			.filter(j -> j.getJoinedHierarchy().getRootEntity().getDiscriminatorColumn().equals(columnName))
			.anyMatch(j -> 
				j.getTable().equals(table.getName())
			)
			||
			getEntityTables().stream()
			.filter(h -> h.getSingleTableHierarchy()!= null)
			.filter(s -> s.getSingleTableHierarchy().getRootEntity().getDiscriminatorColumn().equals(columnName))
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
		return getEntityTables().stream()
			.filter(h -> h.getJoinedHierarchy()!= null)
			.flatMap(h -> h.getJoinedHierarchy().getEntityTable().stream())
			.anyMatch(et -> containsSupertableJoinRelation(et, table.getName(), foreignKeyName));
	}
	
	
	@Override
	public TableModel findTable(String name) {
		return databaseModel.getTable(name);
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
			for(RootEntityTableDto et : getEntityTables()) {
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
	public ForeignKeyModel findTableForeignKey(String tableName, String foreignKeyName) {
		return Optional.ofNullable(findTable(tableName))
			.map(table -> table.findForeignKey(foreignKeyName))
			.orElse(null);
	}
	
	private EnumSet<CascadeType> toCascadeTypes(List<CascadeTypeDto> cascade) {
		EnumSet<CascadeType> result = EnumSet.noneOf(CascadeType.class);
		for(CascadeTypeDto c : cascade) {
			// we simply map the DTO to the actual cascade type by name
			result.add(CascadeType.valueOf(c.name()));
		}
		return result;
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
		
		EnumSet<CascadeType> cascade = toCascadeTypes(xToMany.getCascade());
		EnumSet<CascadeType> inverseCascade = toCascadeTypes(xToMany.getCascadeInverse());
		return new ChildTableRelation(kind, table, fk, entityName, cascade, xToMany.getInverseEntity(), inverseCascade);
	}
	
	private boolean containsExcludedTableColumns(String tableName, ForeignKeyModel fk) {
		return fk.getColumns().stream()
			.anyMatch(col -> isColumnExcluded(tableName, col.getName()));
	}
	
	private JoinTableRelation toJoinTableRelation(JoinTableDto manyToMany) {
		JoinTableRelation.Kind kind = manyToMany.isUnidirectional() ? JoinTableRelation.Kind.UNI_MANY_TO_MANY : JoinTableRelation.Kind.MANY_TO_MANY;
		TableModel table = Optional.ofNullable(databaseModel.getTable(manyToMany.getTable()))
			.orElseThrow(()->new RuntimeException("table not found in relation: '" + manyToMany.getTable() + "'"));
		
		Optional<OwnerTargetRelationDto> ownerRelation = Optional.ofNullable(manyToMany.getOwnerRelation());
		String ownerFkName = ownerRelation
			.map(r -> r.getForeignKey())
			.orElse(null);
		String ownerEntityName = ownerRelation
			.map(r -> r.getOwningEntity())
			.orElse(null);
		EnumSet<CascadeType> ownerCascade = toCascadeTypes(ownerRelation
			.map(r -> r.getCascade())
			.orElse(Collections.EMPTY_LIST)
		);
		
		Optional<InverseTargetRelationDto> inverseRelation = Optional.ofNullable(manyToMany.getInverseRelation());
		String inverseFkName = inverseRelation
			.map(r -> r.getForeignKey())
			.orElse(null);
		String inverseEntityName = inverseRelation
			.map(r -> r.getInverseEntity())
			.orElse(null);
		EnumSet<CascadeType> inverseCascade = toCascadeTypes(inverseRelation
			.map(r -> r.getCascade())
			.orElse(Collections.EMPTY_LIST)
		);
		
		ForeignKeyModel ownerFk = null;
		ForeignKeyModel inverseFk = null;
		if(ownerFkName != null) {
			ownerFk = toTableForeignKey(table.getName(), ownerFkName);
		}
		if(inverseFkName != null) {
			inverseFk = toTableForeignKey(table.getName(), inverseFkName);
		}
		
		// NOTE: We know that there must be exactly two relations in such a table
		// that are relevant to us. For the common case that there are exactly
		// two foreign keys in such a table, users can omit specifying the
		// inverse side, because we can pick that ourselves.
		if(inverseFk == null || ownerFk == null) {
			if(table.getForeignKeys().size() != 2) {
				throw new RuntimeException(String.format("cannot determine inverse side foreign key for table %s: either specify explicitely or make sure that there are exactly two foreign keys specified in this table", table.getName()));
			}
			if(ownerFk == null && inverseFk == null) {
				List<ForeignKeyModel> fks = table.getForeignKeys();
				fks = sortedFks(table, fks.get(0), fks.get(1));
				ownerFk = fks.get(0);
				inverseFk = fks.get(1);
			} else if(inverseFk == null) {
				inverseFk = findOtherFk(table, ownerFk);
			} else if(ownerFk == null) {
				ownerFk = findOtherFk(table, inverseFk);
			}
		}
		
		assert ownerFk != null && inverseFk != null;
		
		return new JoinTableRelation(kind, table, ownerFk, ownerEntityName, ownerCascade, inverseFk, inverseEntityName, inverseCascade);
	}
	
	private ForeignKeyModel findOtherFk(TableModel table, ForeignKeyModel existingFk) {
		return table.getForeignKeys().stream()
			.filter(fk -> !fk.getName().equals(existingFk.getName()))
			.findAny()
			.get();
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
		
		for(RootEntityTableDto rt : getEntityTables()) {
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
		for(TableModel t : databaseModel.getTables()) {
			if(isTableExcluded(t.getName())) {
				continue;
			}
			
			CollectionTableDecl collectionTable = getCollectionTableDeclaration(t.getName());
			
			Directionality defaultDirectionality = collectionTable == null
				?	Directionality.BIDIRECTIONAL
				:	Directionality.UNIDIRECTIONAL;
			
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
				if(null == findRootOrJoinedEntityOfTable(fk.getTargetTable())) {
					// if this foreign key does not refer to an entity, ignore
					// it. However, this could be a problem in the schema,
					// especially if the foreign key column is NOT NULL -
					// inserts or update would fail in this case.
					logger.warn("foreign key %s on table %s refers to non-entity table %s; ignoring foreign key", fk.getName(), t.getName(), fk.getTargetTable().getName());
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
	public List<RootEntityInfo> getRootEntityDefinitions() {
		return entityInfos.stream()
			.filter(ei -> (ei instanceof HierarchyRootEntityInfo) || (ei instanceof PlainEntityInfo))
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
