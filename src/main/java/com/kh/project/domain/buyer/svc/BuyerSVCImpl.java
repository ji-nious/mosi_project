package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.web.exception.BusinessValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class BuyerSVCImpl implements BuyerSVC {

    private final BuyerDAO buyerDAO;

    @Override
    public Buyer save(Buyer buyer) {
        log.info("구매자 회원가입: email={}", buyer.getEmail());
        return buyerDAO.save(buyer);
    }

    @Override
    public Buyer join(Buyer buyer) {
        log.info("구매자 스마트 회원가입 시작: email={}", buyer.getEmail());
        
        // 1. 기존 회원 조회 (탈퇴 회원 포함)
        Optional<Buyer> existingBuyerOpt = buyerDAO.findByEmail(buyer.getEmail());
        
        if (existingBuyerOpt.isPresent()) {
            Buyer existingBuyer = existingBuyerOpt.get();
            
            // 2. 활성 회원이면 중복 에러
            if (existingBuyer.canLogin() || existingBuyer.getStatus() != 0) {
                log.warn("이미 활성화된 구매자 계정: email={}", buyer.getEmail());
                throw new BusinessValidationException("이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
            }
            
            // 3. 탈퇴 회원이면 재가입 처리
            log.info("탈퇴 회원 재가입 처리: email={}", buyer.getEmail());
            
            // 닉네임 중복 체크 (활성 회원만)
            if (buyerDAO.existsByNickname(buyer.getNickname())) {
                log.warn("닉네임 중복: nickname={}", buyer.getNickname());
                throw new BusinessValidationException("이미 사용중인 닉네임입니다.");
            }
            
            // 재가입 처리
            int rejoinResult = buyerDAO.rejoin(buyer);
            if (rejoinResult > 0) {
                log.info("구매자 재가입 성공: email={}", buyer.getEmail());
                return buyerDAO.findByEmail(buyer.getEmail()).orElseThrow();
            } else {
                log.error("구매자 재가입 실패: email={}", buyer.getEmail());
                throw new BusinessValidationException("재가입 처리에 실패했습니다.");
            }
        } else {
            // 4. 신규 회원가입
            log.info("신규 구매자 회원가입: email={}", buyer.getEmail());
            
            // 중복 체크
            if (buyerDAO.existsByEmail(buyer.getEmail())) {
                throw new BusinessValidationException("이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
            }
            if (buyerDAO.existsByNickname(buyer.getNickname())) {
                throw new BusinessValidationException("이미 사용중인 닉네임입니다.");
            }
            
            return buyerDAO.save(buyer);
        }
    }

    @Override
    public Optional<Buyer> findById(Long buyerId) {
        return buyerDAO.findById(buyerId);
    }

    @Override
    public Optional<Buyer> findByEmail(String email) {
        return buyerDAO.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return buyerDAO.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        return buyerDAO.existsByNickname(nickname);
    }

    @Override
    public String getGubunInfo(Buyer buyer) {
        MemberGubun memberGubun = buyer.getMemberGubun();
        if (memberGubun == null) memberGubun = MemberGubun.NEW;
        
        return switch (memberGubun) {
            case NEW -> "신규 회원";
            case BRONZE -> "브론즈 회원";
            case SILVER -> "실버 회원"; 
            case GOLD -> "골드 회원";
            default -> "일반 회원";
        };
    }

    @Override
    public int update(Long buyerId, Buyer buyer) {
        return buyerDAO.update(buyerId, buyer);
    }

    @Override
    public int withdrawWithReason(Long buyerId, String reason) {
        return buyerDAO.withdrawWithReason(buyerId, reason);
    }

    @Override
    public List<Buyer> findWithdrawnMembers() {
        return buyerDAO.findWithdrawnMembers();
    }

    @Override
    public List<Buyer> findAll() {
        return buyerDAO.findAll();
    }

    @Override
    public int reactivate(String email, String password) {
        return buyerDAO.reactivate(email, password);
    }

    @Override
    public int rejoin(Buyer buyer) {
        return buyerDAO.rejoin(buyer);
    }
}