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
package com.github.gentity.core.fields;

import com.github.gentity.core.config.dto.TableConfigurationDto;
import com.github.gentity.core.config.dto.TableFieldDto;
import com.github.gentity.core.model.TableModel;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author count
 */
public class PlainTableFieldColumnSource extends AbstractFieldColumnSource{

	private final TableModel table;
	private final TableConfigurationDto tableConfiguration;

	
	public PlainTableFieldColumnSource(TableModel table) {
		this(table, null);
	}
	public PlainTableFieldColumnSource(TableModel table, TableConfigurationDto tc) {
		this.table = table;
		this.tableConfiguration = tc;
	}
	
	@Override
	public List<FieldMapping> getFieldMappings() {
		Map<String,TableFieldDto> fieldMap = tableConfiguration == null
			?	Collections.EMPTY_MAP
			:	tableConfiguration.getField().stream()
				.collect(Collectors.toMap(TableFieldDto::getColumn, f->f));
		
		return table.getColumns().stream()
			.map(c -> toDefaultColumnFieldMapping(table, c, fieldMap.get(c.getName())))
			.collect(Collectors.toList())
			;
	}
	
}
