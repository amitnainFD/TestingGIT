package com.freshdirect.fdstore.mail;

import java.util.Collection;

import com.freshdirect.cms.ContentKey;
import com.freshdirect.common.address.PhoneNumber;
import com.freshdirect.common.customer.EnumCardType;
import com.freshdirect.common.pricing.EnumDiscountType;
import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.customer.EnumComplaintLineMethod;
import com.freshdirect.customer.EnumComplaintLineType;
import com.freshdirect.customer.EnumComplaintStatus;
import com.freshdirect.customer.EnumDeliverySetting;
import com.freshdirect.customer.EnumDeliveryType;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpSaleModel;
import com.freshdirect.enums.EnumModel;
import com.freshdirect.framework.xml.XMLSerializer;

public class FDXMLSerializer extends XMLSerializer {

	/**
	 * @see com.freshdirect.framework.xml.XMLSerializer#getExcludedMethods()
	 */
	protected Collection getExcludedMethods() {
		Collection c = super.getExcludedMethods();
		c.add("getAccountNumber");
		return c;
	}

	/**
	 * @see com.freshdirect.framework.xml.XMLSerializer#getPrimitiveClasses()
	 */
	protected Collection getPrimitiveClasses() {
		Collection c = super.getPrimitiveClasses();

		c.add(PhoneNumber.class);
		c.add(EnumCardType.class);
		c.add(EnumSaleStatus.class);

		c.add(EnumTransactionSource.class);
		c.add(EnumChargeType.class);
		c.add(EnumDeliverySetting.class);
		c.add(ErpSaleModel.class);
		c.add(EnumComplaintStatus.class);
		c.add(EnumComplaintLineType.class);
		c.add(EnumComplaintLineMethod.class);
		c.add(EnumDeliveryType.class);

		c.add(EnumDiscountType.class);
		c.add( EnumModel.class );
		
		c.add(ContentKey.class);
		
		return c;
	}

}
