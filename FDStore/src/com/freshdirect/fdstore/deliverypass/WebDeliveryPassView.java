package com.freshdirect.fdstore.deliverypass;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;

import org.apache.log4j.Category;

import com.freshdirect.deliverypass.DeliveryPassModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDSkuNotFoundException;
import com.freshdirect.fdstore.content.ContentFactory;
import com.freshdirect.fdstore.content.Html;
import com.freshdirect.fdstore.content.ProductModel;
import com.freshdirect.fdstore.customer.FDCustomerManager;
import com.freshdirect.framework.util.log.LoggerFactory;

public class WebDeliveryPassView {
	private static Category LOGGER = LoggerFactory.getInstance(WebDeliveryPassView.class);
	
	private DeliveryPassModel model;
	private static final String DEFAULT_HEADER_INFO_TEXT = "Your membership details.";
	private static final String NO_PASS_DETAIL_INFO_TEXT = "You are not currently a member";
	private static final String BSGS_PASS_DETAIL_INFO_TEXT = ": ? deliveries remaining";
	private static final String UNLIMITED_PASS_DETAIL_INFO_TEXT = " - Good for orders placed on or before ?";
	private static final String PASS_USAGE_INFO_TEXT = "You have used this pass for ? deliveries.";
	private static final String SINGLE_PASS_USAGE_INFO_TEXT="You have used this pass for ? delivery.";
	private static final String PASS_PURCHASE_INFO_TEXT = "Purchased on ? for ?";
	
	private static final String PASS_USAGE_PURCHASE_INFO_TEXT_1 = "Your current membership was purchased on ? for ? and has been used for ? delivery";
	private static final String PASS_USAGE_PURCHASE_INFO_TEXT_2 = "Your current membership was purchased on ? for ? and has been used for ? deliveries";
	
	protected static SimpleDateFormat dateFmtDisplay = new SimpleDateFormat("MM/dd/yyyy",Locale.US);
	NumberFormat currencyFormatter = java.text.NumberFormat.getCurrencyInstance( Locale.US );
		
	public WebDeliveryPassView() {
		super();
	}

	public WebDeliveryPassView(DeliveryPassModel model) {
		this.model = model;
		
	}
	
	public DeliveryPassModel getModel() {
		return model;
	}

	public String getHeaderInfo() {
		if(model == null){
			return getTextAfterSetParams(DEFAULT_HEADER_INFO_TEXT, new String[]{"active"});
		}
		return getTextAfterSetParams(DEFAULT_HEADER_INFO_TEXT, new String[]{model.getStatus().getDisplayName().toLowerCase()});
	}
	
	public String getPassName() {
		
		String passName ="";
		if(model != null){
			return model.getType().getName()+" DeliveryPass";
		}else{
			return NO_PASS_DETAIL_INFO_TEXT;
		}
	}
	public String getDetailInfo() {
		String detailInfo = null;
		String[] params = null;
		if(model != null){
			if(model.getType().isUnlimited()){
				Date expDate = model.getExpirationDate();
				params = new String[] {dateFmtDisplay.format(expDate)};
				detailInfo = getTextAfterSetParams(UNLIMITED_PASS_DETAIL_INFO_TEXT, params);
			}else{
				//BSGS pass.
				params = new String[] {String.valueOf(model.getRemainingDlvs())};
				detailInfo = getTextAfterSetParams(BSGS_PASS_DETAIL_INFO_TEXT, params);
			}
		}else{
			detailInfo = "";
		}
		return detailInfo;
	}
	
	public Html getDescription() {
		Html prodDesc = null;
		try{
			if(model != null){
				ProductModel prodModel = ContentFactory.getInstance().getProduct(model.getType().getCode());
				prodDesc = prodModel.getProductDescription();
			}
		}catch(FDSkuNotFoundException exp){
			LOGGER.error("Delivery pass description not found.", exp);
		}
		return prodDesc;
	}
	
	public String getUsageInfo() {
		String usageInfo = null;
		int usedDlvsCount = 0;
		if(model != null){
			usedDlvsCount = model.getUsageCount();
			String[] params = new String[] {String.valueOf(usedDlvsCount)};
			if(usedDlvsCount!=1){
			    usageInfo = getTextAfterSetParams(PASS_USAGE_INFO_TEXT, params); 
			}
			else {
				usageInfo = getTextAfterSetParams(SINGLE_PASS_USAGE_INFO_TEXT, params);
			}
		}
		return usageInfo;
	}
	
	public String getUsageAndPurchaseInfo() {
		String usageInfo = null;
		int usedDlvsCount = 0;
		if(model != null){
			usedDlvsCount = model.getUsageCount();
			String purchaseDate = dateFmtDisplay.format(model.getPurchaseDate());
			String purchasePrice = currencyFormatter.format(model.getAmount());

			// String[] params = new String[] {model.getStatus().getDisplayName().toLowerCase(),purchaseDate,purchasePrice,String.valueOf(usedDlvsCount)};
			String[] params = new String[] {purchaseDate,purchasePrice,String.valueOf(usedDlvsCount)};

			
			if(usedDlvsCount!=1){
			    usageInfo = getTextAfterSetParams(PASS_USAGE_PURCHASE_INFO_TEXT_2, params); 
			}
			else {
				usageInfo = getTextAfterSetParams(PASS_USAGE_PURCHASE_INFO_TEXT_1, params);
			}
		}
		return usageInfo;
		
		
	}
	
	public String getPurchaseInfo() {
		String purchaseInfo = null;
		if(model != null){
			String purDate = dateFmtDisplay.format(model.getPurchaseDate());
			String purPrice = currencyFormatter.format(model.getAmount());
			String[] params = new String[] {purDate, purPrice};
			purchaseInfo = getTextAfterSetParams(PASS_PURCHASE_INFO_TEXT, params); 

		}
			
		return purchaseInfo;
	}
	
	public String getId(){
		if(model != null){
			return model.getPK().getId();	
		}else{
			return null;
		}
		
	}
	private String getTextAfterSetParams(String text, String[] params){
		StringBuffer buf = new StringBuffer(text);
		int index = -1;
		int paramIndex = 0;
		while((index = buf.indexOf("?")) != -1){
			buf.replace(index, index+1, params[paramIndex]);
			paramIndex++;
		}
		return buf.toString();
	}
}