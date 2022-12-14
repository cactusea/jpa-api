package com.jpa.reviewsystem.controller;

import com.jpa.reviewsystem.dto.req.ReviewSaveReqDto;
import com.jpa.reviewsystem.dto.req.ReviewUpdateReqDto;
import com.jpa.reviewsystem.dto.res.ReviewResDto;
import com.jpa.reviewsystem.service.ReviewService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional(readOnly = true)
@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
public class ReviewControllerTest {

    private static final String DEFAULT_PATH = "{class-name}/{method-name}";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @Autowired
    private ReviewService reviewService;

    @BeforeEach
    public void setUp(RestDocumentationContextProvider restDocumentationContextProvider) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                            .apply(documentationConfiguration(restDocumentationContextProvider))
                            .addFilters(new CharacterEncodingFilter("UTF-8", true))  // ?????? ?????? ??????
                            .alwaysDo(print())
                            .build();
    }

    /**
     * 1. ?????? ?????? ?????????
     *  - ?????? ??????
     *  - ????????? ??? ????????? ??????
     * 2. ?????? ???????????? ?????????
     * 3. ?????? ?????? ?????????
     * 4. ?????? ?????? ?????????
     *  - ????????? ??????
     *  - List(page) ??????
     *      - ???????????? default test
     *      - ???????????? ?????? test
     *  - ????????? ??? ????????? ??????
     */

    @DisplayName("?????? ??????")
    @Transactional
    @Test
    public void reviewPage() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.get("/review")
                        .param("lecture", "1")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "like,DESC")
        ).andExpect(status().isOk()).andDo(
                document(DEFAULT_PATH,
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        requestParameters(
                                parameterWithName("lecture").description("?????? ID"),
                                parameterWithName("page").description("????????? ??????"),
                                parameterWithName("size").description("????????? ??? ????????? ???"),
                                parameterWithName("sort").description("?????? ??????")
                        ),
                        responseFields(
                                beneathPath("data").withSubsectionId("data")
                                , fieldWithPath("content").description("????????? ?????????")
                                , fieldWithPath("content.[].id").description("?????? ID")
                                , fieldWithPath("content.[].grade").description("?????? ??????")
                                , fieldWithPath("content.[].like").description("?????? ????????? ???")
                                , fieldWithPath("content.[].context").description("?????? ??????")
                                , fieldWithPath("content.[].createdDate").description("?????? ??????").type(JsonFieldType.STRING).optional()
                                , fieldWithPath("content.[].modifiedDate").description("?????? ??????").type(JsonFieldType.STRING).optional()
                        ).and(subsectionWithPath("pageable").type(JsonFieldType.OBJECT).description("????????? Data"))
                         .and(subsectionWithPath("last").description(""))
                         .and(subsectionWithPath("totalPages").description(""))
                         .and(subsectionWithPath("totalElements").description(""))
                         .and(subsectionWithPath("numberOfElements").description(""))
                         .and(subsectionWithPath("size").description(""))
                         .and(subsectionWithPath("sort").type(JsonFieldType.OBJECT).description(""))
                         .and(subsectionWithPath("first").description(""))
                         .and(subsectionWithPath("number").description(""))
                         .and(subsectionWithPath("empty").description(""))
                        //page?????? ??????
                )
        );
    }

    @DisplayName("?????? ??????(??????)")
    @Transactional
    @Test
    public void review() throws Exception{
        Long id = 1L;
        mockMvc.perform(MockMvcRequestBuilders.get("/review/{reviewId}", id))
                .andExpect(status().isOk())
                .andDo(
                        document(DEFAULT_PATH,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                responseFields(
                                        beneathPath("data").withSubsectionId("data")
                                        , fieldWithPath("id").description("?????? ID")
                                        , fieldWithPath("grade").description("?????? ??????")
                                        , fieldWithPath("like").description("?????? ????????? ???")
                                        , fieldWithPath("context").description("?????? ??????")
                                        , fieldWithPath("createdDate").description("?????? ??????")
                                        , fieldWithPath("modifiedDate").description("?????? ??????")
                                )
                        )
                );
    }

    @DisplayName("?????? ??????")
    @Transactional
    @Test
    public void createReview() throws Exception {
        ReviewSaveReqDto req = new ReviewSaveReqDto();
        req.setContext("review create test");
        req.setGrade(3);
        req.setUserId(1L);
        req.setLectureId(1L);

        String body = objectMapper.writeValueAsString(req);

        mockMvc.perform(RestDocumentationRequestBuilders.post("/review").content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(DEFAULT_PATH,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("userId").description("userId").type(JsonFieldType.NUMBER)
                                        ,fieldWithPath("lectureId").description("lectureId").type(JsonFieldType.NUMBER)
                                        ,fieldWithPath("grade").description("grade").type(JsonFieldType.NUMBER)
                                        ,fieldWithPath("context").description("context").type(JsonFieldType.STRING)
                                )
                               , responseFields(
                                        beneathPath("data").withSubsectionId("data")
                                        ,fieldWithPath("reviewId").type(JsonFieldType.NUMBER).description("????????? ?????? ID")
                                )
                        )
                );
    }

    @DisplayName("?????? ??????")
    @Transactional
    @Test
    public void updateReview() throws Exception{
        ReviewUpdateReqDto req = new ReviewUpdateReqDto();
        req.setId(4L);
        req.setContext("review update test");
        req.setGrade(5);
        req.setLike(1);

        String body = objectMapper.writeValueAsString(req);
        Long reviewId = 4L;

        mockMvc.perform(RestDocumentationRequestBuilders.patch("/review/{reviewId}", reviewId).content(body).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(DEFAULT_PATH,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                requestFields(
                                        fieldWithPath("id").type(JsonFieldType.NUMBER).description("?????? ID")
                                        ,fieldWithPath("context").type(JsonFieldType.STRING).description("?????? ??????")
                                        ,fieldWithPath("grade").type(JsonFieldType.NUMBER).description("?????? ??????")
                                        ,fieldWithPath("like").type(JsonFieldType.NUMBER).description("?????? ????????? ???")
                                ), responseFields(
                                        beneathPath("data").withSubsectionId("data")
                                        ,fieldWithPath("reviewId").type(JsonFieldType.NUMBER).description("?????? ????????? ?????? ID")
                                )

                        )
                );
    }

    @DisplayName("?????? ????????? ??????")
    @Transactional
    @Test
    public void addLikeReview() throws Exception {
        Long id = 1L;
        ReviewResDto review = reviewService.getReview(id);
        int expect_like = review.getLike() + 1;

        reviewService.addLikeReview(1L);
        int current_like = reviewService.getReview(id).getLike();

        if(expect_like == current_like){
            mockMvc.perform(RestDocumentationRequestBuilders.patch("/review/like/{reviewId}", id).contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andDo(
                            document(DEFAULT_PATH,
                                    preprocessRequest(prettyPrint()),
                                    preprocessResponse(prettyPrint()),
                                    pathParameters(
                                            parameterWithName("reviewId").description("?????? ID")
                                    ), responseFields(
                                            fieldWithPath("data").type(JsonFieldType.NUMBER).description("?????? ????????? ?????? ID")
                                            ,fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("?????? ??????")
                                            ,fieldWithPath("msg").type(JsonFieldType.STRING).description("?????? ?????????")
                                    )

                            )
                    );
        }else{
            throw new Exception();
        }

    }
    @DisplayName("?????? ??????")
    @Transactional
    @Test
    public void deleteReview() throws Exception{

        Long reviewId = 3L;

        mockMvc.perform(RestDocumentationRequestBuilders.delete("/review/{reviewId}", reviewId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(
                        document(DEFAULT_PATH,
                                preprocessRequest(prettyPrint()),
                                preprocessResponse(prettyPrint()),
                                pathParameters(
                                        parameterWithName("reviewId").description("?????? ID")
                                ), responseFields(
                                        fieldWithPath("data").type(JsonFieldType.BOOLEAN).description("?????? ?????? ??????")
                                        ,fieldWithPath("statusCode").type(JsonFieldType.NUMBER).description("?????? ??????")
                                        ,fieldWithPath("msg").type(JsonFieldType.STRING).description("?????? ?????? ?????????")
                                )
                        )
                );
    }
}
