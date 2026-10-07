package spring.ai.example.spring_ai_demo.dto.v7;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import spring.ai.example.spring_ai_demo.dto.v6.document.InsuranceDocumentSearchResultDTO;

import java.util.List;

/**
 * @author Tahereh Kasehpoor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceRagResponseDTO {

    private String question;

    private String answer;

    // عنی علاوه بر پاسخ LLM، به کاربر می‌گوییم پاسخ بر اساس کدام Documents ساخته شده است.
    private List<InsuranceDocumentSearchResultDTO> sources;
}