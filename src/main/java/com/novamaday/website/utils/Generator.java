package com.novamaday.website.utils;

import com.novamaday.website.crypto.KeyGenerator;
import com.novamaday.website.database.DatabaseManager;
import com.novamaday.website.objects.User;

public class Generator {
    public static String generateEmailConfirmationLink(User user) {
        String code = KeyGenerator.csRandomAlphaNumericString(32);

        //Save to database
        DatabaseManager.getManager().addPendingConfirmation(user, code);

        return "https://novamaday.com/confirm/email?code=" + code;
    }
}