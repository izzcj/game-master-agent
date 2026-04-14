package io.github.izzcj.gamemaster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 对象存储配置。
 *
 * <p>支持本地文件存储与 MinIO 两种实现。
 */
@ConfigurationProperties("agent.storage")
public class StorageProperties {

    /** 存储提供方，支持 {@code local} 和 {@code minio}。 */
    private String provider = "local";
    /** 本地文件存储根目录。 */
    private String localBasePath = "data/uploads";
    /** MinIO 连接配置。 */
    private final Minio minio = new Minio();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getLocalBasePath() {
        return localBasePath;
    }

    public void setLocalBasePath(String localBasePath) {
        this.localBasePath = localBasePath;
    }

    public Minio getMinio() {
        return minio;
    }

    /**
     * MinIO 配置项。
     */
    public static class Minio {

        /** MinIO 服务地址。 */
        private String endpoint;
        /** 默认桶名称。 */
        private String bucket;
        /** Access Key。 */
        private String accessKey;
        /** Secret Key。 */
        private String secretKey;

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getAccessKey() {
            return accessKey;
        }

        public void setAccessKey(String accessKey) {
            this.accessKey = accessKey;
        }

        public String getSecretKey() {
            return secretKey;
        }

        public void setSecretKey(String secretKey) {
            this.secretKey = secretKey;
        }
    }
}
