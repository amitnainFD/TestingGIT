package com.freshdirect.fdstore.survey;

import java.util.HashMap;
import java.util.Map;


public enum EnumSurveyType {

    CUSTOMER_PROFILE_SURVEY("Customer Profile Survey"), 
    REGISTRATION_SURVEY("Registration_survey"), 
    SECOND_ORDER_SURVEY("Second Order Survey"), 
    POST_ORDER_SURVEY("Post Order Survey")

    ,DIET_NUTRITION("DietNutrition")
    ,COS_FEEDBACK_SURVEY("COS_Feedback_Survey")
    ,HAMPTONS("Hamptons05")
    ,POST_ORDER("PostOrder")
    ,POST_ORDER_DETAIL("PostOrderDetail")
    ,MORNING_DELIVERY("MorningDelivery")
    ,ORGANIC_2("Organic2")
    ,USABILITY("Usability")
    ,WINE_REQUEST("Wine Request Feedback")
    ,CANCEL_ORDER_FEEDBACK("Cancel_order_feedback") // this is not used, I believe
    ,COS_SURVEY_V2("COS_Survey_v2") // this is used in CorporateServiceSurveyTag.java
    ,COS_SURVEY_VENDING("COS_Survey_Vending") //added for vending website
    ,COS_SURVEY_CATERING("COS_Survey_Catering") //added for APPDEV-2909
    ,COS_SURVEY_PRODUCT_NOTIFY("Product_Notifications") //added for product notifications
    ;
    
    String label;

    final static Map<String, EnumSurveyType> types = new HashMap<String, EnumSurveyType>();
    
    static {
        for (EnumSurveyType s : values()) {
            types.put(s.getLabel(), s);
        }
    }
    
    private EnumSurveyType(String name) {
        label = name;
    }

    public static EnumSurveyType getEnum(String name) {
        try {
            return types.get(name);
        } catch (NullPointerException e) {
            return null;
        }
    }

    public String getLabel() {
        return label;
    }
    
}
