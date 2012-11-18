package org.gcube.buildutils.mavenizer;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.gcube.buildutils.mavenizer.model.Profile;
import org.gcube.buildutils.mavenizer.model.ProfilePackage;
import org.gcube.buildutils.mavenizer.util.MappingsLoader;
import org.gcube.buildutils.mavenizer.util.MappingRules;
import org.xml.sax.SAXException;

public class ProfileUpdater {
	
	private static boolean isSnapshot = false;
	
	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("Profile2Pom [OPTIONS]", options);		
	}
	
	private static void addMavenCoordinates(ProfilePackage p, MappingRules mapr) throws IOException{
		
		String groupId = mapr.getGroupId(p.getMyProfile().getServiceClass());
		String artifactId = mapr.getArtifactId(p.getPackageName());
		String version = mapr.getVersion(p.getVersion())+(isSnapshot ? "-SNAPSHOT" : "");
		
		try {
			p.setMavenCoordinates(groupId, artifactId, version, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws ParseException, XPathExpressionException, SAXException, IOException, ParserConfigurationException, TransformerException {
		Option helpOpt = OptionBuilder.withLongOpt("help").hasArg(false).withDescription("print this message").create("h");

		Option profileOpt = OptionBuilder.withLongOpt("profile").withArgName("FILENAME").hasArg(true).withDescription("profile.xml file to be used as input").create("p");
		Option extmOpt = OptionBuilder.withLongOpt("extmap").withArgName("PATH").hasArg(true).withDescription("path to ExternalSoftware mapping File").create("e");
		Option staticmOpt = OptionBuilder.withLongOpt("staticmap").withArgName("PATH").hasArg(true).withDescription("path to the static mapping File").create("t");
		Option isSnapshotOpt = OptionBuilder.withLongOpt("snapshot").withDescription("whether postifx verion with -SNAPSHOT or not.").create("s");
		Option appendSystemVersionOpt = OptionBuilder.withLongOpt("systemversion").withDescription("if set, the systemversion  specified is appended to version").create("m");

		
		// create the Options
		Options options = new Options();
		options.addOption(helpOpt);
		options.addOption(profileOpt);
		options.addOption(extmOpt);	
		options.addOption(isSnapshotOpt);
		options.addOption(staticmOpt);
		options.addOption(appendSystemVersionOpt);
		
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);
		
		if (cmd.hasOption(helpOpt.getOpt())) {
			printHelp(options);
			return;
		}
		
		if(cmd.hasOption("s")){
			isSnapshot = true;
		}

		
		if(!cmd.hasOption("p")){
			System.err.println("option --profile is mandatory. Exiting...");
			printHelp(options);
			System.exit(1);
		}
		File inputProfile = new File(cmd.getOptionValue("p"));

		File extMappingFile = null;
		if(cmd.hasOption("e")){
			extMappingFile = new File(cmd.getOptionValue("e"));
		}
		else {
			extMappingFile = new File("externalsMapping.json");
		}		
		
		
		File staticMappingFile = null;
		if(cmd.hasOption("t")){
			staticMappingFile = new File(cmd.getOptionValue("t"));
		}
		else {
			staticMappingFile = new File("staticMappings.json");
		}
		
		String systemVersion = null;
		if(cmd.hasOption("m")){
			systemVersion = cmd.getOptionValue("m");
		}

		Profile profile = Profile.parse(inputProfile);
		
		MappingRules mapr =  new MappingRules(
				MappingsLoader.loadExternalsMapping(extMappingFile),
				MappingsLoader.loadStaticMappings(staticMappingFile));

		for (ProfilePackage p : profile.getPackages()) {
			ProfileUpdater.addMavenCoordinates(p, mapr);
			p.removeDependencies();
			p.removeWSDLs();
			
			if(systemVersion != null){
				String currentVersion = p.getVersion().toString();
				p.setVersion(currentVersion + "-" + systemVersion); 
			}
			
			if(isSnapshot){
				String currentVersion = p.getVersion().toString();
				if(!currentVersion.endsWith("-SNAPSHOT"))
					p.setVersion(currentVersion + "-SNAPSHOT");
			}
		}
		
		//profile.setServiceVersion("1.0.0");
		
		Profile.save(profile, inputProfile);
		
	}

}
