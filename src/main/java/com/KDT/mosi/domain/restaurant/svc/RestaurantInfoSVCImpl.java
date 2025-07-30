package com.KDT.mosi.domain.restaurant.svc;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import com.KDT.mosi.domain.restaurant.svc.dao.RestaurantInfoDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantInfoSVCImpl implements RestaurantInfoSVC {

  private final RestaurantInfoDAO restaurantInfoDAO;
  private final Map<String, List<String>> searchHistoryMap = new ConcurrentHashMap<>();

  @Override
  public Map<String, Object> getDashboardData() {
    try {
      Map<String, Object> districts = restaurantInfoDAO.getAggregation("gugunNm");
      Map<String, Object> categories = restaurantInfoDAO.getAggregation("itemCntnts");

      return Map.of(
          "districts", districts,
          "categories", categories,
          "totalRestaurants", getTotalCount(),
          "totalDistricts", districts.size(),
          "totalCategories", categories.size(),
          "averageRating", 0.0,
          "topDistricts", new java.util.ArrayList<>(),
          "recentRestaurants", new java.util.ArrayList<>()
      );
    } catch (Exception e) {
      // 예외 발생시 기본값 반환
      return Map.of(
          "districts", new HashMap<>(),
          "categories", new HashMap<>(),
          "totalRestaurants", 0L,
          "totalDistricts", 0,
          "totalCategories", 0,
          "averageRating", 0.0,
          "topDistricts", new java.util.ArrayList<>(),
          "recentRestaurants", new java.util.ArrayList<>()
      );
    }
  }

  private long getTotalCount() {
    return restaurantInfoDAO.getRestaurants().size();
  }

  // 기존 페이징 메서드 (검색어 포함, null이면 검색 안함)
  @Override
  public Page<RestaurantInfoDocument> getRestaurantsPaged(int page, int size, String search, String district, String category) {
    return restaurantInfoDAO.getRestaurantsPaged(page, size, search, district, category);
  }

  @Override
  public List<RestaurantInfoDocument> getRestaurantsForMap() {
    return restaurantInfoDAO.getRestaurants();
  }

  @Override
  public RestaurantInfoDocument getRestaurantById(Long id) {
    return restaurantInfoDAO.getRestaurantById(id);
  }

  @Override
  public List<RestaurantInfoDocument> getRelatedRestaurants(String district, Long excludeId, int limit) {
    return restaurantInfoDAO.getRelatedRestaurants(district, excludeId, limit);
  }

  @Override
  public List<String> getDistrictList() {
    return restaurantInfoDAO.getDistrictList();
  }

  @Override
  public List<String> getCategoryList() {
    return restaurantInfoDAO.getCategoryList();
  }


  /**
   * 검색 자동완성 제안어 조회
   */
  @Override
  public List<String> getSearchSuggestions(String query, int limit) {
    List<RestaurantInfoDocument> allRestaurants = restaurantInfoDAO.getRestaurants();

    return allRestaurants.stream()
        .filter(r -> r.getMainTitle() != null)
        .map(RestaurantInfoDocument::getMainTitle)
        .filter(title -> title.contains(query))
        .distinct()
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * 검색 기록 저장 (세션별, 최대 20개)
   */
  @Override
  public void saveSearchHistory(String sessionId, String keyword) {
    if (!StringUtils.hasText(keyword)) return;

    List<String> history = searchHistoryMap.computeIfAbsent(sessionId, k -> new ArrayList<>());

    // 중복 제거 후 맨 앞에 추가
    history.remove(keyword);
    history.add(0, keyword);

    // 최대 10개만 유지
    if (history.size() > 10) {
      history.subList(10, history.size()).clear();
    }
  }

  /**
   * 검색 기록 조회 (세션별)
   */
  @Override
  public List<String> getSearchHistory(String sessionId, int limit) {
    List<String> history = searchHistoryMap.get(sessionId);
    if (history == null || history.isEmpty()) {
      return Collections.emptyList();
    }

    return history.stream()
        .limit(limit)
        .collect(Collectors.toList());
  }

  /**
   * 검색 기록 삭제 (세션별)
   */
  @Override
  public void clearSearchHistory(String sessionId) {
    searchHistoryMap.remove(sessionId);
  }
}