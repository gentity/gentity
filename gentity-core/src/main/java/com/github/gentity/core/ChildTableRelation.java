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

import static com.github.gentity.core.Directionality.*;
import static com.github.gentity.core.Cardinality.*;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.IndexModel;
import com.github.gentity.core.model.TableModel;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.persistence.CascadeType;

/**
 * represents a one-to-many or one-to-one relation
 * @author upachler
 */
public class ChildTableRelation extends Relation{
	
	private final ForeignKeyModel foreignKey;
	private final Kind kind;
	private final String owningEntityName;
	private final String inverseEntityName;
	
	
	public enum Kind implements RelationKind{
		ONE_TO_ONE(BIDIRECTIONAL, ONE, ONE),
		MANY_TO_ONE(BIDIRECTIONAL, MANY, ONE),
		UNI_ONE_TO_ONE(UNIDIRECTIONAL, ONE, ONE),
		UNI_MANY_TO_ONE(UNIDIRECTIONAL, MANY, ONE)
		;
		
		final private Directionality directionality;
		final private Cardinality from;
		final private Cardinality to;

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
	
	public ChildTableRelation(TableModel table, ForeignKeyModel foreignKey, Directionality directionality) {
		this(deriveKind(table, foreignKey, directionality), table, foreignKey, null, null, null, null);
	}
	
	public ChildTableRelation(Kind kind, TableModel table, ForeignKeyModel foreignKey, String owningEntityName, EnumSet<CascadeType> ownerCascade, String inverseEntityName, EnumSet<CascadeType> inverseCascade) {
		super(table, ownerCascade, inverseCascade);
		this.kind = kind;
		this.foreignKey = foreignKey;
		this.owningEntityName = owningEntityName;
		this.inverseEntityName = inverseEntityName;
	}

	public Kind getKind() {
		return kind;
	}
	
	public ForeignKeyModel getForeignKey() {
		return foreignKey;
	}

	public String getOwningEntityName() {
		return owningEntityName;
	}

	public String getInverseEntityName() {
		return inverseEntityName;
	}
	
	public static Kind deriveKind(TableModel table, ForeignKeyModel foreignKey, Directionality directionality) {
		Set<String> fkColNames = foreignKey.getColumns().stream()
			.map(ColumnModel::getName)
			.collect(Collectors.toSet());
		boolean isOneToOne = table.getIndices().stream()
			.filter(IndexModel::isUnique)
			.map(idx -> idx.stream()
				.map(ColumnModel::getName)
				.collect(Collectors.toSet())
			)
			.anyMatch(fkColNames::equals);
		
		switch(directionality) {
			case BIDIRECTIONAL:
				return isOneToOne ? Kind.ONE_TO_ONE : Kind.MANY_TO_ONE;
			case UNIDIRECTIONAL:
				return isOneToOne ? Kind.UNI_ONE_TO_ONE : Kind.UNI_MANY_TO_ONE;
			default:
				throw new RuntimeException("unknown directionality type");
		}
	}
	
	
}
