package com.KDT.mosi.web.controller;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import com.KDT.mosi.domain.restaurant.svc.RestaurantInfoSVC;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/information") // 기본 경로 추가
public class RestaurantController {

  private final RestaurantInfoSVC restaurantInfoSVC;

  // 맛집 메인 페이지 (대시보드 + 목록)
  @GetMapping("/restaurants")
  public String restaurantMain(
      @RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "size", defaultValue = "12") int size,
      @RequestParam(name = "district", required = false) String district,
      @RequestParam(name = "category", required = false) String category,
      Model model) {

    // 대시보드 데이터 (차트용)
    model.addAttribute("dashboardData", restaurantInfoSVC.getDashboardData());

    // 지도용 전체 맛집 데이터 (좌표 있는 것만)
    List<RestaurantInfoDocument> mapRestaurants = restaurantInfoSVC.getRestaurantsForMap();
    model.addAttribute("mapRestaurants", mapRestaurants);

    // 맛집 목록 (페이징)
    Page<RestaurantInfoDocument> restaurants = restaurantInfoSVC.getRestaurantsPaged(page, size, district, category);
    model.addAttribute("restaurants", restaurants);
    model.addAttribute("currentPage", page);
    model.addAttribute("totalPages", restaurants.getTotalPages());

    // 필터 옵션
    model.addAttribute("districts", restaurantInfoSVC.getDistrictList());
    model.addAttribute("categories", restaurantInfoSVC.getCategoryList());
    model.addAttribute("selectedDistrict", district);
    model.addAttribute("selectedCategory", category);

    return "information/restaurants"; // 템플릿 경로도 수정
  }

  // 맛집 상세 페이지
  @GetMapping("/restaurants/{id}")
  public String restaurantDetail(@PathVariable Long id, Model model) {
    RestaurantInfoDocument restaurant = restaurantInfoSVC.getRestaurantById(id);
    if (restaurant == null) {
      return "redirect:/information/restaurants";
    }

    model.addAttribute("restaurant", restaurant);

    // 관련 맛집 (같은 지역)
    List<RestaurantInfoDocument> relatedRestaurants =
        restaurantInfoSVC.getRelatedRestaurants(restaurant.getGugunNm(), id, 4);
    model.addAttribute("relatedRestaurants", relatedRestaurants);

    return "restaurant-detail";
  }

  // 지도용 맛집 데이터 API
  @GetMapping("/api/restaurants/map")
  @ResponseBody
  public List<Map<String, Object>> getRestaurantsForMap(
      @RequestParam(name = "district", required = false) String district,
      @RequestParam(name = "category", required = false) String category) {

    List<RestaurantInfoDocument> restaurants = restaurantInfoSVC.getRestaurantsForMap();

    return restaurants.stream()
        .filter(r -> r.getLat() != null && r.getLng() != null)
        .filter(r -> district == null || district.isEmpty() || district.equals(r.getGugunNm()))
        .filter(r -> category == null || category.isEmpty() ||
            (r.getItemCntnts() != null && r.getItemCntnts().contains(category)))
        .map(r -> {
          Map<String, Object> map = new HashMap<>();
          map.put("id", r.getUcSeq() != null ? r.getUcSeq() : 0L);
          map.put("name", r.getMainTitle() != null ? r.getMainTitle() : "");
          map.put("lat", r.getLat());
          map.put("lng", r.getLng());
          map.put("district", r.getGugunNm() != null ? r.getGugunNm() : "");
          map.put("category", r.getItemCntnts() != null ? r.getItemCntnts() : "");
          map.put("address", r.getAddr1() != null ? r.getAddr1() : "");
          map.put("menu", r.getRprsnTvMenu() != null ? r.getRprsnTvMenu() : "");
          return map;
        })
        .collect(Collectors.toList());
  }

  // AJAX용 목록 API
  @GetMapping("/api/restaurants")
  @ResponseBody
  public Map<String, Object> getRestaurantsApi(
      @RequestParam(name = "page", defaultValue = "1") int page,
      @RequestParam(name = "size", defaultValue = "12") int size,
      @RequestParam(name = "district", required = false) String district,
      @RequestParam(name = "category", required = false) String category) {

    Page<RestaurantInfoDocument> restaurants = restaurantInfoSVC.getRestaurantsPaged(page, size, district, category);

    Map<String, Object> result = new HashMap<>();
    result.put("restaurants", restaurants.getContent());
    result.put("currentPage", page);
    result.put("totalPages", restaurants.getTotalPages());
    result.put("totalElements", restaurants.getTotalElements());

    return result;
  }

  // 대시보드 데이터 API
  @GetMapping("/api/restaurant-info/dashboard")
  @ResponseBody
  public Map<String, Object> getDashboard() {
    return restaurantInfoSVC.getDashboardData();
  }

  // 편의시설 API
  @GetMapping("/api/facilities")
  @ResponseBody
  public List<RestaurantInfoDocument> getFacilities() {
    return restaurantInfoSVC.getFacilities();
  }
}