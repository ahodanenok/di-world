//package ahodanenok.di.metadata;
//
//import org.junit.jupiter.api.Test;
//
//import javax.inject.Inject;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//public class FieldMetadataReaderTest {
//
//    static class Fields {
//        @Inject String injectable;
//        int notInjectable;
//    }
//
//    @Test
//    public void shouldReadFieldAsInjectable() throws Exception {
//        assertThat(new FieldMetadataReader(Fields.class.getDeclaredField("injectable")).readInjectable())
//                .isTrue();
//    }
//
//    @Test
//    public void shouldNotReadFieldAsInjectable() throws Exception {
//        assertThat(new FieldMetadataReader(Fields.class.getDeclaredField("notInjectable")).readInjectable())
//                .isFalse();
//    }
//
//}
