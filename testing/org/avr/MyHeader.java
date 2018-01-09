package org.avr;

import java.util.Date;

public class MyHeader {
	private String fileType;
	private Date runDate;
	private Integer recCount;
	
	
	
	public String getFileType() { return fileType; }
	public void setFileType(String fileType) { this.fileType = fileType; }
	
	public Date getRunDate() { return runDate; }
	public void setRunDate(Date runDate) { this.runDate = runDate; }
	
	public Integer getRecCount() { return recCount; }
	public void setRecCount(Integer recCount) { this.recCount = recCount; }

}
