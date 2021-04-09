//package ahodanenok.di.metadata;
//
//import org.junit.jupiter.api.Test;
//
//import javax.inject.Inject;
//
//import static org.assertj.core.api.Assertions.*;
//
//public class ExecutableMetadataReaderTest {
//
//    static class Methods {
//        @Inject void injectable() { }
//        void notInjectable() { }
//    }
//
//    @Test
//    public void shouldReadMethodAsInjectable() throws Exception {
//        assertThat(new ExecutableMetadataReader(Methods.class.getDeclaredMethod("injectable")).readInjectable())
//                .isTrue();
//    }
//
//    @Test
//    public void shouldNotReadMethodAsInjectable() throws Exception {
//        assertThat(new ExecutableMetadataReader(Methods.class.getDeclaredMethod("notInjectable")).readInjectable())
//                .isFalse();
//    }
//
//}
