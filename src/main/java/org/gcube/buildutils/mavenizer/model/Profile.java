package org.gcube.buildutils.mavenizer.model;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Profile {
	
	private Document dom;
	
	private String serviceClass;
	private String serviceName;
	private String serviceVersion;
	
	private List<ProfilePackage> packages = null;
	
	
	public static void save(Profile p, File file) throws TransformerException{
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
		DOMSource source = new DOMSource(p.dom);
		StreamResult result = new StreamResult(file);
 
		transformer.transform(source, result);	
	}
	
	
	public static Profile parse(File file) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException{
		Profile p = new Profile();
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(file);
		p.dom = doc;
		
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
		p.serviceClass = (String)xPath.evaluate("/Resource/Profile/Class", doc, XPathConstants.STRING);
		p.serviceName = (String)xPath.evaluate("/Resource/Profile/Name", doc, XPathConstants.STRING);
		p.serviceVersion = (String)xPath.evaluate("/Resource/Profile/Version", doc, XPathConstants.STRING);
		
		return p;
	}
	
	/**
	 * 
	 * @param newVersion
	 * @return the old version
	 */
	public String setServiceVersion(String newVersion) throws XPathExpressionException{
		XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        Node n = (Node) xPath.evaluate("/Resource/Profile/Version/text()", this.dom, XPathConstants.NODE);
        String old = n.getTextContent();
        n.setTextContent(newVersion);	
        return old;
	}
	
	
	public List<ProfilePackage> getPackages(){
		if(this.packages == null){
			this.packages = new LinkedList<ProfilePackage>();
			
			XPathFactory factory = XPathFactory.newInstance();
	        XPath xPath = factory.newXPath();
			
	        try{
		        NodeList packageNodes = 
		                (NodeList) xPath.evaluate("/Resource/Profile/Packages/*[name()='Main' or name()='Software' or name()='Plugin']", this.dom, XPathConstants.NODESET);
		        for (int i = 0; i < packageNodes.getLength(); i++) {
		        	this.packages.add(new ProfilePackage(packageNodes.item(i), this));
		        }
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
		}
		
		return this.packages;
	}
	
	
	public String getServiceName() {
		return this.serviceName;
	}
	
	
	public String getServiceClass() {
		return serviceClass;
	}


	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		sb.append("ServiceName: " + this.serviceClass+"\n");
		
		for (ProfilePackage p : this.getPackages()) {
			sb.append("Package: "+p.getPackageName()+":"+p.getVersion()+"\n");
			for (ProfilePackageDependency d : p.getDependencies()) {
				sb.append("\t"+d);
			}
		}
		
		return sb.toString();
	}

}
