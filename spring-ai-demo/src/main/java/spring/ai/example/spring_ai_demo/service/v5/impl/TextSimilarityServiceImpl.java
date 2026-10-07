package spring.ai.example.spring_ai_demo.service.v5.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import spring.ai.example.spring_ai_demo.dto.v5.TextSimilarityRequestDTO;
import spring.ai.example.spring_ai_demo.dto.v5.TextSimilarityResponseDTO;
import spring.ai.example.spring_ai_demo.service.v5.CosineSimilarityService;
import spring.ai.example.spring_ai_demo.service.v5.TextSimilarityService;

/**
 * @author Tahereh Kasehpoor
 */


/**
 *
 * "شرایط بازنشستگی..."
 *         ↓
 * EmbeddingModel
 *         ↓
 * [0.12, -0.31, 0.82, ...]
 *
 *
 * "برای بازنشسته شدن..."
 *         ↓
 * EmbeddingModel
 *         ↓
 * [0.10, -0.29, 0.79, ...]
 *
 *
 *         ↓
 * Cosine Similarity
 *         ↓
 *
 *        0.91
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class TextSimilarityServiceImpl
        implements TextSimilarityService {

    private final EmbeddingModel embeddingModel;

    private final CosineSimilarityService cosineSimilarityService;

    @Override
    public TextSimilarityResponseDTO calculate(
            TextSimilarityRequestDTO request) {

        // 1. Create embedding for text A
        log.info("EmbeddingModel class = {}",
                embeddingModel.getClass().getName());

        log.info("Text A = {}", request.getTextA());

        System.out.println(embeddingModel.getClass().getName());

        float[] vectorA =
                embeddingModel.embed(request.getTextA());
        log.info("vectorA dimension  = {}", vectorA.length); //vectorA dimension  = 768


        // 2. Create embedding for text B
        float[] vectorB =
                embeddingModel.embed(request.getTextB());
        log.info("vectorB dimension  = {}", vectorB.length); //vectorB dimension  = 768

        // 3. Calculate cosine similarity
        double similarity =
                cosineSimilarityService.calculate(
                        vectorA,
                        vectorB
                );

        // 4. Build response
        return TextSimilarityResponseDTO.builder()
                .textA(request.getTextA())
                .textB(request.getTextB())
                .similarity(similarity)
                .build();
    }
}

/**
 *
 * تا اینجا ثابت کردیم که pgvector می‌تواند بردارها را ذخیره کند و بر اساس فاصله‌ی کسینوسی مرتب کند. حالا می‌خواهیم همین کار را با embedding واقعی مدل nomic-embed-text انجام بدهیم.
 */



/**
 V5
 Text
 ↓
 EmbeddingModel
 ↓
 float[768]
 */
/// ///////////////////////////////////////////////////////////////////////////////

/**
 Text
 ↓
 EmbeddingModel
 ↓
 float[768]
 ↓
 PostgreSQL
 ↓
 pgvector
 ↓
 VECTOR(768)



 و در نهایت بتوانیم مثلاً:

 "شرایط بازنشستگی بیمه شده چیست؟"

 را تبدیل کنیم به embedding و داخل PostgreSQL ذخیره کنیم.
 */

/// //////////////////////////////////////////////////////////////////////////////////

/**
 بعداً در V7 / RAG همین embedding را جستجو خواهیم کرد:


 Question
 ↓
 Embedding
 ↓
 PostgreSQL + pgvector
 ↓
 Similar Documents
 ↓
 LLM
 ↓
 Answer

 */

/// //////////////////////////////////////////////////////////////////////////////////

/***
 PostgreSQL برای Java مثل Oracle/MySQL از طریق JDBC قابل استفاده است:

 Spring Boot
 ↓
 Spring Data JPA / JDBC
 ↓
 PostgreSQL JDBC Driver
 ↓
 PostgreSQL

 اما برای vector:

 Spring Boot
 ↓
 Spring AI
 ↓
 EmbeddingModel
 ↓
 768-dimensional vector
 ↓
 pgvector
 ↓
 PostgreSQL

 بنابراین ما دو مفهوم جدا داریم:

 PostgreSQL

 Database relational معمولی:

 CREATE TABLE insurance_document (
 id BIGSERIAL PRIMARY KEY,
 content TEXT
 );
 pgvector

 به PostgreSQL یک data type جدید اضافه می‌کند:

 VECTOR(768)

 یعنی می‌توانیم چیزی شبیه این داشته باشیم:

 CREATE TABLE insurance_document (
 id BIGSERIAL PRIMARY KEY,
 content TEXT,
 embedding VECTOR(768)
 );
 */
/////////////////////////////////////////////////////////////////////////////////////

/**
 *
 * برو داخل همان:
 *
 * psql
 *
 * و اجرا کن:
 *
 * CREATE TABLE insurance_document (
 *     id BIGSERIAL PRIMARY KEY,
 *     content TEXT NOT NULL,
 *     embedding VECTOR(768)
 * );
 *
 * بعد:
 *
 * \d insurance_document
 *
 * باید چیزی شبیه این ببینی:
 *
 *  Column    | Type
 * -----------+----------------
 *  id        | bigint
 *  content   | text
 *  embedding | vector(768)
 *
 * این اولین milestone واقعی V6 است.
 */
////////////////////////////////////////////////////////////////////////////////////

/**
 * مرحله 4 — خود PostgreSQL را با vector امتحان کنیم
 *
 * حالا یک embedding خیلی ساده آزمایشی وارد کنیم.
 *
 * فعلاً چون فقط می‌خواهیم pgvector را یاد بگیریم، vector سه‌بعدی استفاده می‌کنیم:
 *
 * CREATE TABLE test_vectors (
 *     id BIGSERIAL PRIMARY KEY,
 *     embedding VECTOR(3)
 * );
 *
 * بعد:
 *
 * INSERT INTO test_vectors (embedding)
 * VALUES
 *     ('[1,2,3]'),
 *     ('[1,2,4]'),
 *     ('[10,20,30]');
 *
 * بررسی:
 *
 * SELECT *
 * FROM test_vectors;
 *
 * باید چیزی شبیه:
 *
 *  id | embedding
 * ----+-----------
 *   1 | [1,2,3]
 *   2 | [1,2,4]
 *   3 | [10,20,30]
 *
 * یعنی PostgreSQL الان واقعاً vector را می‌شناسد.

 */