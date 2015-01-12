package test.avr;

import org.avr.fileread.FileReadWriter;

public class ParseFile {

	public static void main(String[] args) {
		
		try{ 
			FileReadWriter reader = new FileReadWriter(args[0]);
			
			System.out.println( reader.displayFields() );
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
