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
import com.github.mwbmodel.model.db.mysql.Column;
import com.github.mwbmodel.model.db.mysql.ForeignKey;
import com.github.mwbmodel.model.db.mysql.Table;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author count
 */
public class MwbTableModel implements TableModel{
	
	private final Table table;
	private final MwbPrimaryKeyModel pk;
	private final Map<Column,MwbColumnModel> columnModels;

	public MwbTableModel(Table table) {
		this.table = table;
		
		columnModels = new HashMap<>();
		Map<Column,MwbColumnModel> columnModels = new HashMap<>();
		for(Column c : table.getColumns()) {
			columnModels.put(c, new MwbColumnModel(c));
		}
		
		pk = new MwbPrimaryKeyModel(this, table.getPrimaryKey());
		
		for(ForeignKey fk: table.getForeignKeys()) {
			
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

	@Override
	public List<ForeignKeyModel> getForeignKeys() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public ForeignKeyModel findForeignKey(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public TableColumnGroup<ColumnModel> getColumns() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public List<IndexModel> getIndices() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	
}
