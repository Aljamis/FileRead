package org.avr.fileread.dom;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.avr.fileread.DataRecord;
import org.avr.fileread.FileLayout;
import org.avr.fileread.exceptions.LayoutException;
import org.avr.fileread.fields.Field;
import org.avr.fileread.fields.MegaField;
import org.avr.fileread.records.BasicRecord;
import org.avr.fileread.records.Header;
import org.avr.fileread.records.IRecord;
import org.avr.fileread.records.Trailer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Navigator {
	private Logger logger = Logger.getLogger( Navigator.class );
	
	private Document myDOM;
	private FileLayout layout;
	
	
	
	
	FileLayout parseFromFile(String fileName) throws FileNotFoundException , LayoutException {
		logger.debug("Parsing from file:  "+ fileName);
		this.navigateDOM( new FileInputStream( fileName ) );
		
		return layout;
	}
	
	/**
	 * Only used for unit testing
	 * @param xml
	 * @throws LayoutException
	 */
	void parseFromString(String xml) throws LayoutException {
		logger.debug("Parsing from String :  "+ xml);
		this.navigateDOM( new ByteArrayInputStream( xml.getBytes()) );
	}
	
	
	
	public static FileLayout getLayout(String fileName) throws LayoutException , FileNotFoundException {
		Navigator n = new Navigator();
		return n.parseFromFile(fileName);
	}
	
	
	
	/**
	 * The root Element must be <FileLayout>.
	 * @param xmlStream
	 * @throws LayoutException
	 */
	private void navigateDOM(InputStream xmlStream) throws LayoutException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			
			this.myDOM = builder.parse( xmlStream );
			this.layout = new FileLayout();
			
			Element rootEle = myDOM.getDocumentElement();
			if ( !"FileLayout".equalsIgnoreCase( rootEle.getNodeName() ) )
				throw new LayoutException("Root element is <"+ rootEle.getNodeName() +"> !!! It should be <FileLayout>");
			
			traverseRecordNodes( rootEle.getChildNodes() );

			logger.info( displayFields() );
			
			logger.info("Finished navigating DOM");

		} catch (SAXException | ParserConfigurationException | IOException ex) {
			throw new LayoutException("A "+ ex.getMessage() +" exception has been caught.", ex);
		}
	}
	
	
	
	
	/**
	 * Every element in this node list must be of type record:  Header, Trailer or Record
	 * @param nl
	 * @throws LayoutException
	 */
	private void traverseRecordNodes(NodeList nl) throws LayoutException {
		for (int i = 0; i < nl.getLength() ; i++) {
			if (nl.item(i) instanceof Element) {
				switch ( nl.item(i).getNodeName() ) {
				case "header":
					this.layout.setHeader( new Header() );
					populateRecord( this.layout.getHeader() , (Element)nl.item(i) );
					break;
					
				case "trailer":
					this.layout.setTrailer( new Trailer() );
					populateRecord( this.layout.getTrailer() , (Element)nl.item(i) );
					break;
					
				case "record":
					DataRecord rec = new DataRecord();
					populateRecord(rec, (Element)nl.item(i) );
					addRecordLayout(rec);
					break;
					
				default:
					//  Ignore all other types of nodes
					throw new LayoutException("Node <"+ nl.item(i).getNodeName() +"> is not an record-type Element.");
				}
			}
		}
	}
	
	
	
	
	/**
	 * Populate a Data record with class type, 
	 * @param rec
	 * @param ele
	 * @throws LayoutException
	 */
	private void populateRecord(BasicRecord rec , Element ele) throws LayoutException {
		if ( ele.getChildNodes().getLength() == 0 )
			throw new LayoutException("A record should have have some <fields>.  <"+ ele.getNodeName() +"> is missing some.");
		
		if (  ele.getAttribute("classname")  == null || "".equalsIgnoreCase(  ele.getAttribute("classname").trim() ))
			throw new LayoutException("A Record requires a class type.  <"+ ele.getNodeName() +"> is missing classname attribute.");
		
		rec.setClassName( ele.getAttribute("classname") );
		if ( !ele.getAttribute("uid").isEmpty() ) {
			rec.setUid( ele.getAttribute("uid"));
			rec.setUidStart( parseInt( ele.getAttribute("uidStart") , "uidStart" , true ) );
			rec.setUidEnd( parseInt( ele.getAttribute("uidEnd") , "uidEnd" , true ) );
		}
		if (ele.hasAttribute("delimiter")) {
			if ( ele.getAttribute("delimiter").isEmpty() )
				throw new LayoutException("delimiter attribute provided but it's empty");
			rec.setDelimiter( ele.getAttribute("delimiter"));
		}
		
		if (!rec.validUID())
			throw new LayoutException("Missing uid, uidStart or uidEnd in <record> tag");
		
		traverseFields( rec , ele );
	}
	
	
	
	
	/**
	 * Traverse valid field elements (field of megaField)
	 * @param rec
	 * @param ele
	 * @throws LayoutException
	 */
	private void traverseFields(IRecord rec , Element ele) throws LayoutException {
		for (int i = 0; i < ele.getChildNodes().getLength() ; i++) {
			if (ele.getChildNodes().item(i) instanceof Element) {
				Element newEle = (Element) ele.getChildNodes().item(i);
				logger.debug("Element : "+  newEle.getNodeName() );
				
				switch (newEle.getNodeName()) {
				case "field":
					rec.addField( populateField( newEle ) );
					break;
				case "megaField":
					rec.getFields().add( traverseMegaField(newEle , rec) );
					break;
				default:
					throw new LayoutException(newEle.getNodeName() +" is not a valid field type (field, megaField)");
				}
			}
		}
	}
	
	private Field traverseMegaField(Element ele , IRecord rec) throws LayoutException{
		MegaField megaF = new MegaField();
		
		if ( ele.getAttribute("name").isEmpty() )
			throw new LayoutException("name attribute missing from <MegaField>");
		megaF.setName( ele.getAttribute("name"));
		
		if ( ele.getAttribute("classname").isEmpty() )
			throw new LayoutException("classname attribute missing from <MegaField>");
		megaF.setClassName( ele.getAttribute("classname") );
		
		megaF.setDelimiter( rec.getDelimiter() );
		
		traverseFields( megaF , ele);
		return megaF;
	}
	
	
	/**
	 * Read attributes from <field> element and populate the Field object.
	 * @param ele
	 * @return
	 * @throws LayoutException
	 */
	private Field populateField(Element ele) throws LayoutException {
		logger.debug("Populating Field ["+ ele.getAttribute("name") +"]");
		Field fld = new Field();
		if ( ele.getAttribute("name").isEmpty() )
			throw new LayoutException("name attribute missing from <field>");
		fld.setName( ele.getAttribute("name") );
		
		fld.setTrim("true".equalsIgnoreCase( ele.getAttribute("trim") ) ? true : false ); 
		
		if (!"".equalsIgnoreCase( ele.getAttribute("occurs")))
			fld.setOccurs( parseInt( ele.getAttribute("occurs") , "Occurs" ) );
		
		populateFieldDetails( ele.getChildNodes() , fld);
		return fld;
	}
	
	
	/**
	 * Traverses child nodes of the "field" node populating Field member variables
	 * type, start & end.
	 *  
	 * @param nl
	 * @param fld
	 * @throws LayoutException
	 */
	private void populateFieldDetails( NodeList nl , Field fld) throws LayoutException {
		String prefix = "\t";
		/* Child nodes are empty == missing type, start and end */
		if ( nl.getLength() == 0)
			throw new LayoutException("Missing Field details (type, start, end)");
		
		for (int i = 0; i < nl.getLength() ; i++) {
			if (nl.item(i) instanceof Element) {
				Element newEle = (Element) nl.item(i);
				logger.debug(prefix +"Element : "+  newEle.getNodeName() );
				
				switch ( newEle.getNodeName()) {
				case "type":
					fld.setType( newEle.getTextContent() );
					dateFormat( fld , newEle );
					break;
				case "start":
					fld.setStart( parseInt( newEle.getTextContent() , newEle.getNodeName() ) );
					break;
				case "end":
					fld.setEnd( parseInt( newEle.getTextContent() , newEle.getNodeName() ) );
					break;
				default:
					throw new LayoutException(  newEle.getNodeName() +" is not a valid node for field");
				}
			}
		}
	}
		
	
	
	
	/**
	 * Convert string to Integer, throw an exception.  Use this when the field is REQUIRED.
	 * @param number
	 * @param nodeName
	 * @return
	 * @throws LayoutException
	 */
	private Integer parseInt(String number , String nodeName ) throws LayoutException {
		return this.parseInt(number, nodeName, false);
	}
	/**
	 * Convert string to Integer - returns null if value is Optional And empty, else it
	 * throws an exception
	 * @param number
	 * @param nodeName
	 * @param optional
	 * @return
	 * @throws LayoutException
	 */
	private Integer parseInt(String number , String nodeName , boolean optional ) throws LayoutException {
		try {
			Integer i = Integer.parseInt( number );
			return i;
		} catch (NumberFormatException nfEx) {
			if (optional & "".equalsIgnoreCase(number) ) return 0;
			else throw new LayoutException(nodeName +" is not a valid number ["+ number +"]");
		}
	}
	
	
	/**
	 * If the field type is "date" it must have an attribute "format" defining
	 * the date format in terms of SimpleDateFormat.
	 * @param fld
	 * @param ele
	 */
	private void dateFormat(Field fld , Element ele) throws LayoutException {
		if ("date".equalsIgnoreCase( fld.getType() )) {
			String frmt = ele.getAttribute("format");
			if (frmt == null || "".equalsIgnoreCase( frmt ) )
				throw new LayoutException("Date field ["+ fld.getName() +"] missing format.");
			
			try { 
				new SimpleDateFormat(frmt);
				fld.setFormat(frmt);
			} catch (IllegalArgumentException iaEx) {
				throw new LayoutException( "Invalid Date Format ["+ frmt +"] for field "+ fld.getName() );
			}
		}
	}


	
	
	/**
	 * Display all the records with their corresponding fields (including Header and Trailer).
	 * @return
	 */
	public String displayFields() {
		StringBuffer str = new StringBuffer("\n");
		
		// Display fields in the Header 
		if (this.layout.getHeader() != null) {
			Header hdr = this.layout.getHeader();
			str.append( hdr.toString() );
		}
		
		// Display all the record types
		Set<String> keys = this.layout.getDataRecords().keySet();
		for (String key : keys) {
			DataRecord dr = this.layout.getDataRecords().get(key);
			str.append( dr.toString() );
		}
		
		// Display fields in the Trailer
		if (this.layout.getTrailer() != null) {
			Trailer trlr = this.layout.getTrailer();
			str.append( trlr.toString() );
		}
		
		return str.toString();
	}
	
	
	
	/**
	 * Adds the new record layout to the collection of layouts.  The collection is a
	 * HashMap of DataRecord's with ClassName being the key.  A LayoutException is 
	 * thrown if 2 layouts are defined for the same ClassName.
	 *  
	 * @param dataRec
	 * @throws LayoutException
	 */
	private void addRecordLayout(DataRecord dataRec) throws LayoutException {
		if ( this.layout.getDataRecords().get( dataRec.getClassName() ) != null )
			throw new LayoutException("["+ dataRec.getClassName() +"] already exists as a layout.  Must use a different ClassName.");
		this.layout.getDataRecords().put( dataRec.getClassName() , dataRec );
	}
}
