/*
 * Copyright 2026 The Gentity Project. All rights reserved.
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
package com.github.gentity.core;

import com.github.gentity.core.config.dto.MappingConfigDto;
import com.github.gentity.core.model.ModelReader;
import com.github.gentity.core.model.ReaderContext;
import com.github.gentity.core.model.ResourceReaderContextImpl;
import com.github.gentity.core.model.dbs.DbsModelReaderFactory;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author upachler
 */
public class SchemaModelImplTest {

	public SchemaModelImplTest() {
	}

	private static final String M2M_DEFAULTED_FILENAME = "manytomany_defaulted.dbs";

	private static final ShellLogger LOGGER = new JULShellLogger(Logger.getLogger(SchemaModelImplTest.class.getName()));

	private SchemaModelImpl readSchemaModel(String fileName) throws IOException {
		ReaderContext ctx = new ResourceReaderContextImpl(getClass(), fileName);
		ModelReader reader = new DbsModelReaderFactory().createModelReader(fileName, ctx);
		return new SchemaModelImpl(new MappingConfigDto(), reader, LOGGER);
	}

	private List<String> joinTableNames(SchemaModelImpl sm) {
		return sm.getJoinTableRelations().stream()
			.map(r -> r.getTable().getName())
			.collect(Collectors.toList());
	}

	/**
	 * Join table relations that are not declared in the mapping configuration
	 * are detected by scanning the tables of the model. That scan must keep the
	 * order in which the tables are declared in the model file, because the
	 * order of the relations decides the order in which the generator emits the
	 * corresponding many-to-many fields.
	 * <p>
	 * Collecting the detected tables into hash-ordered sets made that order
	 * depend on identity hash codes, so generated sources differed between JVM
	 * runs even though the model file had not changed.
	 */
	@Test
	public void testDefaultedJoinTableRelationsKeepModelOrder() throws IOException {
		System.out.println("testDefaultedJoinTableRelationsKeepModelOrder");

		SchemaModelImpl sm = readSchemaModel(M2M_DEFAULTED_FILENAME);

		assertEquals(
			Arrays.asList("BOOK_AUTHOR", "AUTHOR_PUBLISHER", "BOOK_PUBLISHER"),
			joinTableNames(sm)
		);
	}

	/**
	 * Reading the same model twice must yield the same relation order. Note
	 * that this cannot fail for hash-ordered collections within a single JVM,
	 * since identity hash codes are stable there - it guards against ordering
	 * that depends on the model instance rather than on the model content.
	 */
	@Test
	public void testDefaultedJoinTableRelationOrderIsRepeatable() throws IOException {
		System.out.println("testDefaultedJoinTableRelationOrderIsRepeatable");

		assertEquals(
			joinTableNames(readSchemaModel(M2M_DEFAULTED_FILENAME)),
			joinTableNames(readSchemaModel(M2M_DEFAULTED_FILENAME))
		);
	}
}
