package co.gwllx.jackson.invert;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.introspect.Annotated;

import co.gwllx.jackson.invert.annotation.JsonInvert;

public class InvertAnnotationIntrospector extends AnnotationIntrospector {

    @Override
    public Object findDeserializer(Annotated am) {
        if (am.hasAnnotation(JsonInvert.class)) {
            return InvertDeserializer.class;
        }

        return null;
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

}
