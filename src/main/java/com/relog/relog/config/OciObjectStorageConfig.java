package com.relog.relog.config;

import com.oracle.bmc.Region;
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OciObjectStorageConfig {

    @Value("${oci.region:ap-chuncheon-1}")
    private String region;

    @Bean
    public ObjectStorageClient objectStorageClient() {
        InstancePrincipalsAuthenticationDetailsProvider provider =
                InstancePrincipalsAuthenticationDetailsProvider.builder().build();

        return ObjectStorageClient.builder()
                .region(Region.fromRegionId(region))
                .build(provider);
    }
}
