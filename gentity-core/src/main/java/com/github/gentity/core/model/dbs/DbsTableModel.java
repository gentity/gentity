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
import com.github.gentity.core.model.IndexModel;
import com.github.gentity.core.model.PrimaryKeyModel;
import com.github.gentity.core.model.TableColumnGroup;
import com.github.gentity.core.model.TableModel;
import com.github.gentity.core.model.util.ArrayListIndexModel;
import com.github.gentity.core.model.util.ArrayListTableColumnGroup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author count
 */
public class DbsTableModel implements TableModel {
	private DbsDatabaseModel dbsDatabaseModel;
	private final TableDto dbsTable;
	private DbsPrimaryKeyModel dbsPrimaryKey;
	private List<DbsColumnModel> columns;
	private final List<DbsForeignKeyModel> dbsForeignKeys = new ArrayList<>();
	private final List<ArrayListIndexModel> indexModels = new ArrayList<>();
	
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
	public List<ForeignKeyModel> getForeignKeys() {
		return Collections.unmodifiableList(dbsForeignKeys);
	}
	
	public List<DbsForeignKeyModel> getForeignKeysImpl() {
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
	public TableColumnGroup getColumns() {
		if(columns == null) {
			columns = dbsTable.getColumn().stream()
				.map(dc -> new DbsColumnModel(this, dc))
				.collect(Collectors.toList());
		}
		return new ArrayListTableColumnGroup(columns);
	}

	public void setDbsPrimaryKey(DbsPrimaryKeyModel dbsPrimaryKey) {
		this.dbsPrimaryKey = dbsPrimaryKey;
	}

	@Override
	public List<IndexModel> getIndices() {
		return Collections.unmodifiableList(indexModels);
	}
	
	public List<ArrayListIndexModel> getIndicesImpl() {
		return indexModels;
	}
	
	public DbsDatabaseModel getDbsDatabaseModel() {
		return dbsDatabaseModel;
	}

	void setDbsDatabaseModel(DbsDatabaseModel dbsDatabaseModel) {
		this.dbsDatabaseModel = dbsDatabaseModel;
	}
}
