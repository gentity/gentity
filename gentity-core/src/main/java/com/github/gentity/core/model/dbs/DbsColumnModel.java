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

import com.github.gentity.core.model.dbs.dto.ColumnDto;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.SequenceModel;
import java.io.Serializable;
import java.sql.JDBCType;
import java.util.List;
import javax.xml.bind.JAXBElement;

/**
 *
 * @author count
 */
public class DbsColumnModel implements ColumnModel {
	private final DbsTableModel parentTable;
	private final ColumnDto dbsColumn;
	private final JDBCType jdbcType;

	public DbsColumnModel(DbsTableModel parentTable, ColumnDto dbsColumn, JDBCType jdbcType) {
		this.parentTable = parentTable;
		this.dbsColumn = dbsColumn;
		this.jdbcType = jdbcType;
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
	public JDBCType getType() {
		return jdbcType;
	}

	@Override
	public Integer getLength() {
		return dbsColumn.getLength();
	}
	
	private JAXBElement findXmlElement(String name, List<Serializable> content) {
		return content.stream()
			.filter(JAXBElement.class::isInstance)
			.map(JAXBElement.class::cast)
			.filter(e -> name.equals(e.getName().getLocalPart()))
			.findAny()
			.orElse(null);
	}
	
	@Override
	public boolean isIdentityColumn() {
		return "y".equals(dbsColumn.getAutoincrement()) || null != findXmlElement("identity", dbsColumn.getContent());
	}

	@Override
	public SequenceModel getSequence() {
		return parentTable.getDbsDatabaseModel().getSequence(dbsColumn.getSequence());
	}

	@Override
	public Integer getPrecision() {
		// there is no separate precision field in DBSchema, so we use the
		// length field
		return dbsColumn.getLength();
	}

	@Override
	public Integer getScale() {
		return dbsColumn.getDecimal() != null
			?	dbsColumn.getDecimal().intValue()
			:	null;
	}
}
