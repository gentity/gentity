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
import com.github.gentity.core.model.SequenceModel;
import com.github.mwbmodel.model.db.mysql.Column;
import java.sql.JDBCType;

/**
 *
 * @author count
 */
public class MwbColumnModel implements ColumnModel{
	
	private final Column column;

	public MwbColumnModel(Column column) {
		this.column = column;
	}
	
	@Override
	public String getName() {
		return column.getName();
	}

	@Override
	public boolean isNullable() {
		return !column.isIsNotNull();
	}

	@Override
	public JDBCType getType() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public int getLength() {
		return column.getLength();
	}

	@Override
	public boolean isIdentityColumn() {
		return column.isAutoIncrement();
	}

	@Override
	public SequenceModel getSequence() {
		return null;
	}
	
}
