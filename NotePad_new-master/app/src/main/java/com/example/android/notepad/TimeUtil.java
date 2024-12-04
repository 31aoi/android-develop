package com.example.android.notepad;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    public static String getCurrentTimeFormatted() {
        Long nowtime = Long.valueOf(System.currentTimeMillis());
        Date date = new Date(nowtime);
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return format.format(date);
    }
}