package com.mifmif.common.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Node class is used here to present a transition position in the Automata
 * state . Each Node has a nbrChar that present the number of possible
 * characters that could be used in the transaction, and a list of Node that
 * present the next transactions.
 * 
 * @author y.mifrah
 *
 */
public class Node {
	private int nbrChar = 1;
	private long nbrMatchedString = 0;
	private List<Node> nextNodes = new ArrayList<Node>();
	private boolean isNbrMatchedStringUpdated;
	private char minChar;
	private char maxChar;

	/**
	 * Calculate the number of string that will be generated until the
	 * transaction presented by this node, and set the result in
	 * <code>nbrMatchedString</code>.
	 */
	public void updateNbrMatchedString() {
		if (isNbrMatchedStringUpdated)
			return;
		if (nextNodes.size() == 0)
			nbrMatchedString = nbrChar;
		else
			for (Node childNode : nextNodes) {
				childNode.updateNbrMatchedString();
				long childNbrChar = childNode.getNbrMatchedString();
				nbrMatchedString += nbrChar * childNbrChar;
			}
		isNbrMatchedStringUpdated = true;
	}

	public List<Node> getNextNodes() {
		return nextNodes;
	}

	public void setNextNodes(List<Node> nextNodes) {
		this.nextNodes = nextNodes;
	}

	public int getNbrChar() {
		return nbrChar;
	}

	public void setNbrChar(int nbrChar) {
		this.nbrChar = nbrChar;
	}

	public long getNbrMatchedString() {
		return nbrMatchedString;
	}

	public void setNbrMatchedString(long nbrMatchedString) {
		this.nbrMatchedString = nbrMatchedString;
	}

	public char getMaxChar() {
		return maxChar;
	}

	public void setMaxChar(char maxChar) {
		this.maxChar = maxChar;
	}

	public char getMinChar() {
		return minChar;
	}

	public void setMinChar(char minChar) {
		this.minChar = minChar;
	}

}