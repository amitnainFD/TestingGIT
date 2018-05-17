package com.freshdirect.smartstore.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.cms.smartstore.CmsRecommenderService;
import com.freshdirect.common.pricing.PricingContext;
import com.freshdirect.common.pricing.ZoneInfo;
import com.freshdirect.fdstore.content.CategoryModel;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.ContentNodeModel;
import com.freshdirect.fdstore.content.Recommender;
import com.freshdirect.fdstore.content.RecommenderStrategy;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.smartstore.RecommendationService;
import com.freshdirect.smartstore.SessionInput;
import com.freshdirect.smartstore.service.CmsRecommenderRegistry;

public class CmsRecommenderServiceImpl implements CmsRecommenderService {

	private static final Logger	LOG					= LoggerFactory.getInstance( CmsRecommenderServiceImpl.class );
	private static final long	serialVersionUID	= 7555105742910594364L;

	@Override
	public List<String> recommendNodes( ContentKey recommenderId, ContentKey categoryId, ZoneInfo zoneInfo ) {
		// TODO handle zoneId
		ContentNodeModel node = ContentFactory.getInstance().getContentNodeByKey( recommenderId );
		if ( node instanceof Recommender ) {
			Recommender recommenderNode = (Recommender)node;
			RecommenderStrategy strategy = recommenderNode.getStrategy();
			if ( strategy == null ) {
				return Collections.emptyList();
			}
			RecommendationService recommender = CmsRecommenderRegistry.getInstance().getService( strategy.getContentName() );
			if ( recommender == null ) {
				LOG.debug( "recommender " + recommenderId + " is not found" );
				return Collections.emptyList();
			}
			node = ContentFactory.getInstance().getContentNodeByKey( categoryId );
			CategoryModel category = null;
			if ( node instanceof CategoryModel )
				category = (CategoryModel)node;
			SessionInput input = new SessionInput( null );
			input.setCurrentNode( category );
			input.setExplicitList( recommenderNode.getScope() );
			input.setPricingContext( new PricingContext( zoneInfo ) );
			List<ContentNodeModel> products = recommender.recommendNodes( input );
			List<String> prodIds = new ArrayList<String>( products.size() );
			for ( int i = 0; i < products.size(); i++ ) {
				prodIds.add( products.get( i ).getContentKey().getId() );
			}
			return prodIds;
			
		}
		return Collections.emptyList();
	}
}
