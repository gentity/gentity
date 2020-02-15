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

import java.io.IOException;
import java.io.InputStream;
import java.util.ServiceLoader;

/**
 * A factory for a particular type of model reader. {@link ModelReaderFactory} 
 * instances are found via the {@link ServiceLoader} mechanism. A reader factory 
 * is used to:
 * <ul>
 * <li>detect if supports the format a particular file via {@link #supportsReading()}.</li>
 * <li>if so, create a {@ModelReader} instance that can load the file using {@link #createModelReader()}</li>
 * </ul>
 * @author count
 */
public interface ModelReaderFactory {
	
	/**
	 * Detects if this factory supports reading the provided file.
	 * @param fileName	the name of the file to read
	 * @param streamSupplier	supplier for the input stream of the file, if the
	 *	factory wants to inspect the contents of the file for further inspection
	 *	to make the decision. 
	 * @return true if reading this file is supported
	 * @throws java.io.IOException	thrown in case that reading from the supplied
	 *	InputStream fails
	 */
	boolean supportsReading(String fileName, InputStreamSupplier streamSupplier) throws IOException;
	
	/**
	 * Creates a {@link ModelReader} instance reading files of the format(s) that
	 * this factory supports.
	 * @param fileName	the name of the file to read
	 * @param inputStream	the {@link InputStream} providing the file content
	 * @return a new model reader.
	 */
	ModelReader createModelReader(String fileName, InputStreamSupplier streamSupplier);
}
