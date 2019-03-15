package com.macchinito.rtgps;

public final class Constants {

    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 300;
    // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 300;
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    // Stores the lat / long pairs in a text file
    public static final String LOCATION_FILE = "sdcard/location.txt";
    // Stores the connect / disconnect data in a text file
    public static final String LOG_FILE = "sdcard/log.txt";

    public static final String RUNNING = "runningInBackground"; // Recording data in background

    public static final String APP_PACKAGE_NAME = "com.macchinito.rtgps";

    public static final double DISTANCE_MOVED = 50.0; // minimum for distance move
    public static final int STABLE_SERVICE_NUM = 3; // stable counter threshold

    public static final double HOME_NOTICE_DISTANCE = 50.0; // distance to home
    public static final double NOTICE_ENABLE_DISTANCE = 1000.0; // distance to enable notice

    public static final double HOME_LAT = 35.625492; // home lat
    public static final double HOME_LNG = 139.652385; // home lng
    public static final double SCHOOL_LAT = 35.713185; // school lat
    public static final double SCHOOL_LNG = 139.575287; // school lng

    /**
     * Suppress default constructor for noninstantiability
     */
    private Constants() {
        throw new AssertionError();
    }
}
