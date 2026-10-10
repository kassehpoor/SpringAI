package spring.ai.example.spring_ai_demo.controller.v7;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v7.PdfChunkResponseDTO;
import spring.ai.example.spring_ai_demo.service.v7.PdfChunkService;

/**
 * @author Tahereh Kasehpoor
 */


@RestController
@RequestMapping("/api/v7/chunk")
@RequiredArgsConstructor
public class PdfChunkController {

    private final PdfChunkService pdfChunkService;

    @PostMapping(
            consumes = "multipart/form-data"
    )
    public PdfChunkResponseDTO chunk(
            @RequestPart("file") MultipartFile file) {

        return pdfChunkService.chunk(file);
    }
}
