package uca.hermes.api.util;

public class StringUtilities {
    public static boolean isBlankOrNull(String s) {
        if(s == null) {
            return true;
        }
        return s.isBlank();
    }
}
