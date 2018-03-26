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
public class ManyToManyRelation extends Relation {
	
	
	private final ForeignKeyDto foreignKey1;
	private final ForeignKeyDto foreignKey2;

	public ManyToManyRelation(TableDto table, ForeignKeyDto foreignKey1, ForeignKeyDto foreignKey2) {
		super(table);
		this.foreignKey1 = foreignKey1;
		this.foreignKey2 = foreignKey2;
	}


	public ForeignKeyDto getOwnerForeignKey() {
		return foreignKey1;
	}
	
	public ForeignKeyDto getReferencedForeignKey() {
		return foreignKey2;
	}
	
}
