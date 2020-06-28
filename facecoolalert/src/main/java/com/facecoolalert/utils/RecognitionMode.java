package com.facecoolalert.utils;

public enum RecognitionMode {
    MODE_BEST_MATCH(0),
    MODE_FIRST_MATCH(1);

    private final int value;

    RecognitionMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
