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
package com.github.gentity.core.model;

import java.sql.JDBCType;

/**
 * Represents the properties of a SQL column. 
 * @author count
 */
public interface ColumnModel {
	String getName();
	boolean isNullable();
	JDBCType getType();
	Integer getLength();
	/**
	 * The precision of a column for {@code NUMERIC} and {@code DECIMAL} types
	 * (and possibley others). 
	 * @return the {@code precision} or {@code null} if none was defined for that
	 * column
	 * @see https://learnsql.com/blog/understanding-numerical-data-types-sql/
	 */
	Integer getPrecision();
	/**
	 * The scale of a column for {@code NUMERIC} and {@code DECIMAL} types
	 * (and possibley others). 
	 * @return the {@code scale} or {@code null} if none was defined for that
	 * column
	 * @see https://learnsql.com/blog/understanding-numerical-data-types-sql/
	 */
	Integer getScale();
	/**
	 * If {@code true} this column was defined with the {@code GENERATED AS IDENTITY}
	 * clause (or what the database uses; MySQL's {@code AUTOINCREMENT} is also
	 * considered an identity column)
	 * @return {@code true} if this column is an identity column {@code false}
	 *	otherwise
	 */
	boolean isIdentityColumn();
	/**
	 * @return the sequence that was assigned to create values for this column
	 *	or {@code null} if no such sequence was set.
	 */
	SequenceModel getSequence();
}
