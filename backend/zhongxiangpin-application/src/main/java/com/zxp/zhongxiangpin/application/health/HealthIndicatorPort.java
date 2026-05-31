package com.zxp.zhongxiangpin.application.health;

/**
 * 健康检查基础设施端口。
 *
 * <p>Application 层只依赖该端口，不直接依赖 MyBatis、JDBC、Redis 或具体客户端。
 * 每个实现必须只做轻量只读探针，不修改业务数据。</p>
 */
public interface HealthIndicatorPort {

    /**
     * 返回该探针对应的组件。
     *
     * @return 健康检查组件
     */
    HealthComponent component();

    /**
     * 执行只读健康探针。
     *
     * @return 组件状态
     */
    HealthComponentStatus check();
}
