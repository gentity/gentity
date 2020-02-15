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
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.TableModel;

/**
 *
 * @author count
 */
public abstract class HierarchyRootEntityInfo<T extends EntityInfo> extends RootEntityInfo<T> {
	
	private final ColumnModel discriminatorColumn;

	public HierarchyRootEntityInfo(TableModel table, FieldColumnSource fieldColumnSource, EntityInfo parentEntityInfo, ColumnModel discriminatorColumn, String discriminatorValue, ConfigurationDto configDto) {
		super(table, fieldColumnSource, parentEntityInfo, discriminatorValue, configDto);
		this.discriminatorColumn = discriminatorColumn;
	}
	
	
	public ColumnModel getDiscriminatorColumn() {
		return discriminatorColumn;
	}
}
