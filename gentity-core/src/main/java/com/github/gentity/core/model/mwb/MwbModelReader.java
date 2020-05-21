/*
 * Copyright 2020 The Gentity Project. All rights reserved.
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
package com.github.gentity.core.model.mwb;

import com.github.gentity.core.Exclusions;
import com.github.gentity.core.model.ColumnModel;
import com.github.gentity.core.model.DatabaseModel;
import com.github.gentity.core.model.ForeignKeyModel;
import com.github.gentity.core.model.ForeignKeyModel.Mapping;
import com.github.gentity.core.model.ModelReader;
import com.github.gentity.core.model.TableColumnGroup;
import com.github.gentity.core.model.util.ArrayListTableColumnGroup;
import com.github.gentity.core.model.types.SQLTypeParser;
import com.github.mwbmodel.Loader;
import com.github.mwbmodel.model.db.SimpleDatatype;
import com.github.mwbmodel.model.db.mysql.Column;
import com.github.mwbmodel.model.db.mysql.Schema;
import com.github.mwbmodel.model.db.mysql.Table;
import com.github.mwbmodel.model.workbench.Document;
import com.github.mwbmodel.model.workbench.physical.Model;
import java.io.IOException;
import java.sql.JDBCType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.gentity.core.model.ReaderContext;
import com.github.gentity.core.model.util.ArrayListIndexModel;
import com.github.mwbmodel.model.db.mysql.ForeignKey;
import com.github.mwbmodel.model.db.mysql.Index;
import com.github.mwbmodel.model.db.mysql.IndexColumn;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 *
 * @author count
 */
public class MwbModelReader implements ModelReader{

	private final ReaderContext context;

	MwbModelReader(String fileName, ReaderContext readerContext) {
		this.context = readerContext;
	}

	private MwbColumnModel toColumnModel(Column c, SQLTypeParser typeParser) {
		SimpleDatatype sdt = c.getSimpleType();
		JDBCType jdbcType = typeParser.parseTypename(sdt.getName(), null);
		if(jdbcType == null) {
			// attempt synonyms
			for(String synonym : sdt.getSynonyms()) {
				jdbcType = typeParser.parseTypename(synonym, null);
				if(jdbcType != null) {
					break;
				}
			}
		}
		
		if(jdbcType == null) {
			throw new UnsupportedOperationException("column type " + sdt.getName() + " was not recognized");
		}
		
		return new MwbColumnModel(c, jdbcType);
	}
	
	@Override
	public DatabaseModel read(Exclusions exclusions) throws IOException {
		Document doc = Loader.loadMwb(context.open());
		
			
		Map<Table,MwbTableModel> tables = new HashMap<>();
		for(Model pm : doc.getPhysicalModels()) {
			SQLTypeParser typeParser = context.findTypeParser(pm.getRdbms().getName());

			List<Schema> schemata = pm.getCatalog().getSchemata();
			if(!schemata.isEmpty()) {
				Schema s = schemata.get(0);
				
				// create tables, columns and indices
				for(Table t : s.getTables()) {
					TableColumnGroup<MwbColumnModel> colModels = new ArrayListTableColumnGroup<>();
		
					Map<Column, MwbColumnModel> colMap = new HashMap<>();					
					for(Column c : t.getColumns()) {
						MwbColumnModel cm = toColumnModel(c, typeParser);
						colModels.add(cm);
						colMap.put(c, cm);
					}
					
					MwbTableModel mt = new MwbTableModel(t, colModels);
					tables.put(t, mt);
					
					for(Index i : t.getIndices()) {
						convertIndex(mt, i);
					}
				}
				
				// create foreign keys (referring to columns)
				for(Table t : s.getTables()) {
					
					MwbTableModel tm = tables.get(t);
					for(ForeignKey fk : t.getForeignKeys()) {
						List<Mapping> mappings = new ArrayList<>();
						MwbTableModel targetTable = tables.get(fk.getReferencedTable());
						for(int i=0; i<fk.getColumns().size(); ++i) {
							
							ColumnModel childColumn = tm.getMappedColumnModel(fk.getColumns().get(i));
							ColumnModel parentColumn = targetTable.getMappedColumnModel(fk.getReferencedColumns().get(i));
							Mapping mapping = new ForeignKeyModel.Mapping(childColumn, parentColumn);
							mappings.add(mapping);
						}
						MwbForeignKeyModel fkm = new MwbForeignKeyModel(fk, mappings, tm, targetTable);
						tm.getForeignKeyImpl().add(fkm);
					}
				}
			}
		}
		
		
		return new MwbDatabaseModel(tables.entrySet().stream()
			.collect(Collectors.toMap(e->e.getKey().getName(), Entry::getValue))
		);
	}
	
	private void convertIndex(MwbTableModel tm, Index idx) {
		ArrayListIndexModel i;
		List<ColumnModel> cm = new ArrayList<>();
		for(IndexColumn ic : idx.getColumns()) {
			ColumnModel c = tm.getMappedColumnModel(ic.getReferencedColumn());
			cm.add(c);
		}
		if(idx.getIndexType() == Index.Type.PRIMARY) {
			if(tm.getPrimaryKey() != null) {
					throw new RuntimeException("table " + tm.getName() + " has duplicate primary keys defined");
			}
			
			MwbPrimaryKeyModel pk = new MwbPrimaryKeyModel(tm, idx);
			tm.setPrimaryKeyModel(pk);
		} else {
			boolean unique = idx.getIndexType() ==  Index.Type.UNIQUE;
			i = new ArrayListIndexModel(idx.getName(), unique, cm);
			tm.getIndicesImpl().add(i);
		}
		
	}
	
}
