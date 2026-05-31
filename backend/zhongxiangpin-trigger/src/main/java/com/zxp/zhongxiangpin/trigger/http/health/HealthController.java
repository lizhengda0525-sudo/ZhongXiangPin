package com.zxp.zhongxiangpin.trigger.http.health;

import com.zxp.zhongxiangpin.application.health.HealthCheckResult;
import com.zxp.zhongxiangpin.application.health.HealthCheckService;
import com.zxp.zhongxiangpin.common.response.ApiResponse;
import com.zxp.zhongxiangpin.contract.health.HealthStatusResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查 HTTP 入口。
 *
 * <p>用于本地和部署环境确认应用、数据库和 Redis 状态。响应遵守统一信封，不暴露连接串、
 * 密码、Redis key 或内部异常。</p>
 */
@RestController
@RequestMapping("/api/v1")
public class HealthController {

    private final HealthCheckService healthCheckService;

    public HealthController(HealthCheckService healthCheckService) {
        this.healthCheckService = healthCheckService;
    }

    /**
     * 查询应用健康状态。
     *
     * @return 统一响应信封，data 包含 app/db/redis/profile
     */
    @GetMapping("/health")
    public ApiResponse<HealthStatusResponse> health() {
        HealthCheckResult result = healthCheckService.check();
        HealthStatusResponse response = new HealthStatusResponse(
                result.getApp().name(),
                result.getDb().name(),
                result.getRedis().name(),
                result.getProfile()
        );
        return ApiResponse.success(response);
    }
}
