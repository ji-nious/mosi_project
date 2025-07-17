package com.kh.project.domain.product.svc;

import com.kh.project.domain.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductSVC {
  // 등록
  Long saveProduct(Product product, Long sid);

  // 조회
  Optional<Product> findById(Long pid);//pid = product id

  // 단체 조회
  List<Product> findByIds(Long sid);

  // 수정
  int updateById(Long pid , Product product);

  // 삭제(단건)
  int deleteById(Long pid);

  //선택 일괄 삭제
  int deleteByIds(List<Long> list);


}


