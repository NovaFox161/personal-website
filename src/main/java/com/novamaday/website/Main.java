package com.novamaday.website;

import com.novamaday.website.objects.SiteSettings;
import com.novamaday.website.utils.SparkUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class Main {
    public static void main(String[] args) throws IOException {
        //Load settings
        Properties p = new Properties();
        p.load(new FileReader("settings.properties"));
        SiteSettings.init(p);

        //Init spark
        SparkUtils.initSpark();
    }
}
