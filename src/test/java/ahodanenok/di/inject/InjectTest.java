package ahodanenok.di.inject;

import ahodanenok.di.ContainerConfiguration;
import ahodanenok.di.ObjectRequest;
import ahodanenok.di.World;
import ahodanenok.di.inject.classes.*;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

public class InjectTest {

    @Test
    public void shouldInjectPublicMethod() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(WithInjectablePublicMethod.class));
        w.getQueue().add(ContainerConfiguration.ofClass(SimpleDependency.class));
        w.getQueue().flush();

        WithInjectablePublicMethod obj = w.find(ObjectRequest.byType(WithInjectablePublicMethod.class));
        assertThat(obj.d).isNotNull();
        assertThat(obj.called_1).isTrue();
        assertThat(obj.called_2).isFalse();
    }

    @Test
    public void shouldInjectPrivateMethod() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(WithInjectablePrivateMethod.class));
        w.getQueue().add(ContainerConfiguration.ofClass(SimpleDependency.class));
        w.getQueue().flush();

        WithInjectablePrivateMethod obj = w.find(ObjectRequest.byType(WithInjectablePrivateMethod.class));
        assertThat(obj.d).isNotNull();
        assertThat(obj.called_1).isTrue();
        assertThat(obj.called_2).isFalse();
    }

    @Test
    public void shouldInjectPackagePrivateMethod() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(WithInjectablePackagePrivateMethod.class));
        w.getQueue().add(ContainerConfiguration.ofClass(SimpleDependency.class));
        w.getQueue().flush();

        WithInjectablePackagePrivateMethod obj = w.find(ObjectRequest.byType(WithInjectablePackagePrivateMethod.class));
        assertThat(obj.d).isNotNull();
        assertThat(obj.called_1).isTrue();
        assertThat(obj.called_2).isFalse();
    }

    @Test
    public void shouldInjectProtectedMethod() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(WithInjectableProtectedMethod.class));
        w.getQueue().add(ContainerConfiguration.ofClass(SimpleDependency.class));
        w.getQueue().flush();

        WithInjectableProtectedMethod obj = w.find(ObjectRequest.byType(WithInjectableProtectedMethod.class));
        assertThat(obj.d).isNotNull();
        assertThat(obj.called_1).isTrue();
        assertThat(obj.called_2).isFalse();
    }

    @Test
    public void shouldInjectParentsFirst() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(P3.class));
        w.getQueue().add(ContainerConfiguration.ofClass(SimpleDependency.class));
        w.getQueue().flush();

        P3 obj = w.find(ObjectRequest.byType(P3.class));
        assertThat(obj.log).containsExactly("D0", "M1", "D1", "M2", "D2", "M3");
    }

    @Test
    public void shouldInjectBySuperclass() {
        World w = new World();
        w.getQueue().add(ContainerConfiguration.ofClass(P3.class));
        w.getQueue().add(ContainerConfiguration.ofClass(DependsOnP0.class));
        w.getQueue().add(ContainerConfiguration.ofClass(SimpleDependency.class));
        w.getQueue().flush();

        DependsOnP0 obj = w.find(ObjectRequest.byType(DependsOnP0.class));
        assertThat(obj.p).isExactlyInstanceOf(P3.class);
    }
}
