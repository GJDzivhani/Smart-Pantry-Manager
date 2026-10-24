package com.richfield.smartpantry.util;

import android.content.Context;
import android.content.SharedPreferences;

/** Thin wrapper around SharedPreferences for the Settings screen. */
public final class Prefs {

    private static final String FILE = "smart_pantry_prefs";
    private static final String KEY_ALERTS = "alerts_enabled";
    private static final String KEY_DAYS = "alert_days";
    private static final String KEY_UNIT = "default_unit";
    private static final String KEY_IGNORE_EXPIRED = "ignore_expired";

    private Prefs() { }

    private static SharedPreferences sp(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static boolean alertsEnabled(Context c) { return sp(c).getBoolean(KEY_ALERTS, true); }
    public static void setAlertsEnabled(Context c, boolean v) { sp(c).edit().putBoolean(KEY_ALERTS, v).apply(); }

    public static int alertDays(Context c) { return sp(c).getInt(KEY_DAYS, 3); }
    public static void setAlertDays(Context c, int v) { sp(c).edit().putInt(KEY_DAYS, v).apply(); }

    public static String defaultUnit(Context c) { return sp(c).getString(KEY_UNIT, "pcs"); }
    public static void setDefaultUnit(Context c, String v) { sp(c).edit().putString(KEY_UNIT, v).apply(); }

    public static boolean ignoreExpired(Context c) { return sp(c).getBoolean(KEY_IGNORE_EXPIRED, false); }
    public static void setIgnoreExpired(Context c, boolean v) { sp(c).edit().putBoolean(KEY_IGNORE_EXPIRED, v).apply(); }
}
