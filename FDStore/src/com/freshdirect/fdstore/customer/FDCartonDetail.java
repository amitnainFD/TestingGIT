/*
 * Created on Mar 7, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.freshdirect.fdstore.customer;

import java.io.Serializable;
import java.util.Iterator;

import com.freshdirect.customer.ErpCartonDetails;

/**
 * @author htai
 *
 */
public class FDCartonDetail implements Serializable {
	private static final long serialVersionUID = 1301122350475006179L;

	private FDCartonInfo cartonInfo;
	private ErpCartonDetails cartonDetail;
	private FDCartLineI cartLine;

	public FDCartonDetail(FDCartonInfo cartonInfo, ErpCartonDetails cartonDetail, FDCartLineI cartLine) {
		this.cartonInfo = cartonInfo;
		this.cartonDetail = cartonDetail;
		this.cartLine = cartLine;
	}

	public ErpCartonDetails getCartonDetail() {
		return this.cartonDetail;
	}

	public FDCartonInfo getCartonInfo() {
		return this.cartonInfo;
	}

	public FDCartLineI getCartLine() {
		return cartLine;
	}

	public boolean isShortShipped(){
		boolean isShortShipped = false;
		if(null != cartonDetail){
			isShortShipped = cartonDetail.isShortShipped();
			if(!isShortShipped){
				for (Iterator<ErpCartonDetails> iterator = cartonDetail.getComponents().iterator(); iterator
						.hasNext();) {
					ErpCartonDetails cartonDetails = (ErpCartonDetails) iterator.next();
					if(cartonDetails.isShortShipped()){
						isShortShipped = cartonDetails.isShortShipped();
						break;
					}
					
				}
			}
		}
		return isShortShipped;
	}
}
