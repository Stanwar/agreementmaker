package test;


import javax.swing.JOptionPane;

import am.application.mappingEngine.StringUtil.Normalizer;
import am.application.mappingEngine.StringUtil.NormalizerParameter;
import am.application.mappingEngine.parametricStringMatcher.ParametricStringMatcher;
import am.application.mappingEngine.parametricStringMatcher.ParametricStringParameters;

public class ProcessStrings {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		/*
		char c = '\u2283';
		Character ch = new Character(c);
		OutputStreamWriter out = new OutputStreamWriter(new ByteArrayOutputStream());
		System.out.println(out.getEncoding());

		System.out.println(c+" "+ch);
		*/
		
		//YOU HAVE TO MAKE PUBLIC beforeAlignmentOperations() & preProcessString
		ParametricStringMatcher p = new ParametricStringMatcher();
		NormalizerParameter par = new NormalizerParameter();
		par.normalizeBlank = true;
		par.normalizeDiacritics = true;
		par.normalizePunctuation = true;
		par.normalizeDigit = true;
		par.removeStopWords = true;
		par.stem = true;
		Normalizer n = new Normalizer(par);
		
		String input = JOptionPane.showInputDialog("Insert string to be processed");
		String process;
		while(input != null) {
			process = n.normalize(input);
			JOptionPane.showMessageDialog(null, process);
			System.out.println(process);
			input = JOptionPane.showInputDialog("Insert string to be processed");
		}
	}

}
