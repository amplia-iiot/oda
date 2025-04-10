package es.amplia.oda.comms.http;

import lombok.Builder;
import lombok.Data;
import org.apache.http.client.config.CookieSpecs;

@Data
@Builder
public class HttpConfig {

    public static String COOKIES_STANDARD = "Standard";
    public static String COOKIES_IGNORE = "Ignore";

    @Builder.Default
    int timeout = 10000;

    String cookiesPolicy;


    public static String getCookiesSpec(String cookies) {
        if (cookies == null || cookies.isEmpty()) {
            return CookieSpecs.DEFAULT;
        }

        if (cookies.equals(COOKIES_STANDARD)) {
            return CookieSpecs.STANDARD;
        } else if (cookies.equals(COOKIES_IGNORE)) {
            return CookieSpecs.IGNORE_COOKIES;
        } else {
            return CookieSpecs.DEFAULT;
        }
    }
}