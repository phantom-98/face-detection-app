package com.facecoolalert.utils.Alert.email;

public class EmailSettings {
    private String service;
    private String smtpHost;
    private int port;
    private String encryption;
    private String email;
    private String password;

    public EmailSettings(String service, String smtpHost, int port, String encryption, String email, String password) {
        this.service = service;
        this.smtpHost = smtpHost;
        this.port = port;
        this.encryption = encryption;
        this.email = email;
        this.password = password;
    }

    public EmailSettings()
    {

    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
