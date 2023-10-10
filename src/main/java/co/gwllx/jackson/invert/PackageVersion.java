package co.gwllx.jackson.invert;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.Versioned;
import com.fasterxml.jackson.core.util.VersionUtil;

public final class PackageVersion implements Versioned {

    public static final Version VERSION = VersionUtil.parseVersion("0.0.1-SNAPSHOT",
            "co.gwllx.jackson.invert", "jackson-invert");

    @Override
    public Version version() {
        return VERSION;
    }
    
}
