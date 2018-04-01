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
import org.apache.maven.project.MavenProject;
import org.bitbucket.gentity.core.FileShell;

/**
 *
 * @author upachler
 */
@Mojo( name = "generate")
public class GentityMojo extends AbstractMojo{
	
	/**
	 * Input DbSchema file that is the source for the entity generator.
	 */
	@Parameter( property = "generate.inputDbsFile", required = true )
	private File inputDbsFile;
	
	/**
	 * The base directory into which the sources are generated. If the sources
	 * are generated into a specific package, do not specify this here, but 
	 * use the {@code targetPackageName} property instead.
	 */
	@Parameter( property = "generate.outputFolder", defaultValue = "target/generated-sources/gentity" )
	private File outputFolder;
	
	/**
	 * Path to the mapping configuration file. If not specfied, a default 
	 * configuration will be used. Use the mapping configuration file to 
	 * customize the entity generation process, (e.g. to specify which 
	 * associations are one-to-many and which are many-to-many)
	 */
	@Parameter( property = "generate.mappingConfigFile" )
	private File mappingConfigFile;
	
	/**
	 * The name of the java package that the sources are generated into. This
	 * setting can be overridden in the mapping configuration file by specifying
	 * a non-empty package name there.
	 */
	@Parameter( property = "generate.targetPackageName" )
	private String targetPackageName;
	
	@Parameter (defaultValue="${project}", required=true, readonly=true)
	private MavenProject project;

	@Override
    public void execute() throws MojoExecutionException {
		project.addCompileSourceRoot(outputFolder.getAbsolutePath());
		
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
		shell.setTargetPackageName(targetPackageName);
		
		try {
			shell.generate(inputDbsFile, mappingConfigFile, outputFolder);
		} catch (IOException ex) {
			throw new MojoExecutionException("error while generating entities", ex);
		}
		
    }
	
}
