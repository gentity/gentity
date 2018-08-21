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

import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import com.github.gentity.core.fields.FieldColumnSource;

/**
 *
 * @author count
 */
public abstract class RootEntityInfo<T extends EntityInfo> extends EntityInfo<T> {
	
	private final ColumnDto discriminatorColumn;

	public RootEntityInfo(TableDto table, FieldColumnSource fieldColumnSource, EntityInfo parentEntityInfo, ColumnDto discriminatorColumn, String discriminatorValue) {
		super(table, table, fieldColumnSource, parentEntityInfo, discriminatorValue);
		this.discriminatorColumn = discriminatorColumn;
	}
	
	
	public ColumnDto getDiscriminatorColumn() {
		return discriminatorColumn;
	}
}
