package ahodanenok.di.inject;

import ahodanenok.di.WorldInternals;
import ahodanenok.di.exception.DependencyInjectionException;
import ahodanenok.di.metadata.ExecutableMetadataReader;
import ahodanenok.di.metadata.FieldMetadataReader;
import ahodanenok.di.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

public class StaticInjector {

    private final Injector injector;
    private final Set<Class<?>> classes;

    public StaticInjector(WorldInternals world) {
        this.injector = new Injector(world);
        this.classes = new HashSet<>();
    }

    public void addClass(Class<?> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Class is null");
        }

        // todo: validate class is applicable for the static injection
        this.classes.add(clazz);
    }

    public void inject() {
        Set<Class<?>> injected = new HashSet<>();

        try {
            for (Class<?> clazz : classes) {
                for (Class<?> h : ReflectionUtils.getInheritanceChain(clazz)) {
                    if (injected.add(h)) {
                        inject(h);
                    }
                }
            }
        } catch (Exception e) {
            throw new DependencyInjectionException("Can't inject static members", e);
        }
    }

    private void inject(Class<?> clazz) throws Exception {
        for (Field f : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                continue;
            }

            FieldMetadataReader metadataReader = new FieldMetadataReader(f);
            if (metadataReader.readInjectable()) {
                InjectionPoint injectionPoint = new InjectionPoint(f, metadataReader.readQualifiers());
                ReflectionUtils.setField(f, null, injector.resolveDependency(injectionPoint));
            }
        }

        for (Method m : clazz.getDeclaredMethods()) {
            if (!Modifier.isStatic(m.getModifiers())) {
                continue;
            }

            ExecutableMetadataReader metadataReader = new ExecutableMetadataReader(m);
            if (metadataReader.readInjectable()) {
                ReflectionUtils.invoke(m, null, injector.resolveArguments(metadataReader));
            }
        }
    }
}
