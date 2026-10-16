package ru.miet.osmsensors.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class AppConfig {
    public static final String GROUP_ROOT = "root";
    public static final String GROUP_USER = "user";
    private static final String DEFAULT_DB_FILE = "sensors.csv";

    private final String login;
    private final String password;
    private final String group;
    private final boolean debug;
    private final boolean autotests;
    private final String dbfile;

    private AppConfig(String login, String  password, String group, boolean debug, boolean autotests, String dbfile) {
        this.login = login;
        this.password = password;
        this.group = group;
        this.debug = debug;
        this.autotests = autotests;
        this.dbfile = dbfile;
    }

    public static AppConfig load(Path file) throws IOException {
        if (!Files.exists(file)) {
            throw new IOException("Файл настроек не найден: " + file.toAbsolutePath());
        }

        Properties props = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(reader);
        }

        String login = props.getProperty("login", "").trim();
        String password = props.getProperty("password", "").trim();
        if (login.isEmpty() || password.isEmpty()) {
            throw new IOException("В файле настроек не заданы login и password");
        }

        String group = props.getProperty("group", GROUP_USER).trim().toLowerCase();
        if(!group.equals(GROUP_ROOT) && !group.equals(GROUP_USER)) {
            throw new IOException("group должен быть root или user а указано: " + group);
        }

        boolean debug = Boolean.parseBoolean(props.getProperty("debug", "false").trim());
        boolean autotests = Boolean.parseBoolean(props.getProperty("autotests", "false").trim());
        String dbfile = props.getProperty("dbfile", DEFAULT_DB_FILE).trim();

        return new AppConfig(login, password, group, debug, autotests, dbfile);
    }

    public String getLogin() {return login;}
    public String getPassword() {return password;}
    public String getGroup() {return group;}
    public boolean isRoot() {return GROUP_ROOT.equals(group);}

    public boolean isDebug() {
        return debug;
    }

    public boolean isAutotests() {
        return autotests;
    }

    public String getDbfile() {
        return dbfile;
    }
}
