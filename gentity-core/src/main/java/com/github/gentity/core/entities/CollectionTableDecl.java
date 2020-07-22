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
import com.github.gentity.core.fields.FieldColumnSource;
import com.github.gentity.core.fields.PlainTableFieldColumnSource;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.TableModel;
import java.util.function.Predicate;

/**
 *
 * @author count
 */
public class CollectionTableDecl extends MappingInfo{
	private final EntityInfo parentEntity;
	private final ForeignKeyModel foreignKey;
	private final TableModel table;
	private final PlainTableFieldColumnSource fieldColumnSource;
	private final boolean basicElementCollection;
	
	public CollectionTableDecl(CollectionTableDto collectionTableDto, TableModel table, ForeignKeyModel foreignKey, EntityInfo parentEntity) {
		super(table, collectionTableDto);
		this.parentEntity = parentEntity;
		this.foreignKey = foreignKey;
		this.table = table;
		
		// map all fields except the table's foreign key columns leading
		// to the containing entity
		Predicate<ColumnModel> foreignKeyColumnFilter = c-> null == foreignKey.getColumns().findColumn(c.getName());
		this.fieldColumnSource = new PlainTableFieldColumnSource(table, collectionTableDto, foreignKeyColumnFilter);
		
		boolean firstFieldDeclarationOverridesName = collectionTableDto != null
			&& !collectionTableDto.getField().isEmpty() 
			&& collectionTableDto.getField().get(0).getName() != null;
		this.basicElementCollection = fieldColumnSource.getFieldMappings().size() == 1 && !firstFieldDeclarationOverridesName;
		
		parentEntity.addCollectionTable(this);
	}
	
	public ForeignKeyModel getForeignKey() {
		return foreignKey;
	}
	
	public TableModel getTable() {
		return table;
	}
	
	/**
	 * @return {@code true} if the collection mapped for this table consists of 
	 *	basic type elements, {@code false} if it consists of embeddable class 
	 *	elements, (e.g. {@code List<String>} vs. {@code List<Person>}).
	 */
	public boolean isBasicElementCollection() {
		return basicElementCollection;
	}

	@Override
	public FieldColumnSource getFieldColumnSource() {
		return fieldColumnSource;
	}
	
}
