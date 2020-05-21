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
import java.io.IOException;
import java.sql.JDBCType;
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
	public void testRead_DBS() throws IOException {
		System.out.println("testRead_DBS");
		testReadCompanyModel(DBS_FACTORY, COMPANY_DBS_FILENAME, COMPANY_DBS_SS);
	}
	
	@Test
	public void testRead_MWB() throws IOException {
		System.out.println("testRead_MWB");
		testReadCompanyModel(MWB_FACTORY, COMPANY_MWB_FILENAME, COMPANY_MWB_SS);
	}
	
	private void testReadCompanyModel(ModelReaderFactory factory, String filename, ReaderContext readerContext) throws IOException {
		
		ModelReader r = factory.createModelReader(filename, readerContext);
		DatabaseModel m = r.read(Exclusions.EMPTY);
		
		TableModel company = m.getTable("company");
		assertNotNull(company);
		
		ColumnModel companyId = company.getColumns().findColumn("id");
		assertEquals(JDBCType.INTEGER, companyId.getType());
		assertTrue(companyId.isIdentityColumn());
		assertFalse(companyId.isNullable());
		assertEquals(companyId.getName(), company.getPrimaryKey().get(0).getName());
		
		ColumnModel companyName = company.getColumns().findColumn("name");
		assertEquals(JDBCType.VARCHAR, companyName.getType());
		assertEquals(100, companyName.getLength());
		assertFalse(companyName.isIdentityColumn());
		assertFalse(companyName.isNullable());
		
		ColumnModel companyEstablished = company.getColumns().findColumn("established");
		assertEquals(JDBCType.TIMESTAMP, companyEstablished.getType());
		
		ColumnModel companyParentId = company.getColumns().findColumn("parent_company_id");
		assertEquals(JDBCType.INTEGER, companyParentId.getType());
		assertTrue(companyParentId.isNullable());
		
		// check foreign key of parent table referencing self (the parent company)
		ForeignKeyModel companyFkCompanyCompany = company.findForeignKey("fk_company_company");
		assertNotNull(companyFkCompanyCompany);
		assertSame(company, companyFkCompanyCompany.getTargetTable());
		assertEquals(1, companyFkCompanyCompany.getColumnMappings().size());
		ForeignKeyModel.Mapping companyFkParentCompanyMapping = companyFkCompanyCompany.getColumnMappings().get(0);
		assertEquals(companyParentId, companyFkParentCompanyMapping.getChildColumn());
		assertEquals(companyId, companyFkParentCompanyMapping.getParentColumn());
		
		// we're not checking employee to thoroughly (because most cases are already
		// covered in the prevous company table tests).
		// We mostly check for the foreign keys here
		TableModel employee = m.getTable("employee");
		assertNotNull(employee);
		ColumnModel employeeCompanyId = employee.getColumns().findColumn("company_id");
		assertFalse(employeeCompanyId.isNullable());
		ForeignKeyModel employeeFkCompany = employee.findForeignKey("fk_employee_company");
		assertSame(company, employeeFkCompany.getTargetTable());
		ForeignKeyModel.Mapping employeeKfCompanyColumnMapping = employeeFkCompany.getColumnMappings().get(0);
		assertSame(companyId, employeeKfCompanyColumnMapping.getParentColumn());
		assertSame(employeeCompanyId, employeeKfCompanyColumnMapping.getChildColumn());
		
		// check that the time_record table exists
		TableModel time_record = m.getTable("time_record");
		assertNotNull(time_record);
		
	}

}
