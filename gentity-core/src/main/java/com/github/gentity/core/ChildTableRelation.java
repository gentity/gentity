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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * represents a one-to-many or one-to-one relation
 * @author upachler
 */
public class OneToXRelation extends Relation{
	
	private final ForeignKeyDto foreignKey;
	private Boolean oneToOne;

	public OneToXRelation(TableDto table, ForeignKeyDto foreignKey) {
		super(table);
		this.foreignKey = foreignKey;
	}
	
	public ForeignKeyDto getForeignKey() {
		return foreignKey;
	}
	
	boolean isOneToOne() {
		if(oneToOne == null) {
			Set<String> fkColNames = foreignKey.getFkColumn().stream()
				.map(ForeignKeyColumnDto::getName)
				.collect(Collectors.toSet());
			oneToOne = table.getIndex().stream()
				.filter(idx -> IndexUniqueDto.UNIQUE.equals(idx.getUnique()))
				.map(idx -> idx.getColumn().stream()
					.map(ColumnDto::getName)
					.collect(Collectors.toSet())
				)
				.anyMatch(fkColNames::equals);
		}
		return oneToOne;
	}
	
}
