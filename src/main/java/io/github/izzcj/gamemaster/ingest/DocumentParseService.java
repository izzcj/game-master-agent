package io.github.izzcj.gamemaster.ingest;

import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.exception.TikaException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

/**
 * 文档解析服务。
 *
 * <p>使用 Apache Tika 将上传文件抽取为纯文本。
 */
@Service
public class DocumentParseService {

    private final Tika tika = new Tika();

    /**
     * 解析输入流为文本内容。
     *
     * @param inputStream 文档输入流
     * @return 解析后的文本
     * @throws IOException 解析失败
     */
    public String parse(InputStream inputStream) throws IOException {
        try {
            return tika.parseToString(inputStream);
        } catch (TikaException exception) {
            throw new IOException("Failed to parse document", exception);
        }
    }
}
