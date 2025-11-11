package com.example.backend.Service;
// package com.example.backend.Service.ServiceImpl;

// import java.io.IOException;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import software.amazon.awssdk.core.sync.RequestBody;
// import software.amazon.awssdk.services.s3.S3Client;
// import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
// import software.amazon.awssdk.services.s3.model.PutObjectRequest;

// @Service
// public class S3Service {

//     private final S3Client s3Client;
//     private final String bucketName;
//     private final String region;

//     public S3Service(S3Client s3Client, 
//                      @Value("${aws.s3.bucketName}") String bucketName, 
//                      @Value("${aws.region}") String region) {
//         this.s3Client = s3Client;
//         this.bucketName = bucketName;
//         this.region = region;
//     }

//     /**
//      * @param file 
//      * @param key 
//      * @return 
//      */
//     public String uploadFile(MultipartFile file, String key) throws IOException {
//         PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                 .bucket(bucketName)
//                 .key(key)
//                 .contentType(file.getContentType())
//                 .build();

//         s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

//         return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
//     }

//     /**
//      * @param key 
//      */
//     public void deleteFile(String key) {
//         DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
//                 .bucket(bucketName)
//                 .key(key)
//                 .build();
//         s3Client.deleteObject(deleteObjectRequest);
//     }
// }