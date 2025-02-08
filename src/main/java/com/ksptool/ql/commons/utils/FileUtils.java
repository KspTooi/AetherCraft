package com.ksptool.ql.commons.utils;

public class FileUtils {
    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    public static String formatFileSize(long size) {
        if (size < K) {
            return size + " B";
        } else if (size < M) {
            return String.format("%.1f KB", (float) size / K);
        } else if (size < G) {
            return String.format("%.1f MB", (float) size / M);
        } else if (size < T) {
            return String.format("%.1f GB", (float) size / G);
        } else {
            return String.format("%.1f TB", (float) size / T);
        }
    }
} 