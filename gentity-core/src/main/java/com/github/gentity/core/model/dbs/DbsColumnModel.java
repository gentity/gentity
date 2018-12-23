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

import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.SequenceModel;

/**
 *
 * @author count
 */
public class DbsColumnModel implements ColumnModel {
	private final DbsTableModel parentTable;
	private final ColumnDto dbsColumn;

	public DbsColumnModel(DbsTableModel parentTable, ColumnDto dbsColumn) {
		this.parentTable = parentTable;
		this.dbsColumn = dbsColumn;
	}

	@Override
	public String getName() {
		return dbsColumn.getName();
	}

	@Override
	public boolean isNullable() {
		return !"y".equals(dbsColumn.getMandatory());
	}

	@Override
	public String getSqlType() {
		return dbsColumn.getType();
	}

	@Override
	public int getLength() {
		return dbsColumn.getLength();
	}

	@Override
	public boolean isIdentityColumn() {
		return "y".equals(dbsColumn.getAutoincrement());
	}

	@Override
	public SequenceModel getSequence() {
		return parentTable.getDbsDatabaseModel().getSequence(dbsColumn.getSequence());
	}

	
}
