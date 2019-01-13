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
import com.github.dbsjpagen.config.ConfigurationDto;
import com.github.dbsjpagen.config.JoinedEntityTableDto;
import com.github.dbsjpagen.config.RootEntityTableDto;
import com.github.dbsjpagen.config.SingleTableEntityDto;
import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.dbsjpagen.dbsmodel.SequenceDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import com.github.gentity.core.entities.EntityInfo;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.TableModel;
import java.util.List;
import java.util.function.Function;
import javax.persistence.GenerationType;

/**
 *
 * @author count
 */
public interface SchemaModel {

	// table specific queries
	boolean isCollectionTable(String tableName);

	boolean isJoinTable(String tableName);

	boolean isTableExcluded(String tableName);
	
	ForeignKeyModel findTableForeignKey(String tableName, String foreignKeyName);
	
	ForeignKeyModel toTableForeignKey(String tableName, String foreignKeyName);

	// column specific queries
	boolean isColumnExcluded(String tableName, String columnName);
	
	boolean isColumnIgnored(String tableName, String columnName);
	
	boolean isColumnPrimaryKey(TableModel table, ColumnModel column);
	
	boolean isColumnNullable(ColumnDto column);

	GenerationType findPrimaryKeyColumnGenerationStrategy(TableModel table, ColumnModel column);
	
	// ----
	RootEntityTableDto findParentRootEntityTable(SingleTableEntityDto singleTableEntity);
	
	List<EntityInfo> getRootEntityDefinitions();
	
	TableModel findTable(String name);
	
	TableModel toTable(String name);
	
	SequenceDto getSequence(String sequenceName);
	
	String getTargetPackageName();
	
	/**
	 * @return The default value for an entity declaration's 'extends' attribute
	 * (which sets the base class), or {@code null} if none is configured.
	 */
	String getDefaultExtends();
	
	List<ChildTableRelation> getChildTableRelations();

	List<JoinTableRelation> getJoinTableRelations();

	ConfigurationDto findClassOptions(String name);

	CollectionTableDecl getCollectionTableDeclaration(String childTableName);
	
	<T> CollectionTableTravesal<T> collectionTableTravesalOf(
		Function<RootEntityTableDto, T> rootContextProvider,
		Function<JoinedEntityTableDto, T> joinedContextProvider,
		Function<SingleTableEntityDto, T> singleTableContextProvider);
}
