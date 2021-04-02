package ahodanenok.di.metadata;

import ahodanenok.di.ClassMetadataReader;
import org.junit.jupiter.api.Test;

import javax.inject.Named;

import static org.assertj.core.api.Assertions.*;

public class ClassMetadataReaderTest {

    static class NotNamedClass { }

    @Named
    static class DefaultNamedClass { }

    @Named("test name")
    static class NamedClass { }

    @Test
    public void shouldReadNullAsClassNameForClassNotAnnotatedWithNamed() {
        assertThat(new ClassMetadataReader<>(NotNamedClass.class).readName()).isNull();
    }

    @Test
    public void shouldReadEmptyStringAsClassNameForClassWithNamedWithoutExplicitName() {
        assertThat(new ClassMetadataReader<>(DefaultNamedClass.class).readName()).isEmpty();
    }

    @Test
    public void shouldReadEnteredClassNameInNamedAnnotation() {
        assertThat(new ClassMetadataReader<>(NamedClass.class).readName()).isEqualTo("test name");
    }
}
