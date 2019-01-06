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
package com.github.gentity.core.model.dbs;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author count
 */
public class Exclusions {
	private static final String SEPARATOR = "#";
	private final Set<String> excludedTableNames = new HashSet<>();
	private final Set<String> excludedTableColumnNames = new HashSet<>();
	
	public void addTable(String name) {
		excludedTableNames.add(name);
	}
	
	public void addTableColumn(String tableName, String columnName) {
		excludedTableColumnNames.add(toTableColumnName(tableName, columnName));
	}
	public boolean isTableExcluded(String tableName) {
		return excludedTableNames.contains(tableName);
	}
	public boolean isTableColumnExcluded(String tableName, String tableColumnName) {
		return excludedTableColumnNames.contains(toTableColumnName(tableName, tableColumnName));
	}
	
	private String toTableColumnName(String tableName, String columnName) {
		return tableName + SEPARATOR + columnName;
	}
	
}
