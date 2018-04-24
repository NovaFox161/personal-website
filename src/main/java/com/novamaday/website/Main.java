package com.novamaday.website;

import com.novamaday.website.account.AccountHandler;
import com.novamaday.website.database.DatabaseManager;
import com.novamaday.website.objects.SiteSettings;
import com.novamaday.website.utils.EmailHandler;
import com.novamaday.website.utils.Logger;
import com.novamaday.website.utils.SparkUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {
        //Load settings
        Properties p = new Properties();
        p.load(new FileReader(new File("settings.properties")));
        SiteSettings.init(p);

        Logger.getLogger().init();

        //Init database
        DatabaseManager.getManager().connectToMySQL();
        DatabaseManager.getManager().createTables();

        //Init spark
        AccountHandler.getHandler().init();
        SparkUtils.initSpark();

        //Init the rest of our services
        EmailHandler.getHandler().init();
    }
}