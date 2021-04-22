package ahodanenok.di.character;

import ahodanenok.di.WorldInternals;
import ahodanenok.di.container.Container;

public interface Character<T> {

    Container<T> build(WorldInternals world);
}
