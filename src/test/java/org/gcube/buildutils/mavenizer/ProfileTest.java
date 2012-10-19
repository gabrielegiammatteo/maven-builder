package org.gcube.buildutils.mavenizer;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.gcube.buildutils.mavenizer.model.Profile;
import org.xml.sax.SAXException;

public class ProfileTest {

	/**
	 * @param args
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws Exception {
		
		File f = new File("C:\\tmp\\mavenizertest\\profile.xml");
		
		
		Profile p = Profile.parse(f);
		System.out.println(p);
	}

}
