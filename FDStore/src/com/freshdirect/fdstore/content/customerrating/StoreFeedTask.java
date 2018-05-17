package com.freshdirect.fdstore.content.customerrating;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.framework.util.log.LoggerFactory;


@Deprecated
public class StoreFeedTask {

	private static final Logger LOGGER = LoggerFactory.getInstance(StoreFeedTask.class);
	private static final String DF = "yyyy-MM-dd'T'HH:mm:ss.SSS";
	private static final int DF_LEN = 23;
	
	private String FEED_FILE = "";
	private String DOWNLOAD_PATH = "";
	private SimpleDateFormat sdf = new SimpleDateFormat(DF);
	

	private List<CustomerRatingsDTO> ratedProducts = new ArrayList<CustomerRatingsDTO>();
	
	public BazaarvoiceFeedProcessResult process(){
		
		FEED_FILE = FDStoreProperties.getBazaarvoiceDownloadFeedFile();
		DOWNLOAD_PATH = FDStoreProperties.getBazaarvoiceDownloadFeedTargetPath();
		
		try {
			parseFeedFileContent();
			CustomerRatingsDAO customerRatingsDAO = new CustomerRatingsDAO();
			customerRatingsDAO.purgeFeedFileContentStorage();
			customerRatingsDAO.storeFeedFileContent(ratedProducts);
			
		} catch (FDResourceException e) {
			LOGGER.error("Storing feed failed!",e);
			return new BazaarvoiceFeedProcessResult(false, e.getMessage());
		}
		
		LOGGER.info("Feed stored.");
		return new BazaarvoiceFeedProcessResult(true, null);
	}

	private void parseFeedFileContent() throws FDResourceException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			DocumentBuilder db = dbf.newDocumentBuilder();

			Document dom = db.parse(DOWNLOAD_PATH + FEED_FILE.substring(0, FEED_FILE.length() - 3));
			NodeList products = dom.getElementsByTagName("Product");

			if(products != null && products.getLength() > 0) {
				for(int i = 0 ; i < products.getLength();i++) {

					Element product = (Element)products.item(i);
					
					CustomerRatingsDTO ratedProduct = new CustomerRatingsDTO();
					
					ratedProduct.setProductId(getElementText(product,"ExternalId"));
					ratedProduct.setNumNativeQuestions(getElementInteger(product, "NumNativeQuestions"));
					ratedProduct.setNumQuestions(getElementInteger(product, "NumQuestions"));
					ratedProduct.setNumNativeAnswers(getElementInteger(product, "NumNativeAnswers"));
					ratedProduct.setNumAnswers(getElementInteger(product, "NumAnswers"));
					ratedProduct.setNumReviews(getElementInteger(product, "NumReviews"));
					ratedProduct.setNumStories(getElementInteger(product, "NumStories"));
					ratedProduct.setProductPageURL(getElementText(product, "ProductPageUrl"));
					ratedProduct.setProductReviewsURL(getElementText(product, "ProductReviewsUrl"));
					ratedProduct.setImageURL(getElementText(product, "ImageUrl"));
					
					Element reviewStatistic = (Element)product.getElementsByTagName("ReviewStatistics").item(0);
					if (reviewStatistic != null){

						ratedProduct.setAverageOverallRating((BigDecimal)getValue(BigDecimal.class, reviewStatistic, "AverageOverallRating"));
						ratedProduct.setOverallRatingRange(getElementInteger(reviewStatistic, "OverallRatingRange"));
						ratedProduct.setTotalReviewCount(getElementInteger(reviewStatistic, "TotalReviewCount"));
						ratedProduct.setRatingsOnlyReviewCount(getElementInteger(reviewStatistic, "RatingsOnlyReviewCount"));
						ratedProduct.setRecommendedCount(getElementInteger(reviewStatistic, "RecommendedCount"));
						ratedProduct.setNotRecommendedCount(getElementInteger(reviewStatistic, "NotRecommendedCount"));
						
						Element avgR = (Element)reviewStatistic.getElementsByTagName("AverageRatingValues").item(0);
						if (avgR!=null){
							
							NodeList averageRatingValues = avgR.getElementsByTagName("AverageRatingValue");
							if (averageRatingValues != null && averageRatingValues.getLength() > 0) {
								for (int a = 0; a < averageRatingValues.getLength(); a++) {
									Element averageRatingValue = (Element)averageRatingValues.item(a);
									if("Quality".equals(averageRatingValue.getAttribute("id"))) {
										ratedProduct.setAverageRatingValuesQuality(getElementBigDecimal(averageRatingValue, "AverageRating"));
										ratedProduct.setAverageRatingValuesQualityRange(getElementInteger(averageRatingValue, "RatingRange"));
									} else if("Value".equals(averageRatingValue.getAttribute("id"))) {
										ratedProduct.setAverageRatingValuesValue(getElementBigDecimal(averageRatingValue, "AverageRating"));
										ratedProduct.setAverageRatingValuesValueRange(getElementInteger(averageRatingValue, "RatingRange"));
									}
								}
							}
						}
						
						Element rat = (Element)reviewStatistic.getElementsByTagName("RatingDistribution").item(0);
						if (rat!=null){
							Element ratingDistributionItem = (Element)rat.getElementsByTagName("RatingDistributionItem").item(0);
							
							ratedProduct.setRatingValue((Integer)getValue(Integer.class, ratingDistributionItem, "RatingValue"));
							ratedProduct.setCount((Integer)getValue(Integer.class, ratingDistributionItem, "Count"));
						}
					}
					
					Element nativeReviewStatistic = (Element)product.getElementsByTagName("NativeReviewStatistics").item(0);
					if (nativeReviewStatistic!=null){

						ratedProduct.setNatAverageOverallRating((BigDecimal)getValue(BigDecimal.class, nativeReviewStatistic, "AverageOverallRating"));
						ratedProduct.setNatOverallRatingRange(getElementInteger(nativeReviewStatistic, "OverallRatingRange"));
						ratedProduct.setNatTotalReviewCount(getElementInteger(nativeReviewStatistic, "TotalReviewCount"));
						ratedProduct.setNatRatingsOnlyReviewCount(getElementInteger(nativeReviewStatistic, "RatingsOnlyReviewCount"));
						ratedProduct.setNatRecommendedCount(getElementInteger(nativeReviewStatistic, "RecommendedCount"));
						ratedProduct.setNatNotRecommendedCount(getElementInteger(nativeReviewStatistic, "NotRecommendedCount"));
						
						Element avg = (Element)nativeReviewStatistic.getElementsByTagName("AverageRatingValues").item(0);
						if (avg!=null){
							NodeList natAverageRatingValues = avg.getElementsByTagName("AverageRatingValue");
							if (natAverageRatingValues != null && natAverageRatingValues.getLength() > 0) {
								for (int a = 0; a < natAverageRatingValues.getLength(); a++) {
									Element natAverageRatingValue = (Element)natAverageRatingValues.item(a);
									if("Quality".equals(natAverageRatingValue.getAttribute("id"))) {
										ratedProduct.setNatAverageRatingValuesQuality(getElementBigDecimal(natAverageRatingValue, "AverageRating"));
										ratedProduct.setNatAverageRatingValuesQualityRange(getElementInteger(natAverageRatingValue, "RatingRange"));
									} else if("Value".equals(natAverageRatingValue.getAttribute("id"))) {
										ratedProduct.setNatAverageRatingValuesValue(getElementBigDecimal(natAverageRatingValue, "AverageRating"));
										ratedProduct.setNatAverageRatingValuesValueRange(getElementInteger(natAverageRatingValue, "RatingRange"));
									}
								}
							}
						}
						
						Element nRat = (Element)nativeReviewStatistic.getElementsByTagName("RatingDistribution").item(0);
						if (nRat!=null){

							Element nativeRatingDistributionItem = (Element)nRat.getElementsByTagName("RatingDistributionItem").item(0);
							
							ratedProduct.setNatRatingValue((Integer)getValue(Integer.class, nativeRatingDistributionItem, "RatingValue"));
							ratedProduct.setNatCount((Integer)getValue(Integer.class, nativeRatingDistributionItem, "Count"));
						}
					}
					
					Element feed = (Element)dom.getElementsByTagName("Feed").item(0);
					if (feed!=null){
						try {
							ratedProduct.setExtractDate(sdf.parse(feed.getAttributes().getNamedItem("extractDate").getNodeValue().substring(0,23)).getTime());
						} catch (ParseException e) {
							LOGGER.error(e);
						} catch (IndexOutOfBoundsException e){
							LOGGER.error(e);
						} catch (NullPointerException e){
							LOGGER.error(e);
						}
					}

					Element reviewsTag = (Element)product.getElementsByTagName("Reviews").item(0);
					if (reviewsTag!=null){
						NodeList reviews = reviewsTag.getElementsByTagName("Review");
	
						if(reviews != null && reviews.getLength() > 0) {
							for(int r = 0 ; r < reviews.getLength();r++) {
	
								Element review = (Element)reviews.item(r);
								
								CustomerReviewsDTO customerReview = new CustomerReviewsDTO();
								
								customerReview.setBVReviewId(review.getAttribute("id"));
								customerReview.setProductId(ratedProduct.getProductId());
								customerReview.setModerationStatus(getElementText(review, "ModerationStatus"));
								customerReview.setLastModificationTime(getElementDate(review, "LastModificationTime"));
	
								Element userProfileReference = (Element)review.getElementsByTagName("UserProfileReference").item(0);
								if (userProfileReference!=null){
	
									customerReview.setExternalId(getElementText(userProfileReference, "ExternalId"));
									customerReview.setDisplayName((String)getValue(String.class, userProfileReference, "DisplayName"));
									customerReview.setAnonymous(getElementBoolean(userProfileReference, "Anonymous"));
									customerReview.setHyperlinkingEnabled(getElementBoolean(userProfileReference, "HyperlinkingEnabled"));
								}
								
								customerReview.setRatingsOnly(getElementBoolean(review, "RatingsOnly"));
								customerReview.setTitle(getElementText(review, "Title"));
								customerReview.setReviewText(getElementText(review, "ReviewText"));
								customerReview.setNumComments(getElementInteger(review, "NumComments"));
								customerReview.setCampaignId(getElementText(review, "CampaignId"));
								customerReview.setRating(getElementBigDecimal(review, "Rating"));
								customerReview.setRatingRange(getElementInteger(review, "RatingRange"));
								customerReview.setRecommended((Boolean)getValue(Boolean.class, review, "Recommended"));
								customerReview.setNumFeedbacks(getElementInteger(review, "NumFeedbacks"));
								customerReview.setNumPositiveFeedbacks(getElementInteger(review, "NumPositiveFeedbacks"));
								customerReview.setNumNegativeFeedbacks(getElementInteger(review, "NumNegativeFeedbacks"));
								customerReview.setReviewerLocation((String)getValue(String.class, review, "ReviewerLocation"));
								customerReview.setIpAddress(getElementText(review, "IpAddress"));
								customerReview.setDisplayLocale(getElementText(review, "DisplayLocale"));
								customerReview.setSubmissionTime(getElementDate(review, "SubmissionTime"));
	
								Element badges = (Element)review.getElementsByTagName("Badges").item(0);
								if (badges != null) {
									Element badge = (Element)badges.getElementsByTagName("Badge").item(0);
									if (badge!=null){
										customerReview.setBadgeName(getElementText(badge, "Name"));
										customerReview.setBadgeContentType(getElementText(badge, "ContentType"));
									}
								}
								customerReview.setProductReviewsURL(getElementText(review, "ProductReviewsUrl"));
								customerReview.setProductReviewsDLURL(getElementText(review, "ProductReviewsDeepLinkedUrl"));
								customerReview.setFeatured(getElementBoolean(review, "Featured"));
								customerReview.setNetPromoterScore((BigDecimal)getValue(BigDecimal.class, review, "NetPromoterScore"));
								customerReview.setNetPromoterComment((String)getValue(String.class, review, "NetPromoterComment"));
								customerReview.setAuthenticationType(getElementText(review, "AuthenticationType"));
								customerReview.setUserEmailAddress(getElementText(review, "UserEmailAddress"));
								customerReview.setPublishedEmailAlert(getElementBoolean(review, "SendEmailAlertWhenPublished"));
								customerReview.setCommentedEmailAlert(getElementBoolean(review, "SendEmailAlertWhenCommented"));
								customerReview.setOriginatingDisplayCode(getElementText(review, "OriginatingDisplayCode"));
								customerReview.setContentCodes((String)getValue(String.class, review, "ContentCodes"));
								customerReview.setFirstPublishTime(getElementDate(review, "FirstPublishTime"));
								customerReview.setLastPublishTime(getElementDate(review, "LastPublishTime"));
	
								ratedProduct.addReview(customerReview);
							}
						}
					}
					
					ratedProducts.add(ratedProduct);
				}
			}


		}catch(ParserConfigurationException pce) {
        	throw new FDResourceException("parseFeedFileContent", pce);
		}catch(SAXException se) {
        	throw new FDResourceException("parseFeedFileContent", se);
		}catch(IOException ioe) {
        	throw new FDResourceException("parseFeedFileContent", ioe);
		} catch (DOMException de) {
        	throw new FDResourceException("parseFeedFileContent", de);
		}
	}
	
	private String getElementText(Element root, String tagName){
		NodeList nl=root.getElementsByTagName(tagName);
		if (nl.getLength()>0) { 
			return nl.item(0).getTextContent();
		} else {
			return null;
		}
	}
	
	private Boolean getElementBoolean(Element root, String tagName){
		String value = getElementText(root, tagName);
		if (value!=null){
			return Boolean.parseBoolean(value);
		}
		return null;
	}

	private Integer getElementInteger(Element root, String tagName){
		String value = getElementText(root, tagName);
		if (value!=null){
			try {
				return Integer.parseInt(value);

			} catch (NumberFormatException e){
				LOGGER.error(e);
			}
		}
		return null;
	}

	private BigDecimal getElementBigDecimal(Element root, String tagName){
		String value = getElementText(root, tagName);
		if (value!=null){
			try {
				return new BigDecimal(value);

			} catch (NumberFormatException e){
				LOGGER.error(e);
			}
		}
		return null;
	}

	
	private Long getElementDate(Element root, String tagName){
		String value = getElementText(root, tagName);
		
		if (value!=null){
			try {
				value = value.substring(0,DF_LEN);
				return sdf.parse(value).getTime();
			
			} catch (ParseException e) {
				LOGGER.error(e);
			} catch (IndexOutOfBoundsException e){
				LOGGER.error(e);
			}
		}
		return null;
	}

	
	//Safe generic way to get nullable elements
	private <T> Object getValue(T clazz, Element node, String nodeName) {
		if (node==null){
			return null;
		}
		if (clazz == String.class) {
			try {
				return node.getElementsByTagName(nodeName).item(0).getTextContent();
			} catch (Exception e) {
				return "";
			}
		
		} else if (clazz == Boolean.class) {
			try {
				return new Boolean(node.getElementsByTagName(nodeName).item(0).getTextContent());
			} catch (Exception e) {
				return new Boolean("false");
			}
		} else if (clazz == BigDecimal.class) {
			try {
				return new BigDecimal(node.getElementsByTagName(nodeName).item(0).getTextContent());
			} catch (Exception e) {
				return BigDecimal.ZERO;
			}
		} else if (clazz == Integer.class) {
			try {
				return new Integer(node.getElementsByTagName(nodeName).item(0).getTextContent());
			} catch (Exception e) {
				return new Integer(0);
			}
		}
		return null;
	}

}
