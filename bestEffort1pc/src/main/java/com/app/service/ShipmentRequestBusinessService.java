package com.app.service;

import static org.hamcrest.CoreMatchers.instanceOf;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.exception.ExceptionContext;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spockframework.util.ExceptionUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.bnsf.sirm.persist.api.TSMNativeQueryManager;
import com.bnsf.sirm.persist.api.TSMPersistenceManager;
import com.bnsf.sirm.persist.api.query.MapQueryParameterSource;
import com.bnsf.sirm.persist.api.query.QueryId;
import com.bnsf.sirm.persist.api.query.TSMQueryResultExtractor;
import com.bnsf.sirm.persist.exceptions.TSMDataAccessException;
import com.bnsf.srm.model.shipmentrequest.ShipmentRequest;

/**
 * Component that provides various ShipmentRequest entity related
 * functionality such as persistence, querying etc. 
 *   
 * @author C839796
 */

public class ShipmentRequestBusinessService implements InitializingBean {

	private static final Logger logger = LoggerFactory.getLogger(ShipmentRequestBusinessService.class);
	
	private TSMPersistenceManager persistenceManager;
	
	private TSMNativeQueryManager nativeQueryManager;

	@Override
	public void afterPropertiesSet() throws Exception {
		Objects.requireNonNull(this.persistenceManager, "persistenceManager is required");
		Objects.requireNonNull(this.nativeQueryManager, "nativeQueryManager is required");
	}
	
	public void setPersistenceManager(TSMPersistenceManager persistenceManager) {
		this.persistenceManager = persistenceManager;
	}
	
	public void setNativeQueryManager(TSMNativeQueryManager nativeQueryManager) {
		this.nativeQueryManager = nativeQueryManager;
	}
	
	/**
	 * Persist the shipment request as part of a currently active transaction 
	 * or create new transaction if not started.
	 * 
	 * @param shipmentRequest
	 */
	@Transactional(propagation=Propagation.REQUIRED)
	public void persistShipmentRequest(ShipmentRequest sr) {
		logger.debug("Persisting shipmentRequest...");
		this.persistenceManager.insert(sr);
		logger.debug("shipmentRequest persistence done");
	}
	
	@Transactional(propagation = Propagation.REQUIRED, readOnly = true)
	public String getShipmentRequestIdByTSSCorrelationId(String tssCorrelationId) {
		logger.debug("Retrieve shipmentRequestId for tssCorrelationId: ", tssCorrelationId);
		MapQueryParameterSource paramSource = new MapQueryParameterSource();
		paramSource.addValue("tssCorrelationId", tssCorrelationId);
		QueryId queryId = new QueryId() {
			
			@Override
			public String getId() {
				return "getShipmentRequestIdByTSSCorrelationId";
			}
		};
		String shipmentRequestId = null;
		try {
			shipmentRequestId = this.nativeQueryManager.getSingleResult(queryId, paramSource, new TSMQueryResultExtractor<String>() {
				@Override
				public String extractResult(Map<String, Object> arg0) {
					return (String) arg0.get("SHPMT_RQST_ID");
				}
			});
		} catch (TSMDataAccessException e) {
			if(!(e.getCause() instanceof EmptyResultDataAccessException)) {
				throw e;
			}
		}
		/*String shipmentRequestId = this.nativeQueryManager.getSingleResult(queryId, String.class, tssCorrelationId);*/
		logger.debug(tssCorrelationId, "maps to ",shipmentRequestId);
		return shipmentRequestId;
	}	
}
