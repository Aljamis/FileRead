package org.avr.fileread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.avr.fileread.exceptions.ParsingException;
import org.avr.fileread.records.UnParsableRecord;


/**
 * Class that parses through a record from a file and populates an object.
 * @author Alfonso
 *
 */
public class FileDigester {
	private static Logger log = Logger.getLogger(FileDigester.class);
	
	private DelimitedLine delimitedLine;
	
	private FlatFile myFileLayouts;
	public FlatFile getMyFileLayouts(){ return myFileLayouts; }
	public void setMyFileLayouts(FlatFile f) { this.myFileLayouts = f; }

	
	
	/**
	 * Determine which record type this is (Header, Trailer or common data record)
	 * 
	 * @param line
	 * @return
	 */
	protected Object parseLine(String line ) {
		if (myFileLayouts.getHeader() != null) {
			if( matchRecordType( line , myFileLayouts.getHeader() ) ) {
				log.debug("Found a header record");
				return parseHeader( line , myFileLayouts.getHeader() );
			}
		}
		
		if (myFileLayouts.getTrailer() != null) {
			if( matchRecordType(line, myFileLayouts.getTrailer() )) {
				log.debug("Found a trailer record");
				return parseTrailer( line , myFileLayouts.getTrailer() );
			}
		}
		
		if(myFileLayouts.getDataRecords() != null ) {
			Set<String> keys = myFileLayouts.getDataRecords().keySet();
			for (String key : keys) {
				DataRecord rec = (DataRecord)myFileLayouts.getDataRecords().get( key );
				if( matchRecordType(line, rec) ) {
					log.debug("Found a DATA record ["+ rec.getClassName() +"]");
					return parseDataRec( line , rec );
				}
				
			}
		}
		return new UnParsableRecord("Could not match a Recored" , line);
	}
	
	
	
	
	/**
	 * Find the correct record layout for this class and pass to a method that
	 * will build a string
	 * @param obj
	 * @return
	 */
	protected String buildLine (Object obj) {
		BasicRecord rec = findFileLayout(obj);
		if (rec == null) {
			log.warn("Could not find a Record Layout for ["+ obj.getClass().getName() +"]");
			return null;
		}
		
		StringBuffer str = new StringBuffer();
		if (rec.isDelimited())
			return buildDelimited(obj , rec , str , true);
		else {
			/* Build an empty StringBuffer the MAX length of this record */
			str.append( StringUtils.rightPad(" ", rec.getMaxLength() ) );
			return buildFixedWidth( obj , rec , str );
		}
	}
	
	
	
	
	
	/**
	 * Look for a Record that matches the class type of the Object.
	 * @param obj
	 * @return
	 */
	private BasicRecord findFileLayout(Object obj) {
		if (myFileLayouts.getHeader() != null) {
			if (myFileLayouts.getHeader().getClassName().equals( obj.getClass().getName()))
				return myFileLayouts.getHeader();
		}
		
		if (myFileLayouts.getTrailer() != null) {
			if(myFileLayouts.getTrailer().getClassName().equals( obj.getClass().getName() ))
				return myFileLayouts.getTrailer();
		}
		
		
		return myFileLayouts.getDataRecords().get( obj.getClass().getName() );
	}
	
	
	
	
	
	private String buildDelimited( Object obj , IRecord record , StringBuffer str , boolean firstToken ) {
		for (Field field : record.getFields()) {
			log.debug("Building from field ["+ field.getName() +"]");
			
			if (field instanceof MegaField) {
				Object newObj = getMemberVariable( obj , field );
				buildDelimited( newObj, (MegaField) field, str , firstToken );
				firstToken = false;
				continue;
			}
			
			String methodName = "get"+ field.getName().substring(0, 1).toUpperCase();
			methodName += field.getName().substring(1);
			
			try {
				Class<?> c = obj.getClass();
				Method m = c.getMethod( methodName );
				
				/* DELIMITER only after the  */
				if ( !firstToken ) str.append( record.getDelimiter() );
				str.append( padFormatField(field, obj, m.invoke( obj )));
			} catch (NoSuchMethodException nsmEx) {
				log.error("Field ["+ field +"]" , nsmEx);
			} catch (InvocationTargetException itEx) {
				log.error("", itEx);
			} catch (IllegalAccessException iaEx) {
				log.error(""  , iaEx );
			}
			firstToken = false;
		}
		
		if(record.getUid() != null  &&  !"".equals(record.getUid())) {
			str.replace( record.getUidStart() , record.getUidEnd(), record.getUid() );
		}
		
		return str.toString();
	}
	
	
	
	
	/**
	 * Build string line from the Object using Fields in the Record
	 * @param obj
	 * @param record
	 * @param str
	 * @return
	 */
	private String buildFixedWidth( Object obj , IRecord record , StringBuffer str ) {
		for (Field field : record.getFields()) {
			log.debug("Building from field ["+ field.getName() +"]");
			
			if (field instanceof MegaField) {
				Object newObj = getMemberVariable( obj , field );
				buildFixedWidth( newObj, (MegaField) field, str);
				continue;
			}
			
			String methodName = "get"+ field.getName().substring(0, 1).toUpperCase();
			methodName += field.getName().substring(1);
			
			try {
				Class<?> c = obj.getClass();
				Method m = c.getMethod( methodName );
				
				str.replace( field.getStart() -1 , field.getEnd() , padFormatField(field, obj, m.invoke( obj )));
			} catch (NoSuchMethodException nsmEx) {
				log.error("Field ["+ field +"]" , nsmEx);
			} catch (InvocationTargetException itEx) {
				log.error("", itEx);
			} catch (IllegalAccessException iaEx) {
				log.error(""  , iaEx );
			}
		}
		
		if(record.getUid() != null  &&  !"".equals(record.getUid())) {
			str.replace( record.getUidStart() , record.getUidEnd(), record.getUid() );
		}
		
		return str.toString();
	}
	
	
	
	
	
	/**
	 * Look for the correct record type based on Unique ID and/or the number of delimited 
	 * tokens in the line and the number of expected fields.
	 * @param line
	 * @param rec
	 * @return
	 */
	private boolean matchRecordType(String line , BasicRecord rec) {
		if ( rec.isDelimited() && (rec.getUid() != null && rec.getUid().trim().length() > 0 ) ) {
			DelimitedLine deliLine = new DelimitedLine( line , rec.getDelimiter());
			if ( rec.getUid().equalsIgnoreCase( deliLine.getTheTokens()[ rec.getUidStart()] ) ) {
				return true;
			}
			return false;
		} else {
			Integer start = rec.getUidStart();
			Integer end = rec.getUidEnd();
			
			if( ( line.substring( start , end) ).equalsIgnoreCase( rec.getUid() ) ) {
				return true;
			}
			return false;
		}
	}
	
	
	
	private Object parseHeader(String line, Header hdr) {
		return parseDataRec(line, hdr);
	}
	private Object parseTrailer(String line, Trailer trlr) {
		return parseDataRec(line , trlr);
	}
	private Object parseDataRec(String line, IRecord rec) {
		if (rec.isDelimited() ) {
			this.delimitedLine = new DelimitedLine( line , rec.getDelimiter());
			/* TODO check line to make sure the number of tokens matches the number of fields */
			if ( !lineIsDelimited(rec) ) {
				return new UnParsableRecord("Number of tokens in the delimited line does not match the number of fields in the Record" , line);
			}
		}
		
		Object obj = instantiateRecord( rec.getClassName());
		try {
			parseFields( line , obj , rec );
			return obj;
		} catch (ParsingException pEx) {
			return new UnParsableRecord( pEx.getMessage() , line );
		}
	}
	
	
	
	/**
	 * In an effort to combine delimited records with fixed format records it is 
	 * necessary to check if the line is, in fact  delimited.  If the number of tokens
	 * does not match the number of fields in the record, then the line must be a 
	 * different record.
	 * @return
	 */
	private boolean lineIsDelimited( IRecord rec ) {
		log.debug( "Record has "+ rec.getFields().size() +" fields      The line has "+ delimitedLine.numOfTokens() +" tokens.");
		return delimitedLine.numOfTokens() > rec.getFields().size(); 
	}
	
	
	
	/**
	 * Using reflection, instantiate an object of className...
	 * @param className
	 * @return
	 */
	private Object instantiateRecord(String className) {
		try {
			Class<?> c = Class.forName( className );
			Object obj = c.newInstance();
			return obj;
		} catch (ClassNotFoundException cnfEx) {
			log.error("Could not instantiate class of type ["+ className +"]");
		} catch (IllegalAccessException iaEx) {
			log.error("Could not instantiate class of type ["+ className +"]");
		} catch (InstantiationException iEx) {
			log.error("Could not instantiate class of type ["+ className +"]");
		}
		return null;
	}
	
	
	
	/**
	 * Iterate through each field in the record, parse from the line and set the
	 * member variable in the object.
	 * @param line
	 * @param obj
	 * @param rec
	 */
	private void parseFields(String line , Object obj , IRecord rec ) {
		for (Field fld : rec.getFields()) {
			Object dataField = doFieldStuff( line , obj , fld );
			setMemberVariable( obj , dataField , fld);
		}
	}
	
	
	
	/**
	 * Parse the string into its corresponding record field.  If the field is of 
	 * type MegaField, instantiate its object and iterate through its fields.
	 * @param line
	 * @param obj
	 * @param fld
	 * @return
	 */
	private Object doFieldStuff(String line , Object obj , Field fld ) {	
		if (fld instanceof MegaField) {
			MegaField mFld = (MegaField) fld;
			Object o = instantiateRecord( mFld.getClassName() );
			parseFields(line, o, mFld );
			
			return o;
		}
		
		String prelimField = nextToken(line, fld);
		
		switch (fld.getType()) {
		case "String":
			return prelimField;

		case "int":
			return intField(prelimField , fld.getName());

		case "double":
			return doubleField(prelimField, fld.getName());

		case "date":
			return dateField(prelimField , fld);

		default:
			log.error("Field Type ["+ fld.getType() +"] is not a valid type (String, int, double, date).");
			break;
		}
		
		return null;
	}
	
	
	
	
	
	/**
	 * Set the member variable in the obj by calling it's setMemberVariableName()
	 * @param obj
	 * @param dataField
	 * @param fld
	 */
	private void setMemberVariable( Object obj , Object dataField , Field fld ) {
		String methodName = "set"+ fld.getName().substring(0,1).toUpperCase();
		methodName += fld.getName().substring(1);
		try {
			Method m = obj.getClass().getMethod( methodName , new Class[] { dataField.getClass() } );
			m.invoke(obj, dataField);
		} catch (NoSuchMethodException nsmEx) {
			throw new ParsingException(methodName +" does not exist");
		} catch (InvocationTargetException itEx) {
			throw new ParsingException("Could not invoke "+ methodName +" on field "+ fld.getName());
		} catch (IllegalAccessException iaEx) {
			throw new ParsingException("Could not invoke "+ methodName +" on field "+ fld.getName() +" : IllegalAccess Exception.");
		}
	}
	
	
	
	
	
	/**
	 * Get the object/variable from the obj's getMemberVariableName()
	 * @param obj
	 * @param fld
	 * @return
	 */
	private Object getMemberVariable(Object obj , Field fld) {
		String methodName = "get"+ fld.getName().substring(0,1).toUpperCase();
		methodName += fld.getName().substring(1);
		try {
			Method m = obj.getClass().getMethod( methodName );
			return m.invoke(obj);
		} catch (NoSuchMethodException nsmEx) {
			throw new ParsingException(methodName +" does not exist");
		} catch (InvocationTargetException itEx) {
			throw new ParsingException("Could not invoke "+ methodName +" on field "+ fld.getName());
		} catch (IllegalAccessException iaEx) {
			throw new ParsingException("Could not invoke "+ methodName +" on field "+ fld.getName() +" : IllegalAccess Exception.");
		}
	}
	
	
	
	/**
	 * Retrieve the next portion of the file record (line) to be parsed into a 
	 * member variable.
	 * @param line
	 * @param fld
	 * @return 
	 */
	private String nextToken(String line , Field fld) {
		if (this.delimitedLine == null) {
			if (fld.isTrim())
					return line.substring( fld.getStart() -1 , fld.getEnd()).trim();
			return line.substring( fld.getStart() -1 , fld.getEnd());
		}
		return this.delimitedLine.getNextToken();
	}
	
	
	
	
	/**
	 * Turn the string from the file record into an Integer. Otherwise, throw
	 * a RunTimeException:  ParsingException.
	 * @param in
	 * @param fldName
	 * @return
	 */
	private Integer intField(String in , String fldName) {
		try {
			Integer ingr = new Integer(in);
			return ingr;
		} catch (NumberFormatException nfEx) {
			log.error("["+ fldName +"] is not an Integer : "+ in );
			throw new ParsingException("["+ fldName +"] is not an Integer : "+ in );
		}
	}
	
	
	
	/**
	 * Turn the string from the file record into a Double.  Otherwise, throw
	 * a RunTimeException:  ParsingException.
	 * @param in
	 * @param fldName
	 * @return
	 */
	private Double doubleField(String in , String fldName) {
		try {
			Double dbl = new Double(in);
			return dbl;
		} catch (NumberFormatException nfEx) {
			log.error("["+ fldName +"] is not a Double : "+ in );
			throw new ParsingException("["+ fldName +"] is not a Double: "+ in );
		}
	}
	
	
	
	/**
	 * Turn the string from the file record into a Date.  Otherwise, throw
	 * a RunTimeException:  ParsingException.
	 * @param in
	 * @param fldName
	 * @return
	 */
	private Date dateField(String in , Field fld) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(fld.getFormat());
			Date dt = sdf.parse(in);
			return dt;
		} catch (ParseException pEx) {
			StringBuffer msg = new StringBuffer();
			msg.append( fld.getName() ).append(" [").append(in).append("] does not match format [");
			msg.append(fld.getFormat()).append("]");
			log.error( msg.toString() );
			throw new ParsingException(msg.toString());
		}
	}
	
	
	
	
	/**
	 * Convert Date object to its FORMATTED representation.
	 * @param format
	 * @param dt
	 * @return
	 */
	private String dateToString(String format , Date dt) {
		SimpleDateFormat sdf = new SimpleDateFormat( format );
		return sdf.format( dt );
	}
	
	
	
	
	/**
	 * 
	 * @param fld
	 * @param obj
	 * @param target
	 * @return
	 */
	private String padFormatField( Field field , Object obj , Object x) {
		StringBuffer strBuff = new StringBuffer();
		if (x instanceof String) {
			/* Append spaces to END */
			strBuff.append(x);
			while (strBuff.length() <= field.getEnd() - field.getStart() ) {
				strBuff.append(" ");
			}
		} else if (x instanceof Integer) {
			/* Prefix spaces to the beginning */
			strBuff.append( x );
			while (strBuff.length() <= field.getEnd() - field.getStart() ) {
				strBuff.insert(0, " ");
			}
		} else if (x instanceof Double) {
			DecimalFormat df = new DecimalFormat( field.getFormat() );
			
			strBuff.append( df.format((Double)x) );
			while (strBuff.length() <= field.getEnd() - field.getStart() ) {
				strBuff.insert(0, " ");
			}
		} else if (x instanceof Date) {
			strBuff.append( dateToString(field.getFormat(), (Date)x ));
			while (strBuff.length() <= field.getEnd() - field.getStart() ) {
				strBuff.append(" ");
			}
		}
		return strBuff.toString();
	}	
}