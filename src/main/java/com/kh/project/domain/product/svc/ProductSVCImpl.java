package com.kh.project.domain.product.svc;

import com.kh.project.domain.entity.Product;
import com.kh.project.domain.product.dao.ProductDAO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSVCImpl implements ProductSVC{

  private final ProductDAO productDAO;
  @Override
  public Long saveProduct(Product product, Long sid) {
    return productDAO.saveProduct(product, sid);
  }

  @Override
  public Optional<Product> findById(Long pid) {
    return productDAO.findById(pid);
  }

  @Override
  public List<Product> findByIds(Long sid) {
    return productDAO.findByIds(sid);
  }

  @Override
  public int updateById(Long pid, Product product) {
    return productDAO.updateById(pid, product);
  }

  @Override
  public int deleteById(Long pid) {
    return productDAO.deleteById(pid);
  }

  @Override
  public int deleteByIds(List<Long> list) {
    return productDAO.deleteByIds(list);
  }
}
