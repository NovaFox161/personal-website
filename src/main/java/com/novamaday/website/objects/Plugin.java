package com.novamaday.website.objects;

public class Plugin {
    private final String name;
    private String version;
    private String mainPage;
    private String downloadLink;

    public Plugin(String _name) {
        name = _name;
    }

    //Getters
    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getMainPage() {
        return mainPage;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    //Setters
    public void setVersion(String _version) {
        version = _version;
    }

    public void setMainPage(String _mainPage) {
        mainPage = _mainPage;
    }

    public void setDownloadLink(String _downloadLink) {
        downloadLink = _downloadLink;
    }
}