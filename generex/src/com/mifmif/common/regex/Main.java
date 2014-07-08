package com.mifmif.common.regex;

public class Main {
	public static void main(String[] args) {
		Generex generex = new Generex("[a-f]{1,3}[0-1]");
		generex.printMatchedStrings();
		System.out.println(generex.getMatchedString(14));// aa1
	}

}
