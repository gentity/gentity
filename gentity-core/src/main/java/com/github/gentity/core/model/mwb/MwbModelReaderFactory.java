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
package com.github.gentity.core.model.mwb;

import com.github.gentity.core.model.ModelReader;
import com.github.gentity.core.model.ModelReaderFactory;
import java.io.IOException;
import com.github.gentity.core.model.ReaderContext;

/**
 *
 * @author count
 */
public class MwbModelReaderFactory implements ModelReaderFactory {

	@Override
	public boolean supportsReading(String fileName, ReaderContext streamSupplier) throws IOException {
		return fileName.toLowerCase().endsWith(".mwb");
	}

	@Override
	public ModelReader createModelReader(String fileName, ReaderContext streamSupplier) {
		return new MwbModelReader(fileName, streamSupplier);
	}
	
}
