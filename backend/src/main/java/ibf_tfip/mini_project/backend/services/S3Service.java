package ibf_tfip.mini_project.backend.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import ibf_tfip.mini_project.backend.repositories.S3Repo;

@Service
public class S3Service {
    
    @Autowired
    private S3Repo s3Repo;

    public List<String> saveFilesToS3(MultipartFile[] files, String customJobId) {
        return s3Repo.saveFilesToS3(files, customJobId);
    }

    public List<ByteArrayResource> getFilesFrS3(String folderName) {
        return s3Repo.getFilesFromS3(folderName);
    }
}
