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

import com.github.gentity.core.config.dto.ConfigurationDto;
import com.github.gentity.core.fields.FieldColumnSource;
import com.github.gentity.core.model.TableModel;
import java.util.List;
import java.util.Optional;

/**
 *
 * @author upachler
 */
public abstract class MappingInfo {

	protected final TableModel table;
	
	// NOTE: this reference binds the info to the mapping configuration file model.
	// We may want to refactor this in the future..
	protected final ConfigurationDto configDto;

	public MappingInfo(TableModel table, ConfigurationDto configDto) {
		this.table = table;
		this.configDto = configDto;
	}

	
	public abstract FieldColumnSource getFieldColumnSource();

	/**
	 * The table this entity is based upon. Can be null if Entity was generated
	 * as a subentity in a single table hierarchy.
	 * @return
	 */
	public TableModel getTable() {
		return table;
	}
	
	/**
	 * @return superclass name declared for this class, or {@code null} if none
	 * was declared.
	 */
	public String getExtends() {
		return configDto != null
			?	configDto.getExtends()
			:	null;
	}
	
	public List<String> getImplements() {
		return Optional.ofNullable(configDto)
			.map(ConfigurationDto::getImplements)
			.orElse(null);
	}

	public String getClassNameSuffix() {
		return configDto != null
			?	configDto.getClassNameSuffix()
			:	null;
	}

	public String getClassNamePrefix() {
		return configDto != null
			?	configDto.getClassNamePrefix()
			:	null;
	}

	public String getFieldNameSuffix() {
		return configDto != null
			?	configDto.getFieldNameSuffix()
			:	null;
	}

	public String getFieldNamePrefix() {
		return configDto != null
			?	configDto.getFieldNamePrefix()
			:	null;
	}
	
}
