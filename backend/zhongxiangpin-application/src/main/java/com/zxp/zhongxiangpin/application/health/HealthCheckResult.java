package com.zxp.zhongxiangpin.application.health;

/**
 * 应用健康检查结果。
 *
 * <p>该结果是 trigger 层响应 DTO 的输入模型，不承载 HTTP 语义，也不暴露基础设施异常。</p>
 */
public class HealthCheckResult {

    private final HealthComponentStatus app;
    private final HealthComponentStatus db;
    private final HealthComponentStatus redis;
    private final String profile;

    public HealthCheckResult(HealthComponentStatus app,
                             HealthComponentStatus db,
                             HealthComponentStatus redis,
                             String profile) {
        this.app = app;
        this.db = db;
        this.redis = redis;
        this.profile = profile;
    }

    public HealthComponentStatus getApp() {
        return app;
    }

    public HealthComponentStatus getDb() {
        return db;
    }

    public HealthComponentStatus getRedis() {
        return redis;
    }

    public String getProfile() {
        return profile;
    }
}
