package com.freshdirect.fdstore.customer;

import java.util.Comparator;
import java.util.Date;

import com.freshdirect.framework.core.IdentifiedI;

/**
 * @author ekracoff
 * Created on Oct 4, 2004*/

public interface SaleStatisticsI extends IdentifiedI {
	
	public Date getFirstPurchase();
	
	public Date getLastPurchase();
	
	public int getFrequency();
	
	public final static Comparator FREQUENCY_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			SaleStatisticsI s1 = (SaleStatisticsI) o1;
			SaleStatisticsI s2 = (SaleStatisticsI) o2;
			return s2.getFrequency() - s1.getFrequency();
		}
	};

	public final static Comparator LAST_PURCHASE_COMPARATOR = new Comparator() {
		public int compare(Object o1, Object o2) {
			SaleStatisticsI s1 = (SaleStatisticsI) o1;
			SaleStatisticsI s2 = (SaleStatisticsI) o2;
			return s2.getLastPurchase().compareTo(s1.getLastPurchase());
		}
	};

}
