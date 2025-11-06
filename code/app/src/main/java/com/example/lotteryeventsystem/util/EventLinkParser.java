package com.example.lotteryeventsystem.util;

import android.net.Uri;

import androidx.annotation.Nullable;

/**
 * Pulls an event id out of whatever text was inside the QR code.
 */
public final class EventLinkParser {
    private EventLinkParser() {
    }

    @Nullable
    public static String parseEventId(String rawContent) {
        if (rawContent == null) {
            return null;
        }
        String trimmed = rawContent.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.startsWith("canvas-events://")) {
            return extractFromScheme(Uri.parse(trimmed));
        }
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return extractFromHttp(Uri.parse(trimmed));
        }
        return trimmed;
    }

    private static String extractFromScheme(Uri uri) {
        if (uri == null) {
            return null;
        }
        if ("event".equals(uri.getHost())) {
            return uri.getLastPathSegment();
        }
        return null;
    }

    private static String extractFromHttp(Uri uri) {
        if (uri == null) {
            return null;
        }
        for (String segment : uri.getPathSegments()) {
            if ("events".equals(segment) || "event".equals(segment)) {
                int index = uri.getPathSegments().indexOf(segment);
                if (index >= 0 && index + 1 < uri.getPathSegments().size()) {
                    return uri.getPathSegments().get(index + 1);
                }
            }
        }
        return null;
    }
}
