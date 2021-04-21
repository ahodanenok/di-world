package ahodanenok.di.inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Collections;
import java.util.List;

public final class InjectionPoint {

    private final Member target;
    private final AnnotatedElement annotatedTarget;
    private final int parameterIndex;
    private final Class<?> type;
    private final Type genericType;
    private final List<Annotation> qualifiers;

    public InjectionPoint(Field field, List<Annotation> qualifiers) {
        this.target = field;
        this.annotatedTarget = field;
        this.type = field.getType();
        this.genericType = field.getGenericType();
        this.parameterIndex = -1;
        this.qualifiers = qualifiers;
    }

    public InjectionPoint(Executable target, int parameterIndex, List<Annotation> qualifiers) {
        this.target = target;
        this.annotatedTarget = target.getParameters()[parameterIndex];
        this.type = target.getParameterTypes()[parameterIndex];
        this.genericType = target.getGenericParameterTypes()[parameterIndex];
        this.parameterIndex = parameterIndex;
        this.qualifiers = qualifiers;
    }

    public Class<?> getType() {
        return type;
    }

    public List<Annotation> getQualifiers() {
        return Collections.unmodifiableList(qualifiers);
    }

    /**
     * Target of the injection
     * It will be either method, field or constructor
     */
    public Member getTarget() {
        return target;
    }

    public AnnotatedElement getAnnotatedTarget() {
        return annotatedTarget;
    }

    public Type getGenericType() {
        return genericType;
    }

    public int getParameterIndex() {
        if (parameterIndex < 0) {
            throw new IllegalStateException("not a parameter");
        }

        return parameterIndex;
    }
}
