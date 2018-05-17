package com.freshdirect.fdstore.promotion;

import java.util.Date;

public class DateRangeStrategy implements PromotionStrategyI {

	private final Date startDate;
	private final Date expirationDate;

	/**
	 * @param expirationDate can be null
	 */
	public DateRangeStrategy(Date startDate, Date expirationDate) {
		if (startDate == null) {
			throw new IllegalArgumentException("startDate cannot be null");
		}
		this.startDate = startDate;
		this.expirationDate = expirationDate;
	}

	public Date getExpirationDate() {
		return expirationDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	@Override
	public int evaluate(String promotionCode, PromotionContextI context) {
		Date now = new Date();
		if (now.before(startDate) || (expirationDate != null && now.after(expirationDate))) {
			return DENY;
		}

		return ALLOW;
	}

	@Override
	public int getPrecedence() {
		return 20;
	}

	public String toString() {
		return "DateRangeStrategy[from " + this.startDate + (this.expirationDate == null ? "" : " to " + this.expirationDate) + "]";
	}

	@Override
	public boolean isStoreRequired() {
		return false;
	}
}
