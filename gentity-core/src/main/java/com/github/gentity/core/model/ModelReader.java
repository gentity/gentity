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

import com.github.gentity.core.Exclusions;
import java.io.IOException;

/**
 * Reads {@link DatabaseModel} instances of a particular format. Readers are
 * created via a particular {@link ModelReaderFactory}.
 * @author count
 */
public interface ModelReader {
	
	/**
	 * Reads the provided model, considering the exclusions
	 * @param exclusions exclusions containing database schema constructs that
	 *	the reader should ignore, and that therefore should not be present in
	 *	the model constructed by the reading process.
	 * @return	database model representing the file
	 * @throws IOException	thrown in case errors occur while reading the file
	 */
	DatabaseModel read(Exclusions exclusions) throws IOException;
}
