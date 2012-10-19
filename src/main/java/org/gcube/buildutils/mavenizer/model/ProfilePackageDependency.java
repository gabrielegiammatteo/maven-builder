package org.gcube.buildutils.mavenizer.model;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.gcube.buildutils.mavenizer.model.version.Range;
import org.gcube.buildutils.mavenizer.model.version.VersionSpecificationParser;
import org.gcube.buildutils.mavenizer.model.version.VersionSpecificationParser.InvalidVersionException;
import org.w3c.dom.Node;

public class ProfilePackageDependency {
	
	
	private String serviceClass;
	private String packageName;
	private Range versionRange;
	
	
	public ProfilePackageDependency(Node dependencyNode) throws XPathExpressionException, InvalidVersionException {
		super();
		
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
		
      	
        this.serviceClass = (String) xPath.evaluate("Service/Class/text()", dependencyNode, XPathConstants.STRING);
        this.packageName = (String) xPath.evaluate("Package/text()", dependencyNode, XPathConstants.STRING);
        String vr = (String) xPath.evaluate("Version/text()", dependencyNode, XPathConstants.STRING);
        this.versionRange = VersionSpecificationParser.parse(vr);
	}


	public String getServiceClass() {
		return serviceClass;
	}


	public String getPackageName() {
		return packageName;
	}


	public Range getVersionRange() {
		return versionRange;
	}
	
	public boolean isExternal(){
		return this.serviceClass.equals("ExternalSoftware");
	}
	
	@Override
	public String toString() {
		return this.serviceClass+":"+this.packageName+":"+versionRange;
	}
}
