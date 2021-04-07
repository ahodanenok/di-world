package ahodanenok.di.next.metadata;

import ahodanenok.di.metadata.ClassMetadataReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.inject.Named;
import javax.inject.Qualifier;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static org.assertj.core.api.Assertions.*;

public class ReadClassQualifiersTest {

    @Roots
    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Plant { }

    @Qualifier
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Roots { }

    @Qualifier
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Green { }

    @Qualifier
    @Repeatable(Flavours.class)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Flavour {
        String value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    private @interface Flavours {
        Flavour[] value();
    }

    @Plant @Green
    private static class Grass { }

    @Roots
    @Named("some herb")
    private static class Herb { }

    private static class Chamomile extends Herb { }

    private static class Mint { }

    @Flavour("good")
    @Flavour("nice")
    private static class Cinnamon { }

    @Test
    @DisplayName("should read no qualifiers")
    public void noQualifiers() {
        assertThat(new ClassMetadataReader<>(Mint.class).readQualifiers()).isEmpty();
    }

    @Test
    @DisplayName("should read qualifiers from a class")
    public void qualifiersClass() {
        assertThat(new ClassMetadataReader<>(Grass.class).readQualifiers()).containsExactlyInAnyOrder(
                Grass.class.getAnnotation(Plant.class), Grass.class.getAnnotation(Green.class));
    }

    @Test
    @DisplayName("should read qualifiers from parent given they are @Inherited")
    public void qualifiersParent() {
        assertThat(new ClassMetadataReader<>(Chamomile.class).readQualifiers())
                .containsExactly(Herb.class.getAnnotation(Roots.class));
    }

    @Test
    @DisplayName("should read repeatable qualifiers from a class")
    public void repeatable() {
        assertThat(new ClassMetadataReader<>(Cinnamon.class).readQualifiers())
                .containsExactlyInAnyOrder(Cinnamon.class.getAnnotationsByType(Flavour.class));
    }
}
