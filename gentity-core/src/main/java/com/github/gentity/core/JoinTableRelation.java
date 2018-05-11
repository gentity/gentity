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

import com.github.dbsjpagen.dbsmodel.ForeignKeyDto;
import com.github.dbsjpagen.dbsmodel.TableDto;

/**
 *
 * @author upachler
 */
public class JoinTableRelation extends Relation {
	
	public enum Kind {
		MANY_TO_MANY,
		UNI_ONE_TO_MANY,
		UNI_MANY_TO_MANY
	}
	
	private final Kind kind;
	private final ForeignKeyDto foreignKey1;
	private final ForeignKeyDto foreignKey2;
	private final String ownerEntityName;
	private final String inverseEntityName;
	
	public JoinTableRelation(Kind kind, TableDto table, ForeignKeyDto foreignKey1, String ownerEntityName, ForeignKeyDto foreignKey2, String inverseEntityName) {
		super(table);
		this.kind = kind;
		this.foreignKey1 = foreignKey1;
		this.foreignKey2 = foreignKey2;
		this.ownerEntityName = ownerEntityName;
		this.inverseEntityName = inverseEntityName;
	}

	public Kind getKind() {
		return kind;
	}

	public ForeignKeyDto getOwnerForeignKey() {
		return foreignKey1;
	}
	
	public ForeignKeyDto getInverseForeignKey() {
		return foreignKey2;
	}

	public String getOwnerEntityName() {
		return ownerEntityName;
	}

	public String getInverseEntityName() {
		return inverseEntityName;
	}
	
	
}
