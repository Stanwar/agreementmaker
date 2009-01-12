package agreementMaker;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import sun.net.www.protocol.http.InMemoryCookieStore;

import agreementMaker.application.mappingEngine.AbstractMatcher;
import agreementMaker.application.mappingEngine.IntDoublePair;
import agreementMaker.userInterface.table.MyTableModel;

public class Utility {
	public final static String UNEXPECTED_ERROR = "Unexepcted System Error.\nTry to reset the system and repeat the operation.\nContact developers if the error persists.";
	
	
	//**************************************************USER INTERFACE UTILITIES***********************************************************
	
	public static String[] getPercentStringList() {
		int min = 0;
		int max = 100;
		int spin = 1;
		String[] s = new String[(max/spin) +1];
		String current;
		for(int i =min, j =0; i <= max && j<s.length; i+=spin, j++) {
			current = i+"%";
			s[j] = current;
		}
		return s;
	}
	
	public static double getDoubleFromPercent(String s) {
		String s2 = s.substring(0,s.length()-1);//remove last char %
		double d = Double.parseDouble(s2);
		return d/100;
	}
	/*
	public static String getPercentFromDouble(double d) {
		double p;
		if(0 <= d && d<= 1) {
			p = d * 100;
		}
		else throw new RuntimeException("Developer Error, the value passed to getPercentFromDouble(dobule d) should be between 0 and 1");
		return p+"%";
	}
	*/
	
	public static String getNoFloatPercentFromDouble(double d) {
		int i = (int)(d*100);
		return i+"%";
	}
	
	public static String[] getNumRelList() {
		int min = 1;
		int max = 100;
		int spin = 1;
		String[] list  = new String[(max/spin)+1];
		String any = MyTableModel.ANY;
		for(int i =min, j =0; i <= max && j<list.length-1; i+=spin, j++) {
			list[j] = i+"";
		}
		list[list.length-1] = any;
		return list;
	}
	
	public static int getIntFromNumRelString(String n) {
		int i;
		try {
			i = Integer.parseInt(n);
		}
		catch(Exception e) {
			//the value is the string any
			i = AbstractMatcher.ANY_INT;
		}
		return i;
	}
	
	public static String getStringFromNumRelInt(int n) {
		String s;
		if(n ==AbstractMatcher.ANY_INT)
			s = MyTableModel.ANY;
		else s = n+"";
		return s;
	}
	
	public static String getYesNo(boolean b) {
		if(b)
			return "yes";
		else return "no";
	}
	

	
	/**
	 * This function displays the JOptionPane with title and descritpion
	 *
	 * @param desc 		thedescription you want to display on option pane
	 * @param title 	the tile you want to display on option pane
	 */
	public  static void displayMessagePane(String desc, String title) {
		if(title == null)
			title = "Message Dialog";
		JOptionPane.showMessageDialog(null, desc, title, JOptionPane.PLAIN_MESSAGE);
	}
	
	public static void displayTextAreaPane(String desc, String title) {
		if(title == null)
			title = "Message Dialog";
		String[] split = desc.split("\n");
		int rows = Math.min(30, split.length+5);
		int columns = 0;
		for(int i = 0; i<split.length; i++) {
			if(columns < split[i].length())
				columns = split[i].length();
		}
		columns = Math.min(80, columns/2 +5); //columns/2 because each character is longer then a column so at the end it fits doing this
		
		JTextArea ta = new JTextArea(desc,rows,columns);
		ta.setLineWrap(true);
		ta.setWrapStyleWord(true);
		//ta.setEditable(false);
		JScrollPane sp = new JScrollPane(ta);
		JOptionPane.showMessageDialog(null, sp, title, JOptionPane.PLAIN_MESSAGE);
	}
	
	public static void displayErrorPane(String desc, String title) {
		if(title == null)
			title = "Error";
		JOptionPane.showMessageDialog(null, desc,title, JOptionPane.ERROR_MESSAGE);
	}
	
	
	public static boolean displayConfirmPane(String desc, String title) {
		if(title == null)
			title = "Confirmation required";
		int res =  JOptionPane.showConfirmDialog(null,
			    desc,
			    title,
			    JOptionPane.YES_NO_OPTION);	
		if(res == JOptionPane.YES_OPTION)
			return true;
		else return false;
		}
	
	
	//********************************************MATH UTILITIES**************************************************************
	
	//return f(x) = 1 / ( 1 + exp( -5 (x - k) ) )
	//sigmoid function used by rimom in the weighted average of similarities
	public static double getSigmoidFunction(double d) {
		double k = 0.5;
		double numerator = 1;
		double exponent = (-5) * (d - k);
		double exp = Math.exp(exponent);
		double denominator = 1 + exp;
		return numerator / denominator;
	}
	
	public static double getAverageOfArray(double[] array) {
		if(array.length == 0) {
			return 0;
		}
		double sum = 0;
		for(int i = 0; i < array.length; i++) {
			sum += array[i];
		}
		sum = sum / (double) array.length;
		return sum;
	}
	
	public static IntDoublePair getMaxOfRow(double[][] matrix, int row) {
		IntDoublePair max = IntDoublePair.createFakePair();
		for(int i = 0; i < matrix[row].length; i++) {
			if(matrix[row][i] > max.value) {
				max.value = matrix[row][i];
				max.index = i;
			}
		}
		return max;
	}
	
	//order the array of IntDoublePair so that the minimum is at the beginning
	//the only element in the wrong position is the key element that has to be moved before or later or staeid there
	public static void adjustOrderPairArray(IntDoublePair[] intDoublePairs, int k) {
		IntDoublePair currentPair;
		IntDoublePair nextPair;
		//THE TWO CASES ARE EXCLUSIVE 
		
		
		//if is higher the the next val i need to move the element to the right
		for(int i = k; i < intDoublePairs.length -1 && intDoublePairs[i].value > intDoublePairs[i+1].value; i++) {
			currentPair = intDoublePairs[i];
			nextPair = intDoublePairs[i+1];
			intDoublePairs[i] = nextPair;
			intDoublePairs[i+1] = currentPair;
		}
		
		//if is lower the the prev val i need to move the element to the left
		for(int i = k; i > 0 && intDoublePairs[i].value < intDoublePairs[i-1].value; i--) {
			currentPair = intDoublePairs[i];
			nextPair = intDoublePairs[i-1];
			intDoublePairs[i] = nextPair;
			intDoublePairs[i-1] = currentPair;
		}
		
	}
	
	public static int getMaxOfIntMatrix(int[][] matrix) {
		int max = Integer.MIN_VALUE;
		for(int i = 0; i < matrix.length; i++) {
			for(int j = 0; j < matrix[i].length; j++) {
				int current = matrix[i][j];
				if(current > max)
					max = current;
			}
		}
		return max;
	}

	//*******************************************STRING UTILITIES********************************************************
	public static boolean isIrrelevant(String s) {
		return s == null || s.equals("") ;
	}
	
	public static boolean startsWithSpace(String s) {
		if(s!=null && s.length() >0) {
			char first = s.charAt(0);
			if(first == ' ')
				return true;
		}
		return false;
	}
	
	public static boolean endsWithSpace(String s) {
		if(s!=null && s.length() >0) {
			char last = s.charAt(s.length()-1);
			if(last == ' ')
				return true;
		}
		return false;
	}
	
	//I need to add the string only if it's relevant
	//and i need to put a space only if there is not already one
	public static String smartConcat(String first, String second) {
		//Attention: first can be irrelevant in fact the first time i start concatenating is often empty the first string.
		//it may happen that there will be 2 space between the two words, but is not important
		String result = first;
		if(!isIrrelevant(second)) {
			if(!(endsWithSpace(first) || startsWithSpace(second) || first.equals(""))) {
				result += " ";
			}
			result += second;
		}
		return result;
	}
	//**********************************************************************
	
}
