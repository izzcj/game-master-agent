package io.github.izzcj.gamemaster.ingest;

import java.io.IOException;
import java.io.InputStream;

/**
 * 对象存储抽象。
 */
public interface ObjectStorageService {

    /**
     * 写入对象。
     *
     * @param objectKey 对象键
     * @param inputStream 文件流
     * @param size 文件大小
     * @param contentType 内容类型
     * @return 存储后的路径或对象键
     * @throws IOException 写入失败
     */
    String put(String objectKey, InputStream inputStream, long size, String contentType) throws IOException;

    /**
     * 读取对象。
     *
     * @param objectKey 存储路径或对象键
     * @return 对象包装
     * @throws IOException 读取失败
     */
    StoredObject get(String objectKey) throws IOException;

    /**
     * 已存储对象的读取句柄。
     *
     * @param path 路径或对象键
     * @param inputStream 输入流
     * @param contentType 内容类型
     */
    record StoredObject(String path, InputStream inputStream, String contentType) {
    }
}
