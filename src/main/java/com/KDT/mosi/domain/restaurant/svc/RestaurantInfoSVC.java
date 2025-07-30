package com.KDT.mosi.domain.restaurant.svc;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

public interface RestaurantInfoSVC {
  // 대시보드 데이터
  Map<String, Object> getDashboardData();

  // 페이징 조회
  Page<RestaurantInfoDocument> getRestaurantsPaged(int page, int size, String search, String district, String category);

  // 지도용 맛집 데이터 조회
  List<RestaurantInfoDocument> getRestaurantsForMap();

  // 상세 조회
  RestaurantInfoDocument getRestaurantById(Long id);

  // 맛집 목록
  List<RestaurantInfoDocument> getRelatedRestaurants(String district, Long excludeId, int limit);

  // 검색 옵션
  List<String> getDistrictList();
  List<String> getCategoryList();

  // 검색 기능
  List<String> getSearchSuggestions(String query, int limit);
  void saveSearchHistory(String sessionId, String keyword);
  List<String> getSearchHistory(String sessionId, int limit);
  void clearSearchHistory(String sessionId);
}
