package com.KDT.mosi.domain.product.dao;

import com.KDT.mosi.domain.entity.Product;
import com.KDT.mosi.domain.entity.ProductImage;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class ProductImageDAOImpl implements ProductImageDAO {

  private final NamedParameterJdbcTemplate jdbcTemplate;

  public ProductImageDAOImpl(NamedParameterJdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  private final RowMapper<ProductImage> rowMapper = (rs, rowNum) -> {
    ProductImage pi = new ProductImage();
    pi.setImageId(rs.getLong("IMAGE_ID"));

    // productId 가져와 Product 객체 세팅
    Long productId = rs.getLong("PRODUCT_ID");
    Product product = new Product();
    product.setProductId(productId);
    pi.setProduct(product);

    pi.setImageData(rs.getBytes("IMAGE_DATA"));
    pi.setImageOrder(rs.getInt("IMAGE_ORDER"));
    pi.setFileName(rs.getString("FILE_NAME"));
    pi.setFileSize(rs.getLong("FILE_SIZE"));
    pi.setMimeType(rs.getString("MIME_TYPE"));
    pi.setUploadTime(rs.getDate("UPLOAD_TIME"));
    return pi;
  };

  @Override
  public List<ProductImage> findByProductId(Long productId) {
    String sql = "SELECT * FROM PRODUCT_IMAGE WHERE PRODUCT_ID = :productId ORDER BY IMAGE_ORDER";
    Map<String, Object> params = new HashMap<>();
    params.put("productId", productId);
    List<ProductImage> result = jdbcTemplate.query(sql, params, rowMapper);
    System.out.println("🔍 DB 조회 결과: productId=" + productId + ", 이미지 개수=" + result.size());
    return result;
  }

  @Override
  public int insert(ProductImage productImage) {
    String sql = "INSERT INTO PRODUCT_IMAGE " +
        "(IMAGE_ID, PRODUCT_ID, IMAGE_DATA, IMAGE_ORDER, FILE_NAME, FILE_SIZE, MIME_TYPE, UPLOAD_TIME) VALUES " +
        "(IMAGE_IMAGE_ID_SEQ.NEXTVAL, :productId, :imageData, :imageOrder, :fileName, :fileSize, :mimeType, SYSDATE)";
    Map<String, Object> params = new HashMap<>();

    // productId를 Product 객체에서 얻어야 함
    if (productImage.getProduct() == null || productImage.getProduct().getProductId() == null) {
      throw new IllegalArgumentException("Product object or productId is null in ProductImage");
    }
    params.put("productId", productImage.getProduct().getProductId());

    params.put("imageData", productImage.getImageData());
    params.put("imageOrder", productImage.getImageOrder());
    params.put("fileName", productImage.getFileName());
    params.put("fileSize", productImage.getFileSize());
    params.put("mimeType", productImage.getMimeType());
    return jdbcTemplate.update(sql, params);
  }

  @Override
  public int delete(Long imageId) {
    String sql = "DELETE FROM PRODUCT_IMAGE WHERE IMAGE_ID = :imageId";
    Map<String, Object> params = new HashMap<>();
    params.put("imageId", imageId);
    return jdbcTemplate.update(sql, params);
  }

  @Override
  public int deleteByProductId(Long productId) {
    String sql = "DELETE FROM PRODUCT_IMAGE WHERE PRODUCT_ID = :productId";
    Map<String, Object> params = new HashMap<>();
    params.put("productId", productId);
    return jdbcTemplate.update(sql, params);
  }

  @Override
  public Optional<ProductImage> findById(Long imageId) {
    String sql = "SELECT * FROM PRODUCT_IMAGE WHERE IMAGE_ID = :imageId";
    Map<String, Object> params = new HashMap<>();
    params.put("imageId", imageId);

    List<ProductImage> results = jdbcTemplate.query(sql, params, rowMapper);
    return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
  }
}