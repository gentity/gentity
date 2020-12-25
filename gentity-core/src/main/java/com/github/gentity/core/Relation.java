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
package com.github.gentity.core;

import com.github.gentity.core.model.TableModel;
import java.util.EnumSet;
import javax.persistence.CascadeType;

/**
 *
 * @author count
 */
public abstract class Relation {

	protected final TableModel table;
	protected final EnumSet<CascadeType> ownerCascadeTypes;
	protected final EnumSet<CascadeType> inverseCascadeTypes;
	
	protected Relation(TableModel table, EnumSet<CascadeType> ownerCascadeTypes, EnumSet<CascadeType> inverseCascadeTypes) {
		this.table = table;
		this.ownerCascadeTypes = ownerCascadeTypes==null
			?	EnumSet.noneOf(CascadeType.class)
			:	ownerCascadeTypes;
		this.inverseCascadeTypes = inverseCascadeTypes==null
			?	EnumSet.noneOf(CascadeType.class)
			:	inverseCascadeTypes;
	}
	public TableModel getTable() {
		return table;
	}
	public EnumSet<CascadeType> getOwnerCascadeTypes() {
		return ownerCascadeTypes;
	}

	public EnumSet<CascadeType> getInverseCascadeTypes() {
		return inverseCascadeTypes;
	}
	
}
