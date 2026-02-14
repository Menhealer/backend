package com.relog.relog.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "firebase.app-check")
public class FirebaseAppCheckProperties {

    private boolean enabled = false;

    private String projectNumber;
}
