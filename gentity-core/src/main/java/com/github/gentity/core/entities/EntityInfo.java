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

import com.github.dbsjpagen.dbsmodel.ForeignKeyDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import com.github.gentity.core.fields.FieldColumnSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author count
 */
public class EntityInfo<T extends EntityInfo> {
	
	private final EntityInfo parentEntityInfo;
	private final TableDto baseTable;
	private final FieldColumnSource fieldColumnSource;
	private final TableDto table;
	private final List<T> childEntities = new ArrayList<>();
	private final String discriminatorValue;


	public EntityInfo(TableDto table, FieldColumnSource fieldColumnSource, EntityInfo parentEntityInfo, String discriminatorValue) {
		this(table, table, fieldColumnSource, parentEntityInfo, discriminatorValue);
	}
	
	public EntityInfo(TableDto table, TableDto baseTable, FieldColumnSource fieldColumnSource, EntityInfo parentEntityInfo, String discriminatorValue) {
		this.table = table;
		this.baseTable = baseTable;
		this.fieldColumnSource = fieldColumnSource;
		this.parentEntityInfo = parentEntityInfo;
		if(parentEntityInfo != null) {
			this.parentEntityInfo.childEntities.add(this);
		}
		this.discriminatorValue = discriminatorValue;
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
	 * hierarchy. Otherwise, returns the same as #getTable().
	 * @return 
	 */
	public TableDto getBaseTable() {
		return baseTable;
	}
	
	public List<T> getChildren() {
		return Collections.unmodifiableList(childEntities);
	}

	public String getDiscriminatorValue() {
		return discriminatorValue;
	}
	
}
