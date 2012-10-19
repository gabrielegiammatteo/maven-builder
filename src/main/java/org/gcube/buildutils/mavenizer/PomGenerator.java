package org.gcube.buildutils.mavenizer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.gcube.buildutils.mavenizer.model.Profile;
import org.gcube.buildutils.mavenizer.model.ProfilePackage;
import org.gcube.buildutils.mavenizer.model.ProfilePackageDependency;
import org.gcube.buildutils.mavenizer.util.MappingRules;
import org.gcube.buildutils.mavenizer.util.MappingsLoader;
import org.xml.sax.SAXException;

public class PomGenerator {
	
	private static boolean isSnapshot = false;
	
	
	
	private static ProfilePackage extractCoordinatesFromProfile(Map<String, String> coordinates, File inputProfile, boolean firstPackage, String artifactName, String packageName, MappingRules mapr) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException{
		
		ProfilePackage matchingPackage = null;

		Profile profile = Profile.parse(inputProfile);
		
		if(firstPackage){
			matchingPackage = profile.getPackages().get(0);
		}
		else if(artifactName!=null){
			System.out.println("[PomGenerator][INFO] Analyzing profile "+inputProfile+" searching for a package containing "+artifactName+"...");
			for (ProfilePackage p : profile.getPackages()) {
				if(matchingPackage != null) break;
				Set<String> files = p.getFiles();
				for(String f: files){
					if(f.contains(artifactName)){
						System.out.println("[PomGenerator][INFO] Match Found in pakage "+p.getPackageName());
						matchingPackage = p;
						break;
					}
				}
			}
		}
		else {
			System.out.println("Analyzing profile "+inputProfile+" searching for a package with name "+packageName+"...");
			for (ProfilePackage p : profile.getPackages()) {
				if(matchingPackage != null) break;
				if(p.getPackageName().equals(packageName)){
						System.out.println("[PomGenerator][INFO] Match Found in pakage "+p.getPackageName());
						matchingPackage = p;
						break;
				}
			}
		}

		if(matchingPackage == null){
			if(artifactName!=null){
				System.out.println("[PomGenerator][ERROR] Couldn't find any matching package containing file " + artifactName +" in profile "+inputProfile+".");				
				System.err.println("[PomGenerator][ERROR] Couldn't find any matching package containing file " + artifactName +" in profile "+inputProfile+".");				
			}
			else {
				System.out.println("[PomGenerator][ERROR] Couldn't find any matching package with name " + packageName +" in profile "+inputProfile+".");				
				System.err.println("[PomGenerator][ERROR] Couldn't find any matching package with name " + packageName +" in profile "+inputProfile+".");				
			}
		}
		else {
			coordinates.put("groupId",mapr.getGroupId(matchingPackage.getMyProfile().getServiceClass()));
			coordinates.put("artifactId",mapr.getArtifactId(matchingPackage.getPackageName()));
			String version = mapr.getVersion(matchingPackage.getVersion());
			if(isSnapshot && !version.endsWith("-SNAPSHOT")) version = version + "-SNAPSHOT";
			coordinates.put("version",version);
		}
		
		return matchingPackage;
	}
	
	
	
	private static Model createPomModel(Map<String, String> coordinates){
		Model model = new Model();
		model.setModelVersion("4.0.0");
		model.setGroupId(coordinates.get("groupId"));
		model.setArtifactId(coordinates.get("artifactId"));
		model.setVersion(coordinates.get("version"));
		if(coordinates.containsKey("packaging")){
			if(coordinates.get("packaging").equals("gar")){
				System.out.println("[PomGenerator][WARNING] skipping set packaging in output pom since it is equals to \"gar\"");
			}
			else {
				model.setPackaging(coordinates.get("packaging"));
			}
		}
		return model;
		
	}
	
	
	private static void writePom(Model model, File outputPom) throws IOException{
		System.out.println("Writing to " + outputPom);
		FileWriter outputWriter = new FileWriter(outputPom);
		new MavenXpp3Writer().write(outputWriter, model);		
	}
	
	

	/**
	 * @param args
	 * @throws ParseException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws XPathExpressionException 
	 */
	public static void main(String[] args) throws ParseException, XPathExpressionException, SAXException, IOException, ParserConfigurationException {
		
		Option helpOpt = OptionBuilder.withLongOpt("help").hasArg(false).withDescription("print this message").create("h");

		Option profileOpt = OptionBuilder.withLongOpt("profile").withArgName("FILENAME").hasArg(true).withDescription("profile.xml file to be used as input").create("p");
		Option artifactNameOpt = OptionBuilder.withLongOpt("artifactname").withArgName("NAME").hasArg(true).withDescription("name of the artifact found in the component's folder. Overrides --package").create("a");
		Option pomOutputOpt = OptionBuilder.withLongOpt("pomoutput").withArgName("FILENAME").hasArg(true).withDescription("optional. The output filename").create("o");
		Option packageNameOpt = OptionBuilder.withLongOpt("package").withArgName("FILENAME").hasArg(true).withDescription("optional. Name of package to process").create("k");

		Option groupIdOpt = OptionBuilder.withLongOpt("groupid").withArgName("FILENAME").hasArg(true).withDescription("optional. groupId: will override the one from profile").create("g");
		Option artifactIdOpt = OptionBuilder.withLongOpt("artifactid").withArgName("FILENAME").hasArg(true).withDescription("optional. artifactId: will override the one from profile").create("r");
		Option versionOpt = OptionBuilder.withLongOpt("version").withArgName("FILENAME").hasArg(true).withDescription("optional. verion: will override the one from profile").create("v");		
		Option packagingOpt = OptionBuilder.withLongOpt("packaging").withArgName("STRING").hasArg(true).withDescription("optional. packaging type that will be set in pom").create("n");
		Option printCoordOpt = OptionBuilder.withLongOpt("printcoord").withDescription("only prints out maven coordinates").create("c");
		
		Option isSnapshotOpt = OptionBuilder.withLongOpt("snapshot").withDescription("whether postifx verion with -SNAPSHOT or not.").create("s");
		Option nodepsOpt = OptionBuilder.withLongOpt("nodeps").withDescription("whether generate also dependencies section of not.").create("d");
		Option firstPackageOpt = OptionBuilder.withLongOpt("firstpackage").withDescription("generate pom for the first package. Overrides --artifactname and --package").create("f");

		Option staticmOpt = OptionBuilder.withLongOpt("staticmap").withArgName("PATH").hasArg(true).withDescription("path to the static mapping File").create("t");
		Option extmOpt = OptionBuilder.withLongOpt("extmap").withArgName("PATH").hasArg(true).withDescription("path to ExternalSoftware mapping File").create("e");

		
		// create the Options
		Options options = new Options();
		options.addOption(helpOpt);
		options.addOption(profileOpt);
		options.addOption(artifactNameOpt);
		options.addOption(pomOutputOpt);
		options.addOption(extmOpt);	
		options.addOption(isSnapshotOpt);
		options.addOption(packageNameOpt);
		options.addOption(nodepsOpt);
		options.addOption(firstPackageOpt);
		options.addOption(groupIdOpt);
		options.addOption(artifactIdOpt);
		options.addOption(versionOpt);
		options.addOption(packagingOpt);
		options.addOption(printCoordOpt);
		options.addOption(staticmOpt);
		
		
		
		
		CommandLineParser parser = new PosixParser();
		CommandLine cmd = parser.parse( options, args);
		
		if (cmd.hasOption(helpOpt.getOpt())) {
			printHelp(options);
			return;
		}
		
		if(cmd.hasOption("s")){
			isSnapshot = true;
		}

		
		if(!cmd.hasOption("p") && !(cmd.hasOption("g") && cmd.hasOption("r") && cmd.hasOption("v"))){
			System.out.println("[PomGenerator][ERROR] If --profile is not provided, then --groupid, --artifactid and --version must be specified");
			System.err.println("[PomGenerator][ERROR] If --profile is not provided, then --groupid, --artifactid and --version must be specified");
			System.exit(1);
		}

		File inputProfile = null;

		if(cmd.getOptionValue("p") != null){
			inputProfile = new File(cmd.getOptionValue("p"));
			if(!inputProfile.exists()){
				System.out.println("[PomGenerator][WARNING] profile.xml file not provided");
			}
			
			if(!cmd.hasOption("a") && !cmd.hasOption("k") && !cmd.hasOption("f")){
				System.out.println("[PomGenerator][ERROR] either --artifactname, --package or --firstpackage must be provided. Exiting...");
				System.err.println("[PomGenerator][ERROR] either --artifactname, --package or --firstpackage must be provided. Exiting...");
				printHelp(options);
				System.exit(1);
			}

		}
			
		String artifactName = cmd.getOptionValue("a");
		String packageName = cmd.getOptionValue("k");
		
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
		

		File outputPom = null;
		if(cmd.hasOption("o")){
			outputPom = new File(cmd.getOptionValue("o"));
		}
		else {
			outputPom = new File("pom.xml");
		}	

		boolean generateDeps = cmd.hasOption("d") ? false : true;	
		if(inputProfile == null) generateDeps = false;
		boolean firstPackage = cmd.hasOption("f") ? true : false;
	
		
		Map<String, String> coordinates = new HashMap<String, String>();
		MappingRules mapr =  new MappingRules(
										MappingsLoader.loadExternalsMapping(extMappingFile),
										MappingsLoader.loadStaticMappings(staticMappingFile));
		
		
		ProfilePackage matchigPackage = null;
		
		if(inputProfile != null){
			matchigPackage = extractCoordinatesFromProfile(coordinates, inputProfile,firstPackage,artifactName,packageName, mapr);
		}
		
		if(cmd.hasOption("g")){
			coordinates.put("groupId", cmd.getOptionValue("g"));
		}

		if(cmd.hasOption("r")){
			coordinates.put("artifactId", cmd.getOptionValue("r"));
		}
		
		if(cmd.hasOption("v")){
			String version =  cmd.getOptionValue("v");
			if(isSnapshot && !version.endsWith("-SNAPSHOT")) version = version + "-SNAPSHOT";
			coordinates.put("version", version);
		}
		
		if(cmd.hasOption("n")){
			coordinates.put("packaging", cmd.getOptionValue("n"));
		}
		
		
		if(!( coordinates.containsKey("groupId") &&
			  coordinates.containsKey("artifactId") &&
			  coordinates.containsKey("version"))){
			System.err.println("[PomGenerator][ERROR] Couldn't calculate Maven coordinates ("+
					coordinates.get("groupId")+":"+coordinates.get("artifactId")+":"+coordinates.get("version")+
					"). Impossible to continue.");
			System.exit(1);
		}
		
		
		if(cmd.hasOption("c")){
			FileWriter fstream = new FileWriter(outputPom);
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write("groupId=" + coordinates.get("groupId") + "\n");
			  out.write("artifactId=" + coordinates.get("artifactId") + "\n");
			  out.write("version=" + coordinates.get("version") + "\n");
			  out.close();
			  System.exit(0);
		}
		
		Model model = createPomModel(coordinates);
		
		if(generateDeps && matchigPackage != null){
			for (ProfilePackageDependency d : matchigPackage.getDependencies()) {
				Set<Dependency> deps = mapr.getDependency(d);
				for (Dependency dependency : deps) {
					if(isSnapshot && !dependency.getVersion().endsWith("-SNAPSHOT")) dependency.setVersion(dependency.getVersion() + "-SNAPSHOT");
					model.addDependency(dependency);
				}
			}	
		}
		
		writePom(model, outputPom);
		
			
	}

	private static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("PomGenerator [OPTIONS]", options);		
	}

}
