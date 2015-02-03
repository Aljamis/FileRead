package org.avr.fileread;

/**
 * For lines that are delimited the current token position MUST be managed in one central
 * location.  Separating it completely from the rest of code is the best approach  
 * 
 * @author axviareque
 *
 */
class DelimitedLine {
	
	private String theLine;
	private String theDelimiter;
	private String[] theTokens;
	private int currentIndex;
	
	private final static String META_CHARS = "<([{\\^-=$!|]})?*+.>";
	
	
	public DelimitedLine(String line, String delimiter ) {
		this.setTheDelimiter( delimiter );
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
	 * Is this character a META character used in REGEX matching.
	 * @param c
	 * @return
	 */
	private boolean isMetaChar(char c) {
		return META_CHARS.indexOf( c ) >= 0;
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
	 * Examine the delimiter for any META characters used in REGEX matching.  If a 
	 * META character is found it MUST be escaped (prefixed with "\\").
	 * @param theDelimiter The theDelimiter to set.
	 */
	public void setTheDelimiter(String str) {
		if (str.length() == 1 ) {
			if ( isMetaChar( str.charAt(0))) {
				this.theDelimiter = "\\"+ str;
				return ;
			}
		}
		
		StringBuffer retStr = new StringBuffer();
		for (int i=0 ; i<str.length() ; i++) {
			if ( isMetaChar(str.charAt(i))) {
				retStr.append("\\");
			}
			retStr.append( str.charAt(i));
		}
		this.theDelimiter = retStr.toString();
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
