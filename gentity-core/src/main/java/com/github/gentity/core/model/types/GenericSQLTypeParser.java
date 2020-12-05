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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Offers a mapping between generic SQL type names (and some extension types
 * used by some databases, where they are distinct), and a corresponding 
 * JDBCType. Note that a generic mapping like this cannot be perfect.
 * This implementation is intended for cases where there is no better type 
 * mapping available.
 * 
 * An overview of SQL types (not complete, nor completely representing the
 * actual standard) can be found here:
 * https://en.wikibooks.org/wiki/Structured_Query_Language/Data_Types
 * @author count
 */
public class GenericSQLTypeParser implements SQLTypeParser {
	
	@Override
	public JDBCType parseTypename(String sqlType, JDBCType defaultValue) {
		switch(sqlType.trim().toLowerCase(Locale.ROOT)) {
			case "char":
			case "character":
				return JDBCType.CHAR;
			case "nchar":
			case "national character":
				return JDBCType.NCHAR;
			case "varchar":
			case "character varying":
				return JDBCType.VARCHAR;
			case "nvarchar":
			case "national character varying":
				return JDBCType.NVARCHAR;
			case "smallint":
				return JDBCType.SMALLINT;
			case "bigint":
				return JDBCType.BIGINT;
			case "int":
			case "integer":
				return JDBCType.INTEGER;
			case "bool":
			case "boolean":
				return JDBCType.BOOLEAN;
			case "numeric":
				return JDBCType.NUMERIC;
			case "decimal":
				return JDBCType.DECIMAL;
			case "float":
				return JDBCType.FLOAT;
			case "bit":
				// NOTE: MySQL (and possibly others) offer parameterized 
				// BIT types - the JDBC standard does not. The MySQL Connect/J
				// driver maps BIT(n) for n>1 to byte[] rather than boolean, 
				// which complicates a generic mapping.
				return JDBCType.BIT;
			case "real":
				return JDBCType.REAL;
			case "double":
			case "double precision":
				return JDBCType.DOUBLE;
			case "timestamp":
			case "datetime":
				// NOTE: on MySQL, there is a TIMESTAMP type, which uses the
				// current time zone for conversion. So effectively, this
				// will return the wrong type for MySQL (there, TIMESTAMP_WITH_TIMEZONE
				// would fit better - but also not perfectly)
				return JDBCType.TIMESTAMP;
			case "date":
				return JDBCType.DATE;
			case "blob":
			case "bytea":
				return JDBCType.BLOB;
			case "binary":
				return JDBCType.BINARY;
			case "varbinary":
				return JDBCType.VARBINARY;
			case "longvarbinary":
				return JDBCType.LONGVARBINARY;
			default:
				return defaultValue;
		}
	}
	
}
