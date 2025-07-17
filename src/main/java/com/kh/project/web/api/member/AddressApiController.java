package com.kh.project.web.api.member;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/address")
public class AddressApiController {

    @Value("${juso.api.confmKey}")
    private String confmKey;
    private final String jusoApiUrl = "https://business.juso.go.kr/addrlink/addrLinkApi.do";

    @GetMapping("/search")
    public String searchAddress(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "currentPage", defaultValue = "1") String currentPage,
            @RequestParam(value = "countPerPage", defaultValue = "10") String countPerPage) throws IOException, InterruptedException {
        
        // API 요청 URL 구성
        String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String requestUrl = jusoApiUrl +
                "?currentPage=" + currentPage +
                "&countPerPage=" + countPerPage +
                "&keyword=" + encodedKeyword +
                "&confmKey=" + confmKey +
                "&resultType=json";

        // Java 11+ 내장 HttpClient를 사용한 API 호출
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(requestUrl))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // API로부터 받은 JSON 응답 본문을 그대로 반환
        return response.body();
    }
}
