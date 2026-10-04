package spring.ai.example.spring_ai_demo.controller.v5;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import spring.ai.example.spring_ai_demo.dto.v5.TextSimilarityRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v5.TextSimilarityResponseDTO;
import spring.ai.example.spring_ai_demo.service.v5.TextSimilarityService;
import org.springframework.web.bind.annotation.*;


/**
 * @author Tahereh Kasehpoor
 */

/**

  {
   "textA": "شرایط بازنشستگی بیمه شده چیست؟",
   "textB": "برای بازنشسته شدن چه شرایطی لازم است؟"
  }

 */


/**

 {
    "textA": "شرایط بازنشستگی بیمه شده چیست؟",
    "textB": "برای دریافت بیمه بیکاری چه مدارکی لازم است؟"
  }
 */

@RestController
@RequestMapping("/api/v5/similarity")
@RequiredArgsConstructor
public class TextSimilarityController {

    private final TextSimilarityService textSimilarityService;

    @PostMapping("/text")
    public TextSimilarityResponseDTO calculate(
            @RequestBody TextSimilarityRequestDTO request) {

        return textSimilarityService.calculate(request);
    }
}