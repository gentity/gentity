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
import com.github.dbsjpagen.dbsmodel.ProjectDto;
import com.github.gentity.core.model.DatabaseModel;
import com.github.gentity.core.model.SequenceModel;
import com.github.gentity.core.model.TableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author count
 */
public class DbsDatabaseModel implements DatabaseModel{

	private final ProjectDto dbsProject;
	private final Exclusions exclusions;
	private final Map<String,DbsTableModel> tables;
	private final Map<String,DbsSequenceModel> sequences;

	public DbsDatabaseModel(ProjectDto dbsProject, Exclusions exclusions, Map<String,DbsTableModel> tables, Map<String,DbsSequenceModel> sequences) {
		this.dbsProject = dbsProject;
		this.exclusions = exclusions;
		this.tables = tables;
		for(DbsTableModel table : tables.values()) {
			table.setDbsDatabaseModel(this);
		}
		this.sequences = sequences;
	}

	@Override
	public TableModel getTable(String name) {
		return tables.get(name);
	}

	@Override
	public List<TableModel> getTables() {
		return Collections.unmodifiableList(new ArrayList<>(tables.values()));
	}

	SequenceModel getSequence(String name) {
		return sequences.get(name);
	}
	
}
