package org.avr.fileread;

import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.avr.fileread.exceptions.LayoutException;
import org.avr.fileread.records.BasicRecord;
import org.avr.fileread.records.DataRecord;
import org.avr.fileread.records.Field;
import org.avr.fileread.records.Header;
import org.avr.fileread.records.IRecord;
import org.avr.fileread.records.MegaField;
import org.avr.fileread.records.Trailer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class FileReadWriter {
	private Logger logger = Logger.getLogger( FileReadWriter.class );
	
	private Document myDOM;
	private FileDigester myDigester = new FileDigester();
		
	public FileReadWriter(String fileLayout) throws ParserConfigurationException
												, SAXException
												, IOException
												, LayoutException {
		logger.info("File laytout being read ["+ fileLayout +"]");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		myDOM = builder.parse( fileLayout );
		myDigester.setMyFileLayouts( new FlatFile() );
		
		this.navigateDOM();
		
		logger.info("Finished navigating DOM");
	}
	
	
	/**
	 * Top level validation:  Root element is FileLayout.  
	 * @throws LayoutException
	 */
	private void navigateDOM() throws LayoutException {
		Element rootEle = myDOM.getDocumentElement();
		if ( !"FileLayout".equalsIgnoreCase( rootEle.getNodeName() ) )
			throw new LayoutException("Root element is <"+ rootEle.getNodeName() +"> !!! It should be <FileLayout>");
		traverseRecords( rootEle.getChildNodes() );
	}
	
	
	/**
	 * Instantiate the collections of records (Header, Trailer or DataRecord)
	 * 
	 * @param nl
	 * @param prefix
	 * @throws LayoutException
	 */
	private void traverseRecords(NodeList nl ) throws LayoutException {
		for (int i = 0; i < nl.getLength() ; i++) {
			if (nl.item(i) instanceof Element) {
				Element newEle = (Element) nl.item(i);
				logger.debug("Element : "+  newEle.getNodeName() );
				
				switch (newEle.getNodeName()) {
				case "header": 
					myDigester.getMyFileLayouts().setHeader( new Header() );
					populateRecord( myDigester.getMyFileLayouts().getHeader() , newEle );
					break;
				case "trailer":
					myDigester.getMyFileLayouts().setTrailer( new Trailer() );
					populateRecord( myDigester.getMyFileLayouts().getTrailer() , newEle );
					break;
				case "record":
					/* THIS LOGIC MUST BE IN THIS ORDER
					 *  - instantiate a DataRecord
					 *  - populate that DataRecord
					 *  - attach it to the MAP
					 */
					DataRecord dataRec = new DataRecord();
					populateRecord( dataRec , newEle );
					myDigester.getMyFileLayouts().getDataRecords().put( dataRec.getClassName() , dataRec );
					break;
				default:
					throw new LayoutException( newEle.getNodeName() +" is not a valid Record type (header, trailer or record).");
				}
			}
		}
	}
	
	
	/**
	 * Populate member variables in the record (Header, Trailer or DataRecord).
	 * 
	 * @param rec
	 * @param ele
	 * @throws LayoutException
	 */
	private void populateRecord(BasicRecord rec , Element ele) throws LayoutException {
		if ( ele.getChildNodes().getLength() == 0 )
			throw new LayoutException("A record should have have some <fields>.  <"+ ele.getNodeName() +"> is missing some.");
		
		logger.debug( ele.getAttribute("classname"));
		logger.debug( ele.getAttribute("uid"));
		
		rec.setClassName( ele.getAttribute("classname") );
		rec.setUid( ele.getAttribute("uid"));
		rec.setUidStart( parseInt( ele.getAttribute("uidStart") , "uidStart" , true ) );
		rec.setUidEnd( parseInt( ele.getAttribute("uidEnd") , "uidEnd" , true ) );
		if (!rec.validUID())
			throw new LayoutException("Missing uid, uidStart or uidEnd in <record> tag");
		
		traverseFields(rec, ele);
	}
	
	
	/**
	 * Traverse valid field elements (field of megaField)
	 * @param rec
	 * @param ele
	 * @throws LayoutException
	 */
//	private void traverseFields(BasicRecord rec , Element ele) throws LayoutException {
	private void traverseFields(IRecord rec , Element ele) throws LayoutException {
		for (int i = 0; i < ele.getChildNodes().getLength() ; i++) {
			if (ele.getChildNodes().item(i) instanceof Element) {
				Element newEle = (Element) ele.getChildNodes().item(i);
				logger.debug("Element : "+  newEle.getNodeName() );
				
				switch (newEle.getNodeName()) {
				case "field":
					rec.getFields().add( populateField( newEle ) );
					break;
				case "megaField":
					rec.getFields().add( traverseMegaField( newEle ) );
					break;
				default:
					throw new LayoutException(newEle.getNodeName() +" is not a valid field type (field, megaField)");
				}
			}
		}
	}
	
	
	private Field populateField(Element ele) throws LayoutException {
		logger.debug("Populating Field ["+ ele.getAttribute("name") +"]");
		Field fld = new Field();
		fld.setName( ele.getAttribute("name"));
		fld.setTrim("true".equalsIgnoreCase( ele.getAttribute("trim") ) ? true : false ); 
		
		if (!"".equalsIgnoreCase( ele.getAttribute("occurs")))
			fld.setOccurs( parseInt( ele.getAttribute("occurs") , "occurs" ) );
		
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
	
	private Field traverseMegaField(Element ele) throws LayoutException{
		MegaField megaF = new MegaField();
		megaF.setClassName( ele.getAttribute("classname") );
		
		traverseFields( megaF , ele);
		return megaF;
	}
	
	
	
	/**
	 * Overloading ORIGINAL parseInt(...) to allow for optional fields.
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
			if (optional & "".equalsIgnoreCase(number)) return null;
			else throw new LayoutException(nodeName +" is not a valid number ["+ number +"]");
		}
	}
	/**
	 * Convert string to Integer, throw an exception 
	 * @param number
	 * @param nodeName
	 * @return
	 * @throws LayoutException
	 */
	private Integer parseInt(String number , String nodeName ) throws LayoutException {
		return this.parseInt(number, nodeName , false);
	}
	
	
	/**
	 * 
	 * @param nl
	 * @param prefix
	 * 
	private void displayNode(NodeList nl , String prefix) {
		for (int i = 0; i < nl.getLength() ; i++) {
			if (nl.item(i) instanceof Element) {
				Element newEle = (Element) nl.item(i);
				System.out.println(prefix +"Element : "+  newEle.getNodeName() );
				displayNode( newEle.getChildNodes() , prefix+"\t");
			}
		}
	}
	 */
	
	
	
	public String displayFields() {
		StringBuffer str = new StringBuffer();
		
		this.myDigester.getMyFileLayouts().getDataRecords();
		Set<String> keys = this.myDigester.getMyFileLayouts().getDataRecords().keySet();
		for (String key : keys) {
			DataRecord dr = this.myDigester.getMyFileLayouts().getDataRecords().get(key);
			str.append( dr.toString() );
		}
		return str.toString();
	}
	
	
	
	
	public Object readNextLine() {
		myDigester.parseLine("");
		return null;
	}

}
