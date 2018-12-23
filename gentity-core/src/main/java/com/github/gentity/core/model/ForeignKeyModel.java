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

import com.github.gentity.core.model.util.ArrayListTableColumnGroup;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author count
 */
public interface ForeignKeyModel {
	class Mapping {
		private final ColumnModel childColumn;
		private final ColumnModel parentColumn;

		public Mapping(ColumnModel childColumn, ColumnModel parentColumn) {
			this.childColumn = childColumn;
			this.parentColumn = parentColumn;
		}
		
		public ColumnModel getChildColumn() {
			return childColumn;
		}
		public ColumnModel getParentColumn() {
			return parentColumn;
		}
	}
	String getName();
	
	List<Mapping> getColumnMappings();
	
	default TableColumnGroup<ColumnModel> getColumns() {
		return _mapColumns(Mapping::getChildColumn);
	}
	
	default TableColumnGroup<ColumnModel> getParentColumns() {
		return _mapColumns(Mapping::getParentColumn);
	}
	
	default TableColumnGroup<ColumnModel> _mapColumns(Function<Mapping,ColumnModel> mapper) {
		return getColumnMappings().stream()
			.map(mapper)
			.collect(Collectors.toCollection(ArrayListTableColumnGroup::new));
	}
	TableModel getTargetTable();
	
	
}
