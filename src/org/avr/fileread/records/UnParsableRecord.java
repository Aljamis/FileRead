package org.avr.fileread.records;


/**
 * A generic record type used when line from a file does not conform to its
 * described layout.
 * 
 * @author Alfonso
 *
 */
public class UnParsableRecord {
	
	public UnParsableRecord(String desc , String line) {
		this.description = desc;
		this.line = line;
	}
	
	private String line;
	public String getLine() { return line; }
	public void setLine(String ln) { this.line = ln; }
	
	private String description;
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }

}
