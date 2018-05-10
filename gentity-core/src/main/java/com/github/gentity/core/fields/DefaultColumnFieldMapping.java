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
package com.github.gentity.core.fields;

import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.dbsjpagen.dbsmodel.TableDto;

/**
 *
 * @author count
 */
class DefaultColumnFieldMapping implements FieldMapping {
	
	private final TableDto table;
	private final ColumnDto column;
	private final String fieldName;

	DefaultColumnFieldMapping(TableDto table, ColumnDto col, String fieldName) {
		this.table = table;
		this.column = col;
		this.fieldName = fieldName;
	}

	@Override
	public String getFieldName() {
		return fieldName;
	}

	@Override
	public ColumnDto getColumn() {
		return column;
	}

	@Override
	public TableDto getTable() {
		return table;
	}
	
}