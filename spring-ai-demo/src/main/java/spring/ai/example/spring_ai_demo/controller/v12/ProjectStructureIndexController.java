package spring.ai.example.spring_ai_demo.controller.v12;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spring.ai.example.spring_ai_demo.dto.v12.ProjectIndexResponse;
import spring.ai.example.spring_ai_demo.service.v12.ProjectStructureIndexService;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v12/project")
public class ProjectStructureIndexController {

    private final ProjectStructureIndexService indexService;

    public ProjectStructureIndexController(
            ProjectStructureIndexService indexService) {
        this.indexService = indexService;
    }

    @PostMapping(
            value = "/index",
            consumes = "multipart/form-data"
    )
    public ResponseEntity<?> buildIndex(
            @RequestPart("file") MultipartFile file) {

        try {
            ProjectIndexResponse response =
                    indexService.buildIndex(file);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(
                    Map.of(
                            "success", false,
                            "message", exception.getMessage()
                    )
            );

        } catch (IOException exception) {
            return ResponseEntity.status(
                    HttpStatus.INTERNAL_SERVER_ERROR
            ).body(
                    Map.of(
                            "success", false,
                            "message", "خطا در خواندن فایل ZIP."
                    )
            );
        }
    }
}
