package ahodanenok.di.character;

import ahodanenok.di.World;
import ahodanenok.di.container.Container;

public interface Character<T> {

    Container<T> build(World world);
}
