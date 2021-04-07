package ahodanenok.di;

import ahodanenok.di.util.ReflectionUtils;
import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

import static org.assertj.core.api.Assertions.*;

public class GetAnnotationsTest {

    @Target(ElementType.ANNOTATION_TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Meta { }

    @Meta
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface MA { }

    @Meta
    @MA
    @Retention(RetentionPolicy.RUNTIME)
    @interface MB { }

    @Meta
    @MB
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface MC { }

    @Meta
    @Retention(RetentionPolicy.RUNTIME)
    @interface MD { }


    @Retention(RetentionPolicy.RUNTIME)
    @interface A { }

    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @interface B { }

    @A @MC static class WithAnnotations { }
    @B @MA @MD static class Superclass { }
    static class Subclass extends Superclass { }


    @Test
    public void shouldReturnAnnotationsWithMetaAnnotationDeclaredOnClass()  {
        assertThat(ReflectionUtils.getAnnotations(WithAnnotations.class, a -> a.annotationType().isAnnotationPresent(Meta.class), true))
                .containsExactlyInAnyOrder(
                        MB.class.getDeclaredAnnotation(MA.class),
                        MC.class.getDeclaredAnnotation(MB.class),
                        WithAnnotations.class.getDeclaredAnnotation(MC.class));
    }

    @Test
    public void shouldReturnAnnotationsWithMetaAnnotationDeclaredOnSuperClass()  {
        assertThat(ReflectionUtils.getAnnotations(Subclass.class, a -> a.annotationType().isAnnotationPresent(Meta.class), true))
                .containsExactlyInAnyOrder(
                        MB.class.getDeclaredAnnotation(MA.class));
    }
}
