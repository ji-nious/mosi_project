package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import com.KDT.mosi.domain.restaurant.svc.RestaurantInfoSVC;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class RestaurantController {

  private final RestaurantInfoSVC restaurantInfoSVC;

  /**
   * 맛집 메인 페이지
   */
  @GetMapping("/information")
  public String restaurantMain(
      @RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "size", defaultValue = "12") int size,
      @RequestParam(name = "search", required = false) String search,
      @RequestParam(name = "district", required = false) String district,
      @RequestParam(name = "category", required = false) String category,
      Model model) {

    // 대시보드 데이터
    model.addAttribute("dashboardData", restaurantInfoSVC.getDashboardData());

    // 지도용 전체 맛집 데이터 (좌표 있는 것만)
    List<RestaurantInfoDocument> mapRestaurants = restaurantInfoSVC.getRestaurantsForMap();
    model.addAttribute("mapRestaurants", mapRestaurants);

    // 맛집 목록
    Page<RestaurantInfoDocument> restaurants = restaurantInfoSVC.getRestaurantsPaged(page, size, search, district, category);
    System.out.println("=== 맛집 목록 디버깅 ===");
    System.out.println("총 맛집 수: " + restaurants.getTotalElements());
    System.out.println("현재 페이지 맛집 수: " + restaurants.getContent().size());
    System.out.println("전체 페이지 수: " + restaurants.getTotalPages());
    System.out.println("현재 페이지: " + page);
    
    model.addAttribute("restaurants", restaurants);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", restaurants.getTotalPages());

    // 필터 옵션
    model.addAttribute("districts", restaurantInfoSVC.getDistrictList());
    model.addAttribute("categories", restaurantInfoSVC.getCategoryList());
    model.addAttribute("selectedSearch", search);
    model.addAttribute("selectedDistrict", district);
    model.addAttribute("selectedCategory", category);

    return "information/restaurants";
  }

  /**
   * 맛집 상세 페이지
   */
  @GetMapping("/restaurants/{id}")
  public String restaurantDetail(@PathVariable Long id, Model model) {
    RestaurantInfoDocument restaurant = restaurantInfoSVC.getRestaurantById(id);
    if (restaurant == null) {
      return "redirect:/information";
    }

    model.addAttribute("restaurant", restaurant);

    // 관련 맛집 (같은 지역)
    List<RestaurantInfoDocument> relatedRestaurants =
        restaurantInfoSVC.getRelatedRestaurants(restaurant.getGugunNm(), id, 4);
    model.addAttribute("relatedRestaurants", relatedRestaurants);

    return "information/restaurant-detail";
  }

  /**
   * 검색 자동완성 API
   * 2글자 이상 입력시 맛집명에서 유사한 키워드 5개 반환
   */
  @GetMapping("/api/search/autocomplete")
  @ResponseBody
  public List<String> getAutocomplete(@RequestParam("query") String query) {
    if (query == null || query.trim().length() < 2) {
      return List.of();
    }
    return restaurantInfoSVC.getSearchSuggestions(query.trim(), 5);
  }

  /**
   * 검색 기록 저장 API
   */
  @PostMapping("/api/search/history")
  @ResponseBody
  public Map<String, String> saveSearchHistory(
      @RequestBody Map<String, String> request,
      HttpServletRequest httpRequest) {

    String keyword = request.get("keyword");
    if (keyword != null && !keyword.trim().isEmpty()) {
      String sessionId = httpRequest.getSession().getId();
      restaurantInfoSVC.saveSearchHistory(sessionId, keyword.trim());
    }

    return Map.of("status", "success");
  }

  /**
   * 검색 기록 조회 API
   * 세션별로 최근 검색어 10개 반환
   */
  @GetMapping("/api/search/history")
  @ResponseBody
  public List<String> getSearchHistory(HttpServletRequest request) {
    String sessionId = request.getSession().getId();
    return restaurantInfoSVC.getSearchHistory(sessionId, 10);
  }

  /**
   * 검색 기록 삭제 API
   */
  @DeleteMapping("/api/search/history")
  @ResponseBody
  public Map<String, String> clearSearchHistory(HttpServletRequest request) {
    String sessionId = request.getSession().getId();
    restaurantInfoSVC.clearSearchHistory(sessionId);
    return Map.of("status", "success");
  }
}