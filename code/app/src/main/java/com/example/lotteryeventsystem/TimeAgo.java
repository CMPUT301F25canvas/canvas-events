package com.example.lotteryeventsystem;

public class TimeAgo {
    /**
     * Converts milliseconds into a readable "time ago" format.
     */
    public static String formatTimeDiff(long diffMillis) {
        long seconds = diffMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) return days + " day" + (days > 1 ? "s" : "") + " ago";
        if (hours > 0) return hours + " hour" + (hours > 1 ? "s" : "") + " ago";
        if (minutes > 0) return minutes + " min" + (minutes > 1 ? "s" : "") + " ago";
        return "Just now";
    }
}
