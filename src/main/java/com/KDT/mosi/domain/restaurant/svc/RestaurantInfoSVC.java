package com.KDT.mosi.domain.restaurant.svc;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Map;

public interface RestaurantInfoSVC {
  // 대시보드 데이터
  Map<String, Object> getDashboardData();

  // 페이징 조회
  Page<RestaurantInfoDocument> getRestaurantsPaged(int page, int size, String district, String category);

  // 상세 조회
  RestaurantInfoDocument getRestaurantById(Long id);

  // 관련 맛집
  List<RestaurantInfoDocument> getRelatedRestaurants(String district, Long excludeId, int limit);

  // 지도용 데이터
  List<RestaurantInfoDocument> getRestaurantsForMap();

  // 필터 옵션
  List<String> getDistrictList();
  List<String> getCategoryList();

  // 편의시설
  List<RestaurantInfoDocument> getFacilities();
}
