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

import com.github.dbsjpagen.config.ConfigurationDto;
import com.github.gentity.core.fields.FieldColumnSource;
import com.github.gentity.core.model.TableModel;

/**
 *
 * @author count
 */
public class SingleTableSubEntityInfo extends EntityInfo<SingleTableSubEntityInfo> {
	private final String name;

	public SingleTableSubEntityInfo(String name, TableModel baseTable, FieldColumnSource fieldColumnSource, EntityInfo parentEntityInfo, String discriminatorValue, ConfigurationDto configDto) {
		super(null, baseTable, fieldColumnSource, parentEntityInfo, discriminatorValue, configDto);
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
	
}
