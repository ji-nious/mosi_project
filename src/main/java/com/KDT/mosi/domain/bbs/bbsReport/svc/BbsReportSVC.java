package com.KDT.mosi.domain.bbs.bbsReport.svc;

import com.KDT.mosi.domain.entity.bbs.BbsReport;

public interface BbsReportSVC {
  // 신고 클릭
  String report(BbsReport bbsReport);

  // 게시글의 좋아요 갯수
  int getTotalCountReport(Long bbsId);
}
