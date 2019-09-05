package com.api.utils;

import com.api.safetynex.R;

public class DrawableUtils {


    public static int getDrawableColor(String code){
        int drawableId;
        switch (code){
            case "LOW":
                drawableId = R.drawable.rounded_corner_low;
                break;
            case "MEDIUM":
                drawableId = R.drawable.rounded_corner_medium;
                break;
            case "HIGH":
                drawableId = R.drawable.rounded_corner_high;
                break;
            case "WARNING":
                drawableId = R.drawable.rounded_corner_warning;
                break;
            case "WARNING_SPEED":
                drawableId = R.drawable.rounded_corner_warning;
                break;
            case "GPS_LOST":
                drawableId = R.drawable.rounded_corner_gps_lost;
                break;
            case "ALERT":
                drawableId = R.drawable.rounded_corner_alert;
                break;
            case "BLACK":
                drawableId = R.color.colorNSXTxtBlack;
                break;
            case "ORANGE":
                drawableId = R.color.colorNSXBgWarningLevel;
                break;
            case "RED":
                drawableId = R.color.colorNSXBgAlertLevel;
                break;
            case "WHITE":
                drawableId = R.color.colorNSXTxtWhite;
                break;
            case "GREY":
                drawableId = R.color.colorNSXTxtGrey;
                break;
            default:
                drawableId = R.color.colorNSX;
                break;
        }
        return drawableId;
    }

    public static int getTextIconDrawable(String code){
        int drawableId;
        switch (code){
            case "LOW":
                drawableId = R.drawable.ic_check_box_black_24dp;
                break;
            case "MEDIUM":
                drawableId = R.drawable.ic_medium_level_warning_24dp;
                break;
            case "HIGH":
                drawableId = R.drawable.ic_high_level_warning_24dp;
                break;
            case "WARNING":
                drawableId = R.drawable.ic_warning_24dp;
                break;
            case "ALERT":
                drawableId = R.drawable.ic_error_black_24dp;
                break;
            default:
                drawableId = R.drawable.ic_check_box_black_24dp;
                break;
        }
        return drawableId;
    }
}
