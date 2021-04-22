package ahodanenok.di;

import ahodanenok.di.augment.Augmentation;
import ahodanenok.di.container.Container;
import ahodanenok.di.queue.EntranceQueue;

import java.util.List;

public interface World extends Iterable<Container<?>> {

    EntranceQueue getQueue();

    <T> T find(ObjectRequest<T> request);

    <T> List<T> findAll(ObjectRequest<T> request);

    void installAugmentation(Augmentation augmentation);

    void fireEvent(Object event);

    void destroy();
}
