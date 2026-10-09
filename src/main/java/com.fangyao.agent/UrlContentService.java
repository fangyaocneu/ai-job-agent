package com.fangyao.agent;

import java.net.InetAddress;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UrlContentService {

    private static final int TIMEOUT_MS = 10_000;
    private static final int MAX_TEXT_LENGTH = 30_000;

    public String fetchUrl(String url) {

        validateUrl(url);

        try {

            Document document =
                    Jsoup.connect(url)
                            .userAgent(
                                    "Mozilla/5.0 AI-Job-Agent/1.0"
                            )
                            .timeout(
                                    TIMEOUT_MS
                            )
                            .followRedirects(
                                    true
                            )
                            .get();

            String contentType =
                    document.connection()
                            .response()
                            .contentType();

            if (contentType != null
                    && !contentType.contains("text/html")) {

                throw new IllegalArgumentException(
                        "Only HTML pages are supported."
                );
            }

            document.select(
                    "script, style, noscript, nav, footer, header, svg"
            ).remove();

            String text =
                    document.body() == null
                            ? ""
                            : document.body().text();

            if (text.isBlank()) {

                throw new IllegalStateException(
                        "No readable text was found on the page."
                );
            }

            if (text.length() > MAX_TEXT_LENGTH) {

                text =
                        text.substring(
                                0,
                                MAX_TEXT_LENGTH
                        );
            }

            return text;

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to read URL: "
                    + e.getMessage(),
                    e
            );
        }
    }

    private void validateUrl(String url) {

        if (url == null
                || url.isBlank()) {

            throw new IllegalArgumentException(
                    "URL cannot be empty."
            );
        }

        try {

            URI uri =
                    URI.create(
                            url.trim()
                    );

            String scheme =
                    uri.getScheme();

            if (scheme == null
                    || (
                        !scheme.equalsIgnoreCase("http")
                        && !scheme.equalsIgnoreCase("https")
                    )) {

                throw new IllegalArgumentException(
                        "Only HTTP and HTTPS URLs are allowed."
                );
            }

            String host =
                    uri.getHost();

            if (host == null
                    || host.isBlank()) {

                throw new IllegalArgumentException(
                        "URL host is invalid."
                );
            }

            InetAddress[] addresses =
                    InetAddress.getAllByName(
                            host
                    );

            for (InetAddress address : addresses) {

                if (address.isAnyLocalAddress()
                        || address.isLoopbackAddress()
                        || address.isLinkLocalAddress()
                        || address.isSiteLocalAddress()) {

                    throw new IllegalArgumentException(
                            "Private or local network URLs are not allowed."
                    );
                }
            }

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid URL: "
                    + e.getMessage(),
                    e
            );
        }
    }
}