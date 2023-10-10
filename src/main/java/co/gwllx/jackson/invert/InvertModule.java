package co.gwllx.jackson.invert;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;

public class InvertModule extends Module {

    @Override
    public String getModuleName() {
        return "InvertModule";
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Override
    public void setupModule(SetupContext context) {
        context.insertAnnotationIntrospector(new InvertAnnotationIntrospector());
    }

}
