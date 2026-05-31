package com.zxp.zhongxiangpin.common.response;

import java.util.Collections;
import java.util.List;

/**
 * 统一分页数据结构。
 *
 * <p>所有分页接口必须把本对象放入 {@link ApiResponse#getData()}，
 * 字段固定为 {@code pageNo/pageSize/total/items}。</p>
 *
 * @param <T> 分页元素类型
 */
public class PageResponse<T> {

    private final int pageNo;        // 当前页码（从 1 开始）
    private final int pageSize;      // 每页条数
    private final long total;        // 总条数
    private final List<T> items;     // 当前页的数据列表

    private PageResponse(int pageNo, int pageSize, long total, List<T> items) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
        // null 时返回空列表，非 null 时用 List.copyOf 做不可变拷贝
        this.items = items == null ? Collections.emptyList() : List.copyOf(items);
    }

    /** 静态工厂方法，Controller 中使用：PageResponse.of(pageNo, pageSize, total, list) */
    public static <T> PageResponse<T> of(int pageNo, int pageSize, long total, List<T> items) {
        return new PageResponse<>(pageNo, pageSize, total, items);
    }

    public int getPageNo() {
        return pageNo;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotal() {
        return total;
    }

    public List<T> getItems() {
        return items;
    }
}
