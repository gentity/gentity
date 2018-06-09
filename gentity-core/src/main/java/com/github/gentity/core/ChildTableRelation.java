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

import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.dbsjpagen.dbsmodel.ForeignKeyColumnDto;
import com.github.dbsjpagen.dbsmodel.ForeignKeyDto;
import com.github.dbsjpagen.dbsmodel.IndexUniqueDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import static com.github.gentity.core.ChildTableRelation.Directionality.BIDIRECTIONAL;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * represents a one-to-many or one-to-one relation
 * @author upachler
 */
public class ChildTableRelation extends Relation{
	
	private final ForeignKeyDto foreignKey;
	private final Kind kind;
	private final String owningEntityName;
	private final String inverseEntityName;
	
	public enum Directionality {
		UNIDIRECTIONAL,
		BIDIRECTIONAL
	}
	
	public enum Kind {
		ONE_TO_ONE(Directionality.BIDIRECTIONAL),
		MANY_TO_ONE(Directionality.BIDIRECTIONAL),
		UNI_ONE_TO_ONE(Directionality.UNIDIRECTIONAL),
		UNI_MANY_TO_ONE(Directionality.UNIDIRECTIONAL)
		;
		
		final private Directionality directionality;

		private Kind(Directionality directionality) {
			this.directionality = directionality;
		}
		
		
		public Directionality getDirectionality() {
			return directionality;
		}
	}
	
	public ChildTableRelation(TableDto table, ForeignKeyDto foreignKey, Directionality directionality) {
		this(deriveKind(table, foreignKey, directionality), table, foreignKey, null, null);
	}
	
	public ChildTableRelation(Kind kind, TableDto table, ForeignKeyDto foreignKey, String owningEntityName, String inverseEntityName) {
		super(table);
		this.kind = kind;
		this.foreignKey = foreignKey;
		this.owningEntityName = owningEntityName;
		this.inverseEntityName = inverseEntityName;
	}

	public Kind getKind() {
		return kind;
	}
	
	public ForeignKeyDto getForeignKey() {
		return foreignKey;
	}

	public String getOwningEntityName() {
		return owningEntityName;
	}

	public String getInverseEntityName() {
		return inverseEntityName;
	}
	
	public static Kind deriveKind(TableDto table, ForeignKeyDto foreignKey, Directionality directionality) {
		Set<String> fkColNames = foreignKey.getFkColumn().stream()
			.map(ForeignKeyColumnDto::getName)
			.collect(Collectors.toSet());
		boolean isOneToOne = table.getIndex().stream()
			.filter(idx -> IndexUniqueDto.UNIQUE.equals(idx.getUnique()))
			.map(idx -> idx.getColumn().stream()
				.map(ColumnDto::getName)
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
