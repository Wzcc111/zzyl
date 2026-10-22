package com.zzyl.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云 OSS 文件操作客户端。
 */
@Component
public class AliyunOSSOperator
{
    @Autowired
    private AliyunOSSProperties ossProperties;

    /**
     * 上传文件并返回公开访问地址。
     *
     * @param content 文件内容
     * @param originalFilename 原始文件名
     * @return 文件访问地址
     * @throws Exception OSS 凭证或上传异常
     */
    public String upload(byte[] content, String originalFilename) throws Exception
    {
        String endpoint = ossProperties.getEndpoint();
        String bucketName = ossProperties.getBucketName();
        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
        if (accessKeyId == null || accessKeyId.isBlank()
                || accessKeySecret == null || accessKeySecret.isBlank())
        {
            throw new IllegalStateException(
                    "请在运行环境中配置 OSS_ACCESS_KEY_ID 和 OSS_ACCESS_KEY_SECRET");
        }

        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String newFileName = UUID.randomUUID().toString()
                + originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dir + "/" + newFileName;

        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try
        {
            ossClient.putObject(bucketName, objectName, new ByteArrayInputStream(content));
        }
        finally
        {
            if (ossClient != null)
            {
                ossClient.shutdown();
            }
        }

        return endpoint.split("//")[0] + "//" + bucketName + "."
                + endpoint.split("//")[1] + "/" + objectName;
    }
}
