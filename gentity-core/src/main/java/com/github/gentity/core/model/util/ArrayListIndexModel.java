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
package com.github.gentity.core.model.util;

import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.IndexModel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 *
 * @author upachler
 */
public class ArrayListIndexModel extends ArrayList<ColumnModel> implements IndexModel{

	private final boolean unique;
	private final String name;

	public ArrayListIndexModel(String name, boolean unique, Collection<? extends ColumnModel> c) {
		super(c);
		this.name = name;
		this.unique = unique;
	}

	public boolean isUnique() {
		return unique;
	}

	public String getName() {
		return name;
	}
	
}
