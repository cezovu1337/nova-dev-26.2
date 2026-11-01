package dev.fesly.impl.event.bus.impl;
import dev.fesly.Nova;
import dev.fesly.impl.event.Listener;
import dev.fesly.impl.event.Priorities;
import dev.fesly.impl.event.annotations.EventLink;
import dev.fesly.impl.event.bus.Bus;
import dev.fesly.impl.event.impl.other.WorldChangeEvent;
import dev.fesly.impl.util.chat.ChatUtil;
import dev.fesly.impl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
public class EventBus<Event> implements Bus<Event> {
    private final Map<Type, List<CallSite<Event>>> callSiteMap;
    private final Map<Type, List<Listener<Event>>> listenerCache;
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    public EventBus() {
        callSiteMap = new HashMap<>();
        listenerCache = new HashMap<>();
    }
    @Override
    public void register(Object subscriber) {
        try {
            for (Field field : subscriber.getClass().getDeclaredFields()) {
                EventLink annotation = field.getAnnotation(EventLink.class);
                if (annotation != null) {
                    Type eventType = ((ParameterizedType) (field.getGenericType())).getActualTypeArguments()[0];
                    if (!field.isAccessible())
                        field.setAccessible(true);
                    try {
                        Listener<Event> listener =
                                (Listener<Event>) LOOKUP.unreflectGetter(field)
                                        .invokeWithArguments(subscriber);
                        byte priority = annotation.value();
                        List<CallSite<Event>> callSites;
                        CallSite<Event> callSite = new CallSite<>(subscriber, listener, priority);
                        if (this.callSiteMap.containsKey(eventType)) {
                            callSites = this.callSiteMap.get(eventType);
                            callSites.add(callSite);
                            callSites.sort((o1, o2) -> o2.priority - o1.priority);
                        } else {
                            callSites = new ArrayList<>(1);
                            callSites.add(callSite);
                            this.callSiteMap.put(eventType, callSites);
                        }
                    } catch (Throwable exception) {
                        if (!Nova.DEBUG) return;
                        ChatUtil.addText("Exception in console");
                        exception.printStackTrace();
                    }
                }
            }
            this.populateListenerCache();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }
    private void populateListenerCache() {
        Map<Type, List<CallSite<Event>>> callSiteMap = this.callSiteMap;
        Map<Type, List<Listener<Event>>> listenerCache = this.listenerCache;
        for (Type type : callSiteMap.keySet()) {
            List<CallSite<Event>> callSites = callSiteMap.get(type);
            int size = callSites.size();
            List<Listener<Event>> listeners = new ArrayList<>(size);
            for (CallSite<Event> callSite : callSites) listeners.add(callSite.listener);
            listenerCache.put(type, listeners);
        }
    }
    @Override
    public void unregister(Object subscriber) {
        for (List<CallSite<Event>> callSites : this.callSiteMap.values()) {
            callSites.removeIf(eventCallSite -> eventCallSite.owner == subscriber);
        }
        this.populateListenerCache();
    }
    @Override
    public void handle(Event event) {
        if ((Minecraft.getInstance().world == null) && !(event instanceof WorldChangeEvent))
            return;
        List<Listener<Event>> listeners = listenerCache.getOrDefault(event.getClass(), Collections.emptyList());
        int i = 0;
        int listenersSize = listeners.size();
        while (i < listenersSize) {
            listeners.get(i++).call(event);
        }
    }
    public void handle(Event event, Object... listeners) {
        int i = 0;
        int listenersSize = listeners.length;
        List<Object> list = Arrays.asList(listeners);
        while (i < listenersSize) {
            ((Listener) list.get(i++)).call(event);
        }
    }
    private record CallSite<Event>(Object owner, Listener<Event> listener, byte priority) {
    }
}
