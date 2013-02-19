package com.bdt.kiradb;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;

/**
 * Simple Stemmer class
 * 
 * @author David Beckemeyer and Mark Patrovic
 *
 * @see PorterStemmer
 */
public class Stemmer {

	public static String stemString(String inStr, Set stopwords) {
		char[] w = new char[501];
		PorterStemmer s = new PorterStemmer();

		StringReader in = new StringReader(inStr);
		StringBuffer result = new StringBuffer();
		try {
			while (true)

			{
				int ch;
				ch = in.read();

				if (Character.isLetter((char) ch)) {
					int j = 0;
					while (true) {
						ch = Character.toLowerCase((char) ch);
						w[j] = (char) ch;
						if (j < 500)
							j++;
						ch = in.read();
						if (!Character.isLetter((char) ch)) {
							String u = new String(w,0,j);

							Iterator iter = stopwords.iterator();
							boolean stop = false;
						    while (iter.hasNext()) {
						    	String thisStop = (String) iter.next();
						    	//logger.info("check word: " + u + " against stop word " + thisStop);
						    	if (u.equals(thisStop)) {
						    		stop = true;
						    		break;
						    	}
						    }
						    if (!stop) {
								/* to test add(char ch) */
								for (int c = 0; c < j; c++)
									s.add(w[c]);
						    	s.stem();

						    	result.append(s.toString());
						    	result.append(" ");
						    }
							break;

						}
					}
				}
				if (ch < 0)
					break;
				// System.out.print((char) ch);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
		return result.toString().trim();
	}

}
