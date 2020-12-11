package eu.sblendorio.bbs.tenants.petscii;

import eu.sblendorio.bbs.core.Hidden;

@Hidden
public class MedicalFacts extends WordpressProxy {

    public MedicalFacts() {
        super();
        this.logo = LOGO_BYTES;
        this.domain = "https://www.medicalfacts.it";
        this.pageSize = 7;
        this.screenLines = 19;
        this.showAuthor = true;
        this.httpUserAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/85.0.4183.102 Safari/537.36";
    }

    private static final byte[] LOGO_BYTES = new byte[] {
        32, 18, -97, -65, -110, -69, 18, -65, -110, -69, 32, 18, -84, -94, -110, -66,
        18, -95, -94, -94, -110, -69, -95, -84, 18, -94, -110, -65, 32, -84, -65, 32,
        18, -95, -110, 32, 32, 18, 28, -95, -94, -94, -110, 32, -84, -65, 32, 32,
        18, -65, -94, -110, -69, 18, -94, -69, -94, -110, -66, 13, -97, -84, -66, -65,
        -66, -65, 32, 18, -84, -94, -94, -95, -110, 32, 32, -95, -95, -95, 32, 32,
        32, 18, -65, -110, -68, -69, 18, -95, -110, 32, 32, 18, 28, -95, -94, -94,
        -110, -66, 18, -65, -110, -68, -69, 18, -95, -110, 32, 32, 32, 32, 18, -95,
        -110, -84, 18, -94, -110, 13, 18, -97, -65, -110, 32, 32, 32, -68, -69, -95,
        32, 32, 18, -95, -110, 32, 32, -95, -95, -65, 32, -84, -84, 18, -94, -94,
        -110, -65, 18, -95, -110, 32, 32, 18, 28, -95, -110, 32, 32, -84, 18, -94,
        -94, -110, -65, -68, -69, 32, -69, 32, 18, -95, -110, 32, -65, 13, -97, -66,
        32, 32, 32, 32, -66, 18, -94, -94, -110, -66, -68, 18, -94, -94, -110, 32,
        -66, 32, 18, -94, -110, -66, -68, 32, 32, -68, -68, 18, -94, -94, -110, 28,
        -68, 32, 32, -68, 32, 32, -68, 32, -68, 18, -94, -110, 32, 32, -68, -68,
        -66, 13
    };

}
