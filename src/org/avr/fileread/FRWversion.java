package org.avr.fileread;

public class FRWversion {
	
	private static final String MAJOR="2";
	private static final String MINOR="0";
	private static final String BuildNumber = FRWversion.class.getPackage().getImplementationVersion();
	
	
	public static String getVersion()  {
		return MAJOR +"."+ MINOR +"."+ BuildNumber;
	}
	
	
	public static String getReleaseDate() {
		return FRWversion.class.getPackage().getImplementationTitle();
	}

}
