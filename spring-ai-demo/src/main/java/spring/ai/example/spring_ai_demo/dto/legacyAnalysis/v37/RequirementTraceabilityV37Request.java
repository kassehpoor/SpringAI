package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v37;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequirementTraceabilityV37Request {

    @NotBlank
    private String requirement;

    private String schema;

    private String oracleObjectName;

    private String oracleObjectType;

    @Builder.Default
    @Min(1)
    @Max(10)
    private Integer maxDepth = 3;

    @Builder.Default
    @Min(1)
    @Max(20)
    private Integer topK = 5;
}