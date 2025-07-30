package com.KDT.mosi.domain.restaurant.svc.dao;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface RestaurantInfoDAO {
  // 기본 데이터 조회
  List<RestaurantInfoDocument> getRestaurants();

  // 지도용 맛집 데이터 조회
  List<RestaurantInfoDocument> getRestaurantsForMap();

  // 페이징 조회
  Page<RestaurantInfoDocument> getRestaurantsPaged(int page, int size, String search, String district, String category);

  // 상세 조회
  RestaurantInfoDocument getRestaurantById(Long id);

  // 관련 맛집 조회
  List<RestaurantInfoDocument> getRelatedRestaurants(String district, Long excludeId, int limit);

  // 집계 데이터 조회
  Map<String, Object> getAggregation(String field);

  // 필터 옵션 조회
  List<String> getDistrictList();
  List<String> getCategoryList();
}
