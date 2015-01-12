package org.avr.fileread;

import java.util.Set;

import org.apache.log4j.Logger;
import org.avr.fileread.records.DataRecord;
import org.avr.fileread.records.Field;
import org.avr.fileread.records.Header;
import org.avr.fileread.records.Trailer;
import org.avr.fileread.records.UnParsableRecord;


/**
 * 
 * @author Alfonso
 *
 */
public class FileDigester {
	private static Logger log = Logger.getLogger(FileDigester.class);
	
	private FlatFile myFileLayouts;
	public FlatFile getMyFileLayouts(){ return myFileLayouts; }
	public void setMyFileLayouts(FlatFile f) { this.myFileLayouts = f; }

	
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
	private Object parseDataRec(String line, Trailer trlr) {
		return null;
	}
}
