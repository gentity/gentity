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
package com.github.gentity.core.model.dbs;

import com.github.gentity.core.model.InputStreamSupplier;
import com.github.gentity.core.model.ModelReader;
import com.github.gentity.core.model.ModelReaderFactory;
import com.github.gentity.core.xsd.R;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author count
 */
public class DbsModelReaderFactory implements ModelReaderFactory {
	
	JAXBContext jaxbContext;
	Schema dbsSchema;
	
	private ValidationEventHandler validationEventHandler = event -> {
		throw new RuntimeException(event.getMessage(), event.getLinkedException());
	};
	

	synchronized Unmarshaller createUnmarshaller() throws JAXBException {
		if(jaxbContext == null) {
			try {
				dbsSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
					.newSchema(R.class.getResource("dbs.xsd"));
			} catch (SAXException ex) {
				throw new RuntimeException(ex);
			}
			jaxbContext = JAXBContext.newInstance(com.github.gentity.core.model.dbs.dto.ObjectFactory.class);
		}
		
		Unmarshaller unmarshaller = 	jaxbContext.createUnmarshaller();
		unmarshaller.setSchema(dbsSchema);
		unmarshaller.setEventHandler(validationEventHandler);
		return unmarshaller;
	}

	@Override
	public boolean supportsReading(String fileName, InputStreamSupplier streamSupplier) throws IOException {
		return fileName.toLowerCase().endsWith(".dbs");
	}

	@Override
	public ModelReader createModelReader(String fileName, InputStreamSupplier streamSupplier) {
		return new DbsModelReader(this, fileName, streamSupplier);
	}
	
}
