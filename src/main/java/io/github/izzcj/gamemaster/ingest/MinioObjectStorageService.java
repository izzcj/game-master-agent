package io.github.izzcj.gamemaster.ingest;

import io.github.izzcj.gamemaster.config.StorageProperties;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * MinIO 存储实现。
 */
@Service
@ConditionalOnProperty(name = "agent.storage.provider", havingValue = "minio")
public class MinioObjectStorageService implements ObjectStorageService {

    private final MinioClient minioClient;
    private final String bucket;

    /**
     * 初始化 MinIO 客户端并确保目标桶存在。
     *
     * @param storageProperties 存储配置
     * @throws Exception MinIO 初始化失败
     */
    public MinioObjectStorageService(StorageProperties storageProperties) throws Exception {
        StorageProperties.Minio minio = storageProperties.getMinio();
        this.bucket = minio.getBucket();
        this.minioClient = MinioClient.builder()
            .endpoint(minio.getEndpoint())
            .credentials(minio.getAccessKey(), minio.getSecretKey())
            .build();
        ensureBucket();
    }

    /**
     * 写入对象到 MinIO。
     */
    @Override
    public String put(String objectKey, InputStream inputStream, long size, String contentType) throws IOException {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .contentType(contentType)
                .stream(inputStream, size, -1)
                .build());
            return objectKey;
        } catch (Exception exception) {
            throw new IOException("Failed to store object in MinIO", exception);
        }
    }

    /**
     * 从 MinIO 读取对象。
     */
    @Override
    public StoredObject get(String objectKey) throws IOException {
        try {
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucket)
                .object(objectKey)
                .build());
            return new StoredObject(objectKey, inputStream, null);
        } catch (Exception exception) {
            throw new IOException("Failed to read object from MinIO", exception);
        }
    }

    private void ensureBucket() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
