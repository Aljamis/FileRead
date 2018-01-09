package org.avr.fileread.dom;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import static org.junit.Assert.*;


public class DOMutilsTest {
	
	private static Document myDOM;
	
	
	@Before
	public void buildDOM() {
		String xml = "<MyDoc><!-- A comment --> xx  </MyDoc>";
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			myDOM = builder.parse( new ByteArrayInputStream( xml.getBytes() ) );
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	@Test
	public void nodeIsComment() {
		Node n1 =  myDOM.getDocumentElement().getChildNodes().item(0) ;
		assertTrue( DOMutils.nodeIsComment( n1 ) );
	}
	
	@Test
	public void nodeIsTextAndEmpty() {
		Node n2 =  myDOM.getDocumentElement().getChildNodes().item(1) ;
		assertFalse( DOMutils.nodeIsTextAndEmpty( n2 ) );
	}
}
