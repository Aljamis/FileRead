package org.avr.fileread.dom;

import org.w3c.dom.Comment;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

public class DOMutils {
	
	
	protected static boolean nodeIsTextAndEmpty(Node node) {
		if (node == null) return false;
		if (! (node instanceof Text) ) return false;
		if (! ((Text)node).getTextContent().trim().isEmpty() ) return false;
		return true;
	}
	
	
	protected static boolean nodeIsComment(Node node) {
		if (node == null) return false;
		if ( node instanceof Comment) return true;
		return false;
	}

}
