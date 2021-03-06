/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.api.core.notification;

import com.codenvy.commons.lang.cache.Cache;
import com.codenvy.commons.lang.cache.LoadingValueSLRUCache;
import com.codenvy.commons.lang.cache.SynchronizedCache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Dispatchers events to listeners. Usage example:
 * <pre>
 *     EventService bus = new EventService();
 *     bus.subscribe(new EventSubscriber&lt;MyEvent&gt;() {
 *         &#64;Override
 *         public void onEvent(MyEvent event) {
 *             // do something with event
 *         }
 *     });
 *     bus.publish(new MyEvent());
 * </pre>
 *
 * @author andrew00x
 */
@Singleton
public final class EventService {
    private static final Logger LOG = LoggerFactory.getLogger(EventService.class);

    private static final int CACHE_NUM  = 1 << 2;
    private static final int CACHE_MASK = CACHE_NUM - 1;
    private static final int SEG_SIZE   = 32;

    private final Cache<Class<?>, Set<Class<?>>>[]              typeCache;
    private final ConcurrentMap<Class<?>, Set<EventSubscriber>> subscribersByEventType;

    @SuppressWarnings("unchecked")
    public EventService() {
        subscribersByEventType = new ConcurrentHashMap<>();
        typeCache = new Cache[CACHE_NUM];
        for (int i = 0; i < CACHE_NUM; i++) {
            typeCache[i] = new SynchronizedCache<>(new LoadingValueSLRUCache<Class<?>, Set<Class<?>>>(SEG_SIZE, SEG_SIZE) {
                @Override
                protected Set<Class<?>> loadValue(Class<?> eventClass) throws RuntimeException {
                    LinkedList<Class<?>> parents = new LinkedList<>();
                    Set<Class<?>> classes = new HashSet<>();
                    parents.add(eventClass);
                    while (!parents.isEmpty()) {
                        Class<?> clazz = parents.pop();
                        classes.add(clazz);
                        Class<?> parent = clazz.getSuperclass();
                        if (parent != null) {
                            parents.add(parent);
                        }
                        Class<?>[] interfaces = clazz.getInterfaces();
                        if (interfaces.length > 0) {
                            Collections.addAll(parents, interfaces);
                        }
                    }
                    return classes;
                }
            });
        }
    }

    /**
     * Publish event {@code event}.
     *
     * @param event
     *         event
     */
    @SuppressWarnings("unchecked")
    public void publish(Object event) {
        if (event == null) {
            throw new IllegalArgumentException("Null event.");
        }
        final Class<?> eventClass = event.getClass();
        for (Class<?> clazz : typeCache[eventClass.hashCode() & CACHE_MASK].get(eventClass)) {
            final Set<EventSubscriber> eventSubscribers = subscribersByEventType.get(clazz);
            if (eventSubscribers != null && !eventSubscribers.isEmpty()) {
                for (EventSubscriber eventSubscriber : eventSubscribers) {
                    try {
                        eventSubscriber.onEvent(event);
                    } catch (RuntimeException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Subscribe event listener.
     *
     * @param subscriber
     *         event subscriber
     */
    public void subscribe(EventSubscriber<?> subscriber) {
        final Class<?> eventType = getEventType(subscriber);
        Set<EventSubscriber> entries = subscribersByEventType.get(eventType);
        if (entries == null) {
            Set<EventSubscriber> newEntries = new CopyOnWriteArraySet<>();
            entries = subscribersByEventType.putIfAbsent(eventType, newEntries);
            if (entries == null) {
                entries = newEntries;
            }
        }
        entries.add(subscriber);
    }

    /**
     * Unsubscribe event listener.
     *
     * @param subscriber
     *         event subscriber
     */
    public void unsubscribe(EventSubscriber<?> subscriber) {
        final Class<?> eventType = getEventType(subscriber);
        final Set<EventSubscriber> entries = subscribersByEventType.get(eventType);
        if (entries != null && !entries.isEmpty()) {
            boolean changed = entries.remove(subscriber);
            if (changed) {
                if (entries.isEmpty()) {
                    subscribersByEventType.remove(eventType);
                }
            }
        }
    }

    private Class<?> getEventType(EventSubscriber<?> subscriber) {
        Class<?> eventType = null;
        Class<?> clazz = subscriber.getClass();
        while (clazz != null && eventType == null) {
            for (Type type : clazz.getGenericInterfaces()) {
                if (type instanceof ParameterizedType) {
                    final ParameterizedType parameterizedType = (ParameterizedType)type;
                    final Type rawType = parameterizedType.getRawType();
                    if (EventSubscriber.class == rawType) {
                        final Type[] typeArguments = parameterizedType.getActualTypeArguments();
                        if (typeArguments.length == 1) {
                            if (typeArguments[0] instanceof Class) {
                                eventType = (Class)typeArguments[0];
                            }
                        }
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        if (eventType == null) {
            throw new IllegalArgumentException(String.format("Unable determine type of events processed by %s", subscriber));
        }
        return eventType;
    }
}
