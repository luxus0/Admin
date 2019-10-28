package spring_boot.spring_boot.Admin;

public interface SpringApplicationAdminMXBean {

    boolean isReady();
    boolean isEmbeddedWebApplication();
    String getProperty(String key);
    void shutDown();
}
