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
package com.github.gentity.core;

import com.github.gentity.core.model.ReaderContext;
import com.github.gentity.core.model.types.ParserResolver;
import com.github.gentity.core.model.types.SQLTypeParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author count
 */
public class FileReaderContextImpl implements ReaderContext{
	private ParserResolver parserResolver = new ParserResolver();
	private final File inputFile;

	public FileReaderContextImpl(File inputFile) {
		this.inputFile = inputFile;
	}
	
	
	@Override
	public InputStream open() throws IOException {
		return new FileInputStream(inputFile);
	}

	@Override
	public SQLTypeParser findTypeParser(String databaseProductName) {
		return parserResolver.findSQLTypeParser(databaseProductName);
	}

	
	
}
