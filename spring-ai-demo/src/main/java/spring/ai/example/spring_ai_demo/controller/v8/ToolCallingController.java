package spring.ai.example.spring_ai_demo.controller.v8;



import org.springframework.web.bind.annotation.*;
import spring.ai.example.spring_ai_demo.dto.v8.ToolCallingResponseDTO;
import spring.ai.example.spring_ai_demo.service.v8.ToolCallingService;


@RestController
@RequestMapping("/api/v8/tools")
public class ToolCallingController {

    private final ToolCallingService toolCallingService;

    public ToolCallingController(ToolCallingService toolCallingService) {
        this.toolCallingService = toolCallingService;
    }

    /**
     تفاوت V7 و V8: در V7 خودمان مستقیماً جست‌وجو را اجرا می‌کردیم؛ در V8 مدل می‌تواند تشخیص دهد چه زمانی باید ابزار جست‌وجو را فراخوانی کند.
     */


    @PostMapping("/ask")
    public ToolCallingResponseDTO ask(@RequestBody Request request) {

        String answer = toolCallingService.ask(request.question());

        return new ToolCallingResponseDTO(request.question(), answer);
    }

    public record Request(String question) {
    }
}


/**
 {
 "question": "حداقل سابقه قانونی بازنشستگی چند سال است؟"
 }

 {
 "question": "حداقل سابقه قانونی بازنشستگی چند سال است؟",
 "answer": "اطلاعات کافی برای پاسخ دقیق به این سؤال در اسناد موجود نیست."
 }
 */


/**
 {
 "question": "شرایط بازنشستگی بیمه‌شده چیست؟"
 }
 */