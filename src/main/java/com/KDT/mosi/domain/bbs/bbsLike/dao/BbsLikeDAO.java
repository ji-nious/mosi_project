package com.KDT.mosi.domain.bbs.bbsLike.dao;

public interface BbsLikeDAO {

  // 좋아요 클릭
  String toggleLike(Long id, Long bbsId);

  // 게시글의 좋아요 갯수
  int getTotalCountLike(Long bbsId);
}
