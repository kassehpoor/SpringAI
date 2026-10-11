
package spring.ai.example.spring_ai_demo.service.v8;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchResultDTO;
import spring.ai.example.spring_ai_demo.service.v7.ChunkSearchService;

import java.util.List;

@Slf4j
@Component
public class InsuranceKnowledgeTools {

    private final ChunkSearchService chunkSearchService;

    public InsuranceKnowledgeTools(
            ChunkSearchService chunkSearchService) {
        this.chunkSearchService = chunkSearchService;
    }

    @Tool(description = """
            Search insurance and pension documents using semantic search.
            Return relevant passages from the documents.
            """)
    public List<ChunkSearchResultDTO> searchInsuranceKnowledge(
            @ToolParam(description = "Question to search in documents")
            String query) {

        log.info("Tool called: searchInsuranceKnowledge");

        if (query == null || query.isBlank()) {
            log.warn("Tool received an empty query");
            return List.of();
        }

        ChunkSearchRequestDTO request =
                new ChunkSearchRequestDTO();

        request.setQuery(query.trim());
        request.setTopK(5);

        List<ChunkSearchResultDTO> results =
                chunkSearchService.search(request);

        if (results == null || results.isEmpty()) {
            log.warn("No search results found");
            return List.of();
        }

        log.info("Tool search completed. Result count={}",
                results.size());

        results.forEach(result ->
                log.info(
                        "Search result: documentId={}, file={}, page={}, similarity={}",
                        result.getDocumentId(),
                        result.getFileName(),
                        result.getPageNumber(),
                        result.getCosineSimilarity()));

        return results;
    }
}
