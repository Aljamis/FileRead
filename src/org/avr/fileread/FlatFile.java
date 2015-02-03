package org.avr.fileread;

import java.util.HashMap;

/**
 * Contains all the record layouts for a specific file.
 * @author Alfonso
 *
 */
class FlatFile {
	
	private Header header;
	private Trailer trailer;
	private HashMap<String, DataRecord> dataRecords = new HashMap<String, DataRecord>();
	
	
	public Header getHeader() { return header; }
	public void setHeader(Header header) { this.header = header; }
	public Trailer getTrailer() { return trailer; }
	public void setTrailer(Trailer trailer) { this.trailer = trailer; }
	public HashMap<String, DataRecord> getDataRecords() { return dataRecords; }
	public void setDataRecords(HashMap<String, DataRecord> dataRecords) { this.dataRecords = dataRecords; }
}
