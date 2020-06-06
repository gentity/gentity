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
 * Groups of SQL types, according to:
 * https://en.wikibooks.org/wiki/Structured_Query_Language/Data_Types
 * @author count
 */
public enum SQLTypeGroup {
	CHARACTER,
	BINARY,
	NUMERIC,
	DATETIME,
	INTERVAL,
	BOOLEAN,
	XML,
	;
	
	public static SQLTypeGroup of(JDBCType jdbcType) {
		switch(jdbcType) {
			case CHAR:
			case VARCHAR:
			case LONGVARCHAR:
			case NCHAR:
			case NVARCHAR:
			case LONGNVARCHAR:
			case CLOB:
			case NCLOB:
				return CHARACTER;
			case TINYINT:
			case SMALLINT:
			case INTEGER:
			case BIGINT:
			case DECIMAL:
			case REAL:
			case FLOAT:
			case DOUBLE:
				return NUMERIC;
			case BIT:
			case BOOLEAN:
				return BOOLEAN;
			case BLOB:
			case BINARY:
			case VARBINARY:
			case LONGVARBINARY:
				return BINARY;
			case DATE:
			case TIME:
			case TIME_WITH_TIMEZONE:
			case TIMESTAMP:
			case TIMESTAMP_WITH_TIMEZONE:
				return DATETIME;
			case SQLXML:
				return XML;
			default:
				return null;
		}
	}
}
