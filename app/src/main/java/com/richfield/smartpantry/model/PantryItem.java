package com.richfield.smartpantry.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * One ingredient the user currently has at home (a row in the pantry_items table).
 * The expiry date is optional and stored as an ISO string (yyyy-MM-dd) so that it
 * sorts correctly and survives time-zone changes.
 */
public class PantryItem {

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DISPLAY_FORMAT = "dd MMM yyyy";

    private long id;
    private String name;
    private double quantity;
    private String unit;
    private String expiry; // null or "yyyy-MM-dd"

    public PantryItem(long id, String name, double quantity, String unit, String expiry) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiry = (expiry == null || expiry.trim().isEmpty()) ? null : expiry.trim();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getName() { return name; }
    public double getQuantity() { return quantity; }
    public String getUnit() { return unit; }
    public String getExpiry() { return expiry; }

    /** Parsed expiry date, or null when no (valid) date was set. */
    public Date getExpiryDate() {
        return parseIso(expiry);
    }

    /**
     * Whole days from today until the expiry date.
     * Negative = already expired, 0 = expires today, null = no expiry date.
     */
    public Long getDaysUntilExpiry() {
        Date d = getExpiryDate();
        if (d == null) return null;
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        double diff = (double) (d.getTime() - today.getTimeInMillis());
        return Math.round(diff / TimeUnit.DAYS.toMillis(1));
    }

    /** Friendly date such as "05 Nov 2026", or an empty string. */
    public String getExpiryDisplay() {
        return toDisplay(expiry);
    }

    // ---------- static helpers ----------

    public static Date parseIso(String iso) {
        if (iso == null || iso.isEmpty()) return null;
        try {
            SimpleDateFormat f = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            f.setLenient(false);
            return f.parse(iso);
        } catch (ParseException e) {
            return null;
        }
    }

    public static String toDisplay(String iso) {
        Date d = parseIso(iso);
        if (d == null) return "";
        return new SimpleDateFormat(DISPLAY_FORMAT, Locale.getDefault()).format(d);
    }

    public static String toIso(Date d) {
        return new SimpleDateFormat(DATE_FORMAT, Locale.US).format(d);
    }
}
