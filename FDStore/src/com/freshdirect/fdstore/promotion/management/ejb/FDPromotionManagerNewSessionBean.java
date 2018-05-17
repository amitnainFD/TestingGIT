package com.freshdirect.fdstore.promotion.management.ejb;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.FinderException;

import org.apache.log4j.Logger;

import com.freshdirect.crm.CrmAgentModel;
import com.freshdirect.fdstore.FDResourceException;
import com.freshdirect.fdstore.customer.ejb.FDSessionBeanSupport;
import com.freshdirect.fdstore.promotion.EnumPromoChangeType;
import com.freshdirect.fdstore.promotion.EnumPromotionStatus;
import com.freshdirect.fdstore.promotion.Promotion;
import com.freshdirect.fdstore.promotion.PromotionDecorator;
import com.freshdirect.fdstore.promotion.PromotionI;
import com.freshdirect.fdstore.promotion.ejb.FDPromotionNewDAO;
import com.freshdirect.fdstore.promotion.management.FDDuplicatePromoFieldException;
import com.freshdirect.fdstore.promotion.management.FDPromoChangeModel;
import com.freshdirect.fdstore.promotion.management.FDPromoCustNotFoundException;
import com.freshdirect.fdstore.promotion.management.FDPromoTypeNotFoundException;
import com.freshdirect.fdstore.promotion.management.FDPromotionNewModel;
import com.freshdirect.fdstore.promotion.management.WSAdminInfo;
import com.freshdirect.fdstore.promotion.management.WSPromotionInfo;
import com.freshdirect.framework.core.PrimaryKey;
import com.freshdirect.framework.util.log.LoggerFactory;

public class FDPromotionManagerNewSessionBean extends FDSessionBeanSupport {
	
	private static final long serialVersionUID = -8621998126945347113L;

	private final static Logger LOGGER = LoggerFactory.getInstance(FDPromotionManagerNewSessionBean.class);

	private static SimpleDateFormat SDF = new SimpleDateFormat("dd-MMM-yyyy");

	public List<FDPromotionNewModel> getPromotions() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			List<FDPromotionNewModel> promoList = FDPromotionManagerNewDAO.getPromotions(conn);

			return promoList;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}

	public List<FDPromotionNewModel> getModifiedOnlyPromotions(Date lastModified) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			List<FDPromotionNewModel> promoList = FDPromotionManagerNewDAO.getModifiedOnlyPromotions(conn, lastModified);

			return promoList;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}
	
	public FDPromotionNewModel getPromotion(String promoId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDPromotionNewModel promotion = FDPromotionManagerNewDAO.getPromotion(conn, promoId);
			return promotion;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}

	public List<WSPromotionInfo> getWSPromotionInfos(Date fromDate, Date toDate, Date dlvDate, String zone, String status) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			List<WSPromotionInfo> promotions = FDPromotionManagerNewDAO.getWSPromotionInfos(conn, fromDate, toDate, dlvDate, zone, status);
			return promotions;
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	public WSPromotionInfo getWSPromotionInfo(String zoneCode, String startTime, 
					String endTime, Date effectiveDate, String[] windowTypes) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.getWSPromotionInfo(conn, zoneCode, startTime, endTime, 
					new java.sql.Date(effectiveDate.getTime()), windowTypes);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
		
	}
	
	public FDPromotionNewModel getPromotionByPk(String pk) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDPromotionNewModel promotion = FDPromotionManagerNewDAO.getPromotionByPk(conn, pk);
			return promotion;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}

	
	public PrimaryKey createPromotion(FDPromotionNewModel promotion)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();

			PrimaryKey pk = FDPromotionManagerNewDAO.createPromotion(conn, promotion);

			return pk;
		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Promotion code or redemption code already exists.");
			/* } else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			*/ } else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	
	
	public void storePromotion(FDPromotionNewModel promotion, boolean saveLog)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();

			FDPromotionManagerNewDAO.updatePromotion(conn, promotion);

			if (saveLog) {
				storePromoChangeLog(promotion, conn, promotion.getPK());
			}

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	

	public void createPromotions(List<FDPromotionNewModel> promotions)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();

			FDPromotionManagerNewDAO.createPromotions(conn, promotions);

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	

	public List<FDPromotionNewModel> getPublishablePromos() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			List<FDPromotionNewModel> promoList = FDPromotionManagerNewDAO.getPublishablePromos(conn);

			return promoList;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}
	
	public List<FDPromoChangeModel> getPromoAuditChanges(String promotionId) throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();

			List<FDPromoChangeModel> promoChanges = FDPromotionManagerNewDAO.getPromoAuditChanges(conn,promotionId);

			return promoChanges;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}

	
	
	public PrimaryKey createPromotionBasic(FDPromotionNewModel promotion)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();
			PrimaryKey pk = FDPromotionManagerNewDAO.createPromotionBasic(conn, promotion);
			storePromoChangeLog(promotion, conn, pk);
			return pk;
		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}



	private void storePromoChangeLog(FDPromotionNewModel promotion,	Connection conn, PrimaryKey pk) throws SQLException {
		FDPromotionManagerNewDAO.storeChangeLogEntries(conn, pk.getId(), promotion.getAuditChanges());
	}



	/**
	 * Store promo change logs for a particular promotion
	 * 
	 * @param conn
	 * @param promoPk Promotion ID
	 * @param changes
	 * @throws SQLException
	 * @throws FDResourceException 
	 */
	public void storeChangeLogEntries(String promoPk, List<FDPromoChangeModel> changes) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			FDPromotionManagerNewDAO.storeChangeLogEntries(conn, promoPk, changes);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}



	public void storePromotionBasic(FDPromotionNewModel promotion)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();
			if(promotion.isBatchPromo()) {
				FDPromotionManagerNewDAO.storePromotionBasicForBatch(conn, promotion);
			} else {
				FDPromotionManagerNewDAO.storePromotionBasic(conn, promotion);
			}
			/*FDPromoChangeModel changeModel = new FDPromoChangeModel();
			changeModel.setPromotionId(promotion.getId());
			changeModel.setActionDate(promotion.getModifiedDate());
			changeModel.setActionType(EnumPromoChangeType.MODIFY);
			changeModel.setUserId(promotion.getModifiedBy());
			FDPromotionManagerNewDAO.savePromoChangeLog(conn, changeModel);	*/
			storePromoChangeLog(promotion, conn, promotion.getPK());

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}


	
	/**
	 * Erases a promotion completely.
	 * 
	 * @param promotionId Promotion PK
	 * 
	 * @throws FDResourceException
	 */
	public void deletePromotion(String promotionId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			FDPromotionManagerNewDAO.deletePromotion(conn, promotionId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}


	public boolean isPromotionCodeUsed(String promoCode) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			return FDPromotionManagerNewDAO.isPromotionCodeUsed(conn, promoCode);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}

	public boolean isPromotionNameUsed(String promoName) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			return FDPromotionManagerNewDAO.isPromotionNameUsed(conn, promoName);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}

	public String findPromotion(String promoCode) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			return FDPromotionManagerNewDAO.findPromotion(conn, promoCode);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}

	public boolean publishPromotion(FDPromotionNewModel promo) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			final boolean result = FDPromotionManagerNewDAO.publishPromotion(conn, promo);
			if (result) {
				// log positive result
				
				// reload promo
				promo = FDPromotionManagerNewDAO.getPromotion(conn, promo.getPromotionCode());
				
				// List<FDPromoChangeModel> changes = new ArrayList<FDPromoChangeModel>(1);

				FDPromoChangeModel changeModel = null;
				if (promo.getPublishes() == 1) {
					changeModel = new FDPromoChangeModel();
					changeModel.setPromotionId(promo.getId());
					changeModel.setActionDate(promo.getLastPublishedDate());
					changeModel.setActionType(EnumPromoChangeType.CREATE);
					changeModel.setUserId(promo.getCreatedBy());

					// changes.add(changeModel);
					
				} else if (promo.getPublishes() > 1) {
					changeModel = new FDPromoChangeModel();
					changeModel.setPromotionId(promo.getId());
					changeModel.setActionDate(promo.getLastPublishedDate());
					changeModel.setActionType(EnumPromoChangeType.MODIFY);
					changeModel.setUserId(promo.getCreatedBy());

					// changes.add(changeModel);
				}
				
				// promo.setAuditChanges(changes);
				
				if (changeModel != null)
					FDPromotionManagerNewDAO.savePromoChangeLog(conn, changeModel);
			}
			return result;
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}

	public boolean cancelPromotion(FDPromotionNewModel promo) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			final boolean result = FDPromotionManagerNewDAO.cancelPromotion(conn, promo);
			if (result) {
				// log positive result

				// reload promo
				promo = FDPromotionManagerNewDAO.getPromotion(conn, promo.getPromotionCode());
				
				// List<FDPromoChangeModel> changes = new ArrayList<FDPromoChangeModel>(1);

				FDPromoChangeModel changeModel = new FDPromoChangeModel();
				changeModel.setPromotionId(promo.getId());
				changeModel.setActionDate(promo.getLastPublishedDate());
				changeModel.setActionType(EnumPromoChangeType.CANCEL);
				changeModel.setUserId(promo.getCreatedBy());

				// changes.add(changeModel);
					
				
				FDPromotionManagerNewDAO.savePromoChangeLog(conn, changeModel);
			}
			return result;
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}


	/**
	 * Post operation called after a successful publish
	 * 
	 * @param codes Collection of Promotion Codes
	 * @return
	 * @throws FDResourceException
	 */
	public boolean fixPromoStatusAfterPublish(Collection<String> codes) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			return FDPromotionManagerNewDAO.fixPromoStatusAfterPublish(conn, codes);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}

	
	
	/**
	 * Log publish event to database
	 * 
	 * @param agent CRM agent who made the publish
	 * @param start
	 * @param end
	 * @param destURL
	 * @param publishResults
	 * @param preStatuses
	 * @param postStatuses
	 * @param changeIDs
	 * @throws FDResourceException
	 */
	public void logPublishEvent(CrmAgentModel agent,
			java.util.Date start, java.util.Date end, String destURL,
			Map<String, Boolean> publishResults,
			Map<String, EnumPromotionStatus> preStatuses,
			Map<String, EnumPromotionStatus> postStatuses,
			Map<String, String> changeIDs) throws FDResourceException {
		
		Connection conn = null;
		try {
			conn = getConnection();

			FDPromotionManagerNewDAO.logPublishEvent(conn, agent.getUserId(), 
					start, end, 
					destURL,
					publishResults, preStatuses, postStatuses, changeIDs);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
		    close(conn);
		}
	}

	public void storePromotionOfferInfo(FDPromotionNewModel promotion)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
	Connection conn = null;
		try {
			conn = getConnection();

			if(promotion.isBatchPromo()) {				
				FDPromotionManagerNewDAO.storePromotionOfferInfoForBatch(conn, promotion);
			} else {
				FDPromotionManagerNewDAO.storePromotionOfferInfo(conn, promotion);
			}
			storePromoChangeLog(promotion, conn, promotion.getPK());

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
		
	}
	
	public void storePromotionCartInfo(FDPromotionNewModel promotion)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();

			if(promotion.isBatchPromo()) {
				FDPromotionManagerNewDAO.storePromotionCartInfoForBatch(conn, promotion);
			} else {
				FDPromotionManagerNewDAO.storePromotionCartInfo(conn, promotion);
			}
			storePromoChangeLog(promotion, conn, promotion.getPK());

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	
	public void storePromotionPaymentInfo(FDPromotionNewModel promotion)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();

			if(promotion.isBatchPromo()) {
				FDPromotionManagerNewDAO.storePromotionPaymentInfoForBatch(conn, promotion);
			} else {
				FDPromotionManagerNewDAO.storePromotionPaymentInfo(conn, promotion);
			}
			storePromoChangeLog(promotion, conn, promotion.getPK());

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	
	public void storePromotionDlvZoneInfo(FDPromotionNewModel promotion)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();

			if(promotion.isBatchPromo()) {
				FDPromotionManagerNewDAO.storePromotionDlvZoneInfoForBatch(conn, promotion);
			} else {
				FDPromotionManagerNewDAO.storePromotionDlvZoneInfo(conn, promotion);
			}
			storePromoChangeLog(promotion, conn, promotion.getPK());

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	
	public void storePromotionCustReqInfo(FDPromotionNewModel promotion)
	throws FDResourceException, FDDuplicatePromoFieldException,
	FDPromoTypeNotFoundException, FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();

			if(promotion.isBatchPromo()) {
				FDPromotionManagerNewDAO.storePromotionCustReqInfoForBatch(conn, promotion);
			} else {
				FDPromotionManagerNewDAO.storePromotionCustReqInfo(conn, promotion);
			}
			storePromoChangeLog(promotion, conn, promotion.getPK());

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf("unique") > -1) {
				throw new FDDuplicatePromoFieldException(
						"Name or promotion code or redemption code already exists.");
			} else if (sqle.getMessage().toLowerCase().indexOf("not found") > -1) {
				throw new FDPromoTypeNotFoundException(
						"Promotion type not found");
			} else if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle);
			}
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}

	/**
	 * @throws FDResourceException 
	 * 
	 */
	public List<PromotionI> getModifiedOnlyPromos(Date lastModified) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			List<PromotionI> promoList = FDPromotionNewDAO.loadModifiedOnlyPromotions(conn, lastModified);
			for (ListIterator<PromotionI> i = promoList.listIterator(); i.hasNext();) {
				PromotionI promo = i.next();
				//This decorate method is called for SIGNUP promos.
				PromotionDecorator.getInstance().decorate(promo);
				//Make sure Promo has a valid applicator.
				if (!((Promotion)promo).isValid()) {
					
					LOGGER.warn("Incomplete promotion configuration for " + promo);
					i.remove();
				}
			}			
			return promoList;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new EJBException(sqle);
		} finally {
            close(conn);
		}
	}
	
	/**
	 * @throws FDResourceException 
	 * 
	 */
	public List<PromotionI> getAllAutomaticPromotions() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();

			List<PromotionI> promoList = FDPromotionNewDAO.loadAllAutomaticPromotions(conn);
			for (ListIterator<PromotionI> i = promoList.listIterator(); i.hasNext();) {
				PromotionI promo = i.next();
				//This decorate method is called for SIGNUP promos.
				PromotionDecorator.getInstance().decorate(promo);
				//Make sure Promo has a valid applicator.
				if (!((Promotion)promo).isValid()) {
					LOGGER.warn("Incomplete promotion configuration for " + promo);
					i.remove();
				}
			}

			return promoList;

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			throw new EJBException(sqle);
		} finally {
            close(conn);
		}
	}
	
	/**
	 * 
	 * @param promoId
	 * @return
	 * @throws FDResourceException 
	 * @throws FinderException
	 * @throws FDResourceException
	 */
	public PromotionI getPromotionForRT(String promoId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			Promotion promotion = (Promotion) FDPromotionNewDAO.loadPromotion(conn, promoId);
			if(promotion == null){
				return null;
			}
			//This decorate method is called for SIGNUP promos.
			PromotionDecorator.getInstance().decorate(promotion);
			//Make sure Promo has a valid applicator.
			if (!promotion.isValid()) {
				LOGGER.warn("Incomplete promotion configuration for " + promotion);
				return null;
			}			
			return promotion;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
             close(conn);
		}
	}
	
	public Integer getRedemptionCount(String promoId, Date requestedDate) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			Integer count = FDPromotionNewDAO.getRedemptions(conn, promoId, requestedDate);
			return count;

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
             close(conn);
		}
	}
	
	public String getRedemptionPromotionId(String redemptionCode) {
		Connection conn = null;
		try {
			conn = getConnection();
			String promoId = FDPromotionNewDAO.getRedemptionPromotionId(conn,
					redemptionCode);
			return promoId;

		} catch (SQLException sqle) {
			throw new EJBException(sqle);
		} finally {
                    close(conn);
		}
	}
	
	public boolean isRedemptionCodeExists(String redemptionCode)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.isRedemptionCodeExists(conn, redemptionCode);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
                    close(conn);
		}
	}
	
	public boolean isRedemptionCodeExists(String redemptionCode, String promotionId)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.isRedemptionCodeExists(conn, redemptionCode, promotionId);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
                    close(conn);
		}
	}
	
	public boolean isRafPromoCodeExists(String rafPromoCode)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.isRafPromoCodeExists(conn, rafPromoCode);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
                    close(conn);
		}
	}
	
	public boolean isRafPromoCodeExists(String rafPromoCode, String promotionId)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.isRafPromoCodeExists(conn, rafPromoCode, promotionId);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
                    close(conn);
		}
	}

	
	public boolean isTSAPromoCodeExists(String tsaPromoCode)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.isTSAPromoCodeExists(conn, tsaPromoCode);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
                    close(conn);
		}
	}
	
	public boolean isTSAPromoCodeExists(String tsaPromoCode, String promotionId)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.isTSAPromoCodeExists(conn, tsaPromoCode, promotionId);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
                    close(conn);
		}
	}
	
	public String getRedemptionCode(String tsaPromoCode)throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionNewDAO.getRedemptionCode(conn, tsaPromoCode);

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
                    close(conn);
		}
	}
	
	public void storePromotionStatus(FDPromotionNewModel promotion,EnumPromotionStatus status) throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();

			if(promotion.isBatchPromo()) {
				FDPromotionManagerNewDAO.storePromotionStatusForBatch(conn, status, promotion);
			} else {
				FDPromotionManagerNewDAO.storePromotionStatus(conn, status, promotion);
			}
			storePromoChangeLog(promotion, conn, promotion.getPK());

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	public void storePromotionHoldStatus(FDPromotionNewModel promotion) throws FDResourceException{
		Connection conn = null;
		try {
			conn = getConnection();

			FDPromotionManagerNewDAO.storePromotionHoldStatus(conn,promotion);
			storePromoChangeLog(promotion, conn, promotion.getPK());

		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public void storeAssignedCustomers(FDPromotionNewModel promotion, String assignedCustomerUserIds) throws FDResourceException,FDPromoCustNotFoundException {
		Connection conn = null;
		try {
			conn = getConnection();

			FDPromotionManagerDAO.storeAssignedCustomers(conn,promotion, assignedCustomerUserIds);

		} catch (SQLException sqle) {
			if (sqle.getMessage().toLowerCase().indexOf(
					"invalid customer id(s)") > -1) {
				throw new FDPromoCustNotFoundException(sqle.getMessage());
			}
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}
	
	public List<String> loadAssignedCustomerUserIds(String promotionId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			List<String> assignedCustomerUserIds =FDPromotionManagerDAO.loadAssignedCustomerUserIds(conn, promotionId);
			return assignedCustomerUserIds;			
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
			close(conn);
		}
	}

	public boolean lookupPromotion(String promotionCode) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.lookupPromotion(conn, promotionCode);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	
	public boolean isCustomerInAssignedList(String userId, String promotionId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.isCustomerInAssignedList(conn, userId, promotionId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	
	public void setDOWLimit(int dayofweek, double limit) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			FDPromotionManagerNewDAO.setDOWLimit(conn, dayofweek, limit);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	
	public Map<Integer, Double> getDOWLimits() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.getDOWLimits(conn);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}		
	}

	public List<WSAdminInfo> getActualAmountSpentByDays() throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.getActualAmountSpentByDays(conn);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}		
	}
	
	public List<WSPromotionInfo> getAllActiveWSPromotions(java.util.Date effectiveDate) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.getAllActiveWSPromotions(effectiveDate, conn);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}		
	}
	
	public List<PromotionI> getReferralPromotions(String customerId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionNewDAO.getReferralPromotions(customerId, conn);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}	
	}
	
	public String createPromotionBatch(FDPromotionNewModel promotion) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			String batchId = FDPromotionManagerNewDAO.createPromotionBatch(conn, promotion);
			//storePromoChangeLog(promotion, conn, pk);
			return batchId;
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}
	
	public List<FDPromotionNewModel> getBatchPromotions(String batchId) throws FDResourceException {
		Connection conn = null;
		try {
			conn = getConnection();
			return FDPromotionManagerNewDAO.getBatchPromotions(conn, batchId);
		} catch (SQLException sqle) {
			throw new FDResourceException(sqle);
		} finally {
            close(conn);
		}
	}	

}
