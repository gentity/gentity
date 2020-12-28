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

import static com.github.gentity.core.Cardinality.*;
import static com.github.gentity.core.Directionality.*;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.TableModel;
import java.util.EnumSet;
import javax.persistence.CascadeType;

/**
 *
 * @author upachler
 */
public class JoinTableRelation extends Relation {

	public enum Kind implements RelationKind{
		MANY_TO_MANY(BIDIRECTIONAL, MANY, MANY),
		UNI_ONE_TO_MANY(UNIDIRECTIONAL, ONE, MANY),
		UNI_MANY_TO_MANY(UNIDIRECTIONAL, MANY, MANY)
		;
		
		private final Cardinality to;
		private final Cardinality from;
		private final Directionality directionality;

		private Kind(Directionality directionality, Cardinality from, Cardinality to) {
			this.directionality = directionality;
			this.from = from;
			this.to = to;
		}
		
		@Override
		public Directionality getDirectionality() {
			return directionality;
		}

		@Override
		public Cardinality getFrom() {
			return from;
		}

		@Override
		public Cardinality getTo() {
			return to;
		}
	}
	
	private final Kind kind;
	private final ForeignKeyModel foreignKey1;
	private final ForeignKeyModel foreignKey2;
	private final String ownerEntityName;
	private final String inverseEntityName;
	
	public JoinTableRelation(Kind kind, TableModel table, ForeignKeyModel foreignKey1, String ownerEntityName, EnumSet<CascadeType> ownerCascade, ForeignKeyModel foreignKey2, String inverseEntityName, EnumSet<CascadeType> inverseCascade) {
		super(table, ownerCascade, inverseCascade);
		this.kind = kind;
		this.foreignKey1 = foreignKey1;
		this.foreignKey2 = foreignKey2;
		this.ownerEntityName = ownerEntityName;
		this.inverseEntityName = inverseEntityName;
	}
	
	public JoinTableRelation(Kind kind, TableModel table, ForeignKeyModel foreignKey1, String ownerEntityName, ForeignKeyModel foreignKey2, String inverseEntityName) {
		this(kind, table, foreignKey1, ownerEntityName, null, foreignKey2, inverseEntityName, null);
	}

	public Kind getKind() {
		return kind;
	}

	public ForeignKeyModel getOwnerForeignKey() {
		return foreignKey1;
	}
	
	public ForeignKeyModel getInverseForeignKey() {
		return foreignKey2;
	}

	public String getOwnerEntityName() {
		return ownerEntityName;
	}

	public String getInverseEntityName() {
		return inverseEntityName;
	}
	
	
}
