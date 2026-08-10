package com.example.Job_Application_Portal.service;

import com.example.Job_Application_Portal.exception.ValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.MalformedURLException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


// Handles resume file storage and validation
@Service
public class ResumeStorageService {

    // Resume upload settings
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // Resume upload setting 5MB
    // PDF files should have the content type application/pdf
    private static final Set<String> PDF_CONTENT_TYPES = Set.of("application/pdf");
    // standard content type for Microsoft Word .docx files.
    private static final Set<String> DOCX_CONTENT_TYPES = Set.of(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private final Path uploadDirectory; // represents the folder where resumes are stored.

    // Sets the resume upload directory
    public ResumeStorageService(@Value("${app.upload.dir:uploads/resumes}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).normalize();
    }

    // Saves an uploaded resume
    public String saveResume(MultipartFile resume) throws IOException {
        validateResume(resume); // it checks whether the file is allowed.

        Files.createDirectories(uploadDirectory); // Create folder

        String extension = getExtension(resume.getOriginalFilename()); // Get extension
        String savedFileName = UUID.randomUUID() + extension; // UUID gives each file a
        Path targetFile = uploadDirectory.resolve(savedFileName).normalize(); // uploads/resumes/unique name

        // security Check make sure its in right directory
        if (!targetFile.startsWith(uploadDirectory)) {
            throw new ValidationException("Invalid resume file name");
        }

        resume.transferTo(targetFile); // file is physically saved inside your project's resume folder.
        return savedFileName; // return file name
    }

    // Deletes a saved resume
    public void deleteResume(String savedFileName) {
        if (savedFileName == null || savedFileName.isBlank()) {
            return;
        }

        try {
            Path targetFile = uploadDirectory.resolve(savedFileName).normalize();
            if (targetFile.startsWith(uploadDirectory)) {
                Files.deleteIfExists(targetFile);
            }
        } catch (IOException ignored) {
        }
    }

    // Loads a saved resume
    public Resource loadResume(String savedFileName) throws MalformedURLException {
        if (savedFileName == null || savedFileName.isBlank()) {
            throw new ValidationException("Resume file name is required");
        }

        Path resumePath = uploadDirectory.resolve(savedFileName).normalize();
        if (!resumePath.startsWith(uploadDirectory)) {
            throw new ValidationException("Invalid resume file name");
        }

        Resource resource = new UrlResource(resumePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new ValidationException("Resume file is missing");
        }

        return resource;
    }

    // Validates resume size and file type
    private void validateResume(MultipartFile resume) {
        if (resume == null || resume.isEmpty()) {
            throw new ValidationException("Please upload a resume file");
        }

        if (resume.getSize() > MAX_FILE_SIZE) {
            throw new ValidationException("Resume file must be 5 MB or smaller");
        }

        String extension = getExtension(resume.getOriginalFilename());
        if (!".pdf".equals(extension) && !".docx".equals(extension)) {
            throw new ValidationException("Resume must be a PDF or DOCX file");
        }

        String contentType = resume.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new ValidationException("Resume file type could not be verified");
        }

        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        boolean validPdf = ".pdf".equals(extension) && PDF_CONTENT_TYPES.contains(normalizedContentType);
        boolean validDocx = ".docx".equals(extension) && DOCX_CONTENT_TYPES.contains(normalizedContentType);
        if (!validPdf && !validDocx) {
            throw new ValidationException("Resume content type must match PDF or DOCX");
        }
    }

    // Gets the file extension
    private String getExtension(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new ValidationException("Resume must be a PDF or DOCX file");
        }

        String cleanFileName = Path.of(fileName).getFileName().toString();
        int extensionStart = cleanFileName.lastIndexOf('.');
        if (extensionStart < 0) {
            throw new ValidationException("Resume must be a PDF or DOCX file");
        }

        return cleanFileName.substring(extensionStart).toLowerCase(Locale.ROOT);
    }
}
