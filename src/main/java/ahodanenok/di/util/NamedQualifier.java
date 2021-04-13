package ahodanenok.di.util;

import javax.inject.Named;
import java.lang.annotation.Annotation;

public final class NamedQualifier implements Named {

    private String value;

    public NamedQualifier(String value) {
        if (value == null) {
            throw new IllegalArgumentException("not null");
        }

        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof Named)) {
            return false;
        }

        Named that = (Named) o;
        return value.equals(that.value());
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return String.format("@%s(value=%s)", Named.class.getName(), value);
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return Named.class;
    }
}
