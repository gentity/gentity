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

import com.github.gentity.core.AbstractReaderContext;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author upachler
 */
public class ResourceReaderContextImpl extends AbstractReaderContext {
	
	private final Class<?> clazz;
	private final String resourceName;

	public ResourceReaderContextImpl(Class<?> clazz, String resourceName) {
		this.clazz = clazz;
		this.resourceName = resourceName;
	}
	
	
	@Override
	public InputStream open() throws IOException {
		InputStream is = clazz.getResourceAsStream(resourceName);
		if(is == null) {
			throw new FileNotFoundException("the resource " + resourceName + " could not be found");
		}
		return is;
	}
	
}
