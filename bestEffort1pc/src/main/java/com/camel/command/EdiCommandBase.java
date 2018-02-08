package com.camel.command;

import com.bnsf.platform.event.model.Event;
import com.bnsf.sirm.ediBeans.EdiMessage;
import com.bnsf.sirm.utils.SirmTransEnum;
import com.bnsf.spm.platform.processors.AbstractCommandTemplate;
import com.bnsf.spm.platform.processors.InOutMessageWrapper;
import com.bnsf.spm.rm.event.EventingSupport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Base class for commands that work with EdiMessages.
 *
 * @param <T>
 * @param <V>
 */
public abstract class EdiCommandBase<T, V> extends AbstractCommandTemplate<InOutMessageWrapper, V> {
	
	private static final Logger logger = LoggerFactory.getLogger(EdiCommandBase.class);	
	
    public static final String PROP_KEY_ERROR_LIST = "ErrorList";
    public static final String PROP_KEY_EVENT_LIST = "EventList";

    /*
     * Eventing support utility to create and publish events.
     */
    private EventingSupport eventingSupport;
    
    /**
     * Extract the inbound body which is expected to be the {@link com.bnsf.sirm.ediBeans.EdiMessage}.
     */
    protected EdiMessage getEdiMessagePayload(InOutMessageWrapper msg) {
        return msg.getInBodyOfType(EdiMessage.class);
    }

    /**
     * Return the list of events from the message header.
     */
	@SuppressWarnings("unchecked") // we control the 'set'
    protected List<Event> getEventList(InOutMessageWrapper msg) {
		return (List<Event>) msg.getHeaderProperty(PROP_KEY_EVENT_LIST);
    }
    
    /**
     * Capture the event in the message header.
     */
    protected void captureEvent(InOutMessageWrapper msg, Event event) {
		List<Event> events = getEventList(msg);
    	if (events == null) {
    		events = new ArrayList<Event>();
    		msg.setHeaderProperty(PROP_KEY_EVENT_LIST, events);
    	}
    	events.add(event);
    	
		// for backward compat - will change
		if (event.getEventLevel().isErrorLevel()) {
			addErrorMessage(msg, event.getEventIdentifier().toString());
		}
    }
    
    /**
     * Publish all events that have been collected in the message.
     * 
     * @param msg
     */
    protected void publishAllCollectedEvents(InOutMessageWrapper msg) {
		final List<Event> events = getEventList(msg);
		if (events == null || events.isEmpty()) {
			logger.debug("Event list is null or empty, nothing to publish");
			return;
		}
		getEventingSupport().publishEvents(events);
		logger.debug("Done publishing events, size: " + events.size());
		//TODO: do we clear out the event list? or do we mark them as 'published'
		//events.clear();
    }
    
    /**
     * Return the error list from header.
     */
    @SuppressWarnings("unchecked") // unchecked ok bcos we control the 'set'
    protected List<String> getErrorList(InOutMessageWrapper msg) {
        return (List<String>) msg.getHeaderProperty(PROP_KEY_ERROR_LIST);
    }

    /**
     * Add the errorMsg to the list in header, creates new list if not set already.
     */
    protected void addErrorMessage(InOutMessageWrapper msg, String errorMsg) {
        List<String> errorList = getErrorList(msg);
        if (errorList == null) {
            errorList = new ArrayList<String>();
            msg.setHeaderProperty(PROP_KEY_ERROR_LIST, errorList);
        }
        errorList.add(errorMsg);
    }

    /**
     * Returns boolean indicating if there are any errors in the current exchange.
     */
    protected boolean hasErrors(InOutMessageWrapper msg) {
        final List<String> errorList = getErrorList(msg);
        return errorList != null && !errorList.isEmpty();
    }

    /**
     * @param coll
     * @return
     */
    protected boolean isEmpty(Collection<? extends Object> coll) {
        return coll == null || coll.isEmpty();
    }
    
    /**
     * @return EventingSupport
     */
	protected EventingSupport getEventingSupport() {
		return eventingSupport;
	}
	
    /**
     * @param EventingSupport
     */
	public void setEventingSupport(EventingSupport eventingSupport) {
		this.eventingSupport = eventingSupport;
	}
	
	/**
	 * @param msgCtx
	 * @param msg
	 * @param moduleName
	 * @param args
	 * Creates and Publishes event
	 */
	protected void createAndPublishEvent(InOutMessageWrapper msgCtx, SirmTransEnum msg, String moduleName, Object... args) {
	    final Event event = getEventingSupport().createEvent(msg, msgCtx.getMessageId(), moduleName, args);
	    getEventingSupport().publishEvent(event);
	}

}