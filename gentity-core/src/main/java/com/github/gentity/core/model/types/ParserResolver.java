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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author count
 */
public class ParserResolver {
	
	private static final SQLTypeParser GENERIC_PARSER = new GenericSQLTypeParser();
	
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
	
	/**
	 * Tries to find SQL type parser for a RDBMS name. Names can be like 'MyQL', 
	 * 'Postgres', etc. If no parser is found, the method responds with 
	 * a default SQL type parser.
	 * @param database name
	 * @return a parser
	 */
	public SQLTypeParser findSQLTypeParser(String databaseProductName) {
		for(PatternParserMapping ppm : ppms) {
			if(ppm.pattern.matcher(databaseProductName).matches()) {
				return ppm.parser;
			}
		}
		return GENERIC_PARSER;
	}
}
