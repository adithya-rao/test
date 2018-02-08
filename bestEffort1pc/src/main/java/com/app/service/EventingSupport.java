package com.app.service;

import com.bnsf.platform.event.create.EventAttributes;
import com.bnsf.platform.event.create.EventAttributesBuilder;
import com.bnsf.platform.event.create.EventBuildCallbackAdapter;
import com.bnsf.platform.event.create.EventFactory;
import com.bnsf.platform.event.model.Event;
import com.bnsf.platform.event.model.EventIdentifier;
import com.bnsf.platform.event.publish.EventPublisher;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;

/**
 * General APIs used by the app to create and publish events.
 */
public class EventingSupport implements InitializingBean {
    private EventFactory eventFactory;
    private EventPublisher eventPublisher;

    /**
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.eventFactory == null) {
            throw new IllegalArgumentException("eventFactory required");
        }
        if (this.eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher required");
        }
    }

    /**
     * @param eventIdentifier
     * @param corrId
     * @param module
     * @param msgParms
     * @return
     */
    public Event createEvent(EventIdentifier eventIdentifier, String corrId, String module, Object[] msgParms) {
        return eventFactory.createEvent(eventIdentifier, new EventBuildCallbackAdapter() {
            @Override
            public EventAttributes eventAttributes() {
                return new EventAttributesBuilder(eventIdentifier).correlationId(corrId).moduleName(module).messageParms(msgParms).build();
            }
        });
    }

    /**
     * @param event
     */
    public void publishEvent(Event event) {
        this.eventPublisher.publish(event);
    }

    /**
     * @param events
     */
    public void publishEvents(List<Event> events) {
        this.eventPublisher.batchPublish(events.toArray(new Event[0]));
    }

    /**
     * @param eventFactory
     */
    public void setEventFactory(EventFactory eventFactory) {
        this.eventFactory = eventFactory;
    }

    /**
     * @param eventPublisher
     */
    public void setEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
