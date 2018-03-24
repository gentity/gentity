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
package org.bitbucket.gentity.core;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.bitbucket.dbsjpagen.config.ConfigurationDto;
import org.bitbucket.dbsjpagen.config.ExclusionDto;
import org.bitbucket.dbsjpagen.config.MappingConfigDto;
import org.bitbucket.dbsjpagen.config.TableConfigurationDto;
import org.bitbucket.dbsjpagen.dbsmodel.ColumnDto;
import org.bitbucket.dbsjpagen.dbsmodel.IndexUniqueDto;
import org.bitbucket.dbsjpagen.dbsmodel.ProjectDto;
import org.bitbucket.dbsjpagen.dbsmodel.TableDto;

/**
 *
 * @author upachler
 */
public class Generator {
	
	private final MappingConfigDto cfg;
	private final ProjectDto project;
	JCodeModel cm;
	
	private static final int MAX_CANDIDATES = 100;
	private Set<String> excludedTables;
	private Set<String> excludedTableColumns;
	private Map<String, ConfigurationDto> tableConfigurations;
	private ConfigurationDto globalConfiguration;
	
	public Generator(MappingConfigDto cfg, ProjectDto project) {
		this.cfg = cfg;
		this.project = project;
	}
	
	
	JCodeModel generate() {
		if(cm == null) {
			cm = new JCodeModel();
		}
		JPackage p = cm._package(cfg.getTargetPackageName());
		try {
			JClass serializableClass = cm.ref(Serializable.class);
			for(TableDto table : project.getSchema().getTable()) {
				if(isTableExcluded(table.getName())) {
					continue;
				}
				JDefinedClass cls = p._class(JMod.PUBLIC, toClassName(p, table.getName()));
				
				cls._implements(serializableClass);
				cls.annotate(Entity.class);
				cls.annotate(Table.class)
					.param("name", table.getName());
				
				for(ColumnDto column : table.getColumn()) {
					if(!isColumnExcluded(table.getName(), column.getName())) {
						genColumn(cls, table, column);
					}
				}
			}
		} catch(JClassAlreadyExistsException caex) {
			throw new RuntimeException(caex);
		}
		
		return cm;
	}
	
	private void genColumn(JDefinedClass cls, TableDto table, ColumnDto column) {
		
		String fieldName = toFieldName(cls, column.getName());
		
		JType colType = mapColumnType(column);
		JFieldVar field = cls.field(JMod.PRIVATE, colType, fieldName);
		
		// @Id
		if(isColumnPrimaryKey(table, column)) {
			field.annotate(Id.class);
		}
		
		// @Column
		JAnnotationUse columnAnnotation = field
			.annotate(Column.class);
		columnAnnotation
			.param("name", column.getName());
		if(colType.equals(cm._ref(String.class))) {
			columnAnnotation.param("length", column.getLength());
		}
		if(isColumnNullable(column)) {
			columnAnnotation.param("nullable", true);
		}
	}
	
	private boolean isColumnNullable(ColumnDto column) {
		return !"y".equals(column.getMandatory());
	}
	
	private boolean isColumnPrimaryKey(TableDto table, ColumnDto column) {
		return table.getIndex().stream()
			.filter(idx -> IndexUniqueDto.PRIMARY_KEY == idx.getUnique())
			.flatMap(idx -> idx.getColumn().stream())
			.anyMatch(col -> col.getName().equals(column.getName()));
	}
	
	private String javatizeName(String name, boolean startUppercase) {
		boolean needsUppercasing = startUppercase;
		StringBuilder sb = new StringBuilder();
		for(char c : name.toCharArray()) {
			boolean alpha = (c >= 'a' && c<='z' || c>='A' && c<='Z');
			if(!alpha) {
				needsUppercasing = false;
				continue;
			}	
			
			if(!needsUppercasing && !Character.isUpperCase(c)) {
				c = Character.toUpperCase(c);
				needsUppercasing = false;
			}
			
			sb.append(c);
		}
		return sb.toString();
	}
	
	private String findNonexistingName(String prefix, String suffix, Predicate<String> existenceTest) {
		String candidate = "";
		for(int n=0; n<MAX_CANDIDATES; ++n) {
			String candidateNumber = n==0 ? "" : Integer.toString(n);
			candidate = prefix + candidateNumber + suffix;
			
			if(!existenceTest.test(candidate)) {
				return candidate;
			}
		}
		throw new RuntimeException("too many attempts to form a name for table, last unsuccessful candidate was '" + candidate + "'");
	}
	
	private String toClassName(JPackage p, String name) {
		
		String baseName = javatizeName(name, true);
		
		ConfigurationDto cfg = findClassOptions(name);
		return findNonexistingName(cfg.getClassNamePrefix() + baseName, cfg.getClassNameSuffix(), p::isDefined);
	}

	private String toFieldName(JDefinedClass cls, String name) {
		String baseName = javatizeName(name, false);
		
		ConfigurationDto cfg = findClassOptions(name);
		return findNonexistingName(cfg.getFieldNamePrefix() + baseName, cfg.getFieldNameSuffix(), cls.fields()::containsKey);
	}
	
	private ConfigurationDto findClassOptions(String name) {
		if(tableConfigurations == null) {
			tableConfigurations = 
				cfg.getTable().stream()
				.collect(Collectors.toMap(TableConfigurationDto::getTable, cfg -> cfg));
		}
		
		if(globalConfiguration == null) {
			globalConfiguration = Optional.ofNullable(cfg.getConfiguration()).orElse(new ConfigurationDto());
		}
		
		return tableConfigurations.getOrDefault(name, globalConfiguration);
	}
	
	private boolean isTableExcluded(String tableName) {
		if(excludedTables == null) {
			excludedTables = cfg.getExclude().stream()
				.filter( x -> x.getColumn() == null)
				.map(ExclusionDto::getTable)
				.collect(Collectors.toSet());
		}
		return excludedTables.contains(tableName);
	}
	
	private String toColumnExclusionKey(String tableName, String columnName) {
		return tableName + '|' + columnName;
	}
	
	private boolean isColumnExcluded(String tableName, String columnName) {
		if(excludedTableColumns == null) {
			excludedTableColumns = cfg.getExclude().stream()
				.filter(x -> x.getColumn() != null)
				.map(x -> toColumnExclusionKey(x.getTable(), x.getColumn()))
				.collect(Collectors.toSet());
		}
		return excludedTableColumns.contains(toColumnExclusionKey(tableName, columnName));
	}

	
	private String normalizeTypeName(String typeName) {
		return typeName.toLowerCase();
	}
	
	
	private JType mapColumnType(ColumnDto column) {
		String type = normalizeTypeName(column.getType());
		
		JType jtype;
		switch(type) {
			case "char":
			case "character":
				if(Short.valueOf((short)1).equals(column.getLength())) {
					jtype = cm.ref(Character.class);
				} else {
					jtype = cm.ref(String.class);
				}
				break;
			case "varchar":
			case "character varying":
				jtype = cm.ref(String.class);
				break;
			case "bigint":
				jtype = cm.LONG;
				break;
			case "integer":
				jtype = cm.INT;
				break;
			case "bool":
			case "boolean":
				jtype = cm.BOOLEAN;
				break;
			case "float":
				jtype = cm.FLOAT;
				break;
			case "real":
			case "double":
			case "double precision":
				jtype = cm.DOUBLE;
				break;
			default:
				throw new RuntimeException("no mapping found for SQL type '" + column.getType() + "'");
		}
		
		if(isColumnNullable(column)) {
			jtype = jtype.boxify();
		}
		
		return jtype;
	}

}
