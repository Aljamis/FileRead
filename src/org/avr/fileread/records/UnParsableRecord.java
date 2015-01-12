package org.avr.fileread.records;

public class UnParsableRecord {
	
	public UnParsableRecord(String line) {
		this.line = line;
	}
	
	private String line;
	public String getLine() { return line; }
	public void setLine(String ln) { this.line = ln; }

}
