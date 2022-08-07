package com.godot.community.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

@Configuration
public class WKConfig {

    private static final Logger logger = LoggerFactory.getLogger(WKConfig.class);

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @PostConstruct
    public void init() {
        // Create wk image directory
        File file = new File(wkImageStorage);
        if (!file.exists()) {
            file.mkdir();
            logger.info("Create WK file Directory: " + wkImageStorage);
        }
    }

}