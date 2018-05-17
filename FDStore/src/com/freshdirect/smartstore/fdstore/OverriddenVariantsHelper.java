package com.freshdirect.smartstore.fdstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.FDRuntimeException;
import com.freshdirect.fdstore.customer.FDUserI;
import com.freshdirect.fdstore.customer.ProfileModel;
import com.freshdirect.fdstore.util.EnumSiteFeature;
import com.freshdirect.smartstore.Variant;
import com.freshdirect.smartstore.service.VariantRegistry;

public class OverriddenVariantsHelper {
	private static final String PROFILE_KEY = "OverrideVariants";

	public static class VariantInfo {
		public String variant = null;
		public boolean exists = false; // variant exists
		public boolean duplicate = false;
		public EnumSiteFeature feature = null;

		public VariantInfo(String variant) {
			this.variant = variant;
		}

		public boolean isValid() {
			return exists && !duplicate && feature != null;
		}
	}

	public static class VariantInfoList {
		List<VariantInfo> info;

		public VariantInfoList(List<VariantInfo> infoList) {
			this.info = infoList;
		}

		public VariantInfo get(EnumSiteFeature feature) {
			if (feature != null) {
				for (Iterator<VariantInfo> it = info.iterator(); it.hasNext();) {
					VariantInfo vi = it.next();
					if (feature.equals(vi.feature))
						return vi;
				}
			}
			return null;
		}

		public boolean hasEntries() {
			return info.size() > 0;
		}

		public Iterator<VariantInfo> iterator() {
			return info.iterator();
		}
	}

	public static VariantInfoList consolidateVariantsList(List<String> variantList) {
		ArrayList<EnumSiteFeature> features = new ArrayList<EnumSiteFeature>();
		HashMap<EnumSiteFeature, List<String>> featVarsMap = new HashMap<EnumSiteFeature, List<String>>();
		ArrayList<VariantInfo> variantInfoList = new ArrayList<VariantInfo>();

		// fill feature->variants map
		for (Iterator<EnumSiteFeature> it = EnumSiteFeature
				.getSmartStoreEnumList().iterator(); it.hasNext();) {
			EnumSiteFeature feature = it.next();
			featVarsMap.put(feature, VariantSelection.getInstance()
					.getVariants(feature));
		}

		for (Iterator<String> it = variantList.iterator(); it.hasNext();) {
			String v = it.next();

			VariantInfo vi = new VariantInfo(v);

			for (Iterator<EnumSiteFeature> fit = EnumSiteFeature
					.getSmartStoreEnumList().iterator(); fit.hasNext();) {
				EnumSiteFeature feature = fit.next();

				if (featVarsMap.get(feature).contains(v)) {
					vi.feature = feature;
					vi.exists = true;
					vi.duplicate = features.contains(feature);

					if (!features.contains(feature))
						features.add(feature);
				}
			}

			variantInfoList.add(vi);
		}

		return new VariantInfoList(variantInfoList);
	}

	public static boolean AllowAnonymousUsers = false;
	
	public static List<String> getOverriddenVariantIds(FDUserI user) {
		
		if(!AllowAnonymousUsers && (user.getPrimaryKey() == null || user.getPrimaryKey().length() == 0))
			throw new IllegalArgumentException("user must not be anonymous");
		
		List<String> list = new ArrayList<String>();

		String value;
		try {
			value = user.getFDCustomer().getProfile().getAttribute(PROFILE_KEY);
			if (value != null) {
				StringTokenizer st = new StringTokenizer(value, ",");
				while (st.hasMoreTokens()) {
					String ov = st.nextToken().trim();
					if (ov.length() != 0)
						list.add(ov);
				}
			}
		} catch (IllegalStateException e) {
			// cannot retrieve FD Customer as it is an anonymouse
			return list;
		} catch (FDResourceException e) {
			throw new FDRuntimeException(e, "failed to retrieve overridden variant profile");
		}

		return list;
	}
	
	public static void saveOverriddenVariantIds(FDUserI user, List<String> variantsList)
			throws FDResourceException {
		if (user.getPrimaryKey() == null || user.getPrimaryKey().length() == 0)
			throw new IllegalArgumentException(
					"user must not be anonymous");

		ProfileModel profile = user.getFDCustomer().getProfile();

		if (variantsList == null || variantsList.size() == 0) {
			profile.removeAttribute(PROFILE_KEY);
		} else {
			StringBuffer buf = new StringBuffer();
			final int s = variantsList.size();
			for (int i = 0; i < s; ++i) {
				buf.append(variantsList.get(i).trim());
				if (i < s - 1)
					buf.append(",");
			}
			profile.setAttribute(PROFILE_KEY, buf.toString());
		}
	}

	public static Variant getOverriddenVariant(FDUserI user,
			EnumSiteFeature feature) {
		Map<String, Variant> variants = VariantRegistry.getInstance().getServices(feature);

		for (Iterator<String> it = getOverriddenVariantIds(user).iterator(); it.hasNext();) {
			String vId = it.next();
			if (variants.containsKey(vId))
				return variants.get(vId);
		}

		return null;
	}
}
