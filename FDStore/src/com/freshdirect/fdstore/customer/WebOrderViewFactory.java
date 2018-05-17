package com.freshdirect.fdstore.customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.freshdirect.affiliate.ErpAffiliate;
import com.freshdirect.customer.ErpInvoiceLineI;

/**
 * @author vszathmary
 */
public class WebOrderViewFactory {

	private WebOrderViewFactory() {
	}

	// APPDEV-2031 we introduced separate grouping of new items (items recently added to cart being modified)
	public static WebOrderViewI getOrderView(List<FDCartLineI> cartLines, ErpAffiliate affiliate, boolean modified) {
		if (!affiliate.isPrimary() && !hasItemsForAffiliate(cartLines, affiliate)) {
			return null;
		}

		return new OrderView(affiliate, getOrderLinesForAffiliate(cartLines, affiliate), affiliate.isPrimary()
			&& !hasItemsForSecondaryAffiliates(cartLines), modified);
	}

	public static WebOrderViewI getInvoicedOrderView(List<FDCartLineI> cartLines, List<FDCartLineI> sampleLines, ErpAffiliate affiliate) {
		if (!affiliate.isPrimary() && !hasItemsForAffiliate(cartLines, affiliate)) {
			return null;
		}

		InvoicedOrderView ov = new InvoicedOrderView(affiliate, getOrderLinesForAffiliate(cartLines, affiliate), affiliate
			.isPrimary()
			&& !hasItemsForSecondaryAffiliates(cartLines));

		if (affiliate.isPrimary()) {
			List<FDCartLineI> deliveredSamples = new ArrayList<FDCartLineI>();
			for (Iterator<FDCartLineI> i = sampleLines.iterator(); i.hasNext();) {
				FDCartLineI line = (FDCartLineI) i.next();
				if (line.getInvoiceLine().getQuantity() > 0) {
					deliveredSamples.add(line);
				}
			}

			ov.setSampleLines(deliveredSamples);
		}

		return ov;
	}

	public static List<WebOrderViewI> getOrderViews(List<FDCartLineI> cartLines, boolean modified) {
		ArrayList<WebOrderViewI> views = new ArrayList<WebOrderViewI>();
		for (Iterator<ErpAffiliate> i = getShownAffiliates(cartLines).iterator(); i.hasNext();) {
			WebOrderViewI view = getOrderView(cartLines, i.next(), modified);
			if (view != null) {
				views.add(view);
			}
		}
		return views;
	}

	public static List<WebOrderViewI> getInvoicedOrderViews(List<FDCartLineI> cartLines, List<FDCartLineI> sampleLines) {
		ArrayList<WebOrderViewI> views = new ArrayList<WebOrderViewI>();
		for (Iterator<ErpAffiliate> i = getShownAffiliates(cartLines).iterator(); i.hasNext();) {
			WebOrderViewI view = getInvoicedOrderView(cartLines, sampleLines, i.next());
			if (view != null) {
				views.add(view);
			}
		}
		return views;
	}

	private static List<ErpAffiliate> getShownAffiliates(List<FDCartLineI> cartLines) {
		ErpAffiliate[] affils = new ErpAffiliate[] {
			ErpAffiliate.getEnum(ErpAffiliate.CODE_FD),
			ErpAffiliate.getEnum(ErpAffiliate.CODE_FDX),
			ErpAffiliate.getEnum(ErpAffiliate.CODE_WBL),
			ErpAffiliate.getEnum(ErpAffiliate.CODE_USQ),
			ErpAffiliate.getEnum(ErpAffiliate.CODE_FDW),
			ErpAffiliate.getEnum(ErpAffiliate.CODE_BC)};
		List<ErpAffiliate> l = new ArrayList<ErpAffiliate>();
		for (int i = 0; i < affils.length; i++) {
			//if (affils[i].isPrimary() || hasItemsForAffiliate(cartLines, affils[i])) {
			if (hasItemsForAffiliate(cartLines, affils[i])) {
				l.add(affils[i]);
			}
		}
		return l;
	}

	private static List<FDCartLineI> getOrderLinesForAffiliate(List<FDCartLineI> cartLines, ErpAffiliate affiliate) {
		List<FDCartLineI> lines = new ArrayList<FDCartLineI>();
		for (Iterator<FDCartLineI> i = cartLines.iterator(); i.hasNext();) {
			FDCartLineI line = (FDCartLineI) i.next();
			if (affiliate.equals(line.getAffiliate())) {
				lines.add(line);
			}
		}
		return lines;
	}

	private static boolean hasItemsForAffiliate(List<FDCartLineI> cartLines, ErpAffiliate affiliate) {
		return getOrderLinesForAffiliate(cartLines, affiliate).size() > 0;
	}

	private static boolean hasItemsForSecondaryAffiliates(List<FDCartLineI> cartLines) {
		for (Iterator<FDCartLineI> i = cartLines.iterator(); i.hasNext();) {
			FDCartLineI line = (FDCartLineI) i.next();
			if (!line.getAffiliate().isPrimary()) {
				return true;
			}
		}
		return false;
	}

	public static abstract class AbstractOrderView implements WebOrderViewI {

		private final ErpAffiliate affiliate;
		private final List<FDCartLineI> lines;
		private final List<FDCartLineI> newLines;
		private final List<FDCartLineI> oldLines;
		private final boolean hideDescription;
		private List<FDCartLineI> sampleLines = Collections.emptyList();

		protected double tax;
		protected double subtotal;
		protected double depositValue;
		protected double eTip;

		public AbstractOrderView(ErpAffiliate affiliate, List<FDCartLineI> lines, boolean hideDescription, boolean modified) {
			this.affiliate = affiliate;
			this.lines = lines;
			this.hideDescription = hideDescription;
			this.newLines = new ArrayList<FDCartLineI>(lines.size());
			if (modified) {
				this.oldLines = new ArrayList<FDCartLineI>(lines.size());
				for (FDCartLineI line : lines)
					if (line instanceof FDModifyCartLineI)
						oldLines.add(line);
					else
						newLines.add(line);
				
				Collections.sort(newLines, FDCartModel.NAME_COMPARATOR);
			} else {
				this.oldLines = lines; 
			}
		}

		@Override
		public ErpAffiliate getAffiliate() {
			return this.affiliate;
		}

		@Override
		public List<FDCartLineI> getOrderLines() {
			return this.lines;
		}
		
		@Override
		public List<List<FDCartLineI>> getNewOrderLinesSeparated() {
			List<List<FDCartLineI>> a = new ArrayList<List<FDCartLineI>>(2);
			a.add(newLines);
			a.add(oldLines);
			return a;
		}

		@Override
		public List<FDCartLineI> getSampleLines() {
			return this.sampleLines;
		}

		public void setSampleLines(List<FDCartLineI> sampleLines) {
			this.sampleLines = sampleLines;
		}

		@Override
		public boolean isDisplayDepartment() {
			return affiliate.isPrimary();
		}

		@Override
		public String getDescription() {
			return this.hideDescription ? "" : affiliate.getName();
		}

		@Override
		public double getTax() {
			return tax;
		}

		@Override
		public double getDepositValue() {
			return depositValue;
		}

		@Override
		public double getSubtotal() {
			return subtotal;
		}

		@Override
		public boolean isEstimatedPrice() {
			for (Iterator<FDCartLineI> i = getOrderLines().iterator(); i.hasNext();) {
				FDCartLineI line = (FDCartLineI) i.next();
				if (line.isEstimatedPrice()) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public double getETip() {
			return eTip;
		}

	}

	public static class OrderView extends AbstractOrderView {

		public OrderView(ErpAffiliate affiliate, List<FDCartLineI> lines, boolean hideDescription, boolean modified) {
			super(affiliate, lines, hideDescription, modified);

			for (Iterator<FDCartLineI> i = lines.iterator(); i.hasNext();) {
				FDCartLineI line = i.next();
				tax += line.getTaxValue();
				depositValue += line.getDepositValue();
				subtotal += line.getPrice();
			}
		}

	}

	public static class InvoicedOrderView extends AbstractOrderView {

		public InvoicedOrderView(ErpAffiliate affiliate, List<FDCartLineI> lines, boolean hideDescription) {
			super(affiliate, lines, hideDescription, false);

			for (Iterator<FDCartLineI> i = lines.iterator(); i.hasNext();) {
				FDCartLineI line = i.next();

				ErpInvoiceLineI invoiceLine = line.getInvoiceLine();
				tax += invoiceLine.getTaxValue();
				depositValue += invoiceLine.getDepositValue();
				subtotal += invoiceLine.getPrice();
			}
		}

	}

}

