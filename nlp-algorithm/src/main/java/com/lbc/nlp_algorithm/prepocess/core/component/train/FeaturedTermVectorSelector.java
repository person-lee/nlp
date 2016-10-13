package com.lbc.nlp_algorithm.prepocess.core.component.train;

import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.lbc.nlp_algorithm.prepocess.api.api.Context;
import com.lbc.nlp_algorithm.prepocess.api.api.FeatureTermSelector;
import com.lbc.nlp_algorithm.prepocess.api.api.TermFeatureable;
import com.lbc.nlp_algorithm.prepocess.api.api.constants.ConfigKeys;
import com.lbc.nlp_algorithm.prepocess.core.common.AbstractComponent;
import com.lbc.nlp_algorithm.prepocess.core.utils.ReflectionUtils;

/**
 * Select term vector from the procedure of processing train data. The selection can
 * be based on any effective metric, here we use the Chi-square distance metric to
 * choose suitable terms. Certainly you can use another one to replace the
 * default one.</br>
 * 
 * @author Shirdrn
 */
public class FeaturedTermVectorSelector extends AbstractComponent {

	private static final Log LOG = LogFactory.getLog(FeaturedTermVectorSelector.class);
	private final FeatureTermSelector featuredTermsSelector;
	
	public FeaturedTermVectorSelector(final Context context) {
		super(context);
		String selectorClazz = context.getConfiguration().get(
				ConfigKeys.FEATURE_VECTOR_SELECTOR_CLASS, 
				"org.shirdrn.document.preprocessing.measure.chi.ChiFeatureTermSelector");
		LOG.info("Feature term vector selector: selectorClazz=" + selectorClazz);
		featuredTermsSelector = ReflectionUtils.newInstance(selectorClazz, FeatureTermSelector.class);
	}

	@Override
	public void fire() {
		// select feature term vector 
		Set<TermFeatureable> featureTerms = featuredTermsSelector.select(context);
		context.getVectorMetadata().setFeaturedTerms(featureTerms);
	}
	
}
