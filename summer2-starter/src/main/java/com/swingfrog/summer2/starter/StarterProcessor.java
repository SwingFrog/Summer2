package com.swingfrog.summer2.starter;

import com.swingfrog.summer2.core.ioc.IocProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author: toke
 */
public class StarterProcessor {

    private static final Logger log = LoggerFactory.getLogger(StarterProcessor.class);

    private static final String STARTER_FILE = "summer.starter.properties";
    private static final String COMPONENT_SCAN = "summer.component.scan";
    private static final String SPLIT = ",";

    private final IocProcessor iocProcessor;

    public StarterProcessor(IocProcessor iocProcessor) {
        this.iocProcessor = iocProcessor;
    }

    public void foundStarter() {
        try {
            Enumeration<URL> urls = Thread.currentThread().getContextClassLoader().getResources(STARTER_FILE);
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String[] packs = getComponentPack(url);
                if (packs == null || packs.length == 0) {
                    throw new RuntimeException("not found property -> " + COMPONENT_SCAN + ", file -> " + url.getPath());
                }
                for (String pack : packs) {
                    iocProcessor.scanComponent(pack);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String[] getComponentPack(URL url) {
        Properties properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = url.openStream();
            properties.load(inputStream);
            String value = properties.getProperty(COMPONENT_SCAN);
            if (value == null || value.isEmpty())
                return null;
            return value.split(SPLIT);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        return null;
    }

}
