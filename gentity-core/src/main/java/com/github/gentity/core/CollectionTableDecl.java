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

import com.github.dbsjpagen.config.CollectionTableDto;
import com.github.dbsjpagen.dbsmodel.TableDto;

/**
 *
 * @author count
 */
public class CollectionTableDecl {
	private final CollectionTableDto collectionTableDto;
	private final String parentTableName;
	private final String parentEntityName;

	public CollectionTableDecl(CollectionTableDto collectionTableDto, String parentTableName, String parentEntityName) {
		this.collectionTableDto = collectionTableDto;
		this.parentTableName = parentTableName;
		this.parentEntityName = parentEntityName;
	}
	
	public String getParentTableName(){
		return parentTableName;
	}

	public String getParentEntityName() {
		return parentEntityName;
	}

	String getForeignKey() {
		return collectionTableDto.getForeignKey();
	}
	
	String getTable() {
		return collectionTableDto.getTable();
	}
}
