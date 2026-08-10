package com.example.Job_Application_Portal.service;

import com.example.Job_Application_Portal.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResumeStorageServiceTest {
    @TempDir
    Path uploadDirectory;

    @Test
    void validResumeIsSavedWithUniqueSafeName() throws Exception {
        ResumeStorageService service = new ResumeStorageService(uploadDirectory.toString());
        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "my resume.pdf",
                "application/pdf",
                "resume content".getBytes()
        );

        String savedFileName = service.saveResume(resume);

        assertThat(savedFileName).endsWith(".pdf");
        assertThat(savedFileName).doesNotContain(" ");
        assertThat(Files.exists(uploadDirectory.resolve(savedFileName))).isTrue();
    }

    @Test
    void validDocxResumeIsSavedWithUniqueSafeName() throws Exception {
        ResumeStorageService service = new ResumeStorageService(uploadDirectory.toString());
        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "resume content".getBytes()
        );

        String savedFileName = service.saveResume(resume);

        assertThat(savedFileName).endsWith(".docx");
        assertThat(Files.exists(uploadDirectory.resolve(savedFileName))).isTrue();
    }

    @Test
    void mismatchedContentTypeIsRejected() {
        ResumeStorageService service = new ResumeStorageService(uploadDirectory.toString());
        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/x-msdownload",
                "bad".getBytes()
        );

        assertThatThrownBy(() -> service.saveResume(resume))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Resume content type must match PDF or DOCX");
    }

    @Test
    void invalidResumeTypeIsRejected() {
        ResumeStorageService service = new ResumeStorageService(uploadDirectory.toString());
        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "resume.exe",
                "application/octet-stream",
                "bad".getBytes()
        );

        assertThatThrownBy(() -> service.saveResume(resume))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Resume must be a PDF or DOCX file");
    }

    @Test
    void emptyResumeIsRejected() {
        ResumeStorageService service = new ResumeStorageService(uploadDirectory.toString());
        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "resume.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThatThrownBy(() -> service.saveResume(resume))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Please upload a resume file");
    }

    @Test
    void oversizedResumeIsRejected() {
        ResumeStorageService service = new ResumeStorageService(uploadDirectory.toString());
        MockMultipartFile resume = new MockMultipartFile(
                "resume",
                "resume.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                new byte[(5 * 1024 * 1024) + 1]
        );

        assertThatThrownBy(() -> service.saveResume(resume))
                .isInstanceOf(ValidationException.class)
                .hasMessage("Resume file must be 5 MB or smaller");
    }
}
