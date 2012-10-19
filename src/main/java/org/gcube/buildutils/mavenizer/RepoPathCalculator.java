package org.gcube.buildutils.mavenizer;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public class RepoPathCalculator {
	


	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Profile2Pom [OPTIONS]", options);		
	}


	/**
	 * @param args
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws ParseException {
		Option helpOpt = OptionBuilder.withLongOpt("help").hasArg(false).withDescription("print this message").create("h");

		Option inputPomOpt = OptionBuilder.withLongOpt("pom").withArgName("FILENAME").hasArg(true).withDescription("pom.xml file to use as input").create("p");		
		Option groupIdOpt = OptionBuilder.withLongOpt("groupid").withArgName("GROUP_ID").hasArg(true).withDescription("groupId. Override the one in the pom").create("g");
		Option artifactIdOpt = OptionBuilder.withLongOpt("artifactid").withArgName("ARTIFACT_ID").hasArg(true).withDescription("artifactId. Override the one in the pom").create("a");
		Option versionOpt = OptionBuilder.withLongOpt("version").withArgName("VERSION").hasArg(true).withDescription("version. Override the one in the pom").create("v");		
		
		Option classifierOpt = OptionBuilder.withLongOpt("classifier").withArgName("FILENAME").hasArg(true).withDescription("optional. If provided also packaging must be set").create("c");		
		Option extensionOpt = OptionBuilder.withLongOpt("extension").withArgName("FILENAME").hasArg(true).withDescription("extenasion type").create("e");		
	
		
		
		
		// create the Options
		Options options = new Options();
		options.addOption(helpOpt);
		options.addOption(inputPomOpt);
		options.addOption(classifierOpt);
		options.addOption(extensionOpt);
		options.addOption(groupIdOpt);
		options.addOption(artifactIdOpt);
		options.addOption(versionOpt);
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);
		
		if (cmd.hasOption(helpOpt.getOpt())) {
			printHelp(options);
			return;
		}


		if(cmd.hasOption("c") && !cmd.hasOption("e")){
			System.err.println("if hte classifier option is specified, also extension must be provided. Exiting...");
			printHelp(options);
			System.exit(1);
		}
		String classifier = cmd.getOptionValue("c");
		if(classifier == null) 
			classifier = "";
		else
			classifier = "-" + classifier;
		
		String extension = cmd.getOptionValue("e");
		if(extension == null) extension = "pom";
		
		
		
		


		
		String version = null;
		String artifactId = null;
		String groupId = null;
		
		
		if(cmd.hasOption("p")){
			File inputPom = new File(cmd.getOptionValue("p"));
			Model model = null;
			FileReader reader = null;
			MavenXpp3Reader mavenreader = new MavenXpp3Reader();
			try {
			    reader = new FileReader(inputPom);
			    model = mavenreader.read(reader);
			}catch(Exception ex){ex.printStackTrace();}			
			
			
			Parent parent = model.getParent();
			
			groupId = model.getGroupId();
			if(groupId == null && parent != null){
				groupId = parent.getGroupId();
			}
			
			artifactId = model.getArtifactId();

			version = model.getVersion();
			if(version == null && parent != null){
				version = parent.getVersion();
			}
		}
		
		if(cmd.hasOption("g"))
			groupId = cmd.getOptionValue("g");
		
		if(cmd.hasOption("a"))
			artifactId = cmd.getOptionValue("a");
		
		if(cmd.hasOption("v"))
			version = cmd.getOptionValue("v");
		
		
		System.out.println( "/" + groupId.replace(".", "/") +
							"/" + artifactId +
							"/" + version +
							"/" + artifactId + "-" + version + classifier + "." + extension);
	}

}
