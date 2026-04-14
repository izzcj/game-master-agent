package io.github.izzcj.gamemaster.ingest;

import io.github.izzcj.gamemaster.config.StorageProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * 本地文件系统存储实现。
 */
@Service
@ConditionalOnProperty(name = "agent.storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorageService implements ObjectStorageService {

    private final Path basePath;

    /**
     * 初始化本地存储根目录。
     *
     * @param storageProperties 存储配置
     * @throws IOException 目录创建失败
     */
    public LocalObjectStorageService(StorageProperties storageProperties) throws IOException {
        this.basePath = Path.of(storageProperties.getLocalBasePath()).toAbsolutePath().normalize();
        Files.createDirectories(basePath);
    }

    /**
     * 将文件写入本地目录。
     */
    @Override
    public String put(String objectKey, InputStream inputStream, long size, String contentType) throws IOException {
        Path target = basePath.resolve(objectKey).normalize();
        Files.createDirectories(target.getParent());
        Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    /**
     * 从本地目录读取对象。
     */
    @Override
    public StoredObject get(String objectKey) throws IOException {
        Path target = Path.of(objectKey);
        return new StoredObject(target.toString(), Files.newInputStream(target), Files.probeContentType(target));
    }
}
