package com.KDT.mosi.domain.bbs.bbsLike.svc;

import com.KDT.mosi.domain.bbs.bbsLike.dao.BbsLikeDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BbsLikeSVCImpl implements BbsLikeSVC {
  private final BbsLikeDAO bbsLikeDAO;

  @Override
  public String toggleLike(Long id, Long bbsId) {
    return bbsLikeDAO.toggleLike(id,bbsId);
  }

  @Override
  public int getTotalCountLike(Long bbsId) {
    return bbsLikeDAO.getTotalCountLike(bbsId);
  }
}
