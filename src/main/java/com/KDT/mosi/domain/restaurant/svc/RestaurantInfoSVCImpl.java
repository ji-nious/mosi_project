package com.KDT.mosi.domain.restaurant.svc;

import com.KDT.mosi.domain.documents.RestaurantInfoDocument;
import com.KDT.mosi.domain.restaurant.svc.dao.RestaurantInfoDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RestaurantInfoSVCImpl implements RestaurantInfoSVC{
  private final RestaurantInfoDAO restaurantInfoDAO;

  @Override
  public Map<String, Object> getDashboardData() {
    Map<String, Object> districts = restaurantInfoDAO.getAggregation("gugunNm");
    Map<String, Object> categories = restaurantInfoDAO.getAggregation("itemCntnts");

    return Map.of(
        "districts", districts,
        "categories", categories,
        "totalRestaurants", getTotalCount(),
        "districtCount", districts.size(),
        "categoryCount", categories.size()
    );
  }

  private long getTotalCount() {
    return restaurantInfoDAO.getRestaurants().size();
  }

  @Override
  public Page<RestaurantInfoDocument> getRestaurantsPaged(int page, int size, String district, String category) {
    Pageable pageable = PageRequest.of(page - 1, size);
    return restaurantInfoDAO.getRestaurantsPaged(district, category, pageable);
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
  public List<RestaurantInfoDocument> getRestaurantsForMap() {
    return restaurantInfoDAO.getRestaurantsForMap();
  }

  @Override
  public List<String> getDistrictList() {
    return restaurantInfoDAO.getDistrictList();
  }

  @Override
  public List<String> getCategoryList() {
    return restaurantInfoDAO.getCategoryList();
  }

  @Override
  public List<RestaurantInfoDocument> getFacilities() {
    return restaurantInfoDAO.getFacilities();
  }
}