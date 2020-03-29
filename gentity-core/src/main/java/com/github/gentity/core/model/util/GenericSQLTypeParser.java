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
package com.github.gentity.core.model.util;

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
	
	private static final GenericSQLTypeParser INSTANCE = new GenericSQLTypeParser();
	
	// PatternParserMapping: PatternParserMapping
	static class PatternParserMapping {
		final Pattern pattern;
		final SQLTypeParser parser;

		public PatternParserMapping(Pattern pattern, SQLTypeParser parser) {
			this.pattern = pattern;
			this.parser = parser;
		}
	}
	
	// NOTE: add more patterns here
	private static final List<PatternParserMapping> ppms = Arrays.asList(
		new PatternParserMapping(
			Pattern.compile("mysql", Pattern.CASE_INSENSITIVE), 
			new MySQLTypeParser()
		)
	);
	
	public static GenericSQLTypeParser forUnknownRdbms() {
		return INSTANCE;
	}
	
	protected GenericSQLTypeParser() {
	}
	
	/**
	 * Tries to find SQL type parser for a RDBMS name. Names can be like 'MyQL', 
	 * 'Postgres', etc. If no parser is found, the method responds with 
	 * {@code null}. Callers can decide whether or not they want to proceed with
	 * a generic SQL parser that they can get via {@link #forUnknownRdbms()}.
	 * @param database name
	 * @return a parser or {@code null} no parser matching this particular
	 *	RDBMS was found.
	 */
	public static SQLTypeParser forRdbms(String database) {
		for(PatternParserMapping ppm : ppms) {
			if(ppm.pattern.matcher(database).matches()) {
				return ppm.parser;
			}
		}
		return null;
	}

	@Override
	public JDBCType parseTypename(String sqlType) {
		switch(sqlType.trim().toLowerCase(Locale.ROOT)) {
			case "char":
			case "character":
				return JDBCType.CHAR;
			case "varchar":
			case "character varying":
				return JDBCType.VARCHAR;
			case "bigint":
				return JDBCType.BIGINT;
			case "int":
			case "integer":
				return JDBCType.INTEGER;
			case "bool":
			case "boolean":
				return JDBCType.BOOLEAN;
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
				throw new IllegalArgumentException("Unrecognized type name '" + sqlType + "'");
		}
	}
	
}
