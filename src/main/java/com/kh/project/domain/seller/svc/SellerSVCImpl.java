package com.kh.project.domain.seller.svc;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.util.SellerStatusHelper;
import com.kh.project.web.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerSVCImpl implements SellerSVC {

    private final SellerDAO sellerDAO;

    @Override
    public Seller save(Seller seller) {
        log.info("판매자 회원가입: email={}", seller.getEmail());
        
        // 이메일 중복 체크 (모든 상태)
        if (sellerDAO.existsByEmail(seller.getEmail())) {
            throw new BusinessValidationException("이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
        }

        // 사업자번호 중복 체크 (모든 상태)  
        if (sellerDAO.existsByBizRegNo(seller.getBizRegNo())) {
            throw new BusinessValidationException("이미 등록된 사업자등록번호입니다. 탈퇴 후 재가입은 불가능합니다.");
        }
        
        return sellerDAO.save(seller);
    }

    @Override
    public Seller join(Seller seller) {
        // join()은 save()를 호출하여 중복 제거
        return save(seller);
    }

    /**
     * 활성 회원 중복 체크
     */
    private void validateDuplicateForActiveMembers(Seller seller) {
        // 활성 상태 회원만 중복 체크
        if (sellerDAO.existsByEmailAndStatus(seller.getEmail(), SellerStatusHelper.STATUS_ACTIVE)) {
            log.warn("활성 이메일 중복: email={}", seller.getEmail());
            throw new BusinessValidationException("이미 사용 중인 이메일입니다.");
        }
        
        if (sellerDAO.existsByBizRegNo(seller.getBizRegNo())) {
            log.warn("사업자등록번호 중복 (재가입 불가): bizRegNo={}", seller.getBizRegNo());
            throw new BusinessValidationException("이미 등록된 사업자등록번호입니다. 탈퇴 후 재가입은 불가능합니다.");
        }
        
        if (sellerDAO.existsByTelAndStatus(seller.getTel(), SellerStatusHelper.STATUS_ACTIVE)) {
            log.warn("활성 전화번호 중복: tel={}", seller.getTel());
            throw new BusinessValidationException("이미 사용 중인 전화번호입니다.");
        }
        
        log.info("활성 회원 중복 체크 통과: email={}", seller.getEmail());
    }

    @Override
    public Optional<Seller> findById(Long sellerId) {
        return sellerDAO.findById(sellerId);
    }

    @Override
    public Optional<Seller> findByEmail(String email) {
        return sellerDAO.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return sellerDAO.existsByEmail(email);
    }

    @Override
    public boolean existsByBizRegNo(String bizRegNo) {
        return sellerDAO.existsByBizRegNo(bizRegNo);
    }

    @Override
    public boolean existsByShopName(String shopName) {
        return sellerDAO.existsByShopName(shopName);
    }

    @Override
    public boolean existsByName(String name) {
        return sellerDAO.existsByName(name);
    }

    @Override
    public Optional<Seller> findByBizRegNo(String bizRegNo) {
        return sellerDAO.findByBizRegNo(bizRegNo);
    }

    @Override
    public int update(Long sellerId, Seller seller) {
        return sellerDAO.update(sellerId, seller);
    }

    @Override
    public int withdrawWithReason(Long sellerId, String reason) {
        log.info("판매자 탈퇴 처리 - sellerId={}", sellerId);
        
        // 판매자 조회
        Optional<Seller> sellerOpt = sellerDAO.findById(sellerId);
        if (sellerOpt.isEmpty()) {
            throw new BusinessValidationException("존재하지 않는 판매자입니다.");
        }
        
        Seller seller = sellerOpt.get();
        
        // 상태 조합 체크
        if (!SellerStatusHelper.canWithdraw(seller)) {
            String statusDesc = SellerStatusHelper.getWithdrawStatusDescription(seller);
            log.warn("탈퇴 불가 상태: sellerId={}, status={}, description={}", 
                    sellerId, SellerStatusHelper.getStatusCombinationString(seller), statusDesc);
            throw new BusinessValidationException(statusDesc);
        }
        
        // 탈퇴 처리
        log.info("탈퇴 가능 확인 완료 {}, 탈퇴 진행: sellerId={}", 
                SellerStatusHelper.getStatusCombinationString(seller), sellerId);
        return sellerDAO.withdrawWithReason(sellerId, reason);
    }

    @Override
    public List<Seller> findWithdrawnMembers() {
        return sellerDAO.findWithdrawnMembers();
    }

    @Override
    public List<Seller> findAll() {
        return sellerDAO.findAll();
    }

    @Override
    public int reactivate(String email, String password) {
        return sellerDAO.reactivate(email, password);
    }

    @Override
    public int rejoin(Seller seller) {
        return sellerDAO.rejoin(seller);
    }
}
