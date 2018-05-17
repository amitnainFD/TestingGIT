package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Category;

import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.dsl.CompileException;
import com.freshdirect.smartstore.fdstore.FactorRequirer;
import com.freshdirect.smartstore.sampling.ImpressionSampler;
import com.freshdirect.smartstore.sampling.RankedContent;
import com.freshdirect.smartstore.scoring.CachingDataGenerator;
import com.freshdirect.smartstore.scoring.DataAccess;
import com.freshdirect.smartstore.scoring.DataGenerator;
import com.freshdirect.smartstore.scoring.OrderingFunction;
import com.freshdirect.smartstore.scoring.PrioritizedDataAccess;
import com.freshdirect.smartstore.scoring.ScoringAlgorithm;

/**
 * This recommendation service returns items based on a domain specific
 * language.
 * 
 * @author zsombor
 * 
 */
public class ScriptedRecommendationService extends AbstractRecommendationService implements FactorRequirer {
	private static Category LOGGER = LoggerFactory.getInstance(ScriptedRecommendationService.class);

	private DataGenerator generator;
	private ScoringAlgorithm scoring;

	public ScriptedRecommendationService(Variant variant, ImpressionSampler sampler,
			boolean includeCartItems, String generator) throws CompileException {
		this(variant, sampler, includeCartItems, generator, null);
	}

	public ScriptedRecommendationService(Variant variant, ImpressionSampler sampler,
			boolean includeCartItems, String generator, String scoring) throws CompileException {
		super(variant, sampler, includeCartItems);
		if (generator == null) {
			throw new IllegalArgumentException("generator cannot be null");
		}
		this.generator = GlobalCompiler.getInstance().createDataGenerator(generator);
		if (scoring != null && scoring.trim().length() != 0) {
			this.scoring = GlobalCompiler.getInstance().createScoringAlgorithm(scoring);
		}
	}

	public List<ContentNodeModel> doRecommendNodes(SessionInput input) {
		return recommendNodes(input, new PrioritizedDataAccess(input.getExclusions(), input.isUseAlternatives(), input.isShowTemporaryUnavailable(),
				input.isExcludeAlcoholicContent()));
	}

	public List<ContentNodeModel> recommendNodes(SessionInput input, DataAccess dataAccess) {
		// generate content node list based on the 'generator' expression.
		List<? extends ContentNodeModel> result = generator.generate(input, dataAccess);

		String userId = input.getCustomerId();
                PricingContext pricingCtx = input.getPricingContext();
		List<RankedContent.Single> rankedContents;

		boolean aggregatable;
		if (scoring != null && scoring.getReturnSize() > 0) {
			String[] variableNames = scoring.getVariableNames();

			if (scoring.getReturnSize() > 1) {
				OrderingFunction orderingFunction = scoring.createOrderingFunction();
				for (Iterator<? extends ContentNodeModel> iter = result.iterator(); iter.hasNext();) {
					ContentNodeModel contentNode = iter.next();
					double[] values = dataAccess.getVariables(userId, pricingCtx, contentNode, variableNames);
					double[] score = scoring.getScores(values);
					orderingFunction.addScore(contentNode, score);
				}
				rankedContents = orderingFunction.getRankedContents();
				aggregatable = false;
			} else {
				// one score computed, interpret as 'weight' or probability.
				TreeSet<RankedContent.Single> scores = new TreeSet<RankedContent.Single>();
				rankedContents = new ArrayList<RankedContent.Single>(result.size());

				for (Iterator<? extends ContentNodeModel> iter = result.iterator(); iter.hasNext();) {
					ContentNodeModel contentNode = iter.next();
					double[] values = dataAccess.getVariables(userId, pricingCtx, contentNode, variableNames);
					double[] score = scoring.getScores(values);
					scores.add(new RankedContent.Single(score[0], contentNode));
				}
				for (Iterator<RankedContent.Single> iter = scores.iterator(); iter.hasNext();) {
					rankedContents.add(iter.next());
				}
				aggregatable = true;
			}
		} else {
			rankedContents = rankListByOrder(result);
			aggregatable = false;
		}

		List<? extends ContentNodeModel> prioritized = dataAccess.getPrioritizedNodes();
		List<ContentNodeModel> sample;
		List<? extends ContentNodeModel> deprioritized = dataAccess.getPosteriorNodes();
		input.setPrioritizedCount( prioritized.size() );

		
		// sample items excluding the union of low and high priority nodes
		{
			Set<ContentNodeModel> items2exclude = new HashSet<ContentNodeModel>(prioritized);
			items2exclude.addAll(deprioritized);

			sample = sample(input, rankedContents, aggregatable, items2exclude);
		}


		/** 
		 *  put the final list together
		 *  [priority items]+[sampled normal items]+[low priority items]
		 */
		List<ContentNodeModel> finalList = new ArrayList<ContentNodeModel>(prioritized.size() + sample.size() + deprioritized.size());
		finalList.addAll(prioritized);
		finalList.addAll(sample);
		finalList.addAll(deprioritized);
		
		return finalList;
	}

	/**
	 * Collect needed factors into the buffer
	 * 
	 * @param buffer
	 *            Collection<String>
	 * @return the original buffer.
	 */
	public void collectFactors(Collection<String> buffer) {
		buffer.addAll(generator.getFactors());
		if (scoring != null) {
			String[] variableNames = scoring.getVariableNames();
			for (int i = 0; i < variableNames.length; i++) {
				buffer.add(variableNames[i]);
			}
		}
	}

	public String getDescription() {
		return "generator:" + this.generator + ", scoring:" + this.scoring;
	}

	public boolean isCacheable() {
		return generator instanceof CachingDataGenerator;
	}

	public boolean isCacheEnabled() {
		return generator instanceof CachingDataGenerator && ((CachingDataGenerator) generator).isCacheEnabled();
	}

	public ScoringAlgorithm getScoring() {
		return scoring;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[sampler=" + sampler
				+ ", includeCartItems=" + isIncludeCartItems()
				+ ", generator=" + generator
				+ ", scoring=" + scoring + "]";
	}

	// for debugging purposes
	public DataGenerator getGenerator() {
		return generator;
	}
}
