package com.camel.command;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;

import com.app.service.ShipmentRequestBusinessService;
import com.bnsf.sirm.utils.SirmTransEnum;
import com.bnsf.sirm.utils.Util;
import com.bnsf.spm.platform.processors.CamelRouteExecutionContext;
import com.bnsf.spm.platform.processors.InOutMessageWrapper;

/**
 * Transaction Begin Processor:
 * - marks transaction begin
 * - generates and sets the message id (corr id) in the message with key CamelRouteExecutionContext.EXCH_PROP_MESSAGE_ID 
 * @author C839987
 */
public class TransactionBeginCommand extends EdiCommandBase<InOutMessageWrapper, Void> implements InitializingBean {
    private static final String EDI_SEGMENT_CID = "CID";
	private static final String TX_START_MODULE_NAME = "ProcessBegin";
    private static final String MSG_ID_GEN_MODULE_NAME = "MessageIdGenerator";
    private static final String MODULE_NAME_ID_GEN_TSS_CORRELATION_ID = "SetTssCorrelationId";
    private static final char EDI_EOF_CHAR = 0xd4;
    private ShipmentRequestBusinessService shipmentRequestService;
    
    @Override
    protected Void doExecute(InOutMessageWrapper message) {
		setShipmentRequestId(message);
		setTssCorrelationId(message);
		bestEffort1PCDuplicateCheck(message);
		createAndPublishEvent(message, SirmTransEnum.TRANSACTION_START, TX_START_MODULE_NAME);
		createAndPublishEvent(message, SirmTransEnum.MSG_GEN_EDI_RECEIVED, MSG_ID_GEN_MODULE_NAME);
		createAndPublishEvent(message, SirmTransEnum.MSG_GEN_ID_GENERATION_SUCCESS, MSG_ID_GEN_MODULE_NAME);
		return null;
    }
    
    private void bestEffort1PCDuplicateCheck(InOutMessageWrapper message) {
    	if (Boolean.TRUE.equals(message.getProperty(CamelRouteExecutionContext.EXCH_PROP_REDELIVERED))) {
			String shipmentRequestId = this.shipmentRequestService.getShipmentRequestIdByTSSCorrelationId(String.valueOf(message.getProperty(CamelRouteExecutionContext.EXCH_PROP_TSS_CORRELATION_ID)));
			if (StringUtils.isNotEmpty(shipmentRequestId)) {
				message.setProperty("SpmDuplicateShipmentRequest", true);
				createAndPublishEvent(message, SirmTransEnum.TRANSACTION_START, TX_START_MODULE_NAME, shipmentRequestId);
			}
		}
	}

	/**
     * Sets 32 char UUID without hyphens
     * @param message
     */
    private void setShipmentRequestId(InOutMessageWrapper message) {
	String msgId = Util.generateIDWithoutHyphen();
	message.setProperty(CamelRouteExecutionContext.EXCH_PROP_MESSAGE_ID, msgId);
    }

    public void setTssCorrelationId(InOutMessageWrapper message) {
    	String correlationId = null;
    	
    	String messageStream = message.getInBodyOfType(String.class);
    	if(StringUtils.isNotBlank(messageStream)){
    		int beginIndex = messageStream.indexOf(EDI_SEGMENT_CID);
    		if(StringUtils.isNotBlank(messageStream) && beginIndex != -1){
    			int endIndex = messageStream.indexOf(EDI_EOF_CHAR);
    			if(endIndex == -1){
    				endIndex = messageStream.length()-1 ;
    			}
    			correlationId = messageStream.substring(beginIndex+StringUtils.length(EDI_SEGMENT_CID),endIndex);
    			if(StringUtils.isNotBlank(correlationId)){
    				correlationId = correlationId.trim();
    			}
    		}
    	}
    	message.setProperty(CamelRouteExecutionContext.EXCH_PROP_TSS_CORRELATION_ID, correlationId);
    	createAndPublishEvent(message, SirmTransEnum.ID_GEN_TSS_CORRELATION_SUCCESS, MODULE_NAME_ID_GEN_TSS_CORRELATION_ID, correlationId);
    }

	@Override
    public void afterPropertiesSet() throws Exception {	
		if (getEventingSupport() == null) {
		    throw new IllegalArgumentException("Eventing support is null");
		}
		Objects.requireNonNull(this.shipmentRequestService, "shipmentRequestService is required");
    }
	
	public void setShipmentRequestService(ShipmentRequestBusinessService shipmentRequestService) {
		this.shipmentRequestService = shipmentRequestService;
	}
	
	public static void main(String args[]) {
		System.out.println(Boolean.TRUE.equals(null));
		System.out.println(Boolean.TRUE.equals("true"));		
	}
}
