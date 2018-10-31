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

import com.github.dbsjpagen.dbsmodel.TableDto;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.PrimaryKeyModel;
import com.github.gentity.core.model.TableModel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author count
 */
public class DbsTableModel implements TableModel<DbsColumnModel, DbsForeignKeyModel> {
	private final TableDto dbsTable;
	private DbsPrimaryKeyModel dbsPrimaryKey;
	private DbsTableColumnGroup columns;
	private final List<DbsForeignKeyModel> dbsForeignKeys = new ArrayList<>();

	public DbsTableModel(TableDto dbsTable) {
		this.dbsTable = dbsTable;
	}
	
	public TableDto getTableDto() {
		return dbsTable;
	}
	
	@Override
	public String getName() {
		return dbsTable.getName();
	}

	@Override
	public PrimaryKeyModel getPrimaryKey() {
		return dbsPrimaryKey;
	}

	@Override
	public List<DbsForeignKeyModel> getForeignKeys() {
		return dbsForeignKeys;
	}

	@Override
	public DbsForeignKeyModel findForeignKey(String name) {
		return dbsForeignKeys.stream()
			.filter(fk -> name.equals(fk.getName()))
			.findAny()
			.orElse(null);
	}

	@Override
	public DbsTableColumnGroup getColumns() {
		return columns;
	}

	public void setDbsPrimaryKey(DbsPrimaryKeyModel dbsPrimaryKey) {
		this.dbsPrimaryKey = dbsPrimaryKey;
	}
	
	
}
