package ibf_tfip.mini_project.backend.repositories;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import ibf_tfip.mini_project.backend.utils.GeneralUtils;

@Repository
public class S3Repo {

    @Autowired
    private AmazonS3 s3;

    public List<String> saveFilesToS3(MultipartFile[] files, String customJobId) {

        // Generate a dynamic folder name
        String folderName = "uploads_" + customJobId + "/";

        List<String> urls = new ArrayList<>();

        for (MultipartFile file: files) {
            String key = folderName + UUID.randomUUID().toString().substring(0, 8) + "_" + file.getOriginalFilename();
            try {
                // User metadata
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(file.getContentType());
                metadata.setContentLength(file.getSize());
                metadata.addUserMetadata("upload-timestamp", (new Date()).toString());
                metadata.addUserMetadata("filename", file.getOriginalFilename());
    
                // Upload file to S3
                PutObjectRequest putReq = new PutObjectRequest(GeneralUtils.S3_BUCKET_NAME, key, file.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead);
                PutObjectResult result = s3.putObject(putReq);
    
                System.out.println("PutObjectResult: " + result.toString());

                urls.add(s3.getUrl(GeneralUtils.S3_BUCKET_NAME, key).toString());
    
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return urls;
    }

    
    // ByteArrayResource because OpenAI accepts this when uploading files
    public List<ByteArrayResource> getFilesFromS3(String folderName) {
        List<String> fileIds = new ArrayList<>();

        // List objects in the specified S3 bucket
        ObjectListing objectListing = s3.listObjects(GeneralUtils.S3_BUCKET_NAME);
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            if (objectSummary.getKey().startsWith(folderName)) { // Filter objects by folderName
                fileIds.add(objectSummary.getKey());
            }
        }

        List<ByteArrayResource> resourceList = new ArrayList<>();

        for (String fileId : fileIds) {
            try {
                GetObjectRequest getRequest = new GetObjectRequest(GeneralUtils.S3_BUCKET_NAME, fileId);
                S3Object result = s3.getObject(getRequest);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                try (S3ObjectInputStream is = result.getObjectContent()) {
                    while ((len = is.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, len);
                    }

                    byte[] bytes = byteArrayOutputStream.toByteArray();
                    ByteArrayResource resource = new ByteArrayResource(bytes) {
                        @Override
                        public String getFilename() {
                            return result.getObjectMetadata().getUserMetadata().get("filename"); // Provide a meaningful file name here
                        }
                    };

                    resourceList.add(resource);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException("Error reading file content from S3", ex);
                }
            } catch (AmazonS3Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException("Error retrieving file metadata from S3", ex);
            }
        }

        return resourceList;
    }
}
