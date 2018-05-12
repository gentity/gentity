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

import com.github.dbsjpagen.dbsmodel.TableDto;
import com.github.gentity.core.fields.FieldColumnSource;

/**
 *
 * @author count
 */
public class EntityInfo {
	
	private final TableDto baseTable;
	private final FieldColumnSource fieldColumnSource;
	private final TableDto table;

	public EntityInfo(TableDto table, FieldColumnSource fieldColumnSource) {
		this(table, table, fieldColumnSource);
	}
	
	public EntityInfo(TableDto table, TableDto baseTable, FieldColumnSource fieldColumnSource) {
		this.table = table;
		this.baseTable = baseTable;
		this.fieldColumnSource = fieldColumnSource;
	}

	
	public FieldColumnSource getFieldColumnSource() {
		return fieldColumnSource;
	}

	/**
	 * The table this entity is based upon. Can be null if Entity was generated
	 * as a subentity in a single table hierarchy.
	 * @return 
	 */
	public TableDto getTable() {
		return table;
	}

	/**
	 * If the entity is part of a hierarchy, this returns the base table of that
	 * hierarchy. Otherwise, returns the same a table.
	 * @return 
	 */
	TableDto getBaseTable() {
		return baseTable;
	}
	
	
}
