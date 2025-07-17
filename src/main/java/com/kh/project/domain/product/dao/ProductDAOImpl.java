package com.kh.project.domain.product.dao;

import com.kh.project.domain.entity.Product;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
@Slf4j
public class ProductDAOImpl implements ProductDAO{


  private final NamedParameterJdbcTemplate template;

  /**
   * 등록
   * @param product
   * @return pid 게시글 아이디
   */
  @Override  //sid = seller id
  public Long saveProduct(Product product , Long sid) {
    StringBuffer sql = new StringBuffer();
    sql.append(" INSERT INTO product(product_id,seller_id,title,content,price,quantity,thumbnail,status, ");
    sql.append(" product_name,delivery_fee,delivery_information,delivery_method,country_of_origin,cdate,udate) ");
    sql.append(" VALUES(product_product_id.NEXTVAL,:sid,:title,:content,:price,:quantity,:thumbnail,:status, ");
    sql.append(":productName,:deliveryFee,:deliveryInformation,:deliveryMethod,:countryOfOrigin,SYSTIMESTAMP,SYSTIMESTAMP) ");


    MapSqlParameterSource param = new MapSqlParameterSource();

    param.addValue("sid", sid);
    param.addValue("title", product.getTitle());
    param.addValue("content", product.getContent());
    param.addValue("price", product.getPrice());
    param.addValue("quantity", product.getQuantity());
    param.addValue("thumbnail", product.getThumbnail());
    param.addValue("status", product.getStatus());
    param.addValue("productName", product.getProductName());
    param.addValue("deliveryFee", product.getDeliveryFee());
    param.addValue("deliveryInformation", product.getDeliveryInformation());
    param.addValue("deliveryMethod", product.getDeliveryMethod());
    param.addValue("countryOfOrigin", product.getCountryOfOrigin());


    KeyHolder keyHolder = new GeneratedKeyHolder();

    int row = template.update(sql.toString(), param, keyHolder, new String[]{"product_id"});

    Number pid = (Number) keyHolder.getKey();

    return pid != null ? pid.longValue() : null;
  }



  RowMapper<Product> productRowMapper(){
    return (rs, rowNum) -> {
      Product product = new Product();
      product.setProductId(rs.getLong("product_id"));
      product.setContent(rs.getString("content"));
      product.setPrice(rs.getLong("price"));
      product.setQuantity(rs.getLong("quantity"));
      product.setTitle(rs.getString("title"));
      product.setStatus(rs.getString("status"));
      product.setThumbnail(rs.getString("thumbnail"));
      product.setSellerId(rs.getLong("seller_id"));
      product.setCdate(rs.getTimestamp("cdate").toLocalDateTime());
      product.setUdate(rs.getTimestamp("udate").toLocalDateTime());
      product.setProductName(rs.getString("product_name"));
      product.setDeliveryFee(rs.getLong("delivery_fee"));
      product.setDeliveryInformation(rs.getString("delivery_information"));
      product.setDeliveryMethod(rs.getString("delivery_method"));
      product.setCountryOfOrigin(rs.getString("country_of_origin"));
      return product;
    };
  }
  /**
   * 조회 (전체)
   * @param sid 상품 게시글 번호 seller_id
   * @return List<Product> 리스트
   */
  @Override
  public List<Product> findByIds(Long sid) {
    StringBuffer sql = new StringBuffer();
    sql.append(" SELECT product_id,seller_id,title,content,price,quantity,thumbnail,status, product_name, delivery_fee, delivery_information, delivery_method, country_of_origin,cdate,udate ");
    sql.append(" FROM product where seller_id = :sid ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("sid",sid);

    List<Product> All = template.query(sql.toString(),param,productRowMapper());

    return All;
  }

  /**
   * 조회 단건
   * @param pid = product_id
   * @return Optional<Product>
   */
  @Override
  public Optional<Product> findById(Long pid) {
    StringBuffer sql = new StringBuffer();
    sql.append(" SELECT product_id,seller_id,title,content,price,quantity,thumbnail,status,product_name, delivery_fee, delivery_information, delivery_method, country_of_origin,cdate,udate ");
    sql.append(" FROM product WHERE product_id = :pid ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("pid",pid);

    try {
      Product product = template.queryForObject(sql.toString(), param, BeanPropertyRowMapper.newInstance(Product.class));
      return Optional.ofNullable(product);
    } catch (EmptyResultDataAccessException e) {
      return Optional.empty();
    }
  }

  /**
   * 수정
   * @param pid
   * @param product
   * @return
   */
  @Override
  public int updateById(Long pid, Product product) {
    StringBuffer sql = new StringBuffer();
    sql.append(" UPDATE product SET ");
    sql.append(" title = :title, content = :content, ");
    sql.append(" price = :price, quantity = :quantity , thumbnail = :thumbnail, ");
    sql.append(" product_name = :productName, delivery_fee = :deliveryFee, ");
    sql.append(" delivery_information = :deliveryInformation, delivery_method = :deliveryMethod, ");
    sql.append(" country_of_origin = :countryOfOrigin, ");
    sql.append(" udate = systimestamp ");
    sql.append("WHERE product_id = :pid");

    MapSqlParameterSource param = new MapSqlParameterSource();

    param.addValue("pid",pid);
    param.addValue("title",product.getTitle());
    param.addValue("content",product.getContent());
    param.addValue("quantity",product.getQuantity());
    param.addValue("thumbnail",product.getThumbnail());
    param.addValue("price",product.getPrice());
    param.addValue("productName", product.getProductName());
    param.addValue("deliveryFee", product.getDeliveryFee());
    param.addValue("deliveryInformation", product.getDeliveryInformation());
    param.addValue("deliveryMethod", product.getDeliveryMethod());
    param.addValue("countryOfOrigin", product.getCountryOfOrigin());

    int row = template.update(sql.toString(), param);

    return row;
  }

  /**
   * 단건 삭제
   * @param pid
   * @return
   */
  @Override
  public int deleteById(Long pid) {
  StringBuffer sql = new StringBuffer();
    sql.append(" DELETE FROM product ");
    sql.append(" WHERE product_id = :pid ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("pid",pid);

    int i = template.update(sql.toString(), param);
    return i;
  }

  /**
   * 선택 일괄 삭제
   * @param list
   * @return
   */
  @Override
  public int deleteByIds(List<Long> list) {
    StringBuffer sql = new StringBuffer();
    sql.append(" DELETE FROM product ");
    sql.append(" WHERE product_id in (:list) ");

    SqlParameterSource param = new MapSqlParameterSource().addValue("list",list);

    int i = template.update(sql.toString(), param);
    return i;
  }
}
