package cn.powernukkitx.util;

import cn.powernukkitx.Bootstrap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class LanguageUtils {
    public static final Locale locale = Locale.getDefault();
    private static Map<String, String> langMap;

    public static void init() {
        try (final InputStream stream = Bootstrap.class.getClassLoader()
                .getResourceAsStream("lang/" + locale.toLanguageTag().toLowerCase() + "/lang.ini")) {
            if (stream != null) {
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    langMap = INIUtils.parseINI(reader);
                }
            }
        } catch (IOException e) {
            langMap = new HashMap<>(0);
        }
    }

    /**
     * 本地化键名
     *
     * @param key 键名
     */
    public static String tr(String key, String... args) {
        String s = langMap.getOrDefault(key, key);
        for (int i = 0, len = args.length; i < len; i++) {
            s = s.replace("%" + i, args[i]);
        }
        return s;
    }
}
