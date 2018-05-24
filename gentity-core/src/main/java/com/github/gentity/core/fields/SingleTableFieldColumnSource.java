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

import com.github.dbsjpagen.config.SingleTableEntityDto;
import com.github.dbsjpagen.config.TableFieldDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author count
 */
public class SingleTableFieldColumnSource extends AbstractFieldColumnSource{

	private final SingleTableEntityDto entity;
	private final TableDto table;

	public SingleTableFieldColumnSource(TableDto table, SingleTableEntityDto entity) {
		this.table = table;
		this.entity = entity;
	}
	
	
	@Override
	public List<FieldMapping> getFieldMappings() {
		Set<String> columns = entity.getField().stream()
			.map(f -> f.getColumn())
			.collect(Collectors.toSet());
		Map<String,TableFieldDto> fieldMap = entity.getField().stream()
			.collect(Collectors.toMap(TableFieldDto::getColumn, f->f));
		return table.getColumn().stream()
			.filter(c -> columns.contains(c.getName()))
			.map(c -> toDefaultColumnFieldMapping(table, c, fieldMap.get(c.getName())))
			.collect(Collectors.toList());
	}
	
}
