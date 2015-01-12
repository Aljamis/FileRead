package org.avr.fileread;

import java.util.Set;

import org.apache.log4j.Logger;
import org.avr.fileread.records.DataRecord;
import org.avr.fileread.records.DelimitedLine;
import org.avr.fileread.records.Field;
import org.avr.fileread.records.Header;
import org.avr.fileread.records.IRecord;
import org.avr.fileread.records.MegaField;
import org.avr.fileread.records.Trailer;
import org.avr.fileread.records.UnParsableRecord;


/**
 * 
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
			int start = myFileLayouts.getHeader().getUidStart();
			int end = myFileLayouts.getHeader().getUidEnd();
			
			if( ( line.substring( start , end) ).equalsIgnoreCase( myFileLayouts.getHeader().getUid() ) ) {
				log.debug("Found a header record");
				return parseHeader( line , myFileLayouts.getHeader() );
			}
		}
		
		if (myFileLayouts.getTrailer() != null) {
			int start = myFileLayouts.getTrailer().getUidStart();
			int end = myFileLayouts.getTrailer().getUidEnd();
			
			if( ( line.substring( start , end) ).equalsIgnoreCase( myFileLayouts.getTrailer().getUid() ) ) {
				log.debug("Found a trailer record");
				return parseTrailer( line , myFileLayouts.getTrailer() );
			}
		}
		
		if(myFileLayouts.getDataRecords() != null ) {
			Set<String> keys = myFileLayouts.getDataRecords().keySet();
			for (String key : keys) {
				DataRecord rec = (DataRecord)myFileLayouts.getDataRecords().get( key );
				int start = rec.getUidStart();
				int end = rec.getUidEnd();
				
				if( ( line.substring( start , end) ).equalsIgnoreCase( myFileLayouts.getTrailer().getUid() ) ) {
					log.debug("Found a DATA record");
					return parseDataRec( line , myFileLayouts.getTrailer() );
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
		parseFields( line , obj , rec );
		return null;
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
			setMemberVariable( obj , dataField );
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
		}
		
		String prelimField = delimitedLine.getNextToken();
		
		return null;
	}
	
	
	private void setMemberVariable( Object obj , Object field ) {
		
	}
	
	
	
	private String nextToken() {
		
		return "";
	}
}
