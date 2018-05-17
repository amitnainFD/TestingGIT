/*
 * $Workfile$
 *
 * $Date$
 *
 * Copyright (c) 2001 FreshDirect, Inc.
 *
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;

import com.freshdirect.customer.EnumPaymentType;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.framework.util.StringUtil;
import com.freshdirect.logistics.delivery.model.EnumReservationType;

/**
 * Lightweight information about a Sale.
 *
 * @version $Revision$
 * @author $Author$
 */
public class FDCustomerOrderInfo implements Serializable {

	private String saleId;
	private Date deliveryDate;
	private Date cutoffTime;
	private EnumSaleStatus status;
	private double amount;
	private String firstName="";
	private String lastName="";
	private FDIdentity identity;
	private String email;
	private String phone;
	private String altPhone;
	private String waveNum;
	private String routeNum;
	private String stopSequence;
	private double subTotal;
	private double promotionAmt;
	private String deliveryType;
	private EnumPaymentType paymentType;
	private boolean chefsTable;
	private boolean vip;
	private Date lastCroModDate;
	private Date startTime;
	private Date endTime;
	private EnumReservationType rsvType;
	private String emailType;
	private String eStore;
	private String facility;

	public String getEmailType() {
		return emailType;
	}

	public void setEmailType(String emailType) {
		this.emailType = emailType;
	}

	public EnumReservationType getRsvType() {
		return rsvType;
	}

	public void setRsvType(EnumReservationType rsvType) {
		this.rsvType = rsvType;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getSaleId() {
		return saleId;
	}

	public void setSaleId(String saleId) {
		this.saleId = saleId;
	}

	public Date getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(Date deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public void setCutoffTime(Date cutoffTime) {
		this.cutoffTime = cutoffTime;
	}

	public Date getCutoffTime() {
		return this.cutoffTime;
	}

	public EnumSaleStatus getOrderStatus() {
		return status;
	}

	public void setOrderStatus(EnumSaleStatus status) {
		this.status = status;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		if(!StringUtil.isEmpty(firstName))
			this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		if(!StringUtil.isEmpty(lastName))
			this.lastName = lastName;
	}

	public FDIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(FDIdentity identity) {
		this.identity = identity;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAltPhone() {
		return altPhone;
	}

	public void setAltPhone(String altPhone) {
		this.altPhone = altPhone;
	}

	public String getWaveNum() {
		return waveNum;
	}

	public String getRouteNum() {
		return routeNum;
	}

	public String getStopSequence() {
		return stopSequence;
	}

	public void setWaveNum(String string) {
		waveNum = string;
	}

	public void setRouteNum(String string) {
		routeNum = string;
	}

	public void setStopSequence(String string) {
		stopSequence = string;
	}

	public void setSubTotal(double subTotal) {
		this.subTotal = subTotal;
	}

	public void setPromotionAmt(double promoAmt) {
		this.promotionAmt = promoAmt;
	}

	public double getSubTotal() {
		return this.subTotal;
	}

	public double getPromotionAmt() {
		return this.promotionAmt;
	}

	public String getDeliveryType() {
		return this.deliveryType;
	}

	public void setDeliveryType(String deliveryType) {
		this.deliveryType = deliveryType;
	}

	public EnumPaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(EnumPaymentType paymentType) {
		this.paymentType = paymentType;
	}
	
	public void setChefsTable(boolean chefsTable) {
		this.chefsTable = chefsTable;
	}
	
	public boolean isChefsTable() {
		return this.chefsTable;
	}
	
	public void setVip(boolean vip) {
		this.vip = vip;
	}
	
	public boolean isVIP() {
		return this.vip;
	}
	
	public Date getLastCroModDate() {
		return this.lastCroModDate;
	}
	
	public void setLastCroModDate(Date lastCroModDate) {
		this.lastCroModDate = lastCroModDate;
	}
	
	public String geteStore() {
		return eStore;
	}

	public void seteStore(String eStore) {
		this.eStore = eStore;
	}

	public String getFacility() {
		return facility;
	}

	public void setFacility(String facility) {
		this.facility = facility;
	}

	public final static Comparator CustomerNameComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return (o.getLastName() + o.getFirstName()).toLowerCase();
		}
	};

	public final static Comparator CustomerIdComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return new BigDecimal(o.getIdentity().getFDCustomerPK());
		}
	};

	public final static Comparator DeliveryTypeComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getDeliveryType().toLowerCase();
		}
	};

	public final static Comparator PaymentTypeComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getPaymentType() == null ? null : o.getPaymentType().getName();
		}
	};

	public final static Comparator EmailComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getEmail();
		}
	};

	public final static Comparator PhoneComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getPhone();
		}
	};

	public final static Comparator AltPhoneComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getAltPhone();
		}
	};

	public final static Comparator DeliveryDateComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getDeliveryDate();
		}
	};

	public final static Comparator OrderStatusComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getOrderStatus() == null ? null : o.getOrderStatus().getName();
		}
	};

	public final static Comparator SaleIdComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return new BigDecimal(o.getSaleId());
		}
	};

	public final static Comparator AmountComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return new Double(o.getAmount());
		}
	};

	public final static Comparator WaveNumComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getWaveNum() == null ? null : Integer.valueOf(o.getWaveNum());
		}
	};

	public final static Comparator RouteNumComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getRouteNum() == null ? null : Integer.valueOf(o.getRouteNum());
		}
	};

	public final static Comparator StopSequenceComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getStopSequence() == null ? null : Integer.valueOf(o.getStopSequence());
		}
	};

	public final static Comparator CutoffTimeComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getCutoffTime();
		}
	};
	
	public final static Comparator CustomerTypeComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			String type = o.isVIP() ? "vip" : "";
			type += o.isChefsTable() ? "chef" : "";
			return type;
		}
	};

	public final static Comparator StoreComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.geteStore();
		}
	};

	public final static Comparator FacilityComparator = new OrderInfoComparator() {
		protected Comparable getValue(FDCustomerOrderInfo o) {
			return o.getFacility();
		}
	};

	private abstract static class ValueComparator implements Comparator {

		public final int compare(Object o1, Object o2) {
			Comparable v1 = getValue(o1);
			Comparable v2 = getValue(o2);
			if (v1 == null && v2 == null) {
				return 0;
			} else if (v1 == null) {
				return -1;
			} else if (v2 == null) {
				return 1;
			} else {
				return v1.compareTo(v2);
			}
		}

		protected abstract Comparable getValue(Object o);

	}

	private abstract static class OrderInfoComparator extends ValueComparator {

		protected final Comparable getValue(Object o) {
			return getValue((FDCustomerOrderInfo) o);
		}

		protected abstract Comparable getValue(FDCustomerOrderInfo o);

	}


}