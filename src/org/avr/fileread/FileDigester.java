package org.avr.fileread;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.apache.log4j.Logger;
import org.avr.fileread.exceptions.ParsingException;
import org.avr.fileread.records.DelimitedLine;
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
			Integer start = myFileLayouts.getHeader().getUidStart();
			Integer end = myFileLayouts.getHeader().getUidEnd();
			
			if( ( line.substring( start , end) ).equalsIgnoreCase( myFileLayouts.getHeader().getUid() ) ) {
				log.debug("Found a header record");
				return parseHeader( line , myFileLayouts.getHeader() );
			}
		}
		
		if (myFileLayouts.getTrailer() != null) {
			Integer start = myFileLayouts.getTrailer().getUidStart();
			Integer end = myFileLayouts.getTrailer().getUidEnd();
			
			if( ( line.substring( start , end) ).equalsIgnoreCase( myFileLayouts.getTrailer().getUid() ) ) {
				log.debug("Found a trailer record");
				return parseTrailer( line , myFileLayouts.getTrailer() );
			}
		}
		
		if(myFileLayouts.getDataRecords() != null ) {
			Set<String> keys = myFileLayouts.getDataRecords().keySet();
			for (String key : keys) {
				DataRecord rec = (DataRecord)myFileLayouts.getDataRecords().get( key );
				Integer start = rec.getUidStart();
				Integer end = rec.getUidEnd();
				
				if( ( line.substring( start , end) ).equalsIgnoreCase( rec.getUid() ) ) {
					log.debug("Found a DATA record");
					return parseDataRec( line , rec );
				}
				
			}
		}
		return new UnParsableRecord(line);
	}
	
	
	
	private Object parseHeader(String line, Header hdr) {
		return null;
	}
	private Object parseTrailer(String line, Trailer trlr) {
		return null;
	}
	private Object parseDataRec(String line, IRecord rec) {
		if (rec.isDelimited())
			this.delimitedLine = new DelimitedLine( line , rec.getDelimiter());
		
		Object obj = instantiateRecord( rec.getClassName());
		try {
			parseFields( line , obj , rec );
			return obj;
		} catch (ParsingException pEx) {
			return new UnParsableRecord( line );
		}
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
		
		catch (NullPointerException npEx) {
			System.out.println( fld +"  "+ methodName);
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
			throw new ParsingException(fld.getName() +" does not match format ["+ fld.getFormat() +"]");
		}
	}
}