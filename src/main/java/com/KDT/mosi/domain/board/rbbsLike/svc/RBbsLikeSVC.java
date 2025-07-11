package com.KDT.mosi.domain.board.rbbsLike.svc;

public interface RBbsLikeSVC {
  // 좋아요 클릭
  String toggleLike(Long id, Long rbbsId);

  // 게시글의 좋아요 갯수
  int getTotalCountLike(Long rbbsId);
}
