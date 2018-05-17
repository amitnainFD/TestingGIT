package com.freshdirect.fdstore.content.customerrating;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class CustomerRatingsDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1068945251536494464L;
	private String id;
	private String productId;
	private String productPageURL;
	private String productReviewsURL;
	private String imageURL;
	private Integer numNativeQuestions;
	private Integer numQuestions;
	private Integer numNativeAnswers;
	private Integer numAnswers;
	private Integer numReviews;
	private Integer numStories;
	private BigDecimal averageOverallRating;
	private Integer overallRatingRange;
	private Integer totalReviewCount;
	private Integer ratingsOnlyReviewCount;
	private Integer recommendedCount;
	private Integer notRecommendedCount;
	private BigDecimal averageRatingValuesQuality;
	private Integer averageRatingValuesQualityRange;
	private BigDecimal averageRatingValuesValue;
	private Integer averageRatingValuesValueRange;
	private Integer ratingValue;
	private Integer count;
	private BigDecimal natAverageOverallRating;
	private Integer natOverallRatingRange;
	private Integer natTotalReviewCount;
	private Integer natRatingsOnlyReviewCount;
	private Integer natRecommendedCount;
	private Integer natNotRecommendedCount;
	private BigDecimal natAverageRatingValuesQuality;
	private Integer natAverageRatingValuesQualityRange;
	private BigDecimal natAverageRatingValuesValue;
	private Integer natAverageRatingValuesValueRange;
	private BigDecimal natAverageRatingValues;
	private Integer natRatingValue;
	private Integer natCount;
	private Long extractDate;
	private List<CustomerReviewsDTO> reviews = new ArrayList<CustomerReviewsDTO>();
	
//	public CustomerRatingsDTO() {};
	
//	public CustomerRatingsDTO(String id, String productId, BigDecimal averageOverallRating, Integer totalReviewCount, Integer ratingValue) {
//		
//		this.id = id;
//		this.productId = productId;
//		this.averageOverallRating = averageOverallRating;
//		this.totalReviewCount = totalReviewCount;
//		this.ratingValue = ratingValue;
//	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public BigDecimal getAverageOverallRating() {
		return averageOverallRating;
	}

	public void setAverageOverallRating(BigDecimal averageOverallRating) {
		this.averageOverallRating = averageOverallRating;
	}

	public Integer getTotalReviewCount() {
		return totalReviewCount;
	}

	public void setTotalReviewCount(Integer totalReviewCount) {
		this.totalReviewCount = totalReviewCount;
	}

	public Integer getRatingValue() {
		return ratingValue;
	}

	public void setRatingValue(Integer ratingValue) {
		this.ratingValue = ratingValue;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getProductPageURL() {
		return productPageURL;
	}

	public void setProductPageURL(String productPageURL) {
		this.productPageURL = productPageURL;
	}

	public String getProductReviewsURL() {
		return productReviewsURL;
	}

	public void setProductReviewsURL(String productReviewsURL) {
		this.productReviewsURL = productReviewsURL;
	}

	public String getImageURL() {
		return imageURL;
	}

	public void setImageURL(String imageURL) {
		this.imageURL = imageURL;
	}

	public Integer getOverallRatingRange() {
		return overallRatingRange;
	}

	public void setOverallRatingRange(Integer overallRatingRange) {
		this.overallRatingRange = overallRatingRange;
	}

	public Integer getRatingsOnlyReviewCount() {
		return ratingsOnlyReviewCount;
	}

	public void setRatingsOnlyReviewCount(Integer ratingsOnlyReviewCount) {
		this.ratingsOnlyReviewCount = ratingsOnlyReviewCount;
	}

	public Integer getRecommendedCount() {
		return recommendedCount;
	}

	public void setRecommendedCount(Integer recommendedCount) {
		this.recommendedCount = recommendedCount;
	}

	public Integer getNotRecommendedCount() {
		return notRecommendedCount;
	}

	public void setNotRecommendedCount(Integer notRecommendedCount) {
		this.notRecommendedCount = notRecommendedCount;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public BigDecimal getNatAverageOverallRating() {
		return natAverageOverallRating;
	}

	public void setNatAverageOverallRating(BigDecimal natAverageOverallRating) {
		this.natAverageOverallRating = natAverageOverallRating;
	}

	public Integer getNatOverallRatingRange() {
		return natOverallRatingRange;
	}

	public void setNatOverallRatingRange(Integer natOverallRatingRange) {
		this.natOverallRatingRange = natOverallRatingRange;
	}

	public Integer getNatTotalReviewCount() {
		return natTotalReviewCount;
	}

	public void setNatTotalReviewCount(Integer natTotalReviewCount) {
		this.natTotalReviewCount = natTotalReviewCount;
	}

	public Integer getNatRatingsOnlyReviewCount() {
		return natRatingsOnlyReviewCount;
	}

	public void setNatRatingsOnlyReviewCount(Integer natRatingsOnlyReviewCount) {
		this.natRatingsOnlyReviewCount = natRatingsOnlyReviewCount;
	}

	public Integer getNatRecommendedCount() {
		return natRecommendedCount;
	}

	public void setNatRecommendedCount(Integer natRecommendedCount) {
		this.natRecommendedCount = natRecommendedCount;
	}

	public Integer getNatNotRecommendedCount() {
		return natNotRecommendedCount;
	}

	public void setNatNotRecommendedCount(Integer natNotRecommendedCount) {
		this.natNotRecommendedCount = natNotRecommendedCount;
	}

	public BigDecimal getNatAverageRatingValues() {
		return natAverageRatingValues;
	}

	public void setNatAverageRatingValues(BigDecimal natAverageRatingValues) {
		this.natAverageRatingValues = natAverageRatingValues;
	}

	public Integer getNatRatingValue() {
		return natRatingValue;
	}

	public void setNatRatingValue(Integer natRatingValue) {
		this.natRatingValue = natRatingValue;
	}

	public Integer getNatCount() {
		return natCount;
	}

	public void setNatCount(Integer natCount) {
		this.natCount = natCount;
	}

	public Long getExtractDate() {
		return extractDate;
	}

	public void setExtractDate(Long extractDate) {
		this.extractDate = extractDate;
	}

	public List<CustomerReviewsDTO> getReviews() {
		return reviews;
	}

	public void setReviews(List<CustomerReviewsDTO> reviews) {
		this.reviews = reviews;
	}
	
	public void addReview(CustomerReviewsDTO review) {
		this.reviews.add(review);
	}

	public Integer getNumNativeQuestions() {
		return numNativeQuestions;
	}

	public void setNumNativeQuestions(Integer numNativeQuestions) {
		this.numNativeQuestions = numNativeQuestions;
	}

	public Integer getNumQuestions() {
		return numQuestions;
	}

	public void setNumQuestions(Integer numQuestions) {
		this.numQuestions = numQuestions;
	}

	public Integer getNumNativeAnswers() {
		return numNativeAnswers;
	}

	public void setNumNativeAnswers(Integer numNativeAnswers) {
		this.numNativeAnswers = numNativeAnswers;
	}

	public Integer getNumAnswers() {
		return numAnswers;
	}

	public void setNumAnswers(Integer numAnswers) {
		this.numAnswers = numAnswers;
	}

	public Integer getNumReviews() {
		return numReviews;
	}

	public void setNumReviews(Integer numReviews) {
		this.numReviews = numReviews;
	}

	public Integer getNumStories() {
		return numStories;
	}

	public void setNumStories(Integer numStories) {
		this.numStories = numStories;
	}

	public BigDecimal getAverageRatingValuesQuality() {
		return averageRatingValuesQuality;
	}

	public void setAverageRatingValuesQuality(BigDecimal averageRatingValuesQuality) {
		this.averageRatingValuesQuality = averageRatingValuesQuality;
	}

	public Integer getAverageRatingValuesQualityRange() {
		return averageRatingValuesQualityRange;
	}

	public void setAverageRatingValuesQualityRange(
			Integer averageRatingValuesQualityRange) {
		this.averageRatingValuesQualityRange = averageRatingValuesQualityRange;
	}

	public BigDecimal getAverageRatingValuesValue() {
		return averageRatingValuesValue;
	}

	public void setAverageRatingValuesValue(BigDecimal averageRatingValuesValue) {
		this.averageRatingValuesValue = averageRatingValuesValue;
	}

	public Integer getAverageRatingValuesValueRange() {
		return averageRatingValuesValueRange;
	}

	public void setAverageRatingValuesValueRange(Integer averageRatingValuesValueRange) {
		this.averageRatingValuesValueRange = averageRatingValuesValueRange;
	}

	public BigDecimal getNatAverageRatingValuesQuality() {
		return natAverageRatingValuesQuality;
	}

	public void setNatAverageRatingValuesQuality(
			BigDecimal natAverageRatingValuesQuality) {
		this.natAverageRatingValuesQuality = natAverageRatingValuesQuality;
	}

	public Integer getNatAverageRatingValuesQualityRange() {
		return natAverageRatingValuesQualityRange;
	}

	public void setNatAverageRatingValuesQualityRange(
			Integer natAverageRatingValuesQualityRange) {
		this.natAverageRatingValuesQualityRange = natAverageRatingValuesQualityRange;
	}

	public BigDecimal getNatAverageRatingValuesValue() {
		return natAverageRatingValuesValue;
	}

	public void setNatAverageRatingValuesValue(
			BigDecimal natAverageRatingValuesValue) {
		this.natAverageRatingValuesValue = natAverageRatingValuesValue;
	}

	public Integer getNatAverageRatingValuesValueRange() {
		return natAverageRatingValuesValueRange;
	}

	public void setNatAverageRatingValuesValueRange(
			Integer natAverageRatingValuesValueRange) {
		this.natAverageRatingValuesValueRange = natAverageRatingValuesValueRange;
	}
}
