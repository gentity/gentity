/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
		
		if(mappingConfigFile != null && mappingConfigFile.exists()) {
			getLog().warn("specified mapping config file '" + mappingConfigFile + "' does not exist, continuing without it");
		}
		
		FileShell shell = new FileShell();
		try {
			shell.generate(inputDbsFile, mappingConfigFile, outputFolder);
		} catch (IOException ex) {
			throw new MojoExecutionException("error while generating entities", ex);
		}
		
    }
	
}
