/*
 * Copyright 2014 y.mifrah
 *

 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mifmif.common.regex;

import java.util.ArrayList;
import java.util.List;

/**
 * Node class is used here to present a position in the Automata state . Each
 * Node has a nbrChar that present the number of possible characters that could
 * be used to go to the next possible position, and a list of Node that present
 * the next positions.
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