package com.wayeal.cloud.zipkin;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertiesUtils {
    private static final Log LOG = LogFactory.getLog(PropertiesUtils.class);
    private static Properties envProp = new Properties();

    public PropertiesUtils() {
    }

    public static Properties getDubboProp() {
        return envProp;
    }

    static {
        InputStream inputStream = PropertiesUtils.class.getClassLoader().getResourceAsStream("dubbo.properties");

        try {
            envProp.load(inputStream);
        } catch (IOException var2) {
            LOG.error(var2);
        }

    }
}
