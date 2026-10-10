//package spring.ai.example.spring_ai_demo.controller.v5;
//
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.web.bind.annotation.*;
//import spring.ai.example.spring_ai_demo.dto.v5.ThreeTextSimilarityRequestDTO;
//import spring.ai.example.spring_ai_demo.dto.v5.ThreeTextSimilarityResponseDTO;
//import spring.ai.example.spring_ai_demo.service.v5.ThreeTextSimilarityService;
//
//
/// **
// * @author Tahereh Kasehpoor
// */
//
//@RestController
//@RequestMapping("/api/v5/similarity")
//@RequiredArgsConstructor
//public class ThreeTextSimilarityController {
//
//    private final ThreeTextSimilarityService similarityService;
//
//    /**
//     *                 ┌── vectorA
//     *                 │
//     * textA ──────────┤
//     *                 │
//     *                 ├──── Similarity A-B
//     *                 │
//     *                 └──── Similarity A-C
//     *
//     *
//     * textB ──→ vectorB
//     *               │
//     *               └──── Similarity B-C
//     *
//     * textC ──→ vectorC
//     */
//
//
//
//    /**
//     *
//     {
//     "textA": "شرایط بازنشستگی بیمه شده چیست؟",
//     "textB": "برای بازنشسته شدن چه شرایطی لازم است؟",
//     "textC": "شرایط دریافت بیمه بیکاری چیست؟"
//     }
//     */
//
//
//    /**
//     *
//     {
//     "textA": "شرایط بازنشستگی بیمه شده چیست؟",
//     "textB": "بیمه شده برای بازنشسته شدن باید چه شرایطی داشته باشد؟",
//     "textC": "برای دریافت کمک هزینه درمان چه شرایطی لازم است؟"
//     }
//     */
//
//    @PostMapping("/three-texts")
//    public ThreeTextSimilarityResponseDTO calculate(
//            @RequestBody ThreeTextSimilarityRequestDTO request) {
//
//        return similarityService.calculate(request);
//    }
//}