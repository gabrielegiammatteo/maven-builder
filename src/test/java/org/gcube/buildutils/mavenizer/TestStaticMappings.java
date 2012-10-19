package org.gcube.buildutils.mavenizer;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.gcube.buildutils.mavenizer.util.MappingsLoader;

public class TestStaticMappings {
	
	public static void main(String[] args) throws IOException {
		
		Map<String, Map<String, String>> map = 
			MappingsLoader.loadStaticMappings(new File("etc/staticMappings.json"));
	
		
		System.out.println(MappingsLoader.printStaticMapping(map));
	
		
		String v = map.get("groupId").get("VREManagement");
		System.out.println(v);
		
	}

}
