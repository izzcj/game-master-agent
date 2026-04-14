package io.github.izzcj.gamemaster.rag;

import io.github.izzcj.gamemaster.support.enums.EvidenceType;
import java.util.Map;

/**
 * 统一证据模型。
 *
 * @param type 证据类型
 * @param title 来源标题
 * @param content 证据正文或摘要
 * @param sourceType 来源分类
 * @param locator 内部定位信息
 * @param url 外部链接或源地址
 * @param score 排序得分
 * @param metadata 附加元数据
 */
public record Evidence(
    EvidenceType type,
    String title,
    String content,
    String sourceType,
    String locator,
    String url,
    double score,
    Map<String, Object> metadata
) {
}
