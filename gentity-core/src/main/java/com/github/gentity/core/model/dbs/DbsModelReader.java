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

import com.github.gentity.core.Exclusions;
import com.github.gentity.core.model.dbs.dto.ColumnDto;
import com.github.gentity.core.model.dbs.dto.ForeignKeyColumnDto;
import com.github.gentity.core.model.dbs.dto.ForeignKeyDto;
import com.github.gentity.core.model.dbs.dto.IndexDto;
import com.github.gentity.core.model.dbs.dto.IndexUniqueDto;
import com.github.gentity.core.model.dbs.dto.ProjectDto;
import com.github.gentity.core.model.dbs.dto.SchemaDto;
import com.github.gentity.core.model.dbs.dto.TableDto;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.DatabaseModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.ModelReader;
import com.github.gentity.core.model.SequenceModel;
import com.github.gentity.core.model.TableColumnGroup;
import com.github.gentity.core.model.TableModel;
import com.github.gentity.core.model.util.ArrayListIndexModel;
import com.github.gentity.core.model.util.ArrayListTableColumnGroup;
import com.github.gentity.core.model.types.SQLTypeParser;
import com.github.gentity.core.util.UnmarshallerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import com.github.gentity.core.model.ReaderContext;
import java.sql.JDBCType;
import java.util.EnumSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author count
 */
public class DbsModelReader implements ModelReader {

	private final UnmarshallerFactory factory;
	private final String fileName;
	private final ReaderContext readerContext;
	
	private ProjectDto dbSchemaProject;
	private SchemaDto dbSchema;
	private Exclusions exclusions;
	private SQLTypeParser typeParser;
	
	private EnumSet<IndexUniqueDto> UNIQUE_INDEX_TYPES = EnumSet.of(
		// DbSchema < 8.3
		IndexUniqueDto.UNIQUE,
		
		// DbSchema 8.3+
		IndexUniqueDto.UNIQUE_KEY,
		IndexUniqueDto.UNIQUE_INDEX
	);

	public DbsModelReader(UnmarshallerFactory factory, String fileName, ReaderContext readerContext) {
		this.factory = factory;
		this.fileName = fileName;
		this.readerContext = readerContext;
	}
	
	private ForeignKeyModel.Mapping toMapping(ForeignKeyDto fk, ForeignKeyColumnDto fkCol, TableModel childTable, TableModel parentTable) {
		ColumnModel childColumn = childTable.getColumns().findColumn(fkCol.getName());
		ColumnModel parentColumn = parentTable.getColumns().findColumn(fkCol.getPk());
		return new ForeignKeyModel.Mapping(childColumn, parentColumn);
	}
	
	private SQLTypeParser findTypeParser(String dbmsName) {
		SQLTypeParser parser = readerContext.findTypeParser(dbmsName);
		if(dbmsName.contains("sqlserver")) {
			// NOTE: DbSchema apparently cannot parse the the <type>(max)
			// construction correctly, which yields columns of lenght=0 but
			// of type VARCHAR(max). It would make more sense to have
			// type VARCHAR and length=2^23, but that's how it is. We mitigate
			// removing (max), making the size unknown because still lenght=0.
			return new SQLTypeParser() {
				Pattern TRAILING_PARENTHESIS_PATTERN = Pattern.compile("(.+)\\(.*\\)");
				@Override
				public JDBCType parseTypename(String sqlType, JDBCType defaultValue) {
					Matcher m = TRAILING_PARENTHESIS_PATTERN.matcher(sqlType);
					if(m.matches()) {
						sqlType = m.group(1);
					}
					return parser.parseTypename(sqlType, defaultValue);
				}
			};
		} else {
			return parser;
		}
		
	}
	
	public DatabaseModel read(Exclusions exclusions) throws IOException {
		
		try (InputStream inputStream = readerContext.open()) {
			Unmarshaller unmarshaller = factory.createUnmarshaller();
			
			Source src = new StreamSource(inputStream);
			src.setSystemId(fileName);
			JAXBElement<ProjectDto> schemaElement = (JAXBElement<ProjectDto>)unmarshaller
				.unmarshal(src);
			
			dbSchemaProject = schemaElement.getValue();
			dbSchema = dbSchemaProject.getSchema();
		} catch (JAXBException ex) {
			throw new IOException(ex);
		}
		
		typeParser = findTypeParser(dbSchemaProject.getDatabase().toLowerCase().trim());
		
		this.dbSchema = dbSchemaProject.getSchema();
		
		this.exclusions = exclusions;
		Map<String, DbsTableModel> tables = dbSchema.getTable().stream()
				.filter(t -> !exclusions.isTableExcluded(t.getName()))
				.map(this::toTable)
				.collect(Collectors.toMap(TableModel::getName, Function.identity()));
		
		for(DbsTableModel table : tables.values()) {
			
			for(ForeignKeyDto fkDto : table.getTableDto().getFk()) {
				if(exclusions.isTableExcluded(fkDto.getToTable())) {
					// if the foreign key's target table is excluded, we
					// see that foreign key as excluded
					continue;
				}
				List<ForeignKeyColumnDto> excludedFkCols = fkDto.getFkColumn().stream()
					.filter(fcol -> exclusions.isTableColumnExcluded(table.getName(), fcol.getName()))
					.collect(Collectors.toList());
				if(excludedFkCols.isEmpty()) {
					DbsTableModel targetTable = tables.get(fkDto.getToTable());
					List<ForeignKeyModel.Mapping> mappings = 
						fkDto.getFkColumn().stream()
						.map(fkCol -> toMapping(fkDto, fkCol, table, targetTable))
						.collect(Collectors.toList())
						;
					DbsForeignKeyModel fk = new DbsForeignKeyModel(fkDto, mappings, table, targetTable);
					table.getForeignKeysImpl().add(fk);
				} else if(excludedFkCols.size() != fkDto.getFkColumn().size()) {
					// either all or no columns in a foreign key may be excluded.
					// partial exclusions cannot be accepted and lead to an error
					String colnames = fkDto.getFkColumn().stream().map(c -> c.getName()).collect(Collectors.joining(","));
					String msg = String.format("columns of foreign key %s are partially excluded (columns: %s)", fkDto.getName(), colnames);
					throw new RuntimeException(msg);
				}
			}
		}
		
		Map<String,DbsSequenceModel> sequences = dbSchema.getSequence().stream()
			.map(DbsSequenceModel::new)
			.collect(Collectors.toMap(SequenceModel::getName, Function.identity()));
		return new DbsDatabaseModel(dbSchemaProject, exclusions, tables, sequences);
	}
	
	DbsTableModel toTable(TableDto tableDto) {
		DbsTableModel table = new DbsTableModel(tableDto);
		TableColumnGroup cmodels = tableDto.getColumn().stream().sequential()
				.filter(c -> !exclusions.isTableColumnExcluded(tableDto.getName(), c.getName()))
				.map(c -> new DbsColumnModel(table, c, typeParser.parseTypename(c.getType())))
				.collect(Collectors.toCollection(ArrayListTableColumnGroup::new));
		table.setDbsColumnModels(cmodels);
		
		for(IndexDto idx : tableDto.getIndex()) {
			
			List<ColumnModel> columns = idx.getColumn().stream().sequential()
				.filter(c -> !exclusions.isTableColumnExcluded(tableDto.getName(), c.getName()))
				.map(c -> c.getName())
				.map(cmodels::findColumn)
				.collect(Collectors.toList());
			
			if(columns.isEmpty()) {
				continue;
			}
			
			if(IndexUniqueDto.PRIMARY_KEY == idx.getUnique()) {
				
				if(table.getPrimaryKey() != null) {
					throw new RuntimeException("table " + tableDto.getName() + " has duplicate primary keys defined");
				}
				DbsPrimaryKeyModel pk = new DbsPrimaryKeyModel(idx);
				pk.addAll(columns);
				table.setDbsPrimaryKey(pk);
			} else {
				boolean unique = UNIQUE_INDEX_TYPES.contains(idx.getUnique());
				table.getIndicesImpl().add(new ArrayListIndexModel(idx.getName(), unique, columns));
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

