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
import java.util.Locale;

/**
 * This parser aims to model the mappings made in MySQL Connector/J.
 * 
 * @author count
 * @see https://dev.mysql.com/doc/connector-j/8.0/en/connector-j-reference-type-conversions.html
 */
public class MySQLTypeParser extends GenericSQLTypeParser{

	@Override
	public JDBCType parseTypename(String sqlType) {
		// For a discussion on TIMESTAMP vs TIMEZONE, see this:
		// https://www.eversql.com/mysql-datetime-vs-timestamp-column-types-which-one-i-should-use/
		switch(sqlType.trim().toLowerCase(Locale.ROOT)) {
			case "timestamp":
				// the TIMESTAMP type on MySQL sort-of stores the timezone:
				// it converts the provided time to UTC and stores it there.
				// so writing it in TZ Berlin and reading it in TZ Beijing
				// will yield different, TZ specific time representations of
				// the very same UTC time. 
				// Therefore, we choose the WITH_TIMEZONE variant. This might
				// not align with the connector/J implementation
				return JDBCType.TIMESTAMP_WITH_TIMEZONE;
			case "datetime":
				// DATETIME does not store the timezone. So the TIMESTAMP type
				// without the timezeone should fit better.
				return JDBCType.TIMESTAMP;
		}
		
		// for now, we rely on the standard implementation in the base class
		// for all other MySQL types...
		return super.parseTypename(sqlType);
	}
	
}
