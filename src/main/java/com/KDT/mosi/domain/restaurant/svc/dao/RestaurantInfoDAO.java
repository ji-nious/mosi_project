package com.KDT.mosi.domain.restaurant.svc.dao;
import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

public interface RestaurantInfoDAO {
  // 기본 데이터 조회
  List<RestaurantInfoDocument> getRestaurants();
  List<RestaurantInfoDocument> getFacilities();

  // 페이징 조회
  Page<RestaurantInfoDocument> getRestaurantsPaged(String district, String category, Pageable pageable);

  // 상세 조회
  RestaurantInfoDocument getRestaurantById(Long id);

  // 관련 맛집 조회
  List<RestaurantInfoDocument> getRelatedRestaurants(String district, Long excludeId, int limit);

  // 지도용 데이터 조회
  List<RestaurantInfoDocument> getRestaurantsForMap();

  // 집계 데이터 조회
  Map<String, Object> getAggregation(String field);

  // 필터 옵션 조회
  List<String> getDistrictList();
  List<String> getCategoryList();
}
