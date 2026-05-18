package com.serve.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

public class XssUtil {

    public static String xssClean(String value) {
        if (value == null) {
            return null;
        }

        // 白名单：只允许基本文本标签（b/p/i/ul/li 等）
        Whitelist basic = Whitelist.basic();
        return Jsoup.clean(value,basic);
    }
}
