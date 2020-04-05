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
package com.github.gentity.core.model;

import com.github.gentity.core.model.types.SQLTypeParser;
import java.io.IOException;
import java.io.InputStream;

/**
 * A context for reader implementations, which supplies the {@link InputStream}
 * to the source as well as other context that may be relevant for a {@link ModelReader}
 * implementation.
 * @author count
 */
public interface ReaderContext {
	/**
	 * Opens the {@link InputStream} to the actual source file.
	 * @return
	 * @throws IOException 
	 */
	InputStream open() throws IOException;
	
	/**
	 * Finds a type parser for a database product identified by its name.
	 * @param databaseProductName	a common name for a DBMS, like 'mysql', etc.
	 * @return 
	 */
	SQLTypeParser findTypeParser(String databaseProductName);
}
