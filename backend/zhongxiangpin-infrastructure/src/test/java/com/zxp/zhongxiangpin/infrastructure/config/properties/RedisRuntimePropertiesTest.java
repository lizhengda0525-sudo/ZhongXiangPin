package com.zxp.zhongxiangpin.infrastructure.config.properties;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RedisRuntimePropertiesTest {

    @Test
    void buildsEnvironmentPrefixFromConfiguredParts() {
        RedisRuntimeProperties properties = new RedisRuntimeProperties();
        properties.setKeyPrefix("zxp");
        properties.setEnvironment("test");

        assertEquals("zxp:test", properties.buildEnvironmentPrefix());
    }
}
