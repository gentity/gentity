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
package com.github.gentity.core;

import com.sun.codemodel.JCodeModel;
import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import com.github.gentity.core.config.dto.MappingConfigDto;
import com.github.gentity.core.model.dbs.dto.ProjectDto;
import com.github.gentity.core.xsd.R;
import javax.xml.bind.ValidationEventHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author upachler
 */
public class FileShell {
	
	private String targetPackageName;

	public void setTargetPackageName(String targetPackageName) {
		this.targetPackageName = targetPackageName;
	}
	
	private ValidationEventHandler validationEventHandler = event -> {
		throw new RuntimeException(event.getMessage(), event.getLinkedException());
	};
	
	public void generate(File inputFile, File configFile, File outputFolder) throws IOException {

		Schema dbsSchema;
		Schema genconfigSchema;
		try {
			dbsSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(R.class.getResource("dbs.xsd"));
			genconfigSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
				.newSchema(R.class.getResource("genconfig.xsd"));
		} catch (SAXException ex) {
			throw new RuntimeException(ex);
		}
		
		MappingConfigDto config = new MappingConfigDto();
		if(configFile != null) {
			try {
				Unmarshaller unmarshaller = JAXBContext.newInstance(com.github.gentity.core.config.dto.ObjectFactory.class)
					.createUnmarshaller();
				unmarshaller.setSchema(genconfigSchema);
				unmarshaller.setEventHandler(validationEventHandler);
				JAXBElement<MappingConfigDto> configElement = (JAXBElement<MappingConfigDto>)unmarshaller
					.unmarshal(configFile);
				config = configElement.getValue();
				

			} catch (JAXBException ex) {
				checkForIOException(ex);
				throw new RuntimeException(ex);
			}
		}
		
		// if a targetPackageName was specified to this shell instance and the
		// configured value for the package name is empty (the default), the
		// value is overridden with the one specified in the shell
		if(targetPackageName != null && config.getTargetPackageName().isEmpty()) {
			config.setTargetPackageName(targetPackageName);
		}
		
		ProjectDto project;
		try {
			Unmarshaller unmarshaller = JAXBContext.newInstance(com.github.gentity.core.model.dbs.dto.ObjectFactory.class)
				.createUnmarshaller();
			unmarshaller.setSchema(dbsSchema);
			unmarshaller.setEventHandler(validationEventHandler);
			JAXBElement<ProjectDto> schemaElement = (JAXBElement<ProjectDto>)unmarshaller
				.unmarshal(inputFile);
			project = schemaElement.getValue();
		} catch (JAXBException ex) {
			checkForIOException(ex);
			throw new RuntimeException(ex);
		}
		
		Generator gen = new Generator(config, project);
		
		JCodeModel cm = gen.generate();
		
		cm.build(outputFolder);
	}

	private void checkForIOException(JAXBException ex) throws IOException {
		if(ex.getCause() instanceof IOException) {
			throw (IOException)ex.getCause();
		}
	}
}
