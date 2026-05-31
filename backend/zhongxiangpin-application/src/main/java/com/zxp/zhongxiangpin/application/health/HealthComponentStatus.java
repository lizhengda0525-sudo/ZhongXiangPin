package com.zxp.zhongxiangpin.application.health;

/**
 * 基础设施健康状态。
 */
public enum HealthComponentStatus {

    /** 组件当前可访问。 */
    UP,

    /** 组件当前不可访问或探针执行失败。 */
    DOWN
}
