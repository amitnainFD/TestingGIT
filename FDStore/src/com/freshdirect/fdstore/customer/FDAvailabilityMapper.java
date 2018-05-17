package com.freshdirect.fdstore.customer;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Category;

import com.freshdirect.common.address.AddressModel;
import com.freshdirect.common.pricing.MunicipalityInfo;
import com.freshdirect.common.pricing.MunicipalityInfoWrapper;
import com.freshdirect.customer.ErpCreateOrderModel;
import com.freshdirect.delivery.restriction.DlvRestrictionsList;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionCriterion;
import com.freshdirect.delivery.restriction.EnumDlvRestrictionReason;
import com.freshdirect.delivery.restriction.FDRestrictedAvailability;
import com.freshdirect.delivery.restriction.FDSpecialRestrictedAvailability;
import com.freshdirect.delivery.restriction.RestrictionI;
import com.freshdirect.fdlogistics.model.FDReservation;
import com.freshdirect.fdstore.FDDeliveryManager;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.atp.FDAvailabilityI;
import com.freshdirect.fdstore.atp.FDMuniAvailability;
import com.freshdirect.fdstore.atp.NullAvailability;
import com.freshdirect.fdstore.util.RestrictionUtil;
import com.freshdirect.framework.util.DateRange;
import com.freshdirect.framework.util.DateUtil;
import com.freshdirect.framework.util.log.LoggerFactory;
import com.freshdirect.sap.PosexUtil;

class FDAvailabilityMapper {

	private final static Category LOGGER = LoggerFactory.getInstance(FDAvailabilityMapper.class);
	
	private FDAvailabilityMapper() {
	}

	/**
	 * @param fdInvs Map of orderLineNumber -> FDInventoryI
	 * @return Map of cartLineId -> FDInventoryI
	 */
	public static Map<String, FDAvailabilityI> mapInventory(FDCartModel cart, ErpCreateOrderModel order, Map<String,FDAvailabilityI> fdInvs, boolean skipModifyLines, boolean sameDeliveryDate) throws FDResourceException {
		Map<String, FDAvailabilityI> cartInvMap = new HashMap<String, FDAvailabilityI>();

		DlvRestrictionsList allRestrictions = FDDeliveryManager.getInstance().getDlvRestrictions();
		MunicipalityInfoWrapper muni = FDDeliveryManager.getInstance().getMunicipalityInfos();

		Date now = new Date();
		DateRange nextDay = getNextDay(now);
		AddressModel address = cart.getDeliveryAddress();
		String county = FDDeliveryManager.getInstance().getCounty(address);
		
		boolean originalDayKept = false;
		if (cart instanceof FDModifyCartModel) {
			FDReservation originalReservation = ((FDModifyCartModel) cart).getOriginalOrder().getDeliveryReservation();
			Date d1 = DateUtil.truncate(cart.getDeliveryReservation().getStartTime());
			Date d2 = DateUtil.truncate(originalReservation.getStartTime());
			originalDayKept = d1.equals(d2);
		}

		int pos = 0;
		for ( FDCartLineI cartline : cart.getOrderLines() ) {
			String posex = PosexUtil.getPosex(pos);
			int orderlineSize = cartline.getErpOrderLineSize();

			FDAvailabilityI inv;
			
				//Regular Availability item.
				if (skipModifyLines && (cartline instanceof FDModifyCartLineI)) {
					// orderlines that came from the original order are always available
					inv = NullAvailability.AVAILABLE;

				} else {
					inv = fdInvs.get(posex);

				}
			


			Set<EnumDlvRestrictionReason> applicableRestrictions = cartline.getApplicableRestrictions();
			
			LOGGER.debug(" applicable restrictions :"+applicableRestrictions);

			if (!applicableRestrictions.isEmpty()) {

				// apply delivery restrictions
				List<RestrictionI> r = allRestrictions.getRestrictions(EnumDlvRestrictionCriterion.DELIVERY, applicableRestrictions);
				LOGGER.debug(" filtered applicable restrictions :"+applicableRestrictions);

				if (!r.isEmpty()) {
					//Filter Alcohol restrictions by current State and county.
					List<RestrictionI> filteredList = RestrictionUtil.filterAlcoholRestrictionsForStateCounty(address.getState(), county, r);
					if(!filteredList.isEmpty())
						inv = new FDRestrictedAvailability(inv, new DlvRestrictionsList(filteredList));
				}

				if (!originalDayKept || !(cartline instanceof FDModifyCartLineI)) {
					// apply purchase-time restrictions
					r = allRestrictions.getRestrictions(EnumDlvRestrictionCriterion.PURCHASE, applicableRestrictions);
					if (!r.isEmpty()) {
						inv = new FDSpecialRestrictedAvailability(inv, new DlvRestrictionsList(r), nextDay, new DateRange(now, now));
					}
				}

				// apply cutoff-time restrictions
				r = allRestrictions.getRestrictions(EnumDlvRestrictionCriterion.CUTOFF, applicableRestrictions);
				if (!r.isEmpty()) {
					FDReservation rsv = cart.getDeliveryReservation();
					DateRange cutoff = new DateRange(rsv.getCutoffTime(), rsv.getCutoffTime());
					DateRange delivery = new DateRange(rsv.getStartTime(), rsv.getEndTime());
					inv = new FDSpecialRestrictedAvailability(inv, new DlvRestrictionsList(r), delivery, cutoff);
				}

			}


			if (applicableRestrictions.contains(EnumDlvRestrictionReason.ALCOHOL)) {

				MunicipalityInfo muniInfo = muni.getMunicipalityInfo(address.getState(), county, address.getCity());

				if (muniInfo.isAlcoholRestricted()) {
					inv = new FDMuniAvailability(muniInfo);
				}
			}

			cartInvMap.put(Integer.toString(cartline.getRandomId()), inv);

			if (skipModifyLines && cartline instanceof FDModifyCartLineI) {
				// orderlines that came from the original order were skipped
				continue;
			}

			pos += orderlineSize;
		}

		return cartInvMap;
	}

	private static DateRange getNextDay(Date baseDate) {
		Date nextDayStart = getTruncatedDelta(baseDate, 1);
		Date nextDayEnd = getTruncatedDelta(baseDate, 2);
		return new DateRange(nextDayStart, nextDayEnd);
	}

	private static Date getTruncatedDelta(Date base, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(base);
		c.add(Calendar.DATE, days);
		c = DateUtil.truncate(c);
		return c.getTime();
	}

}
