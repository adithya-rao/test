package com.camel.command;

import org.springframework.beans.factory.InitializingBean;

import com.bnsf.sirm.utils.SirmTransEnum;
import com.bnsf.spm.platform.processors.InOutMessageWrapper;

/**
 * Transaction End Processor:
 * mark process end
 * @author C839987
 *
 */
public class TransactionEndCommand extends EdiCommandBase<InOutMessageWrapper, Void> implements InitializingBean {
    private static final String moduleName = "ProcessEnd";
    
    @Override
    protected Void doExecute(InOutMessageWrapper message) {
        createAndPublishEvent(message, SirmTransEnum.TRANSACTION_END, moduleName);
        createAndPublishEvent(message, SirmTransEnum.PROCESS_END, moduleName);
	return null;
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {	
	if (getEventingSupport() == null) {
	    throw new IllegalArgumentException("Eventing support is null");
	}
    }

}
