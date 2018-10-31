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
package com.github.gentity.core.model;

import com.github.dbsjpagen.config.ConfigurationDto;
import com.github.dbsjpagen.config.JoinedEntityTableDto;
import com.github.dbsjpagen.config.MappingConfigDto;
import com.github.dbsjpagen.config.RootEntityTableDto;
import com.github.dbsjpagen.config.SingleTableEntityDto;
import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.dbsjpagen.dbsmodel.ForeignKeyDto;
import com.github.dbsjpagen.dbsmodel.SchemaDto;
import com.github.dbsjpagen.dbsmodel.SequenceDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import com.github.gentity.core.ChildTableRelation;
import com.github.gentity.core.entities.CollectionTableDecl;
import com.github.gentity.core.CollectionTableTravesal;
import com.github.gentity.core.ForeignKeyColumn;
import com.github.gentity.core.JoinTableRelation;
import com.github.gentity.core.SchemaModel;
import static com.github.gentity.core.model.SchemaModelImpl2.TableKind.EXCLUDED_TABLE;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 *
 * @author count
 */
public class SchemaModelImpl2 implements SchemaModel{
	
	
	enum TableKind {
		ENTITY_TABLE,
		COLLECTION_TABLE,
		JOIN_TABLE,
		EXCLUDED_TABLE,
	}

	public SchemaModelImpl2(MappingConfigDto cfg, SchemaDto schema) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	private static Map<String,TableKind> readTableClassificationFromCfg(MappingConfigDto cfg) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	
	@Override
	public boolean isCollectionTable(String tableName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isJoinTable(String tableName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isTableExcluded(String tableName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ForeignKeyDto findTableForeignKey(String tableName, String foreignKeyName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ForeignKeyDto toTableForeignKey(String tableName, String foreignKeyName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isColumnExcluded(String tableName, String columnName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isColumnPrimaryKey(TableDto table, ColumnDto column) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isColumnNullable(ColumnDto column) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String findPrimaryKeyColumnGeneratorSequence(TableDto table, ColumnDto column) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public RootEntityTableDto findParentRootEntityTable(SingleTableEntityDto singleTableEntity) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<RootEntityTableDto> getRootEntityDefinitions() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<TableDto> getTables() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public TableDto findTable(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public TableDto toTable(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<String> getPrimaryKeySequenceGeneratorNames(String tableName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public SequenceDto getSequence(String sequenceName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String getTargetPackageName() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<ChildTableRelation> getChildTableRelations() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<JoinTableRelation> getJoinTableRelations() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<ForeignKeyColumn> findForeignKeyColumns(TableDto table, String foreignKeyName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ConfigurationDto findClassOptions(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public CollectionTableDecl getCollectionTableDeclaration(String childTableName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public <T> CollectionTableTravesal<T> collectionTableTravesalOf(Function<RootEntityTableDto, T> rootContextProvider, Function<JoinedEntityTableDto, T> joinedContextProvider, Function<SingleTableEntityDto, T> singleTableContextProvider) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
}
