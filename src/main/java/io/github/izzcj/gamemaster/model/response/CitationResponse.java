package io.github.izzcj.gamemaster.model.response;

/**
 * 引用信息响应。
 *
 * @param sourceType 来源类型
 * @param title 来源标题
 * @param locator 内部定位信息
 * @param url 来源链接
 * @param excerpt 引用摘录
 */
public record CitationResponse(
    String sourceType,
    String title,
    String locator,
    String url,
    String excerpt
) {
}
