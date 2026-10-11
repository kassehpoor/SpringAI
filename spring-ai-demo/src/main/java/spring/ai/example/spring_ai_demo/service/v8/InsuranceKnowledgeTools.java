package spring.ai.example.spring_ai_demo.service.v8;

/**
 * @author Tahereh Kasehpoor
 */


import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v7.ChunkSearchResultDTO;
import spring.ai.example.spring_ai_demo.service.v7.ChunkSearchService;


import java.util.List;

@Component
public class InsuranceKnowledgeTools {

    private final ChunkSearchService chunkSearchService;

    public InsuranceKnowledgeTools(ChunkSearchService chunkSearchService) {
        this.chunkSearchService = chunkSearchService;
    }

    @Tool(description = """
        جست‌وجوی معنایی در اسناد بیمه و مستمری.
        برای پاسخ به پرسش‌هایی درباره مقررات، بازنشستگی،
        خدمات درمانی و محتوای اسناد از این ابزار استفاده کن.
        """)
    public List<ChunkSearchResultDTO> searchInsuranceKnowledge(
            @ToolParam(description = "پرسش دقیق برای جست‌وجو در اسناد")
            String query,

            @ToolParam(description = "تعداد نتایج، عددی بین 1 تا 10")
            Integer topK) {

        int resultCount = (topK == null) ? 5 : Math.clamp(topK, 1, 10);

        ChunkSearchRequestDTO request = new ChunkSearchRequestDTO();
        request.setQuery(query);
        request.setTopK(resultCount);

        return chunkSearchService.search(request);
    }
}