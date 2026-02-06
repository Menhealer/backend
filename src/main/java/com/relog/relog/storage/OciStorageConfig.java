package com.relog.relog.storage;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "oci", name = "enabled", havingValue = "true", matchIfMissing = false)
public class OciStorageConfig {

    @Value("${oci.region}")
    private String region;

    @Bean
    public ObjectStorage objectStorageClient() throws Exception {
        ConfigFileReader.ConfigFile configFile = ConfigFileReader.parseDefault();
        BasicAuthenticationDetailsProvider provider = new ConfigFileAuthenticationDetailsProvider(configFile);

        return ObjectStorageClient.builder()
                .region(Region.fromRegionId(region))
                .build(provider);
    }
}
