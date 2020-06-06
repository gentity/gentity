/*
 * Copyright 2020 The Gentity Project. All rights reserved.
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
package com.github.gentity.core.model.mwb;

import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.IndexModel;
import com.github.gentity.core.model.PrimaryKeyModel;
import com.github.gentity.core.model.TableColumnGroup;
import com.github.gentity.core.model.TableModel;
import com.github.gentity.core.model.util.ArrayListIndexModel;
import com.github.gentity.core.model.util.ArrayListTableColumnGroup;
import com.github.gentity.core.model.util.DebugUtil;
import com.github.upachler.mwbmodel.model.db.mysql.Column;
import com.github.upachler.mwbmodel.model.db.mysql.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author count
 */
public class MwbTableModel implements TableModel{
	
	private final Table table;
	private MwbPrimaryKeyModel pk;
	private final Map<Column,MwbColumnModel> columnModels;
	private final TableColumnGroup<MwbColumnModel> columns;
	private final List<MwbForeignKeyModel> mwbForeignKeys = new ArrayList<>();
	private final List<ArrayListIndexModel> indexModels = new ArrayList<>();

	public MwbTableModel(Table table, TableColumnGroup<MwbColumnModel> columns) {
		this.table = table;
		this.columns = columns;
		
		columnModels = new HashMap<>();
		for(MwbColumnModel cm : columns) {
			columnModels.put(cm.getMappedColumn(), cm);
		}
	}
	
	MwbColumnModel getMappedColumnModel(Column c) {
		return columnModels.get(c);
	}
	
	@Override
	public String getName() {
		return table.getName();
	}

	@Override
	public PrimaryKeyModel getPrimaryKey() {
		return pk;
	}
	
	public void setPrimaryKeyModel(MwbPrimaryKeyModel pk) {
		this.pk = pk;
	}
	
	@Override
	public List<ForeignKeyModel> getForeignKeys() {
		return Collections.unmodifiableList(mwbForeignKeys);
	}

	public List<MwbForeignKeyModel> getForeignKeyImpl() {
		return mwbForeignKeys;
	}

	@Override
	public TableColumnGroup<ColumnModel> getColumns() {
		return new ArrayListTableColumnGroup(columns);
	}

	@Override
	public List<IndexModel> getIndices() {
		return Collections.unmodifiableList(indexModels);
	}
	
	public List<ArrayListIndexModel> getIndicesImpl() {
		return indexModels;
	}
	
	public String toString() {
		return DebugUtil.toDescriptiveSQLString(this);
	}
}
