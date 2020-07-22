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

import com.github.gentity.core.config.dto.ConfigurationDto;
import com.github.gentity.core.fields.FieldColumnSource;
import com.github.gentity.core.model.TableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author count
 */
public abstract class EntityInfo<T extends EntityInfo> extends MappingInfo {
	
	private final EntityInfo parentEntityInfo;
	private final TableModel baseTable;
	private final List<T> childEntities = new ArrayList<>();
	private final String discriminatorValue;
	private final List<CollectionTableDecl> collectionTables = new ArrayList<>();


	public EntityInfo(TableModel table, EntityInfo parentEntityInfo, String discriminatorValue, ConfigurationDto configDto) {
		this(table, table, parentEntityInfo, discriminatorValue, configDto);
	}
	
	public EntityInfo(TableModel table, TableModel baseTable, EntityInfo parentEntityInfo, String discriminatorValue, ConfigurationDto configDto) {
		super(table, configDto);
		this.baseTable = baseTable;
		this.parentEntityInfo = parentEntityInfo;
		if(parentEntityInfo != null) {
			this.parentEntityInfo.childEntities.add(this);
		}
		this.discriminatorValue = discriminatorValue;
	}

	void addCollectionTable(CollectionTableDecl ctdecl) {
		collectionTables.add(ctdecl);
	}
	
	public List<CollectionTableDecl> getCollectionTables() {
		return Collections.unmodifiableList(collectionTables);
	}
	

	/**
	 * If the entity is part of a hierarchy, this returns the base table of that
	 * hierarchy. Otherwise, returns the same as #getTable().
	 * @return 
	 */
	public TableModel getBaseTable() {
		return baseTable;
	}
	
	public List<T> getChildren() {
		return Collections.unmodifiableList(childEntities);
	}

	public String getDiscriminatorValue() {
		return discriminatorValue;
	}

}
