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
package com.github.gentity.core.entities;

import com.github.gentity.core.fields.FieldColumnSource;
import com.github.gentity.core.model.TableModel;

/**
 *
 * @author upachler
 */
public abstract class MappingInfo {

	protected final FieldColumnSource fieldColumnSource;
	protected final TableModel table;

	public MappingInfo(FieldColumnSource fieldColumnSource, TableModel table) {
		this.fieldColumnSource = fieldColumnSource;
		this.table = table;
	}

	
	public FieldColumnSource getFieldColumnSource() {
		return fieldColumnSource;
	}

	/**
	 * The table this entity is based upon. Can be null if Entity was generated
	 * as a subentity in a single table hierarchy.
	 * @return
	 */
	public TableModel getTable() {
		return table;
	}
	
}
