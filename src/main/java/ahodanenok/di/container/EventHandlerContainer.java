package ahodanenok.di.container;

import ahodanenok.di.event.EventHandler;

import java.util.List;

public interface EventHandlerContainer<T> extends Container<T> {

    List<EventHandler> getEventHandlers();
}
