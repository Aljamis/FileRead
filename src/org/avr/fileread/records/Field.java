package org.avr.fileread.records;

public class Field {
	
	private String name;
	private String type;
	private int start;
	private int end;
	private int occurs;
	private Boolean trim;
	private String format;
	
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	
	public int getStart() { return start; }
	public void setStart(int start) { this.start = start; }
	
	public int getEnd() { return end; }
	public void setEnd(int end) { this.end = end; }
	
	public int getOccurs() { return occurs; }
	public void setOccurs(int occurs) { this.occurs = occurs; }
	
	public Boolean isTrim() { return trim; }
	public void setTrim(Boolean trim) { this.trim = trim; }
	
	public String getFormat() { return format; }
	public void setFormat(String frmt) { this.format = frmt; }
}
