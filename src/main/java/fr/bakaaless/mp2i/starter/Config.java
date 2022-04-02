package fr.bakaaless.mp2i.starter;

public class Config {

    private static Config instance;

    public static Config get() {
        if (instance == null)
            instance = new Config();
        return instance;
    }

    static void setConfig(final Config config) {
        instance = config;
    }

    static Config createBlank() {
        return new Config();
    }

    private String version = "1.0.0";
    private String prefix = "&";

    private Config() {
    }

    public String getVersion() {
        return this.version;
    }

    public String getPrefix() {
        return this.prefix;
    }

}
