package com.freshdirect.fdstore.survey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.freshdirect.common.customer.EnumServiceType;

public class BuiltinSurveys {

    Map<EnumSurveyType, FDSurvey> defaults = new HashMap<EnumSurveyType, FDSurvey>();
    Map<SurveyKey, FDSurvey> override = new HashMap<SurveyKey, FDSurvey>();

    
    BuiltinSurveys() {
        List<FDSurveyAnswer> radioOptions_yesNo = new ArrayList<FDSurveyAnswer>();
        radioOptions_yesNo.add(new FDSurveyAnswer("Yes", "Yes"));
        radioOptions_yesNo.add(new FDSurveyAnswer("No", "No"));


        List<FDSurveyAnswer> radioOptions_household = new ArrayList<FDSurveyAnswer>();
        radioOptions_household.add(new FDSurveyAnswer("Single", "Single"));
        radioOptions_household.add(new FDSurveyAnswer("Couple", "Couple"));
        radioOptions_household.add(new FDSurveyAnswer("Family with Children", "Family with Children"));
        radioOptions_household.add(new FDSurveyAnswer("Roommate(s)", "Roommate(s)"));
        radioOptions_household.add(new FDSurveyAnswer("Office", "Office"));
        radioOptions_household.add(new FDSurveyAnswer("Caterer/Person Chef", "Caterer/Person Chef"));


        List<FDSurveyAnswer> radioOptions_gender = new ArrayList<FDSurveyAnswer>();
        radioOptions_gender.add(new FDSurveyAnswer("Male", "Male"));
        radioOptions_gender.add(new FDSurveyAnswer("Female", "Female"));


        
        List<FDSurveyAnswer> radioOptions_shopFor = new ArrayList<FDSurveyAnswer>();
        radioOptions_shopFor.add(new FDSurveyAnswer("1", "1"));
        radioOptions_shopFor.add(new FDSurveyAnswer("2", "2"));
        radioOptions_shopFor.add(new FDSurveyAnswer("3", "3"));
        radioOptions_shopFor.add(new FDSurveyAnswer("4", "4"));
        radioOptions_shopFor.add(new FDSurveyAnswer("5+", "5+"));
        
        {
            
            FDSurvey organic2 = createOrganic2Survey(radioOptions_yesNo, radioOptions_household, 
                    radioOptions_gender);

            addSurvey(organic2);

        }
        {

            FDSurvey postOrder = createPostOrderSurvey();

            addSurvey(postOrder);
        }
        {

            FDSurvey postOrderDetail = createPostOrderDetailSurvey(radioOptions_yesNo);

            addSurvey(postOrderDetail);
        }
        {
            FDSurvey cos_feedback = createCosFeedbackSurvey();

            addSurvey(cos_feedback);
        }

        List<FDSurveyAnswer> radioOptions_rating = new ArrayList<FDSurveyAnswer>();
        radioOptions_rating.add(new FDSurveyAnswer("1", "1"));
        radioOptions_rating.add(new FDSurveyAnswer("2", "2"));
        radioOptions_rating.add(new FDSurveyAnswer("3", "3"));
        radioOptions_rating.add(new FDSurveyAnswer("4", "4"));
        radioOptions_rating.add(new FDSurveyAnswer("5", "5"));
        
        {
            FDSurvey hamptons05 = createHamptonsSurvey(radioOptions_rating);

            addSurvey(hamptons05);
        }
        {
            FDSurvey morningDelivery = createMorningDeliverySurvey(radioOptions_rating);

            addSurvey(morningDelivery);
        }
        {
            FDSurvey usability = createUsabilitySurvey(radioOptions_yesNo, radioOptions_rating);

            addSurvey(usability);
        }
        {
            FDSurvey dietNutrition = createDietNutritionSurvey(radioOptions_rating);

            addSurvey(dietNutrition);
        }
        {
            //FDSurvey rcptPageSurvey2 = createReceiptPageSurvey(radioOptions_household, radioOptions_gender, radioOptions_shopFor);

            //addSurvey(rcptPageSurvey2);
        }
        {
            FDSurvey prodReqFeedback = createWineRequestFeedbackSurvey();

            addSurvey(prodReqFeedback);
        }

        // this is for testing purposes
//        addOverrideSurvey(createCorporateProfileSurvey());
//        addOverrideSurvey(createCorporateSignup());
//        addOverrideSurvey(createCorporateSecondOrderSurvey());
//        
    }

    private FDSurvey createWineRequestFeedbackSurvey() {
        // ****** Wine Request SURVEY ******//
        FDSurvey prodReqFeedback = new FDSurvey(EnumSurveyType.WINE_REQUEST);

        List<FDSurveyAnswer> radioOptions_prodReqFeedback = new ArrayList<FDSurveyAnswer>();
        radioOptions_prodReqFeedback.add(new FDSurveyAnswer("1", "Excellent"));
        radioOptions_prodReqFeedback.add(new FDSurveyAnswer("2", ""));
        radioOptions_prodReqFeedback.add(new FDSurveyAnswer("3", "Good"));
        radioOptions_prodReqFeedback.add(new FDSurveyAnswer("4", ""));
        radioOptions_prodReqFeedback.add(new FDSurveyAnswer("5", "Poor"));

        FDSurveyQuestion fbkQ0 = new FDSurveyQuestion("Feedback_Overall", "Overall shopping experience", false, false, false, true);
        fbkQ0.setAnswers(radioOptions_prodReqFeedback);
        prodReqFeedback.addQuestion(fbkQ0);

        FDSurveyQuestion fbkQ1 = new FDSurveyQuestion("Feedback_ProductSelection", "Product Selection", false, false, false, true);
        fbkQ1.setAnswers(radioOptions_prodReqFeedback);
        prodReqFeedback.addQuestion(fbkQ1);

        FDSurveyQuestion fbkQ2 = new FDSurveyQuestion("Feedback_WineQuality", "Quality of wines you've tried", false, false, false, true);
        fbkQ2.setAnswers(radioOptions_prodReqFeedback);
        prodReqFeedback.addQuestion(fbkQ2);

        FDSurveyQuestion fbkQ3 = new FDSurveyQuestion("Feedback_WineValue", "Value for the price", false, false, false, true);
        fbkQ3.setAnswers(radioOptions_prodReqFeedback);
        prodReqFeedback.addQuestion(fbkQ3);
        return prodReqFeedback;
    }

    private FDSurvey createReceiptPageSurvey(List<FDSurveyAnswer> radioOptions_household, List<FDSurveyAnswer> radioOptions_gender, List<FDSurveyAnswer> radioOptions_shopFor) {
        List<FDSurveyAnswer> radioOptions_recommendRating = new ArrayList<FDSurveyAnswer>();
        radioOptions_recommendRating.add(new FDSurveyAnswer("1", "1"));
        radioOptions_recommendRating.add(new FDSurveyAnswer("2", "2"));
        radioOptions_recommendRating.add(new FDSurveyAnswer("3", "3"));
        radioOptions_recommendRating.add(new FDSurveyAnswer("4", "4"));
        radioOptions_recommendRating.add(new FDSurveyAnswer("5", "5"));
        
        FDSurvey rcptPageSurvey2 = new FDSurvey("ReceiptPageSurvey2");

        FDSurveyQuestion rpsQ1 = new FDSurveyQuestion("RcptPageSurveyShopFor", "When you shop for food, how many people do you typically shop for?", true,
                false);
        rpsQ1.setAnswers(radioOptions_shopFor);
        rcptPageSurvey2.addQuestion(rpsQ1);

        FDSurveyQuestion rpsQ2 = new FDSurveyQuestion("RcptPageSurveyHousehold", "Which best describes your household?", false, false);
        rpsQ2.setShowOptionalText(false);
        rpsQ2.setAnswers(radioOptions_household);
        rcptPageSurvey2.addQuestion(rpsQ2);

        FDSurveyQuestion rpsQ3 = new FDSurveyQuestion("RcptPageSurveyCuisines", "Which of the following are your favorite cuisines?", false, true);
        rpsQ3.setShowOptionalText(false);
        rpsQ3.addAnswer(new FDSurveyAnswer("Italian", "Italian"));
        rpsQ3.addAnswer(new FDSurveyAnswer("French", "French"));
        rpsQ3.addAnswer(new FDSurveyAnswer("Comfort Food", "Comfort Food"));
        rpsQ3.addAnswer(new FDSurveyAnswer("Asian", "Asian"));
        rpsQ3.addAnswer(new FDSurveyAnswer("Caribbean", "Caribbean"));
        rpsQ3.addAnswer(new FDSurveyAnswer("Mexican", "Mexican"));
        rpsQ3.addAnswer(new FDSurveyAnswer("Mediterranean", "Mediterranean"));
        rpsQ3.addAnswer(new FDSurveyAnswer("Other", "Other"));
        rcptPageSurvey2.addQuestion(rpsQ3);

        FDSurveyQuestion rpsQ3a = new FDSurveyQuestion("RcptPageSurveyCuisinesOther", "Other", false, false, true);
        rcptPageSurvey2.addQuestion(rpsQ3a);

        FDSurveyQuestion rpsQ4 = new FDSurveyQuestion("RcptPageSurveyDiet",
                "When you shop for food, what specific nutrition/dietary factors are important to you?", false, true);
        rpsQ4.setShowOptionalText(false);
        rpsQ4.addAnswer(new FDSurveyAnswer("Vegetarian", "Vegetarian"));
        rpsQ4.addAnswer(new FDSurveyAnswer("Kosher", "Kosher"));
        rpsQ4.addAnswer(new FDSurveyAnswer("Organic", "Organic"));
        rpsQ4.addAnswer(new FDSurveyAnswer("No special preferences", "No special preferences"));
        rpsQ4.addAnswer(new FDSurveyAnswer("Other", "Other"));
        rcptPageSurvey2.addQuestion(rpsQ4);

        FDSurveyQuestion rpsQ4a = new FDSurveyQuestion("RcptPageSurveyDietOther", "Other", false, false, true);
        rcptPageSurvey2.addQuestion(rpsQ4a);

        FDSurveyQuestion rpsQ5 = new FDSurveyQuestion("RcptPageSurveyGender", "What is your gender?", false, false);
        rpsQ5.setShowOptionalText(false);
        rpsQ5.setAnswers(radioOptions_gender);
        rcptPageSurvey2.addQuestion(rpsQ5);

        FDSurveyQuestion rpsQ6 = new FDSurveyQuestion("RcptPageSurveyRecommend", "Would you recommend FreshDirect to a friend?", false, false);
        rpsQ6.setShowOptionalText(false);
        rpsQ6.setAnswers(radioOptions_recommendRating);
        rcptPageSurvey2.addQuestion(rpsQ6);

        FDSurveyQuestion rpsQ7 = new FDSurveyQuestion("RcptPageSurveyComments", "Additional comments about FreshDirect", false, false, true);
        rpsQ7.setShowOptionalText(false);
        rcptPageSurvey2.addQuestion(rpsQ7);

        FDSurveyQuestion rpsCOS1 = new FDSurveyQuestion("RcptPageSurveyCOS1", "Company Name:", false, false, true);
        rcptPageSurvey2.addQuestion(rpsCOS1);

        FDSurveyQuestion rpsCOS2 = new FDSurveyQuestion("RcptPageSurveyCOS2", "Person ordering food:", false, false, true);
        rcptPageSurvey2.addQuestion(rpsCOS2);
        return rcptPageSurvey2;
    }

    private FDSurvey createDietNutritionSurvey(List<FDSurveyAnswer> radioOptions_rating) {
        // ****** DIET & NUTRITION SURVEY ******//
        FDSurvey dietNutrition = new FDSurvey(EnumSurveyType.DIET_NUTRITION);

        FDSurveyQuestion dnQ0 = new FDSurveyQuestion("DietNutritionImportance",
                "When you shop for food, how important are considerations of nutrition and diet?", true, false, false, true);
        dnQ0.setAnswers(radioOptions_rating);
        dietNutrition.addQuestion(dnQ0);

        FDSurveyQuestion dnQ1 = new FDSurveyQuestion("DietNutritionSpecific",
                "When you shop for food, what specific nutritional factors are important to you? (please choose all that apply)", true, true);
        dnQ1.addAnswer(new FDSurveyAnswer("high fiber", "high fiber"));
        dnQ1.addAnswer(new FDSurveyAnswer("low calorie", "low calorie"));
        dnQ1.addAnswer(new FDSurveyAnswer("low carb", "low carb"));
        dnQ1.addAnswer(new FDSurveyAnswer("low cholesterol", "low cholesterol"));
        dnQ1.addAnswer(new FDSurveyAnswer("low fat", "low fat"));
        dnQ1.addAnswer(new FDSurveyAnswer("fat free", "fat free"));
        dnQ1.addAnswer(new FDSurveyAnswer("low sodium", "low sodium"));
        dnQ1.addAnswer(new FDSurveyAnswer("MSG-free", "MSG-free"));
        dnQ1.addAnswer(new FDSurveyAnswer("sugar free", "sugar free"));
        dnQ1.addAnswer(new FDSurveyAnswer("trans-fat free", "trans-fat free"));
        dnQ1.addAnswer(new FDSurveyAnswer("whole grains", "whole grains"));
        dnQ1.addAnswer(new FDSurveyAnswer("no special preferences", "no special preferences"));
        dnQ1.addAnswer(new FDSurveyAnswer("other", "other"));
        dietNutrition.addQuestion(dnQ1);

        FDSurveyQuestion dnQ1a = new FDSurveyQuestion("DietNutritionSpecificOther", "Other", false, false, true);
        dietNutrition.addQuestion(dnQ1a);

        FDSurveyQuestion dnQ2 = new FDSurveyQuestion("DietNutritionShop",
                "When you shop for food, how significant a factor is weight maintenance or weight loss?", true, false, false, true);
        dnQ2.setAnswers(radioOptions_rating);
        dietNutrition.addQuestion(dnQ2);

        FDSurveyQuestion dnQ3 = new FDSurveyQuestion(
                "DietNutritionWeight",
                "What best describes your strategy when you try to maintain a healthy weight, or lose weight? <span style=\"\">(choose all that apply)</span>",
                true, true);
        dnQ3.addAnswer(new FDSurveyAnswer("South Beach", "South Beach"));
        dnQ3.addAnswer(new FDSurveyAnswer("Atkins", "Atkins"));
        dnQ3.addAnswer(new FDSurveyAnswer("Jenny Craig", "Jenny Craig"));
        dnQ3.addAnswer(new FDSurveyAnswer("Zone", "Zone"));
        dnQ3.addAnswer(new FDSurveyAnswer("Pritikin", "Pritikin"));
        dnQ3.addAnswer(new FDSurveyAnswer("Weight Watchers", "Weight Watchers"));
        dnQ3.addAnswer(new FDSurveyAnswer("I count calories", "I count calories"));
        dnQ3.addAnswer(new FDSurveyAnswer("I eat smaller portions", "I eat smaller portions"));
        dnQ3.addAnswer(new FDSurveyAnswer("I dine out less often", "I dine out less often"));
        dnQ3.addAnswer(new FDSurveyAnswer("I choose whole grains, fruits, vegetables, seafood, and limited lean meats",
                "I choose whole grains, fruits, vegetables, seafood, and limited lean meats"));
        dnQ3.addAnswer(new FDSurveyAnswer("I avoid fats and fried foods", "I avoid fats and fried foods"));
        dnQ3.addAnswer(new FDSurveyAnswer("I exercise", "I exercise"));
        dnQ3.addAnswer(new FDSurveyAnswer("does not apply", "does not apply"));
        dnQ3.addAnswer(new FDSurveyAnswer("other", "other"));
        dietNutrition.addQuestion(dnQ3);

        FDSurveyQuestion dnQ3a = new FDSurveyQuestion("DietNutritionWeightOther", "Other", false, false, true);
        dietNutrition.addQuestion(dnQ3a);

        FDSurveyQuestion dnQ4 = new FDSurveyQuestion("DietNutritionVegetarian", "Is your household predominantly vegetarian? (please choose one)", true,
                false);
        dnQ4.addAnswer(new FDSurveyAnswer("vegan (no animal products)", "vegan (no animal products)"));
        dnQ4.addAnswer(new FDSurveyAnswer("vegetarian including dairy and eggs", "vegetarian including dairy and eggs"));
        dnQ4.addAnswer(new FDSurveyAnswer("vegetarian including dairy, eggs, and seafood and/or poultry",
                "vegetarian including dairy, eggs, and seafood and/or poultry"));
        dnQ4.addAnswer(new FDSurveyAnswer("does not apply", "does not apply"));
        dnQ4.addAnswer(new FDSurveyAnswer("other", "other"));
        dietNutrition.addQuestion(dnQ4);

        FDSurveyQuestion dnQ4a = new FDSurveyQuestion("DietNutritionVegetarianOther", "Other", false, false, true);
        dietNutrition.addQuestion(dnQ4a);

        FDSurveyQuestion dnQ5 = new FDSurveyQuestion("DietNutritionAllergy", "Does anyone in your household have food allergies? (choose all that apply)",
                true, true);
        dnQ5.addAnswer(new FDSurveyAnswer("milk", "milk"));
        dnQ5.addAnswer(new FDSurveyAnswer("egg", "egg"));
        dnQ5.addAnswer(new FDSurveyAnswer("peanut", "peanut"));
        dnQ5.addAnswer(new FDSurveyAnswer("tree nut (walnut, cashew, etc.)", "tree nut (walnut, cashew, etc.)"));
        dnQ5.addAnswer(new FDSurveyAnswer("fish", "fish"));
        dnQ5.addAnswer(new FDSurveyAnswer("shellfish", "shellfish"));
        dnQ5.addAnswer(new FDSurveyAnswer("soy", "soy"));
        dnQ5.addAnswer(new FDSurveyAnswer("wheat", "wheat"));
        dnQ5.addAnswer(new FDSurveyAnswer("does not apply", "does not apply"));
        dnQ5.addAnswer(new FDSurveyAnswer("other", "other"));
        dietNutrition.addQuestion(dnQ5);

        FDSurveyQuestion dnQ5a = new FDSurveyQuestion("DietNutritionAllergyOther", "Other", false, false, true);
        dietNutrition.addQuestion(dnQ5a);
        return dietNutrition;
    }

    private FDSurvey createCosFeedbackSurvey() {
        // ****** COS FEEDBACK SURVEY ******//
        FDSurvey cos_feedback = new FDSurvey(EnumSurveyType.COS_FEEDBACK_SURVEY);

        List<FDSurveyAnswer> radioOptions_satisfaction = new ArrayList<FDSurveyAnswer>();
        radioOptions_satisfaction.add(new FDSurveyAnswer("Not satisfied at all", "Not satisfied at all"));
        radioOptions_satisfaction.add(new FDSurveyAnswer("Somewhat dissatisfied", "Somewhat dissatisfied"));
        radioOptions_satisfaction.add(new FDSurveyAnswer("Satisfied", "Satisfied"));
        radioOptions_satisfaction.add(new FDSurveyAnswer("Very satisfied", "Very satisfied"));
        radioOptions_satisfaction.add(new FDSurveyAnswer("Extremely satisfied", "Extremely satisfied"));

        List<FDSurveyAnswer> radioOptions_preference = new ArrayList<FDSurveyAnswer>();
        radioOptions_preference.add(new FDSurveyAnswer("Preferred", "Preferred"));
        radioOptions_preference.add(new FDSurveyAnswer("Acceptable", "Acceptable"));
        radioOptions_preference.add(new FDSurveyAnswer("Unacceptable", "Unacceptable"));

        List<FDSurveyAnswer> radioOptions_percentage = new ArrayList<FDSurveyAnswer>();
        radioOptions_percentage.add(new FDSurveyAnswer("1-24%", "1-24%"));
        radioOptions_percentage.add(new FDSurveyAnswer("25-49%", "25-49%"));
        radioOptions_percentage.add(new FDSurveyAnswer("50-74%", "50-74%"));
        radioOptions_percentage.add(new FDSurveyAnswer("75-100%", "75-100%"));

        List<FDSurveyAnswer> radioOptions_performance = new ArrayList<FDSurveyAnswer>();
        radioOptions_performance.add(new FDSurveyAnswer("Much worse", "Much worse"));
        radioOptions_performance.add(new FDSurveyAnswer("Somewhat worse", "Somewhat worse"));
        radioOptions_performance.add(new FDSurveyAnswer("Same", "Same"));
        radioOptions_performance.add(new FDSurveyAnswer("Better", "Better"));
        radioOptions_performance.add(new FDSurveyAnswer("Much better", "Much better"));

        List<FDSurveyAnswer> radioOptions_industry = new ArrayList<FDSurveyAnswer>();
        radioOptions_industry.add(new FDSurveyAnswer("Accounting", "Accounting"));
        radioOptions_industry.add(new FDSurveyAnswer("Arts/Entertainment", "Arts/Entertainment"));
        radioOptions_industry.add(new FDSurveyAnswer("Attorney/Legal Services", "Attorney/Legal Services"));
        radioOptions_industry.add(new FDSurveyAnswer("Banking", "Banking"));
        radioOptions_industry.add(new FDSurveyAnswer("Construction", "Construction"));
        radioOptions_industry.add(new FDSurveyAnswer("Education", "Education"));
        radioOptions_industry.add(new FDSurveyAnswer("Hotels", "Hotels"));
        radioOptions_industry.add(new FDSurveyAnswer("Insurance", "Insurance"));
        radioOptions_industry.add(new FDSurveyAnswer("Investment Services", "Investment Services"));
        radioOptions_industry.add(new FDSurveyAnswer("Manufacturing", "Manufacturing"));
        radioOptions_industry.add(new FDSurveyAnswer("Public Relations/Ad Agency", "Public Relations/Ad Agency"));
        radioOptions_industry.add(new FDSurveyAnswer("Real Estate", "Real Estate"));
        radioOptions_industry.add(new FDSurveyAnswer("Retail", "Retail"));
        radioOptions_industry.add(new FDSurveyAnswer("Technology", "Technology"));
        radioOptions_industry.add(new FDSurveyAnswer("Telecommunications", "Telecommunications"));
        radioOptions_industry.add(new FDSurveyAnswer("Transportation", "Transportation"));
        radioOptions_industry.add(new FDSurveyAnswer("Other", "Other"));

        FDSurveyQuestion cosfQ0 = new FDSurveyQuestion("q1_satisfaction_ques",
                "Please let us know your satisfaction with the following areas of FreshDirect:", false, false);
        cos_feedback.addQuestion(cosfQ0);

        FDSurveyQuestion cosfQ1 = new FDSurveyQuestion("q1_product_quality_hdr", "Product Quality", false, false);
        cos_feedback.addQuestion(cosfQ1);

        FDSurveyQuestion cosfQ1_1 = new FDSurveyQuestion("q1_product_quality_platters", "Platters", true, false);
        cosfQ1_1.setAnswers(radioOptions_satisfaction);
        cos_feedback.addQuestion(cosfQ1_1);

        FDSurveyQuestion cosfQ1_2 = new FDSurveyQuestion("q1_product_quality_produce", "Produce", true, false);
        cosfQ1_2.setAnswers(radioOptions_satisfaction);
        cos_feedback.addQuestion(cosfQ1_2);

        FDSurveyQuestion cosfQ1_3 = new FDSurveyQuestion("q1_product_quality_general", "General Products", true, false);
        cosfQ1_3.setAnswers(radioOptions_satisfaction);
        cos_feedback.addQuestion(cosfQ1_3);

        FDSurveyQuestion cosfQ1_4 = new FDSurveyQuestion("q1_product_quality_additional", "Additional Comments", false, false, true);
        cos_feedback.addQuestion(cosfQ1_4);

        FDSurveyQuestion cosfQ2 = new FDSurveyQuestion("q1_delivery_service_hdr", "Delivery Service", false, false);
        cos_feedback.addQuestion(cosfQ2);

        FDSurveyQuestion cosfQ2_1 = new FDSurveyQuestion("q1_delivery_service_timeslots", "Choice of Time Slots", true, false);
        cosfQ2_1.setAnswers(radioOptions_satisfaction);
        cos_feedback.addQuestion(cosfQ2_1);

        FDSurveyQuestion cosfQ2_2 = new FDSurveyQuestion("q1_delivery_service_quality", "Quality of Service", true, false);
        cosfQ2_2.setAnswers(radioOptions_satisfaction);
        cos_feedback.addQuestion(cosfQ2_2);

        FDSurveyQuestion cosfQ2_3 = new FDSurveyQuestion("q1_delivery_service_additional", "Additional Comments", false, false, true);
        cos_feedback.addQuestion(cosfQ2_3);

        FDSurveyQuestion cosfQ3 = new FDSurveyQuestion("q1_website_hdr", "Web Site", false, false);
        cos_feedback.addQuestion(cosfQ3);

        FDSurveyQuestion cosfQ3_1 = new FDSurveyQuestion("q1_website_ease_of_use", "Ease of use", true, false);
        cosfQ3_1.setAnswers(radioOptions_satisfaction);
        cos_feedback.addQuestion(cosfQ3_1);

        FDSurveyQuestion cosfQ3_2 = new FDSurveyQuestion("q1_website_information", "Information", true, false);
        cosfQ3_2.setAnswers(radioOptions_satisfaction);
        cos_feedback.addQuestion(cosfQ3_2);

        FDSurveyQuestion cosfQ3_3 = new FDSurveyQuestion("q1_website_speed", "Speed", true, false);
        cosfQ3_3.setAnswers(radioOptions_satisfaction);
        cos_feedback.addQuestion(cosfQ3_3);

        FDSurveyQuestion cosfQ3_4 = new FDSurveyQuestion("q1_website_additional", "Additional Comments", false, false, true);
        cos_feedback.addQuestion(cosfQ3_4);

        FDSurveyQuestion cosfQ4 = new FDSurveyQuestion("q2_payment_method_question", "Rate the following payment options:", false, false);
        cos_feedback.addQuestion(cosfQ4);

        FDSurveyQuestion cosfQ4_1 = new FDSurveyQuestion("q2_payment_method_creditcard", "Credit Card", true, false);
        cosfQ4_1.setAnswers(radioOptions_preference);
        cos_feedback.addQuestion(cosfQ4_1);

        FDSurveyQuestion cosfQ4_2 = new FDSurveyQuestion("q2_payment_method_electroniccheck", "Electronic Check", true, false);
        cosfQ4_2.setAnswers(radioOptions_preference);
        cos_feedback.addQuestion(cosfQ4_2);

        FDSurveyQuestion cosfQ4_3 = new FDSurveyQuestion("q2_payment_method_net30daybilling", "Net 30-day billing", true, false);
        cosfQ4_3.setAnswers(radioOptions_preference);
        cos_feedback.addQuestion(cosfQ4_3);

        FDSurveyQuestion cosfQ5 = new FDSurveyQuestion("q3_weekly_fd_purchase",
                "What % of your weekly corporate food purchases are fulfilled by FreshDirect?", true, false);
        cosfQ5.setAnswers(radioOptions_percentage);
        cos_feedback.addQuestion(cosfQ5);

        FDSurveyQuestion cosfQ6 = new FDSurveyQuestion("q4_non_fd_purchase",
                "Are there any specific products or services for which you are currently not using FreshDirect?", false, false, true);
        cos_feedback.addQuestion(cosfQ6);

        FDSurveyQuestion cosfQ7 = new FDSurveyQuestion("q5_compare_ques", "Compared to your other food vendors, how would you rate FreshDirect?", false,
                false);
        cos_feedback.addQuestion(cosfQ7);

        FDSurveyQuestion cosfQ7_1 = new FDSurveyQuestion("q5_compare_quality", "Quality", true, false);
        cosfQ7_1.setAnswers(radioOptions_performance);
        cos_feedback.addQuestion(cosfQ7_1);

        FDSurveyQuestion cosfQ7_2 = new FDSurveyQuestion("q5_compare_selection", "Selection", true, false);
        cosfQ7_2.setAnswers(radioOptions_performance);
        cos_feedback.addQuestion(cosfQ7_2);

        FDSurveyQuestion cosfQ7_3 = new FDSurveyQuestion("q5_compare_price", "Price", true, false);
        cosfQ7_3.setAnswers(radioOptions_performance);
        cos_feedback.addQuestion(cosfQ7_3);

        FDSurveyQuestion cosfQ7_4 = new FDSurveyQuestion("q5_compare_overall_service", "Overall Service", true, false);
        cosfQ7_4.setAnswers(radioOptions_performance);
        cos_feedback.addQuestion(cosfQ7_4);

        FDSurveyQuestion cosfQ7_5 = new FDSurveyQuestion("q5_compare_additional", "Additional Comments", false, false, true);
        cos_feedback.addQuestion(cosfQ7_5);

        FDSurveyQuestion cosfQ8 = new FDSurveyQuestion("q6_fd_buy_ques", "What percentage of the following do you currently use FreshDirect for?", false,
                false);
        cos_feedback.addQuestion(cosfQ8);

        FDSurveyQuestion cosfQ8_1 = new FDSurveyQuestion("q6_fd_buy_platters", "Breakfast/lunch platters", true, false);
        cosfQ8_1.setAnswers(radioOptions_percentage);
        cos_feedback.addQuestion(cosfQ8_1);

        FDSurveyQuestion cosfQ8_2 = new FDSurveyQuestion("q6_fd_buy_pantry_stock", "Pantry stocking", true, false);
        cosfQ8_2.setAnswers(radioOptions_percentage);
        cos_feedback.addQuestion(cosfQ8_2);

        FDSurveyQuestion cosfQ8_3 = new FDSurveyQuestion("q6_fd_buy_office_food", "Office food products", true, false);
        cosfQ8_3.setAnswers(radioOptions_percentage);
        cos_feedback.addQuestion(cosfQ8_3);

        FDSurveyQuestion cosfQ9 = new FDSurveyQuestion("q8_industry", "Industry", true, false);
        cosfQ9.setAnswers(radioOptions_industry);
        cos_feedback.addQuestion(cosfQ9);

        FDSurveyQuestion cosfQ9_1 = new FDSurveyQuestion("q8_industry_other", "Other Industry", false, false, true);
        cos_feedback.addQuestion(cosfQ9_1);

        FDSurveyQuestion cosfQ10 = new FDSurveyQuestion("q7_recommendation", "What recommendations would you make to improve FreshDirect's service? ",
                false, false, true);
        cos_feedback.addQuestion(cosfQ10);
        return cos_feedback;
    }

    private FDSurvey createPostOrderDetailSurvey(List<FDSurveyAnswer> radioOptions_yesNo) {
        // ******* POST ORDER SURVEY DETAIL *******//

        FDSurvey postOrderDetail = new FDSurvey(EnumSurveyType.POST_ORDER_DETAIL);

        List<FDSurveyAnswer> departments = new ArrayList<FDSurveyAnswer>();
        departments.add(new FDSurveyAnswer("Produce", "Produce"));
        departments.add(new FDSurveyAnswer("Meat", "Meat"));
        departments.add(new FDSurveyAnswer("Seafood", "Seafood"));
        departments.add(new FDSurveyAnswer("Deli & Cheese", "Deli & Cheese"));
        departments.add(new FDSurveyAnswer("Dairy", "Dairy"));
        departments.add(new FDSurveyAnswer("Pasta", "Pasta"));
        departments.add(new FDSurveyAnswer("Coffee & Tea", "Coffee & Tea"));
        departments.add(new FDSurveyAnswer("Bakery & Pastry", "Bakery & Pastry"));
        departments.add(new FDSurveyAnswer("Prepared Meals", "Prepared Meals"));
        departments.add(new FDSurveyAnswer("Grocery & Specialty", "Grocery & Specialty"));
        departments.add(new FDSurveyAnswer("Frozen Foods", "Frozen Foods"));
        departments.add(new FDSurveyAnswer("Wine", "Wine"));

        FDSurveyQuestion podQ0 = new FDSurveyQuestion("q_missing_item_hdr", "Item or box was missing", false, false, false);
        postOrderDetail.addQuestion(podQ0);

        FDSurveyQuestion podQ1 = new FDSurveyQuestion("q_missing_item", "What was missing?", false, true, false);
        podQ1.addAnswer(new FDSurveyAnswer("Entire Box", "Entire Box"));
        podQ1.addAnswer(new FDSurveyAnswer("Freezer Bag", "Freezer Bag"));
        for (int i = 0; i < departments.size(); i++) {
            podQ1.addAnswer((FDSurveyAnswer) departments.get(i));
        }
        postOrderDetail.addQuestion(podQ1);

        FDSurveyQuestion podQ2 = new FDSurveyQuestion("q_missing_item_charge", "Were you charged for the item?", false, false, false);
        podQ2.addAnswer(new FDSurveyAnswer("No, I was not charged.", "No, I was not charged."));
        podQ2.addAnswer(new FDSurveyAnswer("Yes, the missing item appeared on my bill.", "Yes, the missing item appeared on my bill."));
        postOrderDetail.addQuestion(podQ2);

        FDSurveyQuestion podQ3 = new FDSurveyQuestion("q_product_damage", "Product broken or bruised", false, true, false);
        podQ3.addAnswer(new FDSurveyAnswer("Broken Eggs", "Broken Eggs"));
        for (int i = 0; i < departments.size(); i++) {
            podQ3.addAnswer((FDSurveyAnswer) departments.get(i));
        }
        postOrderDetail.addQuestion(podQ3);

        FDSurveyQuestion podQ4 = new FDSurveyQuestion("q_package_damage", "Package damaged or leaking", false, true, false);
        podQ4.addAnswer(new FDSurveyAnswer("Milk or juice leaking", "Milk or juice leaking"));
        for (int i = 0; i < departments.size(); i++) {
            podQ4.addAnswer((FDSurveyAnswer) departments.get(i));
        }
        postOrderDetail.addQuestion(podQ4);

        FDSurveyQuestion podQ5 = new FDSurveyQuestion("q_quality_issue", "Quality dissatisfaction", false, true, false);
        podQ5.addAnswer(new FDSurveyAnswer("Produce - ripeness", "Produce - ripeness"));
        podQ5.addAnswer(new FDSurveyAnswer("Produce - general", "Produce - general"));
        for (int i = 1; i < departments.size(); i++) {
            podQ5.addAnswer((FDSurveyAnswer) departments.get(i));
        }
        postOrderDetail.addQuestion(podQ5);

        FDSurveyQuestion podQ6 = new FDSurveyQuestion("q_quantity_issue_hdr", "Size or cut not as expected", false, false, false);
        postOrderDetail.addQuestion(podQ6);

        FDSurveyQuestion podQ7 = new FDSurveyQuestion("q_quantity_issue_type", "What type of item was affected?", false, true, false);
        podQ7.addAnswer(new FDSurveyAnswer("Produce", "Produce"));
        podQ7.addAnswer(new FDSurveyAnswer("Meat", "Meat"));
        podQ7.addAnswer(new FDSurveyAnswer("Seafood", "Seafood"));
        podQ7.addAnswer(new FDSurveyAnswer("Deli & Cheese", "Deli & Cheese"));
        podQ7.addAnswer(new FDSurveyAnswer("Packaged Goods (Dairy, Grocery, Meals, etc.)", "Packaged Goods (Dairy, Grocery, Meals, etc.)"));
        postOrderDetail.addQuestion(podQ7);

        FDSurveyQuestion podQ8 = new FDSurveyQuestion("q_quantity_issue", "How would you best describe the problem?", false, false, false);
        podQ8.addAnswer(new FDSurveyAnswer("Larger than ordered", "Larger than ordered"));
        podQ8.addAnswer(new FDSurveyAnswer("Smaller than ordered", "Smaller than ordered"));
        podQ8.addAnswer(new FDSurveyAnswer("Sliced too thin", "Sliced too thin"));
        podQ8.addAnswer(new FDSurveyAnswer("Sliced too thick", "Sliced too thick"));
        podQ8.addAnswer(new FDSurveyAnswer("Inconsistent cut", "Inconsistent cut"));
        podQ8.addAnswer(new FDSurveyAnswer("Wrong size delivered", "Wrong size delivered"));
        postOrderDetail.addQuestion(podQ8);

        FDSurveyQuestion podQ9 = new FDSurveyQuestion("q_product_expire", "Product near or past expiration date", false, true, false);
        podQ9.setAnswers(departments);
        postOrderDetail.addQuestion(podQ9);

        FDSurveyQuestion podQ10 = new FDSurveyQuestion("q_wrong_item", "Wrong item delivered", false, true, false);
        podQ10.setAnswers(departments);
        postOrderDetail.addQuestion(podQ10);

        FDSurveyQuestion podQ11 = new FDSurveyQuestion("q_delivery", "Delivery", false, true, false);
        podQ11.addAnswer(new FDSurveyAnswer("Early delivery", "Early delivery"));
        podQ11.addAnswer(new FDSurveyAnswer("Late delivery - I was notified by phone prior", "Late delivery - I was notified by phone prior"));
        podQ11.addAnswer(new FDSurveyAnswer("Late delivery - I was not notified before", "Late delivery - I was not notified before"));
        podQ11.addAnswer(new FDSurveyAnswer("Box was missing", "Box was missing"));
        podQ11.addAnswer(new FDSurveyAnswer("Driver was not courteous or helpful", "Driver was not courteous or helpful"));
        podQ11.addAnswer(new FDSurveyAnswer("Order was left with doorman without authorization", "Order was left with doorman without authorization"));
        postOrderDetail.addQuestion(podQ11);

        FDSurveyQuestion podQ12 = new FDSurveyQuestion("q_delivery_other", "Other (please specify)", false, false, true);
        postOrderDetail.addQuestion(podQ12);

        FDSurveyQuestion podQ13 = new FDSurveyQuestion("q_customer_service", "Customer Service", false, true, false);
        podQ13.addAnswer(new FDSurveyAnswer("Could not get through on phone", "Could not get through on phone"));
        podQ13.addAnswer(new FDSurveyAnswer("Email went un-answered", "Email went un-answered"));
        podQ13.addAnswer(new FDSurveyAnswer("Customer Service agent was not courteous or helpful", "Customer Service agent was not courteous or helpful"));
        postOrderDetail.addQuestion(podQ13);

        FDSurveyQuestion podQ14 = new FDSurveyQuestion("q_customer_service_other", "Other (please specify)", false, false, true);
        postOrderDetail.addQuestion(podQ14);

        FDSurveyQuestion podQ15 = new FDSurveyQuestion("q_other", "Other", false, false, true);
        postOrderDetail.addQuestion(podQ15);

        FDSurveyQuestion podQ16 = new FDSurveyQuestion("q_additional_information",
                "Please feel free to provide any additional information that might help us correct this problem:", false, false, true);
        postOrderDetail.addQuestion(podQ16);

        FDSurveyQuestion podQ17 = new FDSurveyQuestion("q_contact_cs", "Have you contacted our Customer Service department about this problem?", true,
                false, false);
        podQ17.setAnswers(radioOptions_yesNo);
        postOrderDetail.addQuestion(podQ17);
        return postOrderDetail;
    }

    private FDSurvey createPostOrderSurvey() {
        // ******* POST ORDER SURVEY *******//
        FDSurvey postOrder = new FDSurvey(EnumSurveyType.POST_ORDER);

        List<FDSurveyAnswer> radioOptions_severity = new ArrayList<FDSurveyAnswer>();
        radioOptions_severity.add(new FDSurveyAnswer("This is a minor problem you should be aware of.", "This is a minor problem you should be aware of."));
        radioOptions_severity.add(new FDSurveyAnswer("This issue is annoying. If it continues it will become a real problem.",
                "This issue is annoying. If it continues it will become a real problem."));
        radioOptions_severity.add(new FDSurveyAnswer("This is a serious problem that must be addressed.",
                "This is a serious problem that must be addressed."));
        radioOptions_severity.add(new FDSurveyAnswer("This is enough to make me never use FreshDirect again.",
                "This is enough to make me never use FreshDirect again."));
        radioOptions_severity.add(new FDSurveyAnswer("N/A", "N/A"));

        FDSurveyQuestion poQ0 = new FDSurveyQuestion("q_post_order_response", "Post Order Survey Response", true, false, true);
        postOrder.addQuestion(poQ0);

        FDSurveyQuestion poQ1 = new FDSurveyQuestion("q_problem_area", "Please indicate the areas of concern with your most recent order:", false, true,
                true);
        poQ1.addAnswer(new FDSurveyAnswer("q1_missing_item", "Item or box was missing"));
        poQ1.addAnswer(new FDSurveyAnswer("q1_product_damage", "Product broken or bruised"));
        poQ1.addAnswer(new FDSurveyAnswer("q1_package_damage", "Package damaged or leaking"));
        poQ1.addAnswer(new FDSurveyAnswer("q1_quality_issue", "Quality dissatisfaction"));
        poQ1.addAnswer(new FDSurveyAnswer("q1_quantity_issue", "Size or cut not as expected"));
        poQ1.addAnswer(new FDSurveyAnswer("q1_product_expire", "Product near or past expiration date"));
        poQ1.addAnswer(new FDSurveyAnswer("q1_wrong_item", "Wrong item delivered"));
        poQ1.addAnswer(new FDSurveyAnswer("q1_delivery", "Delivery problem"));
        poQ1.addAnswer(new FDSurveyAnswer("q1_customer_service", "Customer Service"));
        postOrder.addQuestion(poQ1);

        FDSurveyQuestion poQ2 = new FDSurveyQuestion("q_problem_area_other", "Other (please specify)", false, false, true);
        postOrder.addQuestion(poQ2);

        FDSurveyQuestion poQ3 = new FDSurveyQuestion("q_problem_severity", "How serious is the problem?", true, false, false);
        poQ3.setAnswers(radioOptions_severity);
        postOrder.addQuestion(poQ3);
        return postOrder;
    }

    private FDSurvey createOrganic2Survey(List<FDSurveyAnswer> radioOptions_yesNo, List<FDSurveyAnswer> radioOptions_household, 
            List<FDSurveyAnswer> radioOptions_gender) {
        List<FDSurveyAnswer> radioOptions_income = new ArrayList<FDSurveyAnswer>();
        radioOptions_income.add(new FDSurveyAnswer("< $25,000", "< $25,000"));
        radioOptions_income.add(new FDSurveyAnswer("$25,000 - $50,000", "$25,000 - $50,000"));
        radioOptions_income.add(new FDSurveyAnswer("$50,001 - $75,000", "$50,001 - $75,000"));
        radioOptions_income.add(new FDSurveyAnswer("$75,001 - $100,000", "$75,001 - $100,000"));
        radioOptions_income.add(new FDSurveyAnswer("$100,001 - $150,000", "$100,001 - $150,000"));
        radioOptions_income.add(new FDSurveyAnswer("$150,001 - $200,000", "$150,001 - $200,000"));
        radioOptions_income.add(new FDSurveyAnswer("> $200,000", "> $200,000"));

        List<FDSurveyAnswer> radioOptions_ageRange = new ArrayList<FDSurveyAnswer>();
        radioOptions_ageRange.add(new FDSurveyAnswer("< 18", "< 18"));
        radioOptions_ageRange.add(new FDSurveyAnswer("18 - 25", "18 - 25"));
        radioOptions_ageRange.add(new FDSurveyAnswer("26 - 35", "26 - 35"));
        radioOptions_ageRange.add(new FDSurveyAnswer("36 - 45", "36 - 45"));
        radioOptions_ageRange.add(new FDSurveyAnswer("46 - 55", "46 - 55"));
        radioOptions_ageRange.add(new FDSurveyAnswer("56 - 65", "56 - 65"));
        radioOptions_ageRange.add(new FDSurveyAnswer("> 65", "> 65"));

        List<FDSurveyAnswer> radioOptions_percentageText = new ArrayList<FDSurveyAnswer>();
        radioOptions_percentageText.add(new FDSurveyAnswer("Less than 25%", "Less than 25%"));
        radioOptions_percentageText.add(new FDSurveyAnswer("Between 25-50%", "Between 25-50%"));
        radioOptions_percentageText.add(new FDSurveyAnswer("Between 51-75%", "Between 51-75%"));
        radioOptions_percentageText.add(new FDSurveyAnswer("Above 75%", "Above 75%"));
        
        // ******* ORGANIC SURVEY V2 6/05 *******//
        FDSurvey organic2 = new FDSurvey(EnumSurveyType.ORGANIC_2);

        FDSurveyQuestion oQ1 = new FDSurveyQuestion("q1_loyalty", "Are you more loyal to Organic/Antibiotic-Free products than conventional products?",
                true, false);
        oQ1.setAnswers(radioOptions_yesNo);
        organic2.addQuestion(oQ1);

        FDSurveyQuestion oQ2 = new FDSurveyQuestion("q2_reason", "Why do you buy Organic/Antibiotic-Free products?", true, true);
        oQ2.addAnswer(new FDSurveyAnswer("Quality", "Quality"));
        oQ2.addAnswer(new FDSurveyAnswer("Taste & flavor", "Taste & flavor"));
        oQ2.addAnswer(new FDSurveyAnswer("General good health", "General good health"));
        oQ2.addAnswer(new FDSurveyAnswer("Specific health concerns", "Specific health concerns"));
        oQ2.addAnswer(new FDSurveyAnswer("Environmentally/socially conscious", "Environmentally/socially conscious"));
        oQ2.addAnswer(new FDSurveyAnswer("Other", "Other"));
        organic2.addQuestion(oQ2);

        FDSurveyQuestion oQ2a = new FDSurveyQuestion("q2_reason_other", "Other", false, false, true);
        organic2.addQuestion(oQ2a);

        FDSurveyQuestion oQ3 = new FDSurveyQuestion("q3_current", "What % of your diet is currently composed of Organic/Antibiotic-Free?", true, false);
        oQ3.setAnswers(radioOptions_percentageText);
        organic2.addQuestion(oQ3);

        FDSurveyQuestion oQ4 = new FDSurveyQuestion("q4_desired", "What % of Organic would you like your diet to be? ", true, false);
        oQ4.setAnswers(radioOptions_percentageText);
        organic2.addQuestion(oQ4);

        FDSurveyQuestion oQ5 = new FDSurveyQuestion("q5_store", "Which store do you think has the best choices for Organic products?", true, false);
        oQ5.addAnswer(new FDSurveyAnswer("FreshDirect", "FreshDirect"));
        oQ5.addAnswer(new FDSurveyAnswer("WholeFoods", "WholeFoods"));
        oQ5.addAnswer(new FDSurveyAnswer("Fairway", "Fairway"));
        oQ5.addAnswer(new FDSurveyAnswer("Citarella", "Citarella"));
        oQ5.addAnswer(new FDSurveyAnswer("Other", "Other"));
        organic2.addQuestion(oQ5);

        FDSurveyQuestion oQ5a = new FDSurveyQuestion("q5_store_other", "Other", false, false, true);
        organic2.addQuestion(oQ5a);

        FDSurveyQuestion oQ6 = new FDSurveyQuestion("q6_brand", "Do you buy only certain Organic/Antibiotic-Free brands?", true, false);
        oQ6.setAnswers(radioOptions_yesNo);
        organic2.addQuestion(oQ6);

        FDSurveyQuestion oQ6a = new FDSurveyQuestion("q6_brand_other", "If Yes, why?", false, false, true);
        organic2.addQuestion(oQ6a);

        FDSurveyQuestion oQ7 = new FDSurveyQuestion("q7_type", "Check the categories in which you definitely want to buy Organic", true, true);
        oQ7.addAnswer(new FDSurveyAnswer("Meat", "Meat"));
        oQ7.addAnswer(new FDSurveyAnswer("Cheese", "Cheese"));
        oQ7.addAnswer(new FDSurveyAnswer("Fruit", "Fruit"));
        oQ7.addAnswer(new FDSurveyAnswer("Seafood", "Seafood"));
        oQ7.addAnswer(new FDSurveyAnswer("Grocery", "Grocery"));
        oQ7.addAnswer(new FDSurveyAnswer("Beverages", "Beverages"));
        oQ7.addAnswer(new FDSurveyAnswer("Dairy", "Dairy"));
        oQ7.addAnswer(new FDSurveyAnswer("Vegetable", "Vegetable"));
        oQ7.addAnswer(new FDSurveyAnswer("Frozen", "Frozen"));
        oQ7.addAnswer(new FDSurveyAnswer("Deli", "Deli"));
        oQ7.addAnswer(new FDSurveyAnswer("Tea/Coffee", "Tea/Coffee"));
        oQ7.addAnswer(new FDSurveyAnswer("Bakery", "Bakery"));
        oQ7.addAnswer(new FDSurveyAnswer("Other", "Other"));
        organic2.addQuestion(oQ7);

        FDSurveyQuestion oQ7a = new FDSurveyQuestion("q7_type_other", "other", false, false, true);
        organic2.addQuestion(oQ7a);

        FDSurveyQuestion oQ8 = new FDSurveyQuestion("q8_household", "Which best describes your household?", false, false);
        oQ8.setAnswers(radioOptions_household);
        organic2.addQuestion(oQ8);

        FDSurveyQuestion oQ9 = new FDSurveyQuestion("q9_age", "What age range do you fall into?", false, false);
        oQ9.setAnswers(radioOptions_ageRange);
        organic2.addQuestion(oQ9);

        FDSurveyQuestion oQ10 = new FDSurveyQuestion("q10_gender", "What is your gender?", false, false);
        oQ10.setAnswers(radioOptions_gender);
        organic2.addQuestion(oQ10);

        FDSurveyQuestion oQ11 = new FDSurveyQuestion("q11_income", "What is your annual household income?", false, false);
        oQ11.setAnswers(radioOptions_income);
        organic2.addQuestion(oQ11);

        FDSurveyQuestion oQ12 = new FDSurveyQuestion("q12_feedback", "Do you have feedback on FreshDirect's Organic offerings?", false, false, true);
        organic2.addQuestion(oQ12);
        return organic2;
    }

    private FDSurvey createSummerCheckoutSurvey() {
        FDSurvey summerCheckout = new FDSurvey("SummerCheckout");

        FDSurveyQuestion q1 = new FDSurveyQuestion("quest1_WhereToDeliver", "Where would you be interested to get delivery?", true, false);
        q1.addAnswer(new FDSurveyAnswer("Amagansett", "Amagansett"));
        q1.addAnswer(new FDSurveyAnswer("Bridgehampton", "Bridgehampton"));
        q1.addAnswer(new FDSurveyAnswer("Sagaponack", "Sagaponack"));
        q1.addAnswer(new FDSurveyAnswer("East Hampton", "East Hampton"));
        q1.addAnswer(new FDSurveyAnswer("Wainscott", "Wainscott"));
        q1.addAnswer(new FDSurveyAnswer("Hampton Bays", "Hampton Bays"));
        q1.addAnswer(new FDSurveyAnswer("East Quogue", "East Quogue"));
        q1.addAnswer(new FDSurveyAnswer("Montauk", "Montauk"));
        q1.addAnswer(new FDSurveyAnswer("Sag Harbor", "Sag Harbor"));
        q1.addAnswer(new FDSurveyAnswer("North Haven", "North Haven"));
        q1.addAnswer(new FDSurveyAnswer("Southampton", "Southampton"));
        q1.addAnswer(new FDSurveyAnswer("Water Mill", "Water Mill"));
        q1.addAnswer(new FDSurveyAnswer("Shelter Island", "Shelter Island"));
        q1.addAnswer(new FDSurveyAnswer("Westhampton Beach", "Westhampton Beach"));
        q1.addAnswer(new FDSurveyAnswer("Fire Island", "Fire Island"));
        q1.addAnswer(new FDSurveyAnswer("Quiogue", "Quiogue"));

        q1.sortAnswers();

        summerCheckout.addQuestion(q1);

        FDSurveyQuestion q2 = new FDSurveyQuestion("quest2_DeliveryTimeA", "What time would you prefer delivery?", true, false);
        q2.addAnswer(new FDSurveyAnswer("Morning", "Morning"));
        q2.addAnswer(new FDSurveyAnswer("Afternoon", "Afternoon"));
        q2.addAnswer(new FDSurveyAnswer("Evening", "Evening"));
        summerCheckout.addQuestion(q2);

        FDSurveyQuestion q3 = new FDSurveyQuestion("quest2_DeliveryTimeB", "What time would you prefer delivery?", true, false);
        q3.addAnswer(new FDSurveyAnswer("Morning", "Morning"));
        q3.addAnswer(new FDSurveyAnswer("Afternoon", "Afternoon"));
        q3.addAnswer(new FDSurveyAnswer("Evening", "Evening"));
        summerCheckout.addQuestion(q3);
        return summerCheckout;
    }

    private FDSurvey createLastOrderSurvey() {
        FDSurvey lastOrderSurvey = new FDSurvey("LastOrderSurvey_v2", true);

        FDSurveyQuestion q1 = new FDSurveyQuestion("quest1_OrderInTact", "Did your last order arrive intact?", true, false);
        q1.addAnswer(new FDSurveyAnswer("yes", "Yes"));
        q1.addAnswer(new FDSurveyAnswer("damaged", "No, there were some damaged items"));
        q1.addAnswer(new FDSurveyAnswer("missing", "No, there were some missing items"));
        q1.addAnswer(new FDSurveyAnswer("poor", "No, the quality of one or more items was poor"));
        lastOrderSurvey.addQuestion(q1);

        FDSurveyQuestion q2 = new FDSurveyQuestion("quest2_OnTime", "Was your last order on time?", true, false);
        q2.addAnswer(new FDSurveyAnswer("yes", "Yes"));
        q2.addAnswer(new FDSurveyAnswer("early", "No, it was early"));
        q2.addAnswer(new FDSurveyAnswer("late with call", "No it was late but I received a call"));
        q2.addAnswer(new FDSurveyAnswer("late no call", "No, it was late and I did not receive a call"));
        lastOrderSurvey.addQuestion(q2);

        FDSurveyQuestion q3 = new FDSurveyQuestion("quest3_ContactCS", "Did you contact customer service about your last order?", true, false);
        q3.addAnswer(new FDSurveyAnswer("no", "No, everything was fine"));
        q3.addAnswer(new FDSurveyAnswer("yes fantastic", "Yes, and the representative was fantastic"));
        q3.addAnswer(new FDSurveyAnswer("yes satisfactory", "Yes, and the representative was satisfactory"));
        q3.addAnswer(new FDSurveyAnswer("yes unhelpful", "Yes, but the representative was unhelpful"));
        lastOrderSurvey.addQuestion(q3);
        return lastOrderSurvey;
    }

    private FDSurvey createUsabilitySurvey(List<FDSurveyAnswer> radioOptions_yesNo, List<FDSurveyAnswer> radioOptions_rating) {
        // ****** USABILITY SURVEY ******//
        FDSurvey usability = new FDSurvey(EnumSurveyType.USABILITY);

        List<FDSurveyAnswer> radioOptions_ratingNa = new ArrayList<FDSurveyAnswer>();
        radioOptions_ratingNa.add(new FDSurveyAnswer("1", "1"));
        radioOptions_ratingNa.add(new FDSurveyAnswer("2", "2"));
        radioOptions_ratingNa.add(new FDSurveyAnswer("3", "3"));
        radioOptions_ratingNa.add(new FDSurveyAnswer("4", "4"));
        radioOptions_ratingNa.add(new FDSurveyAnswer("5", "5"));
        radioOptions_ratingNa.add(new FDSurveyAnswer("n/a", "n/a"));


        FDSurveyQuestion usQ0 = new FDSurveyQuestion("UsabilityEase", "How easy was it to use the FreshDirect web site today?", true, false, false, true);
        usQ0.setAnswers(radioOptions_rating);
        usability.addQuestion(usQ0);

        FDSurveyQuestion usQ1 = new FDSurveyQuestion("UsabilityInitialEase", "On your first few orders, how easy was it to use the FreshDirect web site?",
                true, false, false, true);
        usQ1.setAnswers(radioOptions_ratingNa);
        usability.addQuestion(usQ1);

        FDSurveyQuestion usQ2 = new FDSurveyQuestion("UsabilityQuickshop", "How easy is it to reorder?", true, false, false, true);
        usQ2.setAnswers(radioOptions_ratingNa);
        usability.addQuestion(usQ2);

        FDSurveyQuestion usQ3 = new FDSurveyQuestion("UsabilitySearch", "When you \"search\" for products, how useful are the results?", true, false,
                false, true);
        usQ3.setAnswers(radioOptions_ratingNa);
        usability.addQuestion(usQ3);

        FDSurveyQuestion usQ4 = new FDSurveyQuestion("UsabilityAdditional", "What additional features would be most useful to you?", false, false, true);
        usability.addQuestion(usQ4);

        FDSurveyQuestion usQ5 = new FDSurveyQuestion("UsabilityFocusGroup",
                "Would you be interested in participating in a FreshDirect web site focus group?", true, false, false);
        usQ5.setAnswers(radioOptions_yesNo);
        usability.addQuestion(usQ5);
        return usability;
    }

    private FDSurvey createMorningDeliverySurvey(List<FDSurveyAnswer> radioOptions_rating) {
        // ****** MORNING DELIVERY SURVEY ******//

        FDSurvey morningDelivery = new FDSurvey(EnumSurveyType.MORNING_DELIVERY);

        FDSurveyQuestion mdQ0 = new FDSurveyQuestion("MorningDeliveryOption",
                "If FreshDirect offered a weekday morning delivery option, how likely would you be use it?", true, false, false, true);
        mdQ0.setAnswers(radioOptions_rating);
        morningDelivery.addQuestion(mdQ0);

        FDSurveyQuestion mdQ1 = new FDSurveyQuestion("MorningDeliveryTimeslot",
                "At what time of morning would it be most convenient for you to receive delivery?", true, true);
        mdQ1.addAnswer(new FDSurveyAnswer("6-7 a.m.", "6-7 a.m."));
        mdQ1.addAnswer(new FDSurveyAnswer("7-8 a.m.", "7-8 a.m."));
        mdQ1.addAnswer(new FDSurveyAnswer("8-9 a.m.", "8-9 a.m."));
        mdQ1.addAnswer(new FDSurveyAnswer("9-10 a.m.", "9-10 a.m."));
        mdQ1.addAnswer(new FDSurveyAnswer("10-11 a.m.", "10-11 a.m."));
        mdQ1.addAnswer(new FDSurveyAnswer("11 a.m.-noon", "11 a.m.-noon"));
        morningDelivery.addQuestion(mdQ1);

        FDSurveyQuestion mdQ2 = new FDSurveyQuestion("MorningDelivery_hdr", "How likely would you be to use the weekday delivery option if there was:",
                false, false);
        morningDelivery.addQuestion(mdQ2);

        FDSurveyQuestion mdQ3 = new FDSurveyQuestion("MorningDelivery695", "<span style=\"font-weight: normal;\">a)</span> ...a $6.95 delivery fee?", true,
                false, false, true, true);
        mdQ3.setAnswers(radioOptions_rating);
        morningDelivery.addQuestion(mdQ3);

        FDSurveyQuestion mdQ4 = new FDSurveyQuestion("MorningDelivery3Hour", "<span style=\"font-weight: normal;\">b)</span> ...a 3-hour delivery window?",
                true, false, false, true, true);
        mdQ4.setAnswers(radioOptions_rating);
        morningDelivery.addQuestion(mdQ4);

        FDSurveyQuestion mdQ5 = new FDSurveyQuestion("MorningDelivery8Cutoff",
                "<span style=\"font-weight: normal;\">c)</span> ...a cutoff time of 8 p.m. the night before?", true, false, false, true, true);
        mdQ5.setAnswers(radioOptions_rating);
        morningDelivery.addQuestion(mdQ5);

        FDSurveyQuestion mdQ6 = new FDSurveyQuestion("MorningDeliveryOrder",
                "If FreshDirect offered weekday morning delivery would you be likely to order more or less frequently?", true, false, false, true);
        mdQ6.setAnswers(radioOptions_rating);
        morningDelivery.addQuestion(mdQ6);

        FDSurveyQuestion mdQ7 = new FDSurveyQuestion("MorningDeliveryBuy",
                "If FreshDirect offered weekday morning delivery would you be likely to buy more or less food with each order?", true, false, false, true);
        mdQ7.setAnswers(radioOptions_rating);
        morningDelivery.addQuestion(mdQ7);

        FDSurveyQuestion mdQ8 = new FDSurveyQuestion("MorningDeliveryRecipient",
                "If FreshDirect offered weekday morning delivery, who would most likely be available to receive the order?", true, false);
        mdQ8.addAnswer(new FDSurveyAnswer("Me/ My Family", "Me/ My Family"));
        mdQ8.addAnswer(new FDSurveyAnswer("Housekeeper/ Nanny", "Housekeeper/ Nanny"));
        mdQ8.addAnswer(new FDSurveyAnswer("Doorman", "Doorman"));
        mdQ8.addAnswer(new FDSurveyAnswer("Neighbor", "Neighbor"));
        mdQ8.addAnswer(new FDSurveyAnswer("Other", "Other"));
        morningDelivery.addQuestion(mdQ8);

        FDSurveyQuestion mdQ8a = new FDSurveyQuestion("MorningDeliveryRecipientOther", "Other", false, false, true);
        morningDelivery.addQuestion(mdQ8a);

        FDSurveyQuestion mdQ9 = new FDSurveyQuestion("MorningDeliveryPurchase", "Approximately how much of your food is purchased from FreshDirect?", true,
                false);
        mdQ9.addAnswer(new FDSurveyAnswer("100%", "100%"));
        mdQ9.addAnswer(new FDSurveyAnswer("75% or more", "75% or more"));
        mdQ9.addAnswer(new FDSurveyAnswer("50% or more", "50% or more"));
        mdQ9.addAnswer(new FDSurveyAnswer("25% or more", "25% or more"));
        mdQ9.addAnswer(new FDSurveyAnswer("Less than 25%", "Less than 25%"));
        morningDelivery.addQuestion(mdQ9);

        FDSurveyQuestion mdQ10 = new FDSurveyQuestion("MorningDeliverySuggestion", "Do you have any other suggestions or ideas?", false, false, true);
        morningDelivery.addQuestion(mdQ10);
        return morningDelivery;
    }

    private FDSurvey createHamptonsSurvey(List<FDSurveyAnswer> radioOptions_rating) {
        // ****** HAMPTONS SURVEY ******//
        FDSurvey hamptons05 = new FDSurvey(EnumSurveyType.HAMPTONS);



        List<FDSurveyAnswer> radioOptions_location = new ArrayList<FDSurveyAnswer>();
        radioOptions_location.add(new FDSurveyAnswer("East LI Amagansett", "Amagansett"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Bridgehampton", "Bridgehampton"));
        radioOptions_location.add(new FDSurveyAnswer("East LI East Hampton", "East Hampton"));
        radioOptions_location.add(new FDSurveyAnswer("East LI East Quogue", "East Quogue"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Hampton Bays", "Hampton Bays"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Montauk", "Montauk"));
        radioOptions_location.add(new FDSurveyAnswer("East LI North Haven", "North Haven"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Quogue", "Quogue"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Sag Harbor", "Sag Harbor"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Sagaponack", "Sagaponack"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Shelter Island", "Shelter Island"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Southhampton", "Southhampton"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Wainscott", "Wainscott"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Water Mill", "Water Mill"));
        radioOptions_location.add(new FDSurveyAnswer("East LI Westhampton Beach", "Westhampton Beach"));
        radioOptions_location.add(new FDSurveyAnswer("Upstate New York", "Upstate New York"));
        radioOptions_location.add(new FDSurveyAnswer("Jersey Shore", "Jersey Shore"));
        radioOptions_location.add(new FDSurveyAnswer("Connecticut", "Connecticut"));
        radioOptions_location.add(new FDSurveyAnswer("Massachusetts", "Massachusetts"));
        radioOptions_location.add(new FDSurveyAnswer("New Hampshire", "New Hampshire"));
        radioOptions_location.add(new FDSurveyAnswer("Vermont", "Vermont"));
        radioOptions_location.add(new FDSurveyAnswer("Other", "Other"));

        FDSurveyQuestion hamQ1 = new FDSurveyQuestion("VacationDestination", "If you leave the city for vacation this summer where will you go?", true,
                false);
        hamQ1.setAnswers(radioOptions_location);
        hamptons05.addQuestion(hamQ1);

        FDSurveyQuestion hamQ1a = new FDSurveyQuestion("VacationDestinationOther", "Other", false, false, true);
        hamptons05.addQuestion(hamQ1a);

        FDSurveyQuestion hamQ2 = new FDSurveyQuestion("VacationSchedule", "When will you go?", true, true);
        hamQ2.addAnswer(new FDSurveyAnswer("June", "June"));
        hamQ2.addAnswer(new FDSurveyAnswer("July", "July"));
        hamQ2.addAnswer(new FDSurveyAnswer("August", "August"));
        hamptons05.addQuestion(hamQ2);

        FDSurveyQuestion hamQ3 = new FDSurveyQuestion("VacationDuration", "How long will you go for?", true, false);
        hamQ3.addAnswer(new FDSurveyAnswer("Weekends", "Weekends"));
        hamQ3.addAnswer(new FDSurveyAnswer("Weekends plus an extended vacation (1 week to 1 month)",
                "Weekends plus an extended vacation (1 week to 1 month)"));
        hamQ3.addAnswer(new FDSurveyAnswer("Extended vacation only (1 week to 1 month)", "Extended vacation only (1 week to 1 month)"));
        hamQ3.addAnswer(new FDSurveyAnswer("All summer (Memorial Day to Labor Day)", "All summer (Memorial Day to Labor Day)"));
        hamptons05.addQuestion(hamQ3);

        FDSurveyQuestion hamQ4 = new FDSurveyQuestion("VacationAccomodation", "What best describes your vacation accommodations?", true, false);
        hamQ4.addAnswer(new FDSurveyAnswer("Own home", "Own home"));
        hamQ4.addAnswer(new FDSurveyAnswer("Rented house", "Rented house"));
        hamQ4.addAnswer(new FDSurveyAnswer("Summer share", "Summer share"));
        hamQ4.addAnswer(new FDSurveyAnswer("Stay with friends", "Stay with friends"));
        hamQ4.addAnswer(new FDSurveyAnswer("Resort/Hotel/Motel", "Resort/Hotel/Motel"));
        hamptons05.addQuestion(hamQ4);

        FDSurveyQuestion hamQ5 = new FDSurveyQuestion("VacationOrder",
                "If FreshDirect offered delivery to the Hamptons during the summer, how often would you be likely to order from FreshDirect?", true, false);
        hamQ5.addAnswer(new FDSurveyAnswer("2-3 times a week", "2-3 times a week"));
        hamQ5.addAnswer(new FDSurveyAnswer("Once a week", "Once a week"));
        hamQ5.addAnswer(new FDSurveyAnswer("Once a month", "Once a month"));
        hamQ5.addAnswer(new FDSurveyAnswer("Twice a month", "Twice a month"));
        hamQ5.addAnswer(new FDSurveyAnswer("Not at all", "Not at all"));
        hamptons05.addQuestion(hamQ5);

        FDSurveyQuestion hamQ6 = new FDSurveyQuestion("VacationTimeslot", "What delivery days and time periods would be most convenient for you?", true,
                true);
        hamQ6.addAnswer(new FDSurveyAnswer("Monday 9-12", "Monday 9-12"));
        hamQ6.addAnswer(new FDSurveyAnswer("Tuesday 9-12", "Tuesday 9-12"));
        hamQ6.addAnswer(new FDSurveyAnswer("Wednesday 9-12", "Wednesday 9-12"));
        hamQ6.addAnswer(new FDSurveyAnswer("Thursday 9-12", "Thursday 9-12"));
        hamQ6.addAnswer(new FDSurveyAnswer("Friday 9-12", "Friday 9-12"));
        hamQ6.addAnswer(new FDSurveyAnswer("Saturday 9-12", "Saturday 9-12"));
        hamQ6.addAnswer(new FDSurveyAnswer("Sunday 9-12", "Sunday 9-12"));
        hamQ6.addAnswer(new FDSurveyAnswer("Monday 12-3", "Monday 12-3"));
        hamQ6.addAnswer(new FDSurveyAnswer("Tuesday 12-3", "Tuesday 12-3"));
        hamQ6.addAnswer(new FDSurveyAnswer("Wednesday 12-3", "Wednesday 12-3"));
        hamQ6.addAnswer(new FDSurveyAnswer("Thursday 12-3", "Thursday 12-3"));
        hamQ6.addAnswer(new FDSurveyAnswer("Friday 12-3", "Friday 12-3"));
        hamQ6.addAnswer(new FDSurveyAnswer("Saturday 12-3", "Saturday 12-3"));
        hamQ6.addAnswer(new FDSurveyAnswer("Sunday 12-3", "Sunday 12-3"));
        hamQ6.addAnswer(new FDSurveyAnswer("Monday 3-6", "Monday 3-6"));
        hamQ6.addAnswer(new FDSurveyAnswer("Tuesday 3-6", "Tuesday 3-6"));
        hamQ6.addAnswer(new FDSurveyAnswer("Wednesday 3-6", "Wednesday 3-6"));
        hamQ6.addAnswer(new FDSurveyAnswer("Thursday 3-6", "Thursday 3-6"));
        hamQ6.addAnswer(new FDSurveyAnswer("Friday 3-6", "Friday 3-6"));
        hamQ6.addAnswer(new FDSurveyAnswer("Saturday 3-6", "Saturday 3-6"));
        hamQ6.addAnswer(new FDSurveyAnswer("Sunday 3-6", "Sunday 3-6"));
        hamQ6.addAnswer(new FDSurveyAnswer("Monday 6-9", "Monday 6-9"));
        hamQ6.addAnswer(new FDSurveyAnswer("Tuesday 6-9", "Tuesday 6-9"));
        hamQ6.addAnswer(new FDSurveyAnswer("Wednesday 6-9", "Wednesday 6-9"));
        hamQ6.addAnswer(new FDSurveyAnswer("Thursday 6-9", "Thursday 6-9"));
        hamQ6.addAnswer(new FDSurveyAnswer("Friday 6-9", "Friday 6-9"));
        hamQ6.addAnswer(new FDSurveyAnswer("Saturday 6-9", "Saturday 6-9"));
        hamQ6.addAnswer(new FDSurveyAnswer("Sunday 6-9", "Sunday 6-9"));
        hamptons05.addQuestion(hamQ6);

        FDSurveyQuestion hamQ7 = new FDSurveyQuestion("VacationDelivery_hdr", "How likely would you be to use a Hamptons delivery option if there was:",
                false, false);
        hamptons05.addQuestion(hamQ7);

        FDSurveyQuestion hamQ8 = new FDSurveyQuestion("Vacation1495", "<span style=\"font-weight: normal;\">a)</span> ...a $14.95 delivery fee?", true,
                false);
        hamQ8.setAnswers(radioOptions_rating);
        hamptons05.addQuestion(hamQ8);

        FDSurveyQuestion hamQ9 = new FDSurveyQuestion("Vacation3Hour", "<span style=\"font-weight: normal;\">b)</span> ...a 3-hour delivery window?", true,
                false);
        hamQ9.setAnswers(radioOptions_rating);
        hamptons05.addQuestion(hamQ9);

        FDSurveyQuestion hamQ10 = new FDSurveyQuestion("VacationNoFriday", "<span style=\"font-weight: normal;\">c)</span> ...no delivery on Fridays?",
                true, false);
        hamQ10.setAnswers(radioOptions_rating);
        hamptons05.addQuestion(hamQ10);

        FDSurveyQuestion hamQ11 = new FDSurveyQuestion("VacationCentralLocation",
                "<span style=\"font-weight: normal;\">d)</span> ...no home delivery but a centralized pickup location 10-20 minutes away by car?", true,
                false);
        hamQ11.setAnswers(radioOptions_rating);
        hamptons05.addQuestion(hamQ11);

        FDSurveyQuestion hamQ12 = new FDSurveyQuestion(
                "VacationPickup",
                "How likely would you be to pick up your order on your way out of the city at our facility on the other side of the Midtown Tunnel (just a 5-minute detour)?",
                true, false);
        hamQ12.setAnswers(radioOptions_rating);
        hamptons05.addQuestion(hamQ12);

        FDSurveyQuestion hamQ13 = new FDSurveyQuestion("VacationBuy",
                "What type of food would you be most likely to order from FreshDirect while you're in the Hamptons?", true, true);
        hamQ13.addAnswer(new FDSurveyAnswer("Anything & Everything", "Anything & Everything"));
        hamQ13.addAnswer(new FDSurveyAnswer("Fresh fruits & Vegetables", "Fresh fruits & Vegetables"));
        hamQ13.addAnswer(new FDSurveyAnswer("Meat", "Meat"));
        hamQ13.addAnswer(new FDSurveyAnswer("Seafood", "Seafood"));
        hamQ13.addAnswer(new FDSurveyAnswer("Cheese & Deli", "Cheese & Deli"));
        hamQ13.addAnswer(new FDSurveyAnswer("Dairy", "Dairy"));
        hamQ13.addAnswer(new FDSurveyAnswer("Prepared meals & Baked goods", "Prepared meals & Baked goods"));
        hamQ13.addAnswer(new FDSurveyAnswer("Catering platters", "Catering platters"));
        hamQ13.addAnswer(new FDSurveyAnswer("Grocery & Dry goods", "Grocery & Dry goods"));
        hamQ13.addAnswer(new FDSurveyAnswer("Frozen foods", "Frozen foods"));
        hamQ13.addAnswer(new FDSurveyAnswer("Wine", "Wine"));
        hamptons05.addQuestion(hamQ13);

        FDSurveyQuestion hamQ14 = new FDSurveyQuestion("VacationSuggestion", "Do you have any other suggestions or ideas?", false, false, true);
        hamptons05.addQuestion(hamQ14);
        return hamptons05;
    }

    public static FDSurvey createCorporateSignup() {
        FDSurvey surv = new FDSurvey(new SurveyKey(EnumSurveyType.REGISTRATION_SURVEY, EnumServiceType.CORPORATE));

        {
            FDSurveyQuestion q = createRegularProductsQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createEmployeCountQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createIndustryQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createHowDidYouHearAboutFDQuestion();
            surv.addQuestion(q);
        }
        return surv;
    }

    private static FDSurveyQuestion createHowDidYouHearAboutFDQuestion() {
        FDSurveyQuestion q = FDSurveyQuestion.pulldown("cos_reg_how_hear", "How did you hear about FreshDirect?");
        q.addAnswer(new FDSurveyAnswer("hear_flyer","Flyer"));
        q.addAnswer(new FDSurveyAnswer("hear_news","Newspaper"));
        q.addAnswer(new FDSurveyAnswer("hear_mag","Magazine"));
        q.addAnswer(new FDSurveyAnswer("hear_tv","TV"));
        q.addAnswer(new FDSurveyAnswer("hear_radio","Radio"));
        q.addAnswer(new FDSurveyAnswer("hear_email","Email"));
        q.addAnswer(new FDSurveyAnswer("hear_fr","Friend/Colleague"));
        q.addAnswer(new FDSurveyAnswer("hear_post","Poster/Billboard"));
        q.addAnswer(new FDSurveyAnswer("hear_mail","Direct Mail"));
        q.addAnswer(new FDSurveyAnswer("hear_web","Internet/Web Ad"));
        q.addAnswer(new FDSurveyAnswer("hear_truck","FreshDirect Truck"));
        q.addAnswer(new FDSurveyAnswer("hear_taxi","Taxi Tops"));
        return q;
    }

    private static FDSurveyQuestion createIndustryQuestion() {
        FDSurveyQuestion q = FDSurveyQuestion.pulldown("cos_reg_industry", "Which of the following best describes your company’s industry?");
        q.addAnswer(new FDSurveyAnswer("comp_inv","Investment Services"));
        q.addAnswer(new FDSurveyAnswer("comp_hotel","Hotel"));
        q.addAnswer(new FDSurveyAnswer("comp_ret","Retail"));
        q.addAnswer(new FDSurveyAnswer("comp_bank","Banking"));
        q.addAnswer(new FDSurveyAnswer("comp_edu","Education"));
        q.addAnswer(new FDSurveyAnswer("comp_ins","Insurance"));
        q.addAnswer(new FDSurveyAnswer("comp_acc","Accounting"));
        q.addAnswer(new FDSurveyAnswer("comp_tech","Technology"));
        q.addAnswer(new FDSurveyAnswer("comp_arc","Architecture/Design"));
        q.addAnswer(new FDSurveyAnswer("comp_real","Real Estate"));
        q.addAnswer(new FDSurveyAnswer("comp_manu","Manufacturing"));
        q.addAnswer(new FDSurveyAnswer("comp_med","Media/Arts/Entertainment"));
        q.addAnswer(new FDSurveyAnswer("comp_tele","Telecommunications"));
        q.addAnswer(new FDSurveyAnswer("comp_att","Attorney/Legal Services"));
        q.addAnswer(new FDSurveyAnswer("comp_ob","Other Business Services"));
        q.addAnswer(new FDSurveyAnswer("comp_pr","Public Relations/Ad Agencies"));
        q.addAnswer(new FDSurveyAnswer("comp_np","Non-Profit"));
        q.addAnswer(new FDSurveyAnswer("comp_garm","Garment/Fashion Industry"));
        q.addAnswer(new FDSurveyAnswer("comp_other","Other"));
        return q;
    }

    
    private static FDSurveyQuestion createEmployeCountQuestion() {
        FDSurveyQuestion q = FDSurveyQuestion.pulldown("cos_reg_employee_count", "How many employees in your office?");
        q.addAnswer(new FDSurveyAnswer("emp_1_9","1 - 9"));
        q.addAnswer(new FDSurveyAnswer("emp_10_49","10 - 49"));
        q.addAnswer(new FDSurveyAnswer("emp_50_74","50 - 74"));
        q.addAnswer(new FDSurveyAnswer("emp_75_99","75 - 99"));
        q.addAnswer(new FDSurveyAnswer("emp_100_499","100 - 499"));
        q.addAnswer(new FDSurveyAnswer("emp_500p","500 +"));
        return q;
    }

    public static FDSurvey createCorporateProfileSurvey() {
        FDSurvey surv = new FDSurvey(new SurveyKey(EnumSurveyType.CUSTOMER_PROFILE_SURVEY, EnumServiceType.CORPORATE));

        {
            FDSurveyQuestion q = createEmployeCountQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createIndustryQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createRegularProductsQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createHowDidYouHearAboutFDQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCustomerProfileRole();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCorporateProfileImportant();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCorporateProfileLearnQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCorporateEventsQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCorporateReimburseQuestion();
            surv.addQuestion(q);
        }
        return surv;
    }

    
    private static FDSurveyQuestion createRegularProductsQuestion() {
        FDSurveyQuestion q = FDSurveyQuestion.multi("cos_reg_regular_products", "Which of the following products does your office buy on a regular basis?", EnumFormDisplayType.TWO_ANS_PER_ROW);
        q.addAnswer(new FDSurveyAnswer("regp_cold","Cold Beverages (bottled water, soda, juice, iced tea, etc)"));
        q.addAnswer(new FDSurveyAnswer("regp_hot","Hot Beverages (coffee, tea, etc)"));
        q.addAnswer(new FDSurveyAnswer("regp_dairy","Dairy (milk, creamer, etc)"));
        q.addAnswer(new FDSurveyAnswer("regp_yogurt","Yogurt"));
        q.addAnswer(new FDSurveyAnswer("regp_nonp","Non-Perishable Snacks (chips)"));
        q.addAnswer(new FDSurveyAnswer("regp_peris","Perishable Snacks (fruit, yogurt)"));
        q.addAnswer(new FDSurveyAnswer("regp_break","Breakfast Items (Cereal, etc)"));
        q.addAnswer(new FDSurveyAnswer("regp_cat","Catering Platters"));
        q.addAnswer(new FDSurveyAnswer("regp_ind","Individual Meals"));
        return q;
    }        

    public static FDSurvey createCorporateSecondOrderSurvey() {
        FDSurvey surv = new FDSurvey(new SurveyKey(EnumSurveyType.SECOND_ORDER_SURVEY, EnumServiceType.CORPORATE));
        {
            FDSurveyQuestion q = createCustomerProfileRole();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCorporateProfileImportant();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCorporateProfileLearnQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCorporateEventsQuestion();
            surv.addQuestion(q);
        }
        {
            FDSurveyQuestion q = createCorporateReimburseQuestion();
            surv.addQuestion(q);
        }
        return surv;
    }

    private static FDSurveyQuestion createCorporateReimburseQuestion() {
        FDSurveyQuestion q = FDSurveyQuestion.radio("cos_profile_reimburse", "Does your company reimburse employees for meals when they work late at night or through lunchtime?");
        q.addAnswer(new FDSurveyAnswer("reimburse_yes","Yes"));
        q.addAnswer(new FDSurveyAnswer("reimburse_no","No"));
        return q;
    }

    private static FDSurveyQuestion createCorporateEventsQuestion() {
        FDSurveyQuestion q = FDSurveyQuestion.multi("cos_profile_events", "Which of the following office events do you buy food/beverage for:", EnumFormDisplayType.TWO_ANS_PER_ROW);
        q.addAnswer(new FDSurveyAnswer("event_summer","Summer Fridays/Happy Hour"));
        q.addAnswer(new FDSurveyAnswer("event_birth","Birthdays"));
        q.addAnswer(new FDSurveyAnswer("event_wedd","Wedding/Baby Shower"));
        q.addAnswer(new FDSurveyAnswer("event_holiday","Holidays"));
        q.addAnswer(new FDSurveyAnswer("event_comp","Company celebrations"));
        return q;
    }

    private static FDSurveyQuestion createCorporateProfileLearnQuestion() {
        FDSurveyQuestion q = FDSurveyQuestion.multi("cos_profile_learn", "Our company is interested in:", EnumFormDisplayType.SINGLE_ANS_PER_ROW);
        q.addAnswer(new FDSurveyAnswer("learn_health","Healthy meals/snacks for the office"));
        q.addAnswer(new FDSurveyAnswer("learn_deals","Special deals & great prices"));
        q.addAnswer(new FDSurveyAnswer("learn_savings","Time savings tips"));
        q.addAnswer(new FDSurveyAnswer("learn_env_food","Environmentally friendly products"));
        q.addAnswer(new FDSurveyAnswer("learn_local","Local, organic and/or seasonal items"));
        return q;
    }

    private static FDSurveyQuestion createCorporateProfileImportant() {
        FDSurveyQuestion q = FDSurveyQuestion.pulldown("cos_profile_important", "What is most important to you when shopping for food and/or beverages for the office?");
        q.addAnswer(new FDSurveyAnswer("imp_low","Low prices"));
        q.addAnswer(new FDSurveyAnswer("imp_vendor","Convenience of getting all of the products I need from one vendor"));
        q.addAnswer(new FDSurveyAnswer("imp_quality","Quality of perishable food and long expiration dates on products"));
        q.addAnswer(new FDSurveyAnswer("imp_other","Other"));
        return q;
    }

    private static FDSurveyQuestion createCustomerProfileRole() {
        FDSurveyQuestion q = FDSurveyQuestion.pulldown("cos_profile_role", "What is your role in the company?");
        q.addAnswer(new FDSurveyAnswer("role_assist","Administrative Assistant or Receptionist"));
        q.addAnswer(new FDSurveyAnswer("role_offman","Office Manager"));
        q.addAnswer(new FDSurveyAnswer("role_fac","Facilities"));
        q.addAnswer(new FDSurveyAnswer("role_hr","Human Resources"));
        q.addAnswer(new FDSurveyAnswer("role_owner","Owner"));
        q.addAnswer(new FDSurveyAnswer("role_other","Other"));
        return q;
    }
    
    void addSurvey(FDSurvey survey) {
        if (survey.getKey() != null) {
            defaults.put(survey.getKey().getSurveyType(), survey);
        }
    }

    void addOverrideSurvey(FDSurvey survey) {
        if (survey.getKey() != null) {
            override.put(survey.getKey(), survey);
        }
    }

    
    public FDSurvey getDefaultSurvey(SurveyKey key) {
        return defaults.get(key.getSurveyType());
    }

    public FDSurvey getOverrideSurvey(SurveyKey key) {
        return override.get(key);
    }
    

}
