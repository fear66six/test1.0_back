package com.byrski.common.utils;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.model.BucketStat;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Slf4j
@Component
public class OssUtils {


    @Resource
    @Qualifier("ossClient")
    private OSS ossClient;

    public OSS getOssClient() {
        return ossClient;
    }

    public void getBucketDetails(String bucketName) {
        BucketStat stat = ossClient.getBucketStat(bucketName);
        // 获取Bucket的总存储量，单位为字节。
        log.info("Storage Size: {}", stat.getStorageSize());

        // 获取Bucket中总的Object数量。
        log.info("Total Object Count: {}", stat.getObjectCount());

        // 获取Bucket中已经初始化但还未完成（Complete）或者还未中止（Abort）的Multipart Upload数量。
        log.info("Multipart Upload Count: {}", stat.getMultipartUploadCount());

        // 获取标准存储类型Object的存储量，单位为字节。
        log.info("Standard Storage Size: {}", stat.getStandardStorage());

        // 获取标准存储类型的Object的数量。
        log.info("Standard Object Count: {}", stat.getStandardObjectCount());

        // 获取Bucket中Live Channel的数量。
        log.info("Live Channel Count: {}", stat.getLiveChannelCount());

        // 此次调用获取到的存储信息的时间点，格式为时间戳，单位为秒。
        log.info("Last Modified Time: {}", stat.getLastModifiedTime());

        // 获取低频存储类型Object的计费存储量，单位为字节。
        log.info("Infrequent Access Storage Size: {}", stat.getInfrequentAccessStorage());

        // 获取低频存储类型Object的实际存储量，单位为字节。
        log.info("Infrequent Access Real Storage Size: {}", stat.getInfrequentAccessRealStorage());

        // 获取低频存储类型的Object数量。
        log.info("Infrequent Access Object Count: {}", stat.getInfrequentAccessObjectCount());

        // 获取归档存储类型Object的计费存储量，单位为字节。
        log.info("Archive Storage Size: {}", stat.getArchiveStorage());

        // 获取归档存储类型Object的实际存储量，单位为字节。
        log.info("Archive Real Storage Size: {}", stat.getArchiveRealStorage());

        // 获取归档存储类型的Object数量。
        log.info("Archive Object Count: {}", stat.getArchiveObjectCount());

        // 获取冷归档存储类型Object的计费存储量，单位为字节。
        log.info("Cold Archive Storage Size: {}", stat.getColdArchiveStorage());

        // 获取冷归档存储类型Object的实际存储量，单位为字节。
        log.info("Cold Archive Real Storage Size: {}", stat.getColdArchiveRealStorage());

        // 获取冷归档存储类型的Object数量。
        log.info("Cold Archive Object Count: {}", stat.getColdArchiveObjectCount());
    }

    public File getImageFile(String bucketName, String objectName) throws Exception{
//        String bucketName = "dream-factory";
//        String objectName = "public/output/dragon/dragon1.png";
        // 创建一个临时文件
        File tempFile = File.createTempFile("oss-downloaded-", ".tmp");
        tempFile.deleteOnExit();

        try {

            String style = "image/resize,m_fixed,w_768,h_768";
            GetObjectRequest request = new GetObjectRequest(bucketName, objectName);
            request.setProcess(style);

            OSSObject ossObject = ossClient.getObject(request);
            InputStream content = ossObject.getObjectContent();
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = content.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            content.close();
            fileOutputStream.close();

            return tempFile; // 返回临时文件
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}

