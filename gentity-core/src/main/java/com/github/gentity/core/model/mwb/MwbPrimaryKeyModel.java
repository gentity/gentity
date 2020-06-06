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
import com.github.gentity.core.model.PrimaryKeyModel;
import com.github.upachler.mwbmodel.model.db.mysql.Column;
import com.github.upachler.mwbmodel.model.db.mysql.Index;
import com.github.upachler.mwbmodel.model.db.mysql.IndexColumn;
import java.util.ArrayList;

/**
 *
 * @author count
 */
class MwbPrimaryKeyModel  extends ArrayList<ColumnModel> implements PrimaryKeyModel{
	
	private final Index primaryKey;
	
	MwbPrimaryKeyModel(MwbTableModel tableModel, Index primaryKey) {
		this.primaryKey = primaryKey;
		for(IndexColumn ic : primaryKey.getColumns()) {
			Column rc = ic.getReferencedColumn();
			MwbColumnModel cm = tableModel.getMappedColumnModel(rc);
			add(cm);
		}
	}
}
