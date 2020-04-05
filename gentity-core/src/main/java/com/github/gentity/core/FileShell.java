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
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import com.github.gentity.core.config.dto.MappingConfigDto;
import com.github.gentity.core.model.ModelReader;
import com.github.gentity.core.model.ModelReaderFactory;
import com.github.gentity.core.model.ReaderContext;
import com.github.gentity.core.util.UnmarshallerFactory;
import com.github.gentity.core.xsd.R;
import java.io.FileInputStream;
import java.util.ServiceLoader;

/**
 *
 * @author upachler
 */
public class FileShell {
	
	private String targetPackageName;
	private static final UnmarshallerFactory uFactory = new UnmarshallerFactory(R.class.getResource("genconfig.xsd"), com.github.gentity.core.config.dto.ObjectFactory.class);

	public void setTargetPackageName(String targetPackageName) {
		this.targetPackageName = targetPackageName;
	}
	
	public void generate(File inputFile, File configFile, File outputFolder) throws IOException {

		MappingConfigDto config = new MappingConfigDto();
		if(configFile != null) {
			try {
				Unmarshaller unmarshaller = uFactory.createUnmarshaller();
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
		
		ModelReaderFactory modelReaderFactory = null;
		
		Iterable<ModelReaderFactory> factories;
		factories = ServiceLoader.load(ModelReaderFactory.class);
		
		ReaderContext ctx = new FileReaderContextImpl(inputFile);
		for(ModelReaderFactory f : factories) {
			if(f.supportsReading(inputFile.getName(), ctx)) {
				modelReaderFactory = f;
				break;
			}
		}
		if(modelReaderFactory == null) {
			throw new RuntimeException("The format of the provided file '" + inputFile.getName() + "' is not supported");
		}
		ModelReader reader = modelReaderFactory.createModelReader(inputFile.getName(), ctx);
		
		Generator gen = new Generator(config, reader);
		
		JCodeModel cm = gen.generate();
		
		cm.build(outputFolder);
	}

	private void checkForIOException(JAXBException ex) throws IOException {
		if(ex.getCause() instanceof IOException) {
			throw (IOException)ex.getCause();
		}
	}
}
