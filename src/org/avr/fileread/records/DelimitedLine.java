package org.avr.fileread.records;

/**
 * For lines that are delimited the current token position MUST be managed in one central
 * location.  Separating it completely from the rest of code is the best approach  
 * 
 * @author axviareque
 *
 */
public class DelimitedLine {
	
	private String theLine;
	private String theDelimiter;
	private String[] theTokens;
	private int currentIndex;
	
	
	
	public DelimitedLine(String line, String delimiter ) {
		this.theDelimiter = delimiter;
		this.theLine = line;
		
		this.theTokens = theLine.split( this.getTheDelimiter() );
		this.currentIndex = 0;
	}
	
	
	public String getNextToken() {
		return this.getTheTokens()[currentIndex++];
	}
	
	public int numOfTokens() {
		return this.theTokens.length;
	}
	
	
	/**
	 * @return Returns the currentIndex.
	 */
	public int getCurrentIndex() {
		return currentIndex;
	}
	/**
	 * @param currentIndex The currentIndex to set.
	 */
	public void setCurrentIndex(int currentIndex) {
		this.currentIndex = currentIndex;
	}
	/**
	 * @return Returns the theDelimiter.
	 */
	public String getTheDelimiter() {
		return theDelimiter;
	}
	/**
	 * @param theDelimiter The theDelimiter to set.
	 */
	public void setTheDelimiter(String theDelimiter) {
		this.theDelimiter = theDelimiter;
	}
	/**
	 * @return Returns the theLine.
	 */
	public String getTheLine() {
		return theLine;
	}
	/**
	 * @param theLine The theLine to set.
	 */
	public void setTheLine(String theLine) {
		this.theLine = theLine;
	}
	/**
	 * @return Returns the theTokens.
	 */
	public String[] getTheTokens() {
		return theTokens;
	}
	/**
	 * @param theTokens The theTokens to set.
	 */
	public void setTheTokens(String[] theTokens) {
		this.theTokens = theTokens;
	}
}
