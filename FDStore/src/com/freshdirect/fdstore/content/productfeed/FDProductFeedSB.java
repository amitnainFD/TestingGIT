
package com.freshdirect.fdstore.content.productfeed;


import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBObject;
import javax.ejb.FinderException;

import com.freshdirect.content.nutrition.ErpNutritionModel;
import com.freshdirect.content.nutrition.panel.NutritionPanel;
import com.freshdirect.fdstore.FDResourceException;

public interface FDProductFeedSB extends EJBObject{

	 public boolean uploadProductFeed() throws FDResourceException, RemoteException;

}
