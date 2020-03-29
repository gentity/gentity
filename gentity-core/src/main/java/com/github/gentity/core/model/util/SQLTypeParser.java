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

import com.github.gentity.core.model.ModelReader;
import java.sql.JDBCType;


/**
 * This interface represents gentity-provided type parsers that {@link ModelReader}
 * implementations can use to convert column type names in their model 
 * formats to a {@link JDBCType}, which gentity uses as basis for selecting
 * mapped types (along with other metadata in the column).
 * 
 * Use of a {@linkplain SQLTypeParser} is not mandatory for a {@link ModelReader}.
 * Readers may decide by themseves how they best map their type representations
 * onto {@link JDBCType}.
 * @author count
 */
public interface SQLTypeParser {
	
	/**
	 * Convert a column type name to {@link JDBCType}.
	 * @param sqlType column type name
	 * @return the JDBCType representation for the given type name
	 * @throws IllegalArgumentException	the given type name was not recognized
	 *	by the parser
	 */
	JDBCType parseTypename(String sqlType) throws IllegalArgumentException;
	
}
