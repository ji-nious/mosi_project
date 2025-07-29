package com.KDT.mosi.domain.restaurant.svc.dao;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Repository
public class RestaurantInfoDAOImpl implements RestaurantInfoDAO {
  @Value("${busan.api.food.service-key}")
  private String foodServiceKey;

  @Value("${busan.api.food.url}")
  private String foodUrl;

  @Value("${busan.api.facility.service-key}")
  private String facilityServiceKey;

  @Value("${busan.api.facility.url}")
  private String facilityUrl;

  @Value("${elasticsearch.host}")
  private String elasticsearchHost;

  @Value("${elasticsearch.port}")
  private int elasticsearchPort;

  private final RestTemplate restTemplate = new RestTemplate();

  // 캐시용 데이터 저장
  private List<RestaurantInfoDocument> cachedRestaurants;
  private List<RestaurantInfoDocument> cachedFacilities;

  @Override
  public List<RestaurantInfoDocument> getRestaurants() {
    if (cachedRestaurants == null) {
      cachedRestaurants = fetchRestaurantsFromAPI();
    }
    return cachedRestaurants;
  }

  @Override
  public List<RestaurantInfoDocument> getFacilities() {
    if (cachedFacilities == null) {
      cachedFacilities = fetchFacilitiesFromAPI();
    }
    return cachedFacilities;
  }

  private List<RestaurantInfoDocument> fetchRestaurantsFromAPI() {
    try {
      String url = String.format("%s/getFoodKr?serviceKey=%s&resultType=json&numOfRows=437&pageNo=1",
          foodUrl, foodServiceKey);

      System.out.println("🔗 부산 맛집 API 호출: " + url);
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      System.out.println("📡 API 응답: " + response);

      Map<String, Object> getFoodKr = (Map<String, Object>) response.get("getFoodKr");
      List<Map<String, Object>> items = (List<Map<String, Object>>) getFoodKr.get("item");

      List<RestaurantInfoDocument> restaurants = new ArrayList<>();
      for (Map<String, Object> item : items) {
        RestaurantInfoDocument doc = new RestaurantInfoDocument();

        // API 응답 필드명을 엔티티 필드명으로 매핑
        doc.setUcSeq(getLong(item, "UC_SEQ"));
        doc.setMainTitle(getString(item, "MAIN_TITLE"));
        doc.setGugunNm(getString(item, "GUGUN_NM"));
        doc.setLat(getDouble(item, "LAT"));
        doc.setLng(getDouble(item, "LNG"));
        doc.setPlace(getString(item, "PLACE"));
        doc.setTitle(getString(item, "TITLE"));
        doc.setSubTitle(getString(item, "SUBTITLE"));
        doc.setAddr1(getString(item, "ADDR1"));
        doc.setAddr2(getString(item, "ADDR2"));
        doc.setCntctTel(getString(item, "CNTCT_TEL"));
        doc.setHomepageUrl(getString(item, "HOMEPAGE_URL"));
        doc.setUsageDayWeekAndTime(getString(item, "USAGE_DAY_WEEK_AND_TIME"));
        doc.setRprsnTvMenu(getString(item, "RPRSNTV_MENU"));
        doc.setMainImgNormal(getString(item, "MAIN_IMG_NORMAL"));
        doc.setMainImgThumb(getString(item, "MAIN_IMG_THUMB"));
        doc.setItemCntnts(getString(item, "ITEMCNTNTS"));

        restaurants.add(doc);
      }
      
      System.out.println("✅ 맛집 데이터 " + restaurants.size() + "개 로드 완료");
      return restaurants;
    } catch (Exception e) {
      System.err.println("❌ API 호출 실패: " + e.getMessage());
      e.printStackTrace();
      return new ArrayList<>(); // 빈 리스트 반환
    }
  }

  private List<RestaurantInfoDocument> fetchFacilitiesFromAPI() {
    try {
      String url = String.format("%s/getFcltsDsgstInfo?serviceKey=%s&resultType=json&numOfRows=100&pageNo=1",
          facilityUrl, facilityServiceKey);

      Map<String, Object> response = restTemplate.getForObject(url, Map.class);

      Map<String, Object> getFacilities = (Map<String, Object>) response.get("getFcltsDsgstInfo");
      List<Map<String, Object>> items = (List<Map<String, Object>>) getFacilities.get("item");

      List<RestaurantInfoDocument> facilities = new ArrayList<>();
      for (Map<String, Object> item : items) {
        RestaurantInfoDocument doc = new RestaurantInfoDocument();

        // 편의시설 API 응답을 맛집 엔티티에 매핑 (공통 필드만)
        doc.setUcSeq(getLong(item, "UC_SEQ"));
        doc.setMainTitle(getString(item, "MAIN_TITLE"));
        doc.setGugunNm(getString(item, "GUGUN_NM"));
        doc.setLat(getDouble(item, "LAT"));
        doc.setLng(getDouble(item, "LNG"));
        doc.setAddr1(getString(item, "ADDR1"));
        doc.setCntctTel(getString(item, "CNTCT_TEL"));
        doc.setUsageDayWeekAndTime(getString(item, "USAGE_DAY_WEEK_AND_TIME"));
        doc.setMainImgNormal(getString(item, "MAIN_IMG_NORMAL"));

        facilities.add(doc);
      }
      return facilities;
    } catch (Exception e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  @Override
  public Page<RestaurantInfoDocument> getRestaurantsPaged(String district, String category, Pageable pageable) {
    List<RestaurantInfoDocument> allRestaurants = getRestaurants();

    // 필터링
    Stream<RestaurantInfoDocument> stream = allRestaurants.stream();
    if (district != null && !district.isEmpty()) {
      stream = stream.filter(r -> district.equals(r.getGugunNm()));
    }
    if (category != null && !category.isEmpty()) {
      stream = stream.filter(r -> r.getItemCntnts() != null && r.getItemCntnts().contains(category));
    }

    List<RestaurantInfoDocument> filtered = stream.collect(Collectors.toList());

    // 페이징 처리
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), filtered.size());
    List<RestaurantInfoDocument> pageContent = start < filtered.size() ?
        filtered.subList(start, end) : new ArrayList<>();

    return new PageImpl<>(pageContent, pageable, filtered.size());
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
  public List<RestaurantInfoDocument> getRestaurantsForMap() {
    // 좌표 정보가 있는 맛집만 반환 (부산 지역 좌표 범위 필터링)
    return getRestaurants().stream()
        .filter(r -> r.getLat() != null && r.getLng() != null)
        .filter(r -> r.getLat() > 34.5 && r.getLat() < 36.0) // 부산 위도 범위
        .filter(r -> r.getLng() > 128.5 && r.getLng() < 130.0) // 부산 경도 범위
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
                  "terms", Map.of(
                      "field", field + ".keyword",
                      "size", 20
                  )
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
      // Elasticsearch 연결 실패시 메모리 데이터로 집계
      return getAggregationFromMemory(field);
    }
  }

  private Map<String, Object> getAggregationFromMemory(String field) {
    List<RestaurantInfoDocument> restaurants = getRestaurants();
    Map<String, Long> aggregation = new HashMap<>();

    for (RestaurantInfoDocument restaurant : restaurants) {
      String value = null;
      switch (field) {
        case "gugunNm":
          value = restaurant.getGugunNm();
          break;
        case "itemCntnts":
          value = restaurant.getItemCntnts();
          break;
      }

      if (value != null && !value.trim().isEmpty()) {
        // 업종의 경우 "기타" 제외 및 정제
        if ("itemCntnts".equals(field)) {
          value = cleanCategory(value);
          if (value == null) continue; // "기타" 등은 제외
        }
        aggregation.merge(value, 1L, Long::sum);
      }
    }

    // 상위 항목만 반환 (업종의 경우 8개, 지역의 경우 전체)
    int limit = "itemCntnts".equals(field) ? 8 : 20;

    return aggregation.entrySet().stream()
        .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
        .limit(limit)
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> (Object) entry.getValue(),
            (e1, e2) -> e1,
            LinkedHashMap::new
        ));
  }

  private String cleanCategory(String category) {
    if (category == null) return null;

    String cleaned = category.toLowerCase().trim();

    // "기타" 관련 키워드 제외
    if (cleaned.contains("기타") ||
        cleaned.contains("etc") ||
        cleaned.equals("-") ||
        cleaned.isEmpty() ||
        cleaned.equals("null")) {
      return null;
    }

    return category;
  }

  @Override
  public List<String> getDistrictList() {
    return getRestaurants().stream()
        .map(RestaurantInfoDocument::getGugunNm)
        .filter(Objects::nonNull)
        .filter(district -> !district.trim().isEmpty())
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  @Override
  public List<String> getCategoryList() {
    return getRestaurants().stream()
        .map(RestaurantInfoDocument::getItemCntnts)
        .filter(Objects::nonNull)
        .filter(category -> cleanCategory(category) != null)
        .map(this::cleanCategory)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  // 헬퍼 메소드들
  private String getString(Map<String, Object> map, String key) {
    Object value = map.get(key);
    return value != null ? value.toString() : null;
  }

  private Double getDouble(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null) return null;
    try {
      return Double.parseDouble(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  private Long getLong(Map<String, Object> map, String key) {
    Object value = map.get(key);
    if (value == null) return null;
    try {
      return Long.parseLong(value.toString());
    } catch (NumberFormatException e) {
      return null;
    }
  }

  // 샘플 데이터 생성 메서드
  private List<RestaurantInfoDocument> createSampleRestaurantData() {
    List<RestaurantInfoDocument> sampleData = new ArrayList<>();
    
    RestaurantInfoDocument doc1 = new RestaurantInfoDocument();
    doc1.setUcSeq(1L);
    doc1.setMainTitle("해운대 맛집");
    doc1.setGugunNm("해운대구");
    doc1.setLat(35.1585);
    doc1.setLng(129.1600);
    doc1.setAddr1("부산광역시 해운대구");
    doc1.setItemCntnts("한식");
    doc1.setRprsnTvMenu("불고기");
    sampleData.add(doc1);

    RestaurantInfoDocument doc2 = new RestaurantInfoDocument();
    doc2.setUcSeq(2L);
    doc2.setMainTitle("서면 카페");
    doc2.setGugunNm("부산진구");
    doc2.setLat(35.1579);
    doc2.setLng(129.0589);
    doc2.setAddr1("부산광역시 부산진구");
    doc2.setItemCntnts("카페");
    doc2.setRprsnTvMenu("아메리카노");
    sampleData.add(doc2);

    RestaurantInfoDocument doc3 = new RestaurantInfoDocument();
    doc3.setUcSeq(3L);
    doc3.setMainTitle("중구 맛집");
    doc3.setGugunNm("중구");
    doc3.setLat(35.1040);
    doc3.setLng(129.0338);
    doc3.setAddr1("부산광역시 중구");
    doc3.setItemCntnts("일식");
    doc3.setRprsnTvMenu("초밥");
    sampleData.add(doc3);

    return sampleData;
  }
}