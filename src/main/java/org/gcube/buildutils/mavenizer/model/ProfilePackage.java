package org.gcube.buildutils.mavenizer.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.gcube.buildutils.mavenizer.model.version.ArtifactVersion;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ProfilePackage {
	
	private Profile profile;
	private Node dom;
	private String packageName;
	private ArtifactVersion version;
	private Set<ProfilePackageDependency> dependencies = null;
	private Set<String> files = null;
	
	public ProfilePackage(Node packageNode, Profile profile) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        
        this.profile = profile;
        this.dom = packageNode;
        this.packageName = (String) xPath.evaluate("Name/text()", packageNode, XPathConstants.STRING);
        String v = (String) xPath.evaluate("Version/text()", packageNode, XPathConstants.STRING);	
        this.version = new ArtifactVersion(v);
	}
	
	public void setVersion(String newVersion) throws XPathExpressionException{
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        Node n = (Node) xPath.evaluate("Version/text()", this.dom, XPathConstants.NODE);	
        n.setTextContent(newVersion);
	}
	
	public Set<String> getFiles() {
		if(this.files == null){
			this.files = new HashSet<String>();
			
			XPathFactory factory = XPathFactory.newInstance();
	        XPath xPath = factory.newXPath();
	        try {
	        	NodeList fileNodes = 
	        			(NodeList) xPath.evaluate("Files/File", this.dom, XPathConstants.NODESET);
	
	        	for(int j = 0; j<fileNodes.getLength(); j++){
			    	this.files.add(fileNodes.item(j).getTextContent());
			    }
	        	
	        	String garFile = (String) xPath.evaluate("GARArchive/text()", this.dom, XPathConstants.STRING);
	        	if(garFile != null){
	        		this.files.add(garFile);
	        	}
	        	
			 }catch (Exception e) {
				e.printStackTrace();
			}

		}
		return this.files;
	}
	
	public Set<ProfilePackageDependency> getDependencies(){
		if(this.dependencies == null){
			this.dependencies = new HashSet<ProfilePackageDependency>();

			XPathFactory factory = XPathFactory.newInstance();
	        XPath xPath = factory.newXPath();
	        try {
	        	NodeList dependencies = 
	        			(NodeList) xPath.evaluate("Dependencies/Dependency", this.dom, XPathConstants.NODESET);
	
	        	for(int j = 0; j<dependencies.getLength(); j++){
			    	this.dependencies.add(new ProfilePackageDependency(dependencies.item(j)));
			    }
			 }catch (Exception e) {
				e.printStackTrace();
			}
		}
		return this.dependencies;

	}
	
	
	public void setMavenCoordinates(String groupId, String artifactId, String version, String classifier) throws Exception{
		
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
		

    	Element mavenCoordinates = this.dom.getOwnerDocument().createElement("MavenCoordinates");
    	
    	if(groupId == null) throw new Exception("groupId cannot be null");
		Element el = this.dom.getOwnerDocument().createElement("groupId");
		el.setTextContent(groupId);
		mavenCoordinates.appendChild(el);
    	
    	if(artifactId == null) throw new Exception("artifactId cannot be null");
		Element el2 = this.dom.getOwnerDocument().createElement("artifactId");
		el2.setTextContent(artifactId);
		mavenCoordinates.appendChild(el2);

    	if(version == null) throw new Exception("version cannot be null");
		Element el3 = this.dom.getOwnerDocument().createElement("version");
		el3.setTextContent(version);
		mavenCoordinates.appendChild(el3);
		
    	if(classifier != null){
    		Element el4 = this.dom.getOwnerDocument().createElement("classifier");
    		el4.setTextContent(classifier);
    		mavenCoordinates.appendChild(el4);
    	}

    	Node n = (Node) xPath.evaluate("MavenCoordinates", this.dom, XPathConstants.NODE);
    	if(n != null){
    		this.dom.removeChild(n);
    	}
    	
    	Node v = (Node) xPath.evaluate("Version", this.dom, XPathConstants.NODE);
    	
    	this.dom.insertBefore(mavenCoordinates, v.getNextSibling());
      
		return;
	}
	
	public void removeDependencies(){
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
    	Node n = null;
		try {
			n = (Node) xPath.evaluate("Dependencies", this.dom, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
    	if(n != null){
    		this.dom.removeChild(n);
    	}
	}
	
	public void removeWSDLs(){
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
		try {
        	NodeList porttypes = 
    			(NodeList) xPath.evaluate("PortType", this.dom, XPathConstants.NODESET);

	    	for(int j = 0; j<porttypes.getLength(); j++){
	    		Node pp = porttypes.item(j);
	        	Node wsdl = 
	    			(Node) xPath.evaluate("WSDL", pp, XPathConstants.NODE);
		    	if(wsdl != null)
		    		pp.removeChild(wsdl);
		    }
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
	}

	public String getPackageName() {
		return this.packageName;
	}

	public ArtifactVersion getVersion() {
		return this.version;
	}
	
	public Profile getMyProfile(){
		return this.profile;
	}
}
