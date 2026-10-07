package spring.ai.example.spring_ai_demo.dto.v6.document;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author Tahereh Kasehpoor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//@Schema(description = "این DTO متن سندی را دریافت می کند که قرار است embedding آن ساخته و در دیتابیس ذخیره شود.")
public class InsuranceDocumentRequestDTO {

    @NotBlank(message = "Document content must not be blank")
    private String content;
}
