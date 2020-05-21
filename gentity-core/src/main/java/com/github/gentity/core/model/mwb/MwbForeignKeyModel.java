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
package com.github.gentity.core.model.mwb;

import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.TableModel;
import com.github.mwbmodel.model.db.mysql.ForeignKey;
import java.util.List;

/**
 *
 * @author count
 */
public class MwbForeignKeyModel implements ForeignKeyModel{
	private final ForeignKey fk;
	private final TableModel targetTable;
	private final MwbTableModel childTable;
	private final List<Mapping> mappings;

	MwbForeignKeyModel(ForeignKey fk, List<Mapping> mappings, MwbTableModel childTable, MwbTableModel targetTable) {
		this.fk = fk;
		this.mappings = mappings;
		this.childTable = childTable;
		this.targetTable = targetTable;
	}

	
	@Override
	public String getName() {
		return fk.getName();
	}

	@Override
	public TableModel getTargetTable() {
		return targetTable;
	}

	@Override
	public List<Mapping> getColumnMappings() {
		return mappings;
	}
	
}
