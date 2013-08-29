package am.matcher;

import java.util.Hashtable;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

import am.app.mappingEngine.AbstractMatcher;
import am.matcher.Combination.CombinationMatcher;
import am.matcher.parametricStringMatcher.ParametricStringMatcher;
import am.matcher.multiWords.MultiWordsMatcher;
import am.matcher.LexicalSynonymMatcher.LexicalSynonymMatcher;

public class MatchersActivator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}
	private ServiceRegistration<AbstractMatcher> regCombination;
	private ServiceRegistration<AbstractMatcher> regParametricString;
	private ServiceRegistration<AbstractMatcher> regMultiWords;
	private ServiceRegistration<AbstractMatcher> regLexicalSynonym;
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		MatchersActivator.context = bundleContext;
		System.out.println(context.getBundle().getHeaders().get
				(Constants.BUNDLE_NAME) + " starting...");
		regCombination = context.registerService(AbstractMatcher.class, new CombinationMatcher(), new Hashtable<String,String>());
		regParametricString = context.registerService(AbstractMatcher.class, new ParametricStringMatcher(), new Hashtable<String,String>());
		regMultiWords = context.registerService(AbstractMatcher.class, new MultiWordsMatcher(), new Hashtable<String,String>());
		regLexicalSynonym = context.registerService(AbstractMatcher.class, new LexicalSynonymMatcher(), new Hashtable<String,String>());
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		MatchersActivator.context = null;
		regCombination.unregister();
		regParametricString.unregister();
		regMultiWords.unregister();
		regLexicalSynonym.unregister();
		
		System.out.println(context.getBundle().getHeaders().get
				(Constants.BUNDLE_NAME) + " stopping...");
	}

}
