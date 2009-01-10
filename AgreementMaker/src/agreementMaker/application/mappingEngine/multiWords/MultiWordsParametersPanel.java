package agreementMaker.application.mappingEngine.multiWords;


import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import agreementMaker.Utility;
import agreementMaker.application.mappingEngine.AbstractMatcherParametersPanel;
import agreementMaker.application.mappingEngine.AbstractParameters;
import agreementMaker.application.mappingEngine.StringUtil.NormalizerParameter;

public class MultiWordsParametersPanel extends AbstractMatcherParametersPanel {

	/**
	 * Base Similarity Matcher - The Parameters Panel
	 * @author Cosmin Stroe
	 * @date Nov 22, 2008
	 * ADVIS @ UIC
	 */
	private static final long serialVersionUID = -7652636660460034435L;

	private MultiWordsParameters parameters;
	
	private JLabel titleLabel = new JLabel("Advanced parametrization is allowed. Non exepert users can keep default values.");
	private JLabel selectMetricLabel = new JLabel("Select multi-words string similarity metric: "); 
	private JComboBox metricsCombo;
	
	private JLabel whichTermsLabel = new JLabel("For each concept a multi-words string is built considering several terms related to it.");
	private JLabel conceptLabel = new JLabel("Consider concept's terms (localname, label, comment, seeAlso, isDefBy).");
	private JCheckBox conceptCheck;
	private JLabel neighbourLabel = new JLabel("Consider neighbours terms (parents, siblings, descendants).") ;
	private JCheckBox  neighbourCheck;
	private JLabel indLabel = new JLabel("Consider individuals.");
	private JCheckBox indCheck;
	private JLabel localLabel = new JLabel("Do not consider localnames. To be selected when they are just meaningless codes.");
	private JCheckBox localCheck;

	
	/*
	 * The constructor creates the GUI elements and adds 
	 * them to this panel.  It also creates the parameters object.
	 * 
	 */
	public MultiWordsParametersPanel() {
		
		super();
		//init components
		String[] metricsList = {MultiWordsParameters.TFIDF, MultiWordsParameters.EUCLIDEAN, MultiWordsParameters.COSINE, MultiWordsParameters.JACCARD,  MultiWordsParameters.DICE};
		metricsCombo = new JComboBox(metricsList);
		


		conceptCheck = new JCheckBox();
		conceptCheck.setSelected(true);
		neighbourCheck = new JCheckBox();
		neighbourCheck.setSelected(true);
		indCheck = new JCheckBox();
		indCheck.setSelected(true);
		localCheck = new JCheckBox();
		localCheck.setSelected(false);
		

		//LAYOUT: grouplayout is already complicated but very flexible, plus in this case the matchers list is dynamic so it's even more complicated
		GroupLayout layout = new GroupLayout(this);
		this.setLayout(layout);
		
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(true);

		
		
		// Here we define the horizontal and vertical groups for the layout.
		// Both definitions are required for the GroupLayout to be complete.
		layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
			.addComponent(titleLabel)
			.addGroup(layout.createSequentialGroup()
					.addComponent(selectMetricLabel) 			
					.addComponent(metricsCombo,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE) 	
					)
			.addComponent(whichTermsLabel)
			.addGroup(layout.createSequentialGroup()
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
						.addComponent(conceptCheck)
						.addComponent(neighbourCheck) 
						.addComponent(indCheck) 		
						.addComponent(localCheck) 
					)
					.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(conceptLabel) 	
						.addComponent(neighbourLabel) 	
						.addComponent(indLabel) 	
						.addComponent(localLabel) 	
					)
			)
		);
		// the Vertical group is the same structure as the horizontal group
		// but Sequential and Parallel definition are exchanged
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(titleLabel)
				.addGap(30)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(selectMetricLabel) 			
						.addComponent(metricsCombo,GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,  GroupLayout.PREFERRED_SIZE) 	
						)
				.addGap(30)
				.addComponent(whichTermsLabel)
				.addGap(10)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(conceptCheck) 			
						.addComponent(conceptLabel)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(neighbourCheck) 			
						.addComponent(neighbourLabel)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(indCheck) 			
						.addComponent(indLabel)
						)
				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
						.addComponent(localCheck) 			
						.addComponent(localLabel)
						)
				.addGap(30)
			);
	}
	
	
	public AbstractParameters getParameters() {
		parameters = new MultiWordsParameters();
		
		parameters.measure = (String)metricsCombo.getSelectedItem();
		parameters.considerInstances = indCheck.isSelected();
		parameters.ignoreLocalNames = localCheck.isSelected();
		parameters.considerNeighbors  = neighbourCheck.isSelected();
		parameters.considerConcept = conceptCheck.isSelected();
		return parameters;
		
	}
	
	public String checkParameters() {
		if(!(indCheck.isSelected() || neighbourCheck.isSelected() || conceptCheck.isSelected())) {
			return "At least one of the three terms sources must be selected.\n Select concept's or neighbours or individuals terms.";
		}
		return null;
	}
}
