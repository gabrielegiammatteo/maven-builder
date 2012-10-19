package org.gcube.buildutils.mavenizer.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.apache.maven.model.Dependency;
import org.gcube.buildutils.mavenizer.model.version.ArtifactVersion;
import org.json.simple.JSONValue;

public class MappingsLoader {


	public static Map<String,Map<ArtifactVersion, List<Dependency>>> loadExternalsMapping(File file) throws IOException{		
		
		Map<String,Map<ArtifactVersion, List<Dependency>>> externalsMappingMap = new HashMap<String, Map<ArtifactVersion,List<Dependency>>>();
		
		
		FileInputStream fstream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		FileReader fr = new FileReader(file);
		Map<String,Map<String, List<String>>> obj = (Map<String,Map<String, List<String>>>)JSONValue.parse(fr);
		
		Set<String> packageNames = obj.keySet();
		
		for (String packageName : packageNames) {
			
			Map<ArtifactVersion, List<Dependency>> packageMap = new HashMap<ArtifactVersion, List<Dependency>>();
			
			Set<String> packageVersions = obj.get(packageName).keySet();
			for(String version: packageVersions){
				
				List<Dependency> mavenArtifacts = new LinkedList<Dependency>();
				
				List<String> artifactStrings = obj.get(packageName).get(version);
				
				for (String artSpec : artifactStrings) {
					
					String[] mavenCoord = artSpec.split(":");
					if(mavenCoord.length<3){
						System.err.println("Invalid entry. Skipping");
						continue;
					}
					
					Dependency art = new Dependency();
					art.setGroupId(mavenCoord[0].trim());
					art.setArtifactId(mavenCoord[1].trim());
					art.setVersion(mavenCoord[2].trim());
					mavenArtifacts.add(art);
					
				}
				
				ArtifactVersion av = new ArtifactVersion(version);
				packageMap.put(av, mavenArtifacts);		
			}
			
			externalsMappingMap.put(packageName, packageMap);
			
		}
		
		in.close();
		return externalsMappingMap;
	}

	
	public static String printExternalsMapping(Map<String,Map<ArtifactVersion, List<Dependency>>> map){
		StringBuffer sb = new StringBuffer();
		
		Set<String> packagesKeyset = map.keySet();
		for (String packageName : packagesKeyset) {
			Set<ArtifactVersion> packagesVersionKeyset = map.get(packageName).keySet();
			for (ArtifactVersion packageVersion : packagesVersionKeyset) {
				sb.append(packageName+":"+packageVersion+"\n");
				List<Dependency> mavenArtifacts = map.get(packageName).get(packageVersion);
				for (Iterator iterator = mavenArtifacts.iterator(); iterator.hasNext();) {
					Dependency art = (Dependency) iterator.next();
					sb.append("\t"+art.getGroupId()+":"+art.getArtifactId()+":"+art.getVersion()+"\n");
				}
			}
		}
		
		return sb.toString();
	}
	
	
	

	public static Map<String, Map<String, String>> loadStaticMappings(File file) throws IOException{		
		
		
		
		FileInputStream fstream = new FileInputStream(file);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
		FileReader fr = new FileReader(file);
		Map<String, Map<String, String>> obj = (Map<String, Map<String,String>>) JSONValue.parse(fr);

		
		in.close();
		return obj;
	}

	
	public static String printStaticMapping(Map<String,Map<String, String>> map){
		StringBuffer sb = new StringBuffer();
		
		Set<String> mappingType = map.keySet();
		for (String type : mappingType) {
			
			sb.append("Mappings for " + type +":\n");
			
			Set<String> names = map.get(type).keySet();
			
			for (String name : names) {
				sb.append("\t"+name+" -> "+map.get(type).get(name)+"\n");
			}
			sb.append("\n");
		}
		
		return sb.toString();
	}
	

}
