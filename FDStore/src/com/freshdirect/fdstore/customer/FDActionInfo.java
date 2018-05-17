package com.freshdirect.fdstore.customer;

import java.io.Serializable;

import com.freshdirect.crm.CrmAgentModel;
import com.freshdirect.customer.EnumAccountActivityType;
import com.freshdirect.customer.EnumTransactionSource;
import com.freshdirect.customer.ErpActivityRecord;
import com.freshdirect.fdstore.EnumEStoreId;

public class FDActionInfo implements Serializable {

	private static final long	serialVersionUID	= 5436805819502477394L;
	
	private EnumTransactionSource source;
	private FDIdentity identity;
	private String initiator;
	private String note;
	private EnumAccountActivityType type;
	private CrmAgentModel agent;
    private boolean isPR1;
    private String masqueradeAgent;
    private String fdUserId;
    private EnumEStoreId eStore;
    
    public FDActionInfo(  EnumTransactionSource source, FDIdentity identity, String initiator, String note, CrmAgentModel agent, String fdUserId) {
		this(EnumEStoreId.FD, source, identity, initiator, note, agent, null,fdUserId );
	}
	public FDActionInfo( EnumEStoreId eStore,EnumTransactionSource source, FDIdentity identity, String initiator, String note, CrmAgentModel agent, String fdUserId) {
		this(eStore, source, identity, initiator, note, agent, null,fdUserId );
	}
	
	public FDActionInfo( EnumEStoreId eStore,EnumTransactionSource source, FDIdentity identity, String initiator, String note, CrmAgentModel agent, EnumAccountActivityType type, String fdUserId) {
		this.identity = identity;
		this.source = source;
		this.initiator = initiator;
		this.note = note;
		this.type = type;
		this.agent = agent;		
		this.masqueradeAgent = masqueradeAgentTL.get();
		this.fdUserId = fdUserId;
		this.eStore=eStore;
	}
	
	
	private static ThreadLocal<String> masqueradeAgentTL = new ThreadLocal<String>();
	
	static {
		masqueradeAgentTL.set( null );
	}
	
	public static String getMasqueradeAgentTL() {
		return masqueradeAgentTL.get();
	}
	public static void setMasqueradeAgentTL( String agentId ) {
		masqueradeAgentTL.set( agentId );
	}
	public static void clearMasqueradeAgentTL() {
		masqueradeAgentTL.set( null );
	}
	
	public FDIdentity getIdentity() {
		return identity;
	}
	
	public void setIdentity(FDIdentity identity) {
		this.identity = identity;
	}

	public EnumTransactionSource getSource() {
		return source;
	}

	public void setSource(EnumTransactionSource source) {
		this.source = source;
	}

	public String getInitiator() {
		return initiator;
	}
	
	public CrmAgentModel getAgent() {
		return agent;
	}
	
	public void setAgent(CrmAgentModel agent) {
		this.agent = agent;
	}

	public String getNote() {
		return note;
	}
	
	public void setNote(String note){
		this.note = note;
	}
	
	public EnumAccountActivityType getType() {
		return type;
	}
	
	public void setType( EnumAccountActivityType type ) {
		this.type = type;
	}


	public ErpActivityRecord createActivity() {
		return this.createActivity(null, null);
	}
	
	public ErpActivityRecord createActivity(EnumAccountActivityType atype) {
		return this.createActivity(atype, null);
	}

	public ErpActivityRecord createActivity(EnumAccountActivityType atype, String note) {
		ErpActivityRecord rec = new ErpActivityRecord();
		
		if ( atype != null ) {
			rec.setActivityType(atype);
		} else {
			rec.setActivityType( this.type );
		}

		rec.setSource(source);
		rec.setInitiator(initiator);
		rec.setCustomerId(identity.getErpCustomerPK());
		rec.setMasqueradeAgent( masqueradeAgent );

		StringBuffer sb = new StringBuffer();
		if (note != null) {
			sb.append(note);
		}
		if (this.note != null) {
			sb.append(this.note);
		}
		rec.setNote(sb.toString());

		return rec;
	}

	public boolean isPR1() {
		return isPR1;
	}

	public void setPR1(boolean isPR1) {
		this.isPR1 = isPR1;
	}

	public String getFdUserId() {
		return fdUserId;
	}

	public void setFdUserId(String fdUserId) {
		this.fdUserId = fdUserId;
	}

	public EnumEStoreId geteStore() {
		return eStore;
	}


}
