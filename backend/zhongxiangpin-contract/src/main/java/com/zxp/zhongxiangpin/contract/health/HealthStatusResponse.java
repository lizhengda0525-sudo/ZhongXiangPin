package com.zxp.zhongxiangpin.contract.health;

/**
 * 健康检查响应数据。
 *
 * <p>字段与 OpenAPI 的 {@code HealthStatus} schema 保持一致，只暴露组件状态和当前
 * profile，不返回连接串、Redis key、密码或内部异常细节。</p>
 */
public class HealthStatusResponse {

    private final String app;
    private final String db;
    private final String redis;
    private final String profile;

    public HealthStatusResponse(String app, String db, String redis, String profile) {
        this.app = app;
        this.db = db;
        this.redis = redis;
        this.profile = profile;
    }

    public String getApp() {
        return app;
    }

    public String getDb() {
        return db;
    }

    public String getRedis() {
        return redis;
    }

    public String getProfile() {
        return profile;
    }
}
