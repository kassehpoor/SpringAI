package spring.ai.example.spring_ai_demo.dto.legacyAnalysis.v44;

import java.util.List;

public record DataModelProposalV44DTO(
        String title,
        String designRationale,
        List<EntityProposal> entities,
        List<RelationshipProposal> relationships,
        List<String> businessConstraints,
        List<String> unresolvedQuestions
//        ,String mermaidErd
) {
    public record EntityProposal(
            String name,
            String description,
            List<AttributeProposal> attributes,
            String sourceEvidence,
            String status
    ) {}

    public record AttributeProposal(
            String name,
            String dataType,
            boolean nullable,
            boolean primaryKey,
            String description,
            String sourceEvidence
    ) {}

    public record RelationshipProposal(
            String sourceEntity,
            String targetEntity,
            String relationshipType,
            String description,
            String sourceEvidence,
            String status
    ) {}
}