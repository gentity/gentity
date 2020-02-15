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
package com.github.gentity.core.entities;

import com.github.gentity.core.config.dto.CollectionTableDto;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.TableModel;

/**
 *
 * @author count
 */
public class CollectionTableDecl {
	private final CollectionTableDto collectionTableDto;
	private final EntityInfo parentEntity;
	private final ForeignKeyModel foreignKey;
	private final TableModel table;

	public CollectionTableDecl(CollectionTableDto collectionTableDto, TableModel table, ForeignKeyModel foreignKey, EntityInfo parentEntity) {
		this.collectionTableDto = collectionTableDto;
		this.parentEntity = parentEntity;
		this.foreignKey = foreignKey;
		this.table = table;
		
		parentEntity.addCollectionTable(this);
	}
	
	public ForeignKeyModel getForeignKey() {
		return foreignKey;
	}
	
	public TableModel getTable() {
		return table;
	}
}
