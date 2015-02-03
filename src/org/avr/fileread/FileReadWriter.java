package org.avr.fileread;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.avr.fileread.exceptions.LayoutException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * The principle class in this API used to read an XML (record layout) file and
 * read lines from a file.  Basic DOM XML parsing produces a {@link org.avr.fileread.FlatFile }.
 * This is the blue-print or record definition
 * 
 * @author Alfonso
 *
 */
public class FileReadWriter {
	private Logger logger = Logger.getLogger( FileReadWriter.class );
	
	private Document myDOM;
	private FileDigester myDigester = new FileDigester();
	private BufferedReader reader = null;
	
	private AtomicInteger recordsRead = new AtomicInteger();
		
	public FileReadWriter(String fileLayout , String fileName) throws ParserConfigurationException
												, SAXException
												, IOException
												, LayoutException {
		this.navigateDOM( fileLayout );
		this.openFile( fileName );
	}
	
	
	/**
	 * Top level validation:  Root element is FileLayout.  
	 * @throws LayoutException
	 */
	private void navigateDOM(String fileLayout) throws ParserConfigurationException
													 , IOException
													 , SAXException
													 , LayoutException {
		logger.info("File laytout being read ["+ fileLayout +"]");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		myDOM = builder.parse( fileLayout );
		myDigester.setMyFileLayouts( new FlatFile() );
		Element rootEle = myDOM.getDocumentElement();
		if ( !"FileLayout".equalsIgnoreCase( rootEle.getNodeName() ) )
			throw new LayoutException("Root element is <"+ rootEle.getNodeName() +"> !!! It should be <FileLayout>");
		traverseRecords( rootEle.getChildNodes() );
		
		logger.debug( displayFields() );
		
		logger.info("Finished navigating DOM");
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
		rec.setDelimiter( ele.getAttribute("delimiter"));
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
	
	
	/**
	 * 
	 * @param ele
	 * @return
	 * @throws LayoutException
	 */
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
	
	private Field traverseMegaField(Element ele) throws LayoutException{
		MegaField megaF = new MegaField();
		megaF.setClassName( ele.getAttribute("classname") );
		megaF.setName( ele.getAttribute("name"));
		
		traverseFields( megaF , ele);
		return megaF;
	}
	
	
	
	/**
	 * Convert string to Integer, throw an exception 
	 * @param number - The string being parsed
	 * @param nodeName - The name of the field the Integer will be parsed into
	 * @param optional - (boolean) is this field OPTIONAL
	 * @return
	 * @throws LayoutException
	 */
	private Integer parseInt(String number , String nodeName , boolean optional ) throws LayoutException {
		try {
			Integer i = Integer.parseInt( number );
			return i;
		} catch (NumberFormatException nfEx) {
//			if (optional & "".equalsIgnoreCase(number)) return null;
			if (optional & "".equalsIgnoreCase(number)) return 0;
			else throw new LayoutException(nodeName +" is not a valid number ["+ number +"]");
		}
	}
	/**
	 * Convert string to Integer, throw an exception.  Use this when the field is REQUIRED.
	 * @param number - The string being parsed
	 * @param nodeName - The name of the field the Integer will be parsed into
	 * @return
	 * @throws LayoutException
	 */
	private Integer parseInt(String number , String nodeName ) throws LayoutException {
		return this.parseInt(number, nodeName , false);
	}
	
	
	
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
	
	
	
	/**
	 * Open file for reading.
	 * @param fileName
	 * @throws IOException
	 */
	private void openFile(String fileName ) throws IOException {
		Path paths = FileSystems.getDefault().getPath( fileName );
		try {
			this.reader = Files.newBufferedReader( paths , Charset.defaultCharset() );
		} catch (IOException ioEx) {
			logger.fatal("Could not open file: "+ fileName );
			throw ioEx;
		}
	}
	
	
	
	
	/**
	 * Read a line from the file returning an Object defined in the layout
	 * XML.
	 * 
	 * @return
	 */
	public Object readNextLine() throws IOException {
		String line = null;
		if ( (line = reader.readLine()) != null ) {
			recordsRead.incrementAndGet();
			return myDigester.parseLine(line);
		}
		return null;
	}

}