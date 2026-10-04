package spring.ai.example.spring_ai_demo.service.v4.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v4.PdfUploadResponseDTO;
import spring.ai.example.spring_ai_demo.service.v4.PdfService;

/**
 * @author Tahereh Kasehpoor
 */


@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    @Override
    public PdfUploadResponseDTO upload(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file is empty");
        }

        String contentType = file.getContentType();

        if (!"application/pdf".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        return PdfUploadResponseDTO.builder()
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .size(file.getSize())
                .message("PDF uploaded successfully")
                .build();
    }
}