package com.novamaday.website.objects;

import java.util.UUID;

/**
 * Created by: NovaFox161 at 4/11/2018
 * Website: https://www.novamaday.com
 * For Project: Personal-Site
 */
public class UserAPIAccount {
    private UUID userId;
    private String APIKey;
    private boolean blocked;
    private long timeIssued;
    private int uses;

    //Getters
    public UUID getUserId() {
        return userId;
    }

    public String getAPIKey() {
        return APIKey;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public long getTimeIssued() {
        return timeIssued;
    }

    public int getUses() {
        return uses;
    }

    //Setters
    public void setUserId(UUID _userId) {
        userId = _userId;
    }

    public void setAPIKey(String _apiKey) {
        APIKey = _apiKey;
    }

    public void setBlocked(boolean _blocked) {
        blocked = _blocked;
    }

    public void setTimeIssued(long _time) {
        timeIssued = _time;
    }

    public void setUses(int _uses) {
        uses = _uses;
    }
}