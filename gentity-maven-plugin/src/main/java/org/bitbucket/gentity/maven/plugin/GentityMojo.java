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
package org.bitbucket.gentity.maven.plugin;

import java.io.File;
import java.io.IOException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.bitbucket.gentity.core.FileShell;

/**
 *
 * @author upachler
 */
@Mojo( name = "generate")
public class GentityMojo extends AbstractMojo{
	
	@Parameter( property = "generate.inputDbsFile", required = true )
	private File inputDbsFile;
	
	@Parameter( property = "generate.outputFolder", defaultValue = "target/generated-sources/gentity" )
	private File outputFolder;
	
	@Parameter( property = "generate.mappingConfigFile" )
	private File mappingConfigFile;
	
    public void execute() throws MojoExecutionException {
		if(!outputFolder.exists()) {
			if(!outputFolder.mkdirs()) {
				throw new MojoExecutionException("output folder '" + outputFolder.toString() + "' does not exist, and cannot be created");
			}
		}
		
		if(!inputDbsFile.exists()) {
			throw new MojoExecutionException("input file '" + inputDbsFile.toString() + "' does not exist");
		}
		
		if(mappingConfigFile != null && !mappingConfigFile.exists()) {
			getLog().warn("specified mapping config file '" + mappingConfigFile + "' does not exist, continuing without it");
			mappingConfigFile = null;
		}
		
		FileShell shell = new FileShell();
		try {
			shell.generate(inputDbsFile, mappingConfigFile, outputFolder);
		} catch (IOException ex) {
			throw new MojoExecutionException("error while generating entities", ex);
		}
		
    }
	
}
