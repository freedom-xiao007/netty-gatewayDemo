package com.gateway.util;

import com.gateway.route.RouteTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author lw
 */
public class Config {

    static private final Properties properties = new Properties();
    static private String fileName = "config.properties";

    public static void init(String configFileName) throws IOException {
        fileName = configFileName;
        init();
    }

    public static void init() throws IOException {
        ClassLoader classLoader = Config.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        InputStream input = new FileInputStream(file);
        properties.load(input);
        System.out.println(properties.toString());

        RouteTable.initTable(properties);
    }

    static public String getProperty(String key) {
        return (String) properties.getOrDefault(key, null);
    }
}
