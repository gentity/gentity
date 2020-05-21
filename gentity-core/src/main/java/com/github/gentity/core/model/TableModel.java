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

import java.util.List;

/**
 *
 * @author count
 */
public interface TableModel {
	String getName();
	
	PrimaryKeyModel getPrimaryKey();
	
	List<ForeignKeyModel> getForeignKeys();
	ForeignKeyModel findForeignKey(String name);
	
	TableColumnGroup<ColumnModel> getColumns();
	default ColumnModel findColumn(String name) {
		return getColumns().findColumn(name);
	}
	
	List<IndexModel> getIndices();
	default IndexModel findIndex(String name) {
		return getIndices().stream()
			.filter(i -> i.getName().equals(name))
			.findAny()
			.orElse(null);
	}
}
