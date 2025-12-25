// package com.example.backend.Service;

// import java.io.IOException;
// import java.util.UUID;

// import org.springframework.beans.factory.annotation.Value;
// import org.springframework.stereotype.Service;
// import org.springframework.web.multipart.MultipartFile;

// import com.google.cloud.storage.Blob;
// import com.google.cloud.storage.Bucket;
// import com.google.firebase.cloud.StorageClient;

// @Service
// public class FirebaseStorageService {
//     @Value("${firebase.bucket.name}")
//     private String bucketName;

//     public String uploadImage(MultipartFile file) throws IOException {
//         String fileName = generateFileName(file.getOriginalFilename());
//         Bucket bucket = StorageClient.getInstance().bucket(bucketName);
//         Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());
//         return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", bucketName, fileName);
//     }

//     private String generateFileName(String originalFileName) {
//         return UUID.randomUUID().toString() + "_" + originalFileName;
//     }
// }
