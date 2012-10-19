package org.gcube.buildutils.mavenizer.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.model.Dependency;
import org.gcube.buildutils.mavenizer.model.ProfilePackageDependency;
import org.gcube.buildutils.mavenizer.model.version.ArtifactVersion;
import org.gcube.buildutils.mavenizer.model.version.Range;
import org.gcube.buildutils.mavenizer.model.version.VersionComparator;

public class MappingRules {
	

	public static final String GROUPID_PREFIX = "org.gcube.";
	

	private Map<String,Map<ArtifactVersion, List<Dependency>>> externalsMappingMap;
	private Map<String, Map<String,String>> staticMappings;
	
	
	public MappingRules(Map<String,Map<ArtifactVersion, List<Dependency>>> externalsMappingMap, Map<String, Map<String, String>> staticMappings) {
		this.staticMappings = staticMappings;
		this.externalsMappingMap = externalsMappingMap;
	}
	
	private String getStaticMapping(String type, String name) {
		if(this.staticMappings.containsKey(type))
			return staticMappings.get(type).get(name);
		else 
			return null;
	}

	public String getGroupId(String name) {
		
		if(getStaticMapping("groupId", name) != null) 
			return getStaticMapping("groupId", name);
		
		String newName = name.toLowerCase();
		newName = newName.replaceAll("-", "");
		newName = newName.replaceAll("\\.", "");
		return GROUPID_PREFIX + newName;
	}
	
	
	public String getArtifactId(String name) {
		return name.toLowerCase();
	}
	
	public String getVersion(ArtifactVersion version) {
		return version.toString();
	}
	
	public String getDependencyVersionRange(Range versionRange){
		return versionRange.toString();
	}


	public Set<Dependency> getDependency(ProfilePackageDependency d) {
		if(d.isExternal()){
			return getExternalDependency(d.getPackageName(), d.getVersionRange());
		}
		
		String groupId = this.getGroupId(d.getServiceClass());
		String artifactId = this.getArtifactId(d.getPackageName());
		String version = this.getDependencyVersionRange(d.getVersionRange());
		
		Dependency md = new Dependency();
		md.setGroupId(groupId);
		md.setArtifactId(artifactId);
		md.setVersion(version);
		
		if(getStaticMapping("dependencyType", groupId+":"+artifactId) != null)
			md.setType(getStaticMapping("dependencyType", groupId+":"+artifactId));

		if(getStaticMapping("dependencyScope", groupId+":"+artifactId) != null)
			md.setScope(getStaticMapping("dependencyScope", groupId+":"+artifactId));
		
		
		Set<Dependency> s = new HashSet<Dependency>();
		s.add(md);
		return s;
	}


	private Set<Dependency> getExternalDependency(String packageName, Range versionRange) {
		
		if(!this.externalsMappingMap.containsKey(packageName)){
			System.err.println("[ERROR][PomGenerator] Couldn't find mapping for: " +packageName +":"+versionRange);
			return new HashSet<Dependency>();
		}
		
		Set<ArtifactVersion> availableVersions = 
				this.externalsMappingMap.get(packageName).keySet();
		
		//sorts versions from most recent to oldest
		List<ArtifactVersion> availableVersionsList= new LinkedList<ArtifactVersion>(availableVersions);
		Collections.sort(availableVersionsList);
		Collections.reverse(availableVersionsList);
		
		for (ArtifactVersion artifactVersion : availableVersionsList) {
			if(VersionComparator.isVersionInRange(versionRange, artifactVersion)){
				//found
				Set<Dependency> depSet = new HashSet<Dependency>(this.externalsMappingMap.get(packageName).get(artifactVersion));
				
				for(Dependency d: depSet){
					String artifactId = d.getArtifactId();
					String groupId = d.getGroupId();
					
					if(getStaticMapping("dependencyType", groupId+":"+artifactId) != null)
						d.setType(getStaticMapping("dependencyType", groupId+":"+artifactId));

					if(getStaticMapping("dependencyScope", groupId+":"+artifactId) != null)
						d.setScope(getStaticMapping("dependencyScope", groupId+":"+artifactId));					
				}
				
				return depSet;
			}
		}
		
		System.err.println("[ERROR][PomGenerator] Couldn't find mapping for: " +packageName +":"+versionRange);
		
		
		return new HashSet<Dependency>();
	}
}
