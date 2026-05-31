package com.zxp.zhongxiangpin.trigger.http.trace;

import com.zxp.zhongxiangpin.common.trace.TraceIdContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * HTTP traceId 过滤器。
 *
 * <p>优先复用调用方传入的 {@code X-Trace-Id}，缺失时生成新的 traceId。
 * 请求结束后必须清理 ThreadLocal，避免应用服务器线程复用时串用上一请求的追踪 ID。</p>
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    // 请求/响应头中的 traceId 字段名
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 从请求头读取上游传入的 traceId（跨服务链路追踪时复用）
            TraceIdContext.set(request.getHeader(TRACE_ID_HEADER));
            // 2. 写入响应头（无论是传入的还是新生成的），让调用方能看到
            response.setHeader(TRACE_ID_HEADER, TraceIdContext.currentOrCreate());
            // 3. 放行请求，进入 Controller
            filterChain.doFilter(request, response);
        } finally {
            // 4. 必须清理 ThreadLocal，防止线程池复用导致 traceId 串线
            TraceIdContext.clear();
        }
    }
}
