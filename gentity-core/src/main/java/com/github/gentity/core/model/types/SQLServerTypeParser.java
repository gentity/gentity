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
package com.github.gentity.core.model.types;

import java.sql.JDBCType;

/**
 * parser specialized for M$ SQL Server data types
 * @author upachler
 */
public class SQLServerTypeParser extends GenericSQLTypeParser{

	@Override
	public JDBCType parseTypename(String sqlType, JDBCType defaultValue) {
		switch(sqlType) {
			case "text":
				return JDBCType.VARCHAR;
			case "ntext":
				return JDBCType.NVARCHAR;
			case "image":
				return JDBCType.VARBINARY;
			case "datetime2":
				return JDBCType.TIMESTAMP;
			default:
				return super.parseTypename(sqlType, defaultValue);
		}
	}
}
