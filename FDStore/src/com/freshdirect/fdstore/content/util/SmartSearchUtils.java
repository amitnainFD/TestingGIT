package com.freshdirect.fdstore.content.util;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.hivemind.Registry;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.search.ContentSearchServiceI;
import com.freshdirect.cms.search.SynonymDictionary;
import com.freshdirect.cms.search.spell.CsongorDistance;
import com.freshdirect.cms.search.spell.StringDistance;
import com.freshdirect.cms.search.term.DashAsteriskSplitPermuter;
import com.freshdirect.cms.search.term.EnglishStemmerConv;
import com.freshdirect.cms.search.term.LowercaseCoder;
import com.freshdirect.cms.search.term.SearchTermNormalizer;
import com.freshdirect.cms.search.term.SynonymPermuter;
import com.freshdirect.cms.search.term.SynonymSearchTermNormalizerFactory;
import com.freshdirect.cms.search.term.Term;
import com.freshdirect.cms.search.term.TermCoder;
import com.freshdirect.cms.search.term.TermScoreTermNormalizer;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.fdstore.content.ComparatorChain;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.ContentSearch;
import com.freshdirect.fdstore.content.ContentSearchUtil;
import com.freshdirect.fdstore.content.EnumSortingValue;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.content.FilteringSortingItem;
import com.freshdirect.fdstore.content.SortValueComparator;
import com.freshdirect.fdstore.content.StoreModel;
import com.freshdirect.fdstore.pricing.ProductPricingFactory;
import com.freshdirect.framework.conf.FDRegistry;

public class SmartSearchUtils {
	public static void collectSaleInfo(List<FilteringSortingItem<ProductModel>> products, PricingContext context) {
		for (FilteringSortingItem<ProductModel> product : products) {
			ProductModel p;
			if (context != null)
				p = ProductPricingFactory.getInstance().getPricingAdapter(product.getModel(), context);
			else
				p = product.getModel();
			product.putSortingValue(EnumSortingValue.DEAL, p.getPriceCalculator().getHighestDealPercentage());
		}
	}

	public static void collectAvailabilityInfo(List<FilteringSortingItem<ProductModel>> products, PricingContext context) {
		for (FilteringSortingItem<ProductModel> product : products) {
			ProductModel p;
			if (context != null)
				p = ProductPricingFactory.getInstance().getPricingAdapter(product.getModel(), context);
			else
				p = product.getModel();
			boolean available = p.isFullyAvailable();
			product.putSortingValue(EnumSortingValue.AVAILABILITY, available ? 1 : 0);
		}
	}

	public static void collectOriginalTermInfo(List<FilteringSortingItem<ProductModel>> products, String searchTerm) {
		if (searchTerm == null)
			return;
		boolean exact = ContentSearchUtil.isQuoted(searchTerm);
		if (exact)
			searchTerm = ContentSearchUtil.removeQuotes(searchTerm);
		SearchTermNormalizer normalizer = new SearchTermNormalizer(new Term(searchTerm));
		List<Term> terms = normalizer.getTerms();
		if (terms.isEmpty())
			return;
		Term term = terms.get(0);
		for (FilteringSortingItem<ProductModel> product : products) {
			if (product.getModel().getFullName() == null)
				continue;
			normalizer = new SearchTermNormalizer(new Term(product.getModel().getFullName()));
			terms = normalizer.getTerms();
			if (terms.isEmpty())
				continue;
			if (terms.get(0).equals(term))
				product.putSortingValue(EnumSortingValue.ORIGINAL_TERM, 1);
		}
	}
	
	public static void collectRelevancyCategoryScores(List<FilteringSortingItem<ProductModel>> products, String searchTerm) {
		if (searchTerm == null)
			return;
		boolean exact = ContentSearchUtil.isQuoted(searchTerm);
		if (exact)
			searchTerm = ContentSearchUtil.removeQuotes(searchTerm);
		List<Term> terms = new LowercaseCoder(new Term(searchTerm)).getTerms();
		if (terms.isEmpty())
			return;
		String term = terms.get(0).toString();
		Map<ContentKey, Integer> relevancyScores = ContentSearch.getInstance().getSearchRelevancyScores(term);
		for (FilteringSortingItem<ProductModel> product : products) {
			Integer score = ContentSearch.getRelevancyScore(relevancyScores, product.getModel());
			if (score != null)
				product.putSortingValue(EnumSortingValue.CATEGORY_RELEVANCY, score);
		}
	}

	public static void collectTermScores(List<FilteringSortingItem<ProductModel>> products, String searchTerm) {
		if (searchTerm == null)
			return;
		boolean exact = ContentSearchUtil.isQuoted(searchTerm);
		if (exact)
			searchTerm = ContentSearchUtil.removeQuotes(searchTerm);
		ContentSearchServiceI search = null;
		try {
			Registry registry = FDRegistry.getInstance();
			search = (ContentSearchServiceI) registry.getService(ContentSearchServiceI.class);
		} catch (Exception e) {
		}
		StringDistance sd;
		if (search != null)
			sd = search.getSpellService().getStringDistance();
		else
			sd = new CsongorDistance();
		
		TermCoder normalizer = new DashAsteriskSplitPermuter(new Term(searchTerm));
		normalizer = new TermScoreTermNormalizer(normalizer);
		normalizer = new SynonymPermuter(SynonymDictionary.createFromCms(new SynonymSearchTermNormalizerFactory()), normalizer);
		List<Term> terms = normalizer.getTerms();
		if (terms.isEmpty())
			return;
		for (FilteringSortingItem<ProductModel> product : products) {
			long score = 0l;
			for (Term term : terms) {
				long s = calculateTermScore(product.getModel(), term, sd);
				if (s > score)
					score = s;
			}
			if (score > 0l)
				product.putSortingValue(EnumSortingValue.TERM_SCORE, score);
		}
	}

	private static long calculateTermScore(ProductModel model, Term term, StringDistance sd) {
		ContentNodeModel node = model;
		List<Term> stemmedTerms = new EnglishStemmerConv(term).getTerms();
		if (stemmedTerms.isEmpty())
			return 0;
		Term stemmed = stemmedTerms.get(0);
		if (stemmed.length() != term.length())
			return 0;
		long categoryScore = 0;
		long productScore = 0;
		int level = 0;
		while (node != null && !(node instanceof StoreModel)) {
			if (level > 0)
				categoryScore >>= 8;
			TermCoder normalizer = new DashAsteriskSplitPermuter(new Term(node.getFullName()));
			normalizer = new TermScoreTermNormalizer(normalizer);
			List<Term> orig = normalizer.getTerms();
			normalizer = new EnglishStemmerConv(normalizer);
			if (normalizer.getTerms().isEmpty()) {
				node = node.getParentNode();
				level++;
				continue;
			}
			List<Term> ts = normalizer.getTerms();
			if (ts.size() != orig.size()) {
				node = node.getParentNode();
				level++;
				continue;
			}
			long s = 0;
			for (int i = 0; i < ts.size(); i++) {
				int e1 = 0;
				int d1 = 0;
				Term t = ts.get(i);
				if (t.equals(stemmed)) {
					e1 = 7;
					d1 = sd.getDistance(term.toString(), orig.get(i).toString());
				} else {
					int idx = t.indexOf(stemmed);
					if (idx != -1) {
						e1 = 6;
						String substr = Term.join(orig.get(i).getTokens().subList(idx, idx + term.length()), Term.DEFAULT_SEPARATOR);
						d1 = sd.getDistance(term.toString(), substr);
					} else if (orig.get(i).length() == t.length()) {
						StringBuffer b1 = new StringBuffer();
						StringBuffer b2 = new StringBuffer();
						for (int j = 0; j < stemmed.length(); j++) {
							int idx2 = t.indexOf(stemmed.getTokens().get(j));
							if (idx2 != -1) {
								e1++;
								b1.append(term.getTokens().get(j));
								b1.append(' ');
								b2.append(orig.get(i).getTokens().get(idx2));
								b2.append(' ');
							}
						}
						if (b1.length() > 0 && b2.length() > 0)
							d1 = sd.getDistance(b1.toString(), b2.toString());
						else
							d1 = 15;
						
						if (e1 > 5)
							e1 = 5;
					}
				}

				d1 = Math.max(0, Math.min(15, d1));
				int s1 = (e1 << 4) + (15 - d1);
				if (s1 > s)
					s = s1;
			}
			if (level == 0) {
				productScore = s;
			} else {
				s <<= 56; // 64 - 8
				categoryScore += s;
			}

			level++;
			node = node.getParentNode();
		}
		
		categoryScore >>= 8;
		categoryScore <<= 8;
		return categoryScore + productScore;
	}
	
	public static List<ProductModel> sortBySale(List<ProductModel> products, PricingContext pricingContext, boolean ascending) {
		List<FilteringSortingItem<ProductModel>> items = FilteringSortingItem.wrap(products);
		ComparatorChain<FilteringSortingItem<ProductModel>> comparator = ComparatorChain.create(new SortValueComparator<ProductModel>(EnumSortingValue.DEAL));
		collectSaleInfo(items, pricingContext);
		if (!ascending)
			comparator = ComparatorChain.reverseOrder(comparator);
		comparator.chain(FilteringSortingItem.wrap(ProductModel.FULL_NAME_PRODUCT_COMPARATOR));
		Collections.sort(items, comparator);
		return FilteringSortingItem.unwrap(items);
	}
}
