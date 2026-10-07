package spring.ai.example.spring_ai_demo.service.v7;

/**
 * @author Tahereh Kasehpoor
 */


/** RAGService
 * Question
 *    ↓
 * Embedding
 *    ↓
 * Search PostgreSQL
 *    ↓
 * Take Top K content
 *    ↓
 * Build Context
 *    ↓
 * Send Context + Question to ChatClient
 *    ↓
 * Answer
 */
public class RAGService {
}


/**
 *
 * پس در V7 اول RAG را با همین معماری خودمان می‌سازیم:
 *
 * JdbcTemplate
 *      +
 * pgvector
 *      +
 * EmbeddingModel
 *      +
 * ChatClient
 *
 * بعد که مفهوم RAG کاملاً جا افتاد، می‌توانیم ببینیم Spring AI PgVectorStore چطور همین کار را abstraction می‌کند.
 */



/** v6
 Question
 ↓
 Embedding
 ↓
 Vector Search
 ↓
 Documents


 V6 به تو می‌گوید:
 Document 1
 distance = 0.0903

 Document 2
 distance = 0.1476

 Document 6
 distance = 0.1840
 */





/** v7
 Question
 ↓
 Embedding
 ↓
 Vector Search
 ↓
 Relevant Documents
 ↓
 Context
 ↓
 LLM
 ↓
 Answer




 اما V7 باید از این اطلاعات استفاده کند:
 Context:

 شرایط بازنشستگی بیمه شده بر اساس سن و سابقه پرداخت حق بیمه تعیین می‌شود...

 شده برای شود. بیمهشده بر اساس مقررات مربوط به سن و سابقه پرداخت حق بیمه...





 و بعد به llama3.2 بگوید:
 با استفاده از Context زیر به سؤال کاربر پاسخ بده.

 Context:
 ...

 Question:
 شرایط بازنشستگی بیمه شده چیست؟





 LLM مثلاً پاسخ می‌دهد:

 شرایط بازنشستگی بر اساس سن و سابقه پرداخت حق بیمه تعیین می‌شود. همچنین بیمه‌شده باید حداقل سابقه لازم و شرایط سنی و قانونی مربوط به بازنشستگی را احراز کند.

 این دیگر RAG است.
 */