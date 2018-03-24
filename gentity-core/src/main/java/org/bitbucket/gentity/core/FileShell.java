/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
