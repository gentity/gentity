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
package org.bitbucket.gentity.core;

import org.bitbucket.dbsjpagen.dbsmodel.ForeignKeyDto;
import org.bitbucket.dbsjpagen.dbsmodel.TableDto;

/**
 *
 * @author upachler
 */
public class OneToManyRelation {
	
	private final TableDto table;
	
	private final ForeignKeyDto foreignKey;

	public OneToManyRelation(TableDto table, ForeignKeyDto foreignKey) {
		this.table = table;
		this.foreignKey = foreignKey;
	}

	
	public TableDto getTable() {
		return table;
	}

	public ForeignKeyDto getForeignKey() {
		return foreignKey;
	}
	
	
}
