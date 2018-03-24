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

import com.sun.codemodel.JCodeModel;
import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import org.bitbucket.dbsjpagen.config.MappingConfigDto;
import org.bitbucket.dbsjpagen.config.ObjectFactory;
import org.bitbucket.dbsjpagen.dbsmodel.ProjectDto;

/**
 *
 * @author upachler
 */
public class FileShell {
	
	public void generate(File inputFile, File configFile, File outputFolder) throws IOException {
		
		MappingConfigDto config = new MappingConfigDto();
		if(configFile != null) {
			try {
				JAXBElement<MappingConfigDto> configElement = (JAXBElement<MappingConfigDto>)JAXBContext.newInstance(org.bitbucket.dbsjpagen.config.ObjectFactory.class)
					.createUnmarshaller()
					.unmarshal(configFile);
				config = configElement.getValue();
				

			} catch (JAXBException ex) {
				checkForIOException(ex);
				throw new RuntimeException(ex);
			}
		}
		
		ProjectDto project;
		try {
			JAXBElement<ProjectDto> schemaElement = (JAXBElement<ProjectDto>) JAXBContext.newInstance(org.bitbucket.dbsjpagen.dbsmodel.ObjectFactory.class)
				.createUnmarshaller()
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
