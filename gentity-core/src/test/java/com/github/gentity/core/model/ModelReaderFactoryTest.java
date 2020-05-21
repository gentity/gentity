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
package com.github.gentity.core.model;

import com.github.gentity.core.Exclusions;
import com.github.gentity.core.model.dbs.DbsModelReaderFactory;
import com.github.gentity.core.model.mwb.MwbModelReaderFactory;
import com.github.gentity.core.model.types.SQLTypeParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author upachler
 */
public class ModelReaderFactoryTest {
	
	public ModelReaderFactoryTest() {
	}
	
	private final DbsModelReaderFactory DBS_FACTORY = new DbsModelReaderFactory();
	private final MwbModelReaderFactory MWB_FACTORY = new MwbModelReaderFactory();
	
	private final String COMPANY_DBS_FILENAME = "company.dbs";
	private final String COMPANY_MWB_FILENAME = "company.mwb";
	
	private final ReaderContext COMPANY_DBS_SS = new ResourceReaderContextImpl(getClass(), COMPANY_DBS_FILENAME);
	private final ReaderContext COMPANY_MWB_SS = new ResourceReaderContextImpl(getClass(), COMPANY_MWB_FILENAME);
	
	/**
	 * Test of supportsReading method, of class ModelReaderFactory.
	 */
	@Test
	public void testSupportsReading() throws Exception {
		System.out.println("supportsReading");
		
		// check if factories recognize file formats correctly
		
		assertTrue(DBS_FACTORY.supportsReading(COMPANY_DBS_FILENAME, COMPANY_DBS_SS));
		assertFalse(DBS_FACTORY.supportsReading(COMPANY_MWB_FILENAME, COMPANY_MWB_SS));
		
		assertTrue(MWB_FACTORY.supportsReading(COMPANY_MWB_FILENAME, COMPANY_MWB_SS));
		assertFalse(MWB_FACTORY.supportsReading(COMPANY_DBS_FILENAME, COMPANY_DBS_SS));
	}

	/**
	 * Test of createModelReader method, of class ModelReaderFactory.
	 */
	@Test
	public void testCreateModelReader() throws IOException {
		System.out.println("createModelReader");
		testReadCompanyModel(DBS_FACTORY, COMPANY_DBS_FILENAME, COMPANY_DBS_SS);
		testReadCompanyModel(MWB_FACTORY, COMPANY_MWB_FILENAME, COMPANY_MWB_SS);
	}
	
	private void testReadCompanyModel(ModelReaderFactory factory, String filename, ReaderContext readerContext) throws IOException {
		
		ModelReader r = factory.createModelReader(filename, readerContext);
		DatabaseModel m = r.read(Exclusions.EMPTY);
		
		TableModel company = m.getTable("company");
		assertNotNull(company);
		TableModel employee = m.getTable("employee");
		assertNotNull(employee);
		TableModel time_record = m.getTable("time_record");
		assertNotNull(time_record);
		
	}

}
