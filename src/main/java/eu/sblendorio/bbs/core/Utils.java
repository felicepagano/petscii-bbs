package eu.sblendorio.bbs.core;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.trim;

public class Utils {

    private static final Set<Integer> CONTROL_CHARS = new HashSet<>(Arrays.asList(
            1, 2, 3, 4, 5, 6, 7, 8, 9,
            11, 12,
            14, 15, 16, 17, 18, 19,
            21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
            128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140,
            142, 143, 144, 145, 146, 147,
            149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159
    ));

    // EXTRA_CONTROL_CHARS: 0, 10, 13, 20, 141, 148

    public static boolean isControlChar(int c) { return CONTROL_CHARS.contains(c); }
    public static boolean isControlChar(char c) { return isControlChar((int) c); }

    public static boolean equalsDomain(String a, String b) {
        return normalizeDomain(a).equals(normalizeDomain(b));
    }

    public static String normalizeDomain(String s) {
        return lowerCase(trim(s)).replaceAll("https?:(//)?", EMPTY).replace("www.", EMPTY).replaceAll("/+?$", EMPTY);
    }

    public static byte[] bytes(String s, Charset charset) {
        return s == null ? new byte[] {} : s.getBytes(charset);
    }

    public static byte[] bytes(String s) {
        return bytes(s, StandardCharsets.ISO_8859_1);
    }


    private Utils() {
        throw new IllegalStateException("Utility class");
    }

}
