package com.freshdirect.fdstore.services.tax;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.freshdirect.customer.EnumChargeType;
import com.freshdirect.customer.EnumSaleStatus;
import com.freshdirect.customer.ErpAddressModel;
import com.freshdirect.customer.ErpChargeLineModel;
import com.freshdirect.customer.ErpInvoiceLineI;
import com.freshdirect.fdstore.FDStoreProperties;
import com.freshdirect.fdstore.customer.FDCartI;
import com.freshdirect.fdstore.customer.FDCartLineI;
import com.freshdirect.fdstore.customer.FDOrderI;
import com.freshdirect.fdstore.services.tax.data.Address;
import com.freshdirect.fdstore.services.tax.data.DetailLevel;
import com.freshdirect.fdstore.services.tax.data.DocType;
import com.freshdirect.fdstore.services.tax.data.GetTaxRequest;
import com.freshdirect.fdstore.services.tax.data.Line;
import com.freshdirect.payment.EnumPaymentMethodType;

public class AvalaraTaxRequestConverter {
	private static final Logger LOGGER = Logger.getLogger(AvalaraTaxRequestConverter.class);
	
	public GetTaxRequest convert(AvalaraContext avalaraContext){
		LOGGER.info("Cart to Request conversion begin");
		FDCartI cart = avalaraContext.getCart();
		GetTaxRequest taxRequest = new GetTaxRequest();
		ErpAddressModel fdDeliveryaddress = cart.getDeliveryAddress();
		Address deliveryAddress = convertAddress(fdDeliveryaddress);
		Address[] originAddresses = getDefaultOriginAddresses();
		Address[] taxAddresses = (Address[]) ArrayUtils.add(originAddresses, deliveryAddress);
		List<Line> taxLines = new ArrayList<Line>();
		
		boolean isSalesTaxWaived = false;
		if(cart.getPaymentMethod() != null && EnumPaymentMethodType.EBT.equals(cart.getPaymentMethod().getPaymentMethodType())){
			isSalesTaxWaived = true;
		}
		
		//Sales Tax is illegal on EBT as per federal law.
		if(!isSalesTaxWaived){
			createCartLinesTaxEntry(cart, taxLines);
		}
		
		createAdditionalChargeTaxEntry(cart, taxLines);
		
		Line[] linesArray = new Line[taxLines.size()];
		taxRequest.setLines(taxLines.toArray(linesArray));

		taxRequest.setAddresses(taxAddresses);
		taxRequest.setDocDate(new Date(System.currentTimeMillis()));
		taxRequest.setCompanyCode(FDStoreProperties.getAvalaraCompanyCode());
		taxRequest.setDocCode(cart.getDeliveryReservation().getOrderId());
		if(isTaxCommitRequired(cart)){
			taxRequest.setDocType(DocType.SalesInvoice);
			taxRequest.setCommit(Boolean.TRUE);
		} else {
			taxRequest.setDocType(DocType.SalesOrder);
			taxRequest.setCommit(Boolean.FALSE);
		}
		
		taxRequest.setCurrencyCode("USD");
		taxRequest.setDetailLevel(DetailLevel.Line);
		taxRequest.setCustomerCode("DEFAULT_CUSTOMER_CODE");
		LOGGER.info("Cart to Request conversion End");
		return taxRequest;
	}
	
	private void createAdditionalChargeTaxEntry(FDCartI cart, List<Line> taxLines){
		Collection<ErpChargeLineModel> charges = cart.getCharges();
		if(CollectionUtils.isNotEmpty(charges)){
			for(ErpChargeLineModel charge: charges){
				if(charge.getAmount() > 0.0d){
					Line taxLine = new Line();
					EnumChargeType chargeType = charge.getType();
					String itemCode = StringUtils.defaultString(chargeType.getMaterialNumber(),chargeType.getName());
					taxLine.setItemCode(itemCode);
					taxLine.setLineNo(charge.getType().getCode());
					taxLine.setAmount(BigDecimal.valueOf(charge.getAmount()));
					taxLine.setOriginCode("Origin-01");
					taxLine.setDestinationCode("DEST-01");
					taxLine.setQty(BigDecimal.valueOf(1));
					taxLines.add(taxLine);
				}
			}
		}
	}
	
	private void createCartLinesTaxEntry(FDCartI cart, List<Line> taxLines){

		if(CollectionUtils.isNotEmpty(cart.getOrderLines())){
			for(FDCartLineI line:cart.getOrderLines()){
				
				ErpInvoiceLineI invoice =  line.getInvoiceLine();
				//invoice.
				double price = line.getPrice();
				double quantity = line.getQuantity();
				if(invoice != null){
					price = invoice.getPrice();
					quantity = invoice.getQuantity();
				}
				
				Line taxLine = new Line();
				taxLine.setItemCode(line.getSkuCode());
				
				
				taxLine.setLineNo(StringUtils.defaultIfEmpty(line.getCartlineId(),line.getOrderLineId()));
				taxLine.setAmount(BigDecimal.valueOf(price));
				taxLine.setTaxIncluded(false);
				taxLine.setDiscounted(true);
				taxLine.setOriginCode("Origin-01");
				taxLine.setDestinationCode("DEST-01");
				taxLine.setDescription(line.getDescription());
				taxLine.setQty(BigDecimal.valueOf(quantity));
				taxLines.add(taxLine);
			}
		}
	}
	
	private boolean isTaxCommitRequired(FDCartI cart){
		boolean isTaxCommitRequired = false;
		/* Determines whether tax commit required or not. Preferred to commit the tax after order
		 * fulfillment. Otherwise calculate the tax alone.
		 */
		List<EnumSaleStatus> commitStatuses = Arrays.asList(EnumSaleStatus.SETTLED);
		
		if(cart instanceof FDOrderI){
			FDOrderI order = (FDOrderI) cart;
			EnumSaleStatus status = order.getOrderStatus();
			if(commitStatuses.contains(status)){
				isTaxCommitRequired = true;
				LOGGER.info("Cart details sent to Avalara to Commit the transaction : ");
			}
		}
		return isTaxCommitRequired;
	}
	
	private Address[] getDefaultOriginAddresses(){
		Address address = new Address();
		address.setAddressCode("Origin-01");
		address.setLine1("23 Borden Avenue");
		address.setCity("Long Island City");
		address.setCountry("US");
		address.setPostalCode("11101");
		Address[] originAddresses = {address};
		return originAddresses;
	}
	
	private Address convertAddress(ErpAddressModel fdAddress){
		Address address = null;
		if(fdAddress != null){
			address = new Address();
			address.setAddressCode("DEST-01");
			//address.setAddressType(convertAddressType(fdAddress.getAddressType()));
			address.setLine1(fdAddress.getAddress1());
			address.setLine2(fdAddress.getAddress2());
			address.setCity(fdAddress.getCity());
			address.setCountry(fdAddress.getCountry());
			address.setRegion(fdAddress.getState());
			address.setPostalCode(fdAddress.getZipCode());
		}
		return address;
	}
}