package com.KDT.mosi.domain.restaurant.svc.dao;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class RestaurantInfoDAOImpl implements RestaurantInfoDAO {

  @Value("${busan.api.food.service-key}")
  private String foodServiceKey;

  @Value("${busan.api.food.url}")
  private String foodUrl;

  @Value("${elasticsearch.host}")
  private String elasticsearchHost;

  @Value("${elasticsearch.port}")
  private int elasticsearchPort;

  private final RestTemplate restTemplate;
  private List<RestaurantInfoDocument> cachedRestaurants;

  public RestaurantInfoDAOImpl() {
    this.restTemplate = new RestTemplate();
  }

  @Override
  public List<RestaurantInfoDocument> getRestaurants() {
    if (cachedRestaurants == null) {
      cachedRestaurants = fetchRestaurantsFromAPI();
    }
    return cachedRestaurants;
  }

  private List<RestaurantInfoDocument> fetchRestaurantsFromAPI() {
    try {
      // 가장 직관적이고 통상적인 방법: 웹 검색에서 성공한 URL 형태와 동일하게
      String url = String.format("%s/getFoodKr?serviceKey=%s&numOfRows=500&pageNo=1&resultType=json",
          foodUrl, foodServiceKey);

      log.info("API 호출: {}", url.replaceAll("serviceKey=[^&]*", "serviceKey=***"));
      log.info("실제 서비스키 길이: {}", foodServiceKey.length());

      // JSON 응답 받기 - Accept 헤더 명시적 설정
      HttpHeaders headers = new HttpHeaders();
      headers.set("Accept", "application/json");
      HttpEntity<?> entity = new HttpEntity<>(headers);
      
      ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
      Map<String, Object> jsonResponse = response.getBody();
      
      log.info("응답 Content-Type: {}", response.getHeaders().getContentType());
      
      if (jsonResponse == null || jsonResponse.isEmpty()) {
        log.error("API 응답이 비어있습니다.");
        return new ArrayList<>();
      }
      
      log.info("JSON 응답 키: {}", jsonResponse.keySet());
      log.info("JSON 응답 전체 구조: {}", jsonResponse);
      
      // JSON에서 직접 데이터 추출 (웹 검색 결과 구조에 맞게)
      return parseJsonResponseCorrect(jsonResponse);

    } catch (Exception e) {
      log.error("OpenAPI 호출 중 오류 발생: {}", e.getMessage());
      return new ArrayList<>();
    }
  }
  
  // 웹 검색에서 확인된 정확한 JSON 구조에 맞는 파싱
  private List<RestaurantInfoDocument> parseJsonResponseCorrect(Map<String, Object> response) {
    List<RestaurantInfoDocument> restaurants = new ArrayList<>();
    
    try {
      // 웹 검색 결과 구조: getFoodKr.item 배열
      Map<String, Object> getFoodKr = (Map<String, Object>) response.get("getFoodKr");
      if (getFoodKr == null) {
        log.error("응답에 'getFoodKr' 키가 없습니다.");
        return restaurants;
      }
      
      List<Map<String, Object>> items = (List<Map<String, Object>>) getFoodKr.get("item");
      if (items == null || items.isEmpty()) {
        log.warn("getFoodKr.item이 비어있습니다.");
        return restaurants;
      }
      
      log.info("JSON에서 {}개의 맛집 데이터 발견", items.size());
      
      for (Map<String, Object> item : items) {
        RestaurantInfoDocument restaurant = new RestaurantInfoDocument();
        
        // JSON Map에서 직접 값 추출 (웹 검색 결과 필드명과 정확히 일치)
        restaurant.setUcSeq(getLong(item, "UC_SEQ"));
        restaurant.setMainTitle(getString(item, "MAIN_TITLE"));
        restaurant.setGugunNm(getString(item, "GUGUN_NM"));
        restaurant.setLat(getDouble(item, "LAT"));
        restaurant.setLng(getDouble(item, "LNG"));
        restaurant.setAddr1(getString(item, "ADDR1"));
        restaurant.setCntctTel(getString(item, "CNTCT_TEL"));
        restaurant.setHomepageUrl(getString(item, "HOMEPAGE_URL"));
        restaurant.setUsageDayWeekAndTime(getString(item, "USAGE_DAY_WEEK_AND_TIME"));
        restaurant.setRprsnTvMenu(getString(item, "RPRSNTV_MENU"));
        restaurant.setMainImgThumb(getString(item, "MAIN_IMG_THUMB"));
        restaurant.setItemCntnts(getString(item, "ITEMCNTNTS"));
        
        if (restaurant.getMainTitle() != null && !restaurant.getMainTitle().trim().isEmpty()) {
          restaurants.add(restaurant);
        }
      }
      
      log.info("JSON 파싱 완료: {}개의 맛집 데이터 추출됨", restaurants.size());
      return restaurants;
      
    } catch (Exception e) {
      log.error("JSON 파싱 오류: {}", e.getMessage());
      return restaurants;
    }
  }
  
  // JSON Map 값 추출 헬퍼 메소드들
  private String getString(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value != null ? value.toString() : null;
  }
  
  private Double getDouble(Map<String, Object> map, String key) {
    try {
      Object value = map.get(key);
      return value != null ? Double.parseDouble(value.toString()) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }
  
  private Long getLong(Map<String, Object> map, String key) {
    try {
      Object value = map.get(key);
      return value != null ? Long.parseLong(value.toString()) : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  public Page<RestaurantInfoDocument> getRestaurantsPaged(int page, int size, String search, String district, String category) {
    List<RestaurantInfoDocument> allRestaurants = getRestaurants();

    List<RestaurantInfoDocument> filtered = allRestaurants.stream()
        .filter(r -> {
          if (StringUtils.hasText(search)) {
            String searchLower = search.toLowerCase();
            return (r.getMainTitle() != null && r.getMainTitle().toLowerCase().contains(searchLower)) ||
                (r.getAddr1() != null && r.getAddr1().toLowerCase().contains(searchLower)) ||
                (r.getRprsnTvMenu() != null && r.getRprsnTvMenu().toLowerCase().contains(searchLower));
          }
          return true;
        })
        .filter(r -> !StringUtils.hasText(district) || district.equals(r.getGugunNm()))
        .filter(r -> !StringUtils.hasText(category) ||
            (r.getItemCntnts() != null && r.getItemCntnts().contains(category)))
        .collect(Collectors.toList());

    int totalElements = filtered.size();
    int startIndex = (page - 1) * size;
    int endIndex = Math.min(startIndex + size, totalElements);

    List<RestaurantInfoDocument> pageContent = startIndex < totalElements ?
        filtered.subList(startIndex, endIndex) : Collections.emptyList();

    Pageable pageable = PageRequest.of(page - 1, size);
    return new PageImpl<>(pageContent, pageable, totalElements);
  }

  @Override
  public RestaurantInfoDocument getRestaurantById(Long id) {
    return getRestaurants().stream()
        .filter(r -> id.equals(r.getUcSeq()))
        .findFirst()
        .orElse(null);
  }

  @Override
  public List<RestaurantInfoDocument> getRelatedRestaurants(String district, Long excludeId, int limit) {
    return getRestaurants().stream()
        .filter(r -> district.equals(r.getGugunNm()) && !excludeId.equals(r.getUcSeq()))
        .limit(limit)
        .collect(Collectors.toList());
  }

  @Override
  public Map<String, Object> getAggregation(String field) {
    try {
      String url = String.format("http://%s:%d/restaurant_info/_search", elasticsearchHost, elasticsearchPort);
      Map<String, Object> query = Map.of(
          "size", 0,
          "aggs", Map.of(
              "group_by_field", Map.of(
                  "terms", Map.of("field", field + ".keyword", "size", 20)
              )
          )
      );

      Map<String, Object> response = restTemplate.postForObject(url, query, Map.class);
      Map<String, Object> aggs = (Map<String, Object>) response.get("aggregations");
      Map<String, Object> groupBy = (Map<String, Object>) aggs.get("group_by_field");
      List<Map<String, Object>> buckets = (List<Map<String, Object>>) groupBy.get("buckets");

      Map<String, Object> result = new LinkedHashMap<>();
      for (Map<String, Object> bucket : buckets) {
        result.put(bucket.get("key").toString(), bucket.get("doc_count"));
      }
      return result;
    } catch (Exception e) {
      return getAggregationFromMemory(field);
    }
  }

  private Map<String, Object> getAggregationFromMemory(String field) {
    List<RestaurantInfoDocument> restaurants = getRestaurants();
    Map<String, Long> aggregation = new HashMap<>();

    for (RestaurantInfoDocument restaurant : restaurants) {
      String value = null;
      if ("gugunNm".equals(field)) {
        value = restaurant.getGugunNm();
      } else if ("itemCntnts".equals(field)) {
        value = restaurant.getItemCntnts();
      }

      if (StringUtils.hasText(value)) {
        if ("itemCntnts".equals(field)) {
          value = cleanCategory(value);
          if (value == null) continue;
        }
        aggregation.merge(value, 1L, Long::sum);
      }
    }

    return aggregation.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit("itemCntnts".equals(field) ? 8 : 20)
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> (Object) entry.getValue(),
            (e1, e2) -> e1,
            LinkedHashMap::new
        ));
  }

  private String cleanCategory(String category) {
    if (!StringUtils.hasText(category)) return null;

    String cleaned = category.toLowerCase().trim();
    if (cleaned.contains("기타") || cleaned.contains("etc") ||
        cleaned.equals("-") || cleaned.equals("null")) {
      return null;
    }
    return category;
  }

  @Override
  public List<String> getDistrictList() {
    return getRestaurants().stream()
        .map(RestaurantInfoDocument::getGugunNm)
        .filter(StringUtils::hasText)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getCategoryList() {
    return getRestaurants().stream()
        .map(RestaurantInfoDocument::getItemCntnts)
        .filter(StringUtils::hasText)
        .map(this::cleanCategory)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  @Override
  public List<RestaurantInfoDocument> getRestaurantsForMap() {
    return getRestaurants();
  }
}