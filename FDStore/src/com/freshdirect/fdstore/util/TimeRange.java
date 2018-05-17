package com.freshdirect.fdstore.util;

import java.io.Serializable;

public class TimeRange implements Serializable, Comparable<TimeRange> {
	private static final long serialVersionUID = -4032454263404806142L;

	public final static int NULL = 0;
	public final static int DAY = 1;
	public final static int WEEK = 2;
	public final static int MONTH = 3;

	public final static int OLDER_THAN = 1;
	public final static int NEWER_THAN = 2;

	private int duration;
	private int daysRangeFrom;
	private int daysRangeTo;
	private int recencyType;
	private int fromValue;
	private int toValue;
	private int sequence;

	public TimeRange(int sequence, int fromValue, int toValue, int duration, int recencyType) {
		this.sequence = sequence;
		this.duration = duration;
		this.recencyType = recencyType;
		this.fromValue = fromValue;
		this.toValue = toValue;

		if (this.duration == DAY) {
			this.daysRangeFrom = fromValue;
		} else if (this.duration == WEEK) {
			this.daysRangeFrom = fromValue * 7;
		} else if (this.duration == MONTH) {
			this.daysRangeFrom = fromValue * 30;
		}

		if (this.duration == DAY) {
			this.daysRangeTo = toValue;
		} else if (this.duration == WEEK) {
			this.daysRangeTo = toValue * 7;
		} else if (this.duration == MONTH) {
			this.daysRangeTo = toValue * 30;
		}
	}

	public int getDaysRangeFrom() {
		return this.daysRangeFrom;
	}

	public int getDaysRangeTo() {
		return this.daysRangeTo;
	}
	
	public void adjustDayRangeFrom(int value) {
		this.daysRangeFrom = value;
	}

	public int getFromValue() {
		return this.fromValue;
	}

	public int getToValue() {
		return this.toValue;
	}

	public int getDuration() {
		return this.duration;
	}

	public int getRecencyType() {
		return this.recencyType;
	}

	public int getSequence() {
		return this.sequence;
	}

	public String getDescription() {
		StringBuffer buf = new StringBuffer();
		if (this.duration == DAY) {
			if (this.fromValue > 0 && this.toValue == 0 && this.recencyType == NEWER_THAN) {
				buf.append("Added in the last ").append(ConvertNumberToText.convert(this.fromValue));
				buf.append((this.fromValue > 1) ? " days" : " day");
			} else if (this.fromValue > 0 && this.toValue == 0 && this.recencyType == OLDER_THAN) {
				buf.append("Added ").append(ConvertNumberToText.convert(this.fromValue));
				buf.append((this.fromValue > 1) ? " days ago" : " day ago");
			} else {
				buf.append("Added ").append(ConvertNumberToText.convert(this.fromValue)).append(" to ").append(
						ConvertNumberToText.convert(this.toValue));
				buf.append((this.toValue > 1) ? " days ago" : " day ago");
			}
		}
		if (this.duration == WEEK) {
			if (this.fromValue > 0 && this.toValue == 0 && this.recencyType == NEWER_THAN) {
				buf.append("Added in the last ").append(ConvertNumberToText.convert(this.fromValue));
				buf.append((this.fromValue > 1) ? " weeks" : " week");
			} else if (this.fromValue > 0 && this.toValue == 0 && this.recencyType == OLDER_THAN) {
				buf.append("Added ").append(ConvertNumberToText.convert(this.fromValue));
				buf.append((this.fromValue > 1) ? " weeks ago" : " week ago");
			} else {
				buf.append("Added ").append(ConvertNumberToText.convert(this.fromValue)).append(" to ").append(
						ConvertNumberToText.convert(this.toValue));
				buf.append((this.toValue > 1) ? " weeks ago" : " week ago");
			}
		}
		if (this.duration == MONTH) {
			if (this.fromValue > 0 && this.toValue == 0 && this.recencyType == NEWER_THAN) {
				buf.append("Added in the last ").append(ConvertNumberToText.convert(this.fromValue));
				buf.append((this.fromValue > 1) ? " months" : " month");
			}
			if (this.fromValue > 0 && this.toValue == 0 && this.recencyType == OLDER_THAN) {
				buf.append("Added ").append(ConvertNumberToText.convert(this.fromValue));
				buf.append((this.fromValue > 1) ? " months ago" : " month ago");
			} else {
				buf.append("Added ").append(ConvertNumberToText.convert(this.fromValue));
				buf.append((this.fromValue > 1) ? " months ago" : " month ago");
			}
		}
		// return this.description;
		return buf.toString();
	}

	@Override
	public String toString() {
		return "TimeRange [sequence=" + sequence + ", fromValue=" + fromValue + ", toValue=" + toValue + ", duration=" + duration
				+ ", recencyType=" + recencyType + ", daysRangeFrom=" + daysRangeFrom + ", daysRangeTo=" + daysRangeTo + "]";
	}

	public int compareTo(TimeRange o1) {
		return new Integer(this.getSequence()).compareTo(new Integer(o1.getSequence()));
	}

	public boolean fallsIn(float value) {
		if (recencyType == NEWER_THAN) {
			if (value < daysRangeFrom)
				return true;
			else
				return false;
		} else if (recencyType == OLDER_THAN) {
			if (value >= daysRangeFrom)
				return true;
			else
				return false;
		} else {
			if (value >= daysRangeFrom && value < daysRangeTo)
				return true;
			else
				return false;
		}
	}
}
