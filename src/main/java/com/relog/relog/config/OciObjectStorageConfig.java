package com.relog.relog.config;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OciObjectStorageConfig {

    @Value("${oci.auth.type:instance-principal}")
    private String authType;

    @Value("${oci.region:ap-chuncheon-1}")
    private String region;

    @Bean
    public ObjectStorageClient objectStorageClient() throws Exception {
        BasicAuthenticationDetailsProvider provider;

        if ("config-file".equals(authType)) {
            provider = new ConfigFileAuthenticationDetailsProvider("~/.oci/config", "DEFAULT");
        } else {
            provider = InstancePrincipalsAuthenticationDetailsProvider.builder().build();
        }

        return ObjectStorageClient.builder()
                .region(Region.fromRegionId(region))
                .build(provider);
    }
}
