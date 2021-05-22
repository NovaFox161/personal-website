package com.novamaday.website.objects;

import java.util.Properties;

public enum SiteSettings {
    TIME_OUT, PORT, LOG_FOLDER, RECAP_KEY, EMAIL_USER, EMAIL_PASS;

    private String val;

    SiteSettings() {
    }

    public static void init(Properties properties) {
        for (SiteSettings s : values()) {
            s.set(properties.getProperty(s.name()));
        }
    }

    public String get() {
        return val;
    }

    public void set(String val) {
        this.val = val;
    }
}
