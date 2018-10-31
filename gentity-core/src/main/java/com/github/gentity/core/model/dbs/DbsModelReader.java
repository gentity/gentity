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
package com.github.gentity.core.model.dbs;

import com.github.dbsjpagen.dbsmodel.ColumnDto;
import com.github.dbsjpagen.dbsmodel.ForeignKeyDto;
import com.github.dbsjpagen.dbsmodel.IndexDto;
import com.github.dbsjpagen.dbsmodel.IndexUniqueDto;
import com.github.dbsjpagen.dbsmodel.ProjectDto;
import com.github.dbsjpagen.dbsmodel.SchemaDto;
import com.github.dbsjpagen.dbsmodel.TableDto;
import com.github.gentity.core.model.DatabaseModel;
import com.github.gentity.core.model.TableModel;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author count
 */
public class DbsModelReader {

	private final ProjectDto dbSchemaProject;
	private final SchemaDto dbSchema;
	private final Exclusions exclusions;

	public DbsModelReader(ProjectDto dbSchemaProject, Exclusions exclusions) {
		this.dbSchemaProject = dbSchemaProject;
		this.dbSchema = dbSchemaProject.getSchema();
		this.exclusions = exclusions;
	}
	
	
	public DatabaseModel read() {
		Map<String, DbsTableModel> tables = dbSchema.getTable().stream()
				.filter(t -> !exclusions.isTableExcluded(t.getName()))
				.map(this::toTable)
				.collect(Collectors.toMap(TableModel::getName, Function.identity()));
		
		for(DbsTableModel table : tables.values()) {
			
			for(ForeignKeyDto fkDto : table.getTableDto().getFk()) {
				if(exclusions.isTableExcluded(fkDto.getToTable())) {
					continue;
				}
				DbsTableModel targetTable = tables.get(fkDto.getToTable());
				DbsForeignKeyModel fk = fkDto.getFkColumn().stream()
					.filter(fcol -> exclusions.isTableColumnExcluded(table.getName(), fcol.getName()))
					.map(ForeignKeyDto::getName)
					.map(table.getColumns()::findColumn)
					.collect(Collectors.toCollection(()-> new DbsForeignKeyModel(fkDto, targetTable)))
					;
				if(!fk.isEmpty()) {
					table.getForeignKeys().add(fk);
				}
			}
		}
		
		return new DbsDatabaseModel(dbSchemaProject, exclusions, tables);
	}
	
	DbsTableModel toTable(TableDto tableDto) {
		DbsTableModel table = new DbsTableModel(tableDto);
		DbsTableColumnGroup cmodels = tableDto.getColumn().stream().sequential()
				.filter(c -> !exclusions.isTableColumnExcluded(tableDto.getName(), c.getName()))
				.map(c -> new DbsColumnModel(table, c))
				.collect(Collectors.toCollection(DbsTableColumnGroup::new));
		
		
		IndexDto pkDto = tableDto.getIndex().stream()
				.filter(idx -> IndexUniqueDto.PRIMARY_KEY == idx.getUnique())
				.findAny()
				.orElse(null);
		if(pkDto != null) {
			DbsPrimaryKeyModel pk = pkDto.getColumn().stream().sequential()
				.filter(c -> !exclusions.isTableColumnExcluded(tableDto.getName(), c.getName()))
				.map(c -> c.getName())
				.map(cmodels::findColumn)
				.collect(Collectors.toCollection(() -> new DbsPrimaryKeyModel(pkDto)));
			
			if(!pk.isEmpty()) {
				table.setDbsPrimaryKey(pk);
			}
		}
		
		return table;
	}

	public boolean isColumnPrimaryKey(TableDto table, ColumnDto column) {
		return table.getIndex().stream()
			.filter(idx -> IndexUniqueDto.PRIMARY_KEY == idx.getUnique())
			.flatMap(idx -> idx.getColumn().stream())
			.anyMatch(col -> col.getName().equals(column.getName()));
	}
	
}

