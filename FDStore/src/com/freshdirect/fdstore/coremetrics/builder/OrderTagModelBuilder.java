package com.freshdirect.fdstore.coremetrics.builder;

import com.freshdirect.customer.ErpDiscountLineModel;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdlogistics.model.FDTimeslot;
import com.freshdirect.fdstore.coremetrics.tagmodel.OrderTagModel;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.payment.EnumPaymentMethodType;

public class OrderTagModelBuilder {
	
	FDOrderI order;
	FDUserI user;
	private OrderTagModel model = new OrderTagModel();
	
	public OrderTagModelBuilder(FDOrderI order, FDUserI user) {
		this.order = order;
		this.user = user;
	}

	public OrderTagModel buildTagModel(){
		
		/** basic infos **/
		model.setOrderId(TagModelUtil.getCmOrderId(order));
		model.setOrderSubtotal(Double.toString(order.getSubTotal()));
		model.setOrderShipping(Double.toString(order.getDeliverySurcharge()));
		model.setRegistrationId(user.getPrimaryKey());
		model.setRegistrantCity(order.getDeliveryAddress().getCity());
		model.setRegistrantState(order.getDeliveryAddress().getState());
		model.setRegistrantPostalCode(order.getDeliveryAddress().getZipCode());
		
		model.getAttributesMaps().put(1, TagModelUtil.getOrderType(order.getDeliveryType())+"");
		
		/** delivery time and timeslot **/
		FDReservation reservation = order.getDeliveryReservation();
		if ( reservation != null && null != reservation.getStartTime() ) {
			model.getAttributesMaps().put(2, DateUtil.formatDate(reservation.getStartTime()));
			model.getAttributesMaps().put(3, DateUtil.formatDayOfWeek(reservation.getStartTime()));
			if ( null != reservation.getEndTime() && !"".equals(reservation.getStartTime()) && !"".equals(reservation.getEndTime()) ) 
				model.getAttributesMaps().put(4, FDTimeslot.format(reservation.getStartTime(), reservation.getEndTime()));
		}
		
		model.getAttributesMaps().put(5, reservation.getZoneCode()); //deliveryZone
		
		/** payment promos and discounts **/
		String cardType = order.getPaymentMethod().getPaymentMethodType().equals(EnumPaymentMethodType.CREDITCARD) ? ":"+order.getPaymentMethod().getCardType().getDisplayName() : "";
		model.getAttributesMaps().put(6, order.getPaymentMethod().getPaymentMethodType().getName()+cardType); //paymentTypeId

		if(order.getDiscounts()!=null){
			for(ErpDiscountLineModel dm: order.getDiscounts()){
				if(dm.getDiscount().getPromotionCode()!=null){
					model.getAttributesMaps().put(7, dm.getDiscount().getPromotionCode()); //promotion code
					break;
				}
			}			
		}
		double discountPercentage=(order.getTotalDiscountValue()/order.getSubTotal())*100;
		model.getAttributesMaps().put(8, Math.round(discountPercentage)+""); //discountTotalPercentage
		
		/** SO **/
		model.getAttributesMaps().put(9, order.getStandingOrderId()); //standingOrderId
		
		return model;
	}
	
	
}