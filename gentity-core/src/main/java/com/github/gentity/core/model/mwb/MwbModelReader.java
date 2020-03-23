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
import com.github.gentity.core.model.DatabaseModel;
import com.github.gentity.core.model.InputStreamSupplier;
import com.github.gentity.core.model.ModelReader;
import com.github.mwbmodel.Loader;
import com.github.mwbmodel.model.db.mysql.Column;
import com.github.mwbmodel.model.db.mysql.Schema;
import com.github.mwbmodel.model.db.mysql.Table;
import com.github.mwbmodel.model.workbench.Document;
import com.github.mwbmodel.model.workbench.physical.Model;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author count
 */
public class MwbModelReader implements ModelReader{

	private final InputStreamSupplier streamSupplier;

	MwbModelReader(String fileName, InputStreamSupplier streamSupplier) {
		this.streamSupplier = streamSupplier;
	}

	@Override
	public DatabaseModel read(Exclusions exclusions) throws IOException {
		Document doc = Loader.loadMwb(streamSupplier.get());
		
		Map<Column, MwbColumnModel> colMap = new HashMap<>();
		
		Map<String,MwbTableModel> tables = new HashMap<>();
		for(Model pm : doc.getPhysicalModels()) {
			List<Schema> schemata = pm.getCatalog().getSchemata();
			if(!schemata.isEmpty()) {
				for(Table t : schemata.get(0).getTables()) {
					MwbTableModel mt = new MwbTableModel(t);
					tables.put(mt.getName(), mt);
				}
			}
		}
		
		return new MwbDatabaseModel(tables);
	}
	
}
