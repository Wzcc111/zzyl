package com.zzyl.oss;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AliyunOSSPropertiesTest
{
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfiguration.class)
            .withPropertyValues(
                    "aliyun.oss.endpoint=https://oss-cn-beijing.aliyuncs.com",
                    "aliyun.oss.bucket-name=test-bucket"
            );

    @Test
    void bindsEndpointAndBucketName()
    {
        contextRunner.run(context -> {
            AliyunOSSProperties properties = context.getBean(AliyunOSSProperties.class);
            assertThat(properties.getEndpoint()).isEqualTo("https://oss-cn-beijing.aliyuncs.com");
            assertThat(properties.getBucketName()).isEqualTo("test-bucket");
        });
    }

    @EnableConfigurationProperties(AliyunOSSProperties.class)
    static class TestConfiguration
    {
    }
}
