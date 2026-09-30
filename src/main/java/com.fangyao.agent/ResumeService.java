package com.fangyao.agent;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final ResumeChunkService resumeChunkService;

    public ResumeService() {

        this.resumeRepository =
                new ResumeRepository();

        this.resumeChunkService =
                new ResumeChunkService();
    }

    public ResumeUploadResult processAndSaveResume(
            MultipartFile file
    ) {

        String text =
                extractText(
                        file
                );

        String fileName =
                file.getOriginalFilename();

        long resumeId =
                resumeRepository.save(
                        fileName,
                        text
                );

        List<String> chunks =
                resumeChunkService.chunkAndSave(
                        resumeId,
                        text
                );

        return new ResumeUploadResult(
                resumeId,
                fileName,
                text,
                chunks.size()
        );
    }

    public String extractText(
            MultipartFile file
    ) {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Resume file is required."
            );
        }

        String contentType =
                file.getContentType();

        String fileName =
                file.getOriginalFilename();

        boolean isPdf =
                "application/pdf".equalsIgnoreCase(
                        contentType
                )
                || (
                        fileName != null
                                && fileName
                                .toLowerCase()
                                .endsWith(".pdf")
                );

        if (!isPdf) {

            throw new IllegalArgumentException(
                    "Only PDF resumes are supported."
            );
        }

        try (
                PDDocument document =
                        Loader.loadPDF(
                                file.getBytes()
                        )
        ) {

            PDFTextStripper stripper =
                    new PDFTextStripper();

            String text =
                    stripper.getText(
                            document
                    );

            if (text == null
                    || text.isBlank()) {

                throw new IllegalArgumentException(
                        "No readable text was found in the PDF."
                );
            }

            return text.trim();

        } catch (IOException e) {

            throw new RuntimeException(
                    "Failed to extract resume text.",
                    e
            );
        }
    }

    public static class ResumeUploadResult {

        private final long id;
        private final String fileName;
        private final String text;
        private final int chunkCount;

        public ResumeUploadResult(
                long id,
                String fileName,
                String text,
                int chunkCount
        ) {

            this.id =
                    id;

            this.fileName =
                    fileName;

            this.text =
                    text;

            this.chunkCount =
                    chunkCount;
        }

        public long getId() {
            return id;
        }

        public String getFileName() {
            return fileName;
        }

        public String getText() {
            return text;
        }

        public int getChunkCount() {
            return chunkCount;
        }
    }
}