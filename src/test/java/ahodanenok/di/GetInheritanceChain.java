package ahodanenok.di;

import ahodanenok.di.util.ReflectionUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class GetInheritanceChain {

    static class NoSuperclass { }
    static class OneSuperclass extends NoSuperclass { }
    static class MultipleSuperclasses extends OneSuperclass { }

    @Test
    public void shouldReturnEmptyForObject() {
        assertThat(ReflectionUtils.getInheritanceChain(Object.class)).isEmpty();
    }

    @Test
    public void shouldReturnClassItselfGivenNoSuperclass() {
        assertThat(ReflectionUtils.getInheritanceChain(NoSuperclass.class))
                .containsExactly(NoSuperclass.class);
    }

    @Test
    public void shouldReturnOneSuperclass() {
        assertThat(ReflectionUtils.getInheritanceChain(OneSuperclass.class))
                .containsExactly(NoSuperclass.class, OneSuperclass.class);
    }

    @Test
    public void shouldReturnAllSuperclasses() {
        assertThat(ReflectionUtils.getInheritanceChain(MultipleSuperclasses.class))
                .containsExactly(NoSuperclass.class, OneSuperclass.class, MultipleSuperclasses.class);
    }
}
