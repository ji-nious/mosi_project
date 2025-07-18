package com.kh.project.domain.buyer.svc;

import com.kh.project.domain.buyer.dao.BuyerDAO;
import com.kh.project.domain.seller.dao.SellerDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.MemberGubun;
import com.kh.project.domain.entity.MemberStatus;
import com.kh.project.web.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Service
@Slf4j
@RequiredArgsConstructor
public class BuyerSVCImpl implements BuyerSVC {

    private final BuyerDAO buyerDAO;
    private final SellerDAO sellerDAO;

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

            // 2. 활성 회원이면 중복 에러 - 문자열 비교로 변경
            if (canLogin(existingBuyer) || !MemberStatus.WITHDRAWN.getCode().equals(existingBuyer.getStatus())) {
                log.warn("이미 활성화된 구매자 계정: email={}", buyer.getEmail());
                throw new BusinessException("이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
            }

            // 3. 탈퇴 회원이면 재가입 처리
            log.info("탈퇴 회원 재가입 처리: email={}", buyer.getEmail());

            // 닉네임 중복 체크 (활성 회원만)
            if (buyerDAO.existsByNickname(buyer.getNickname())) {
                log.warn("닉네임 중복: nickname={}", buyer.getNickname());
                throw new BusinessException("이미 사용중인 닉네임입니다.");
            }

            // 재가입 처리
            int rejoinResult = buyerDAO.rejoin(buyer);
            if (rejoinResult > 0) {
                log.info("구매자 재가입 성공: email={}", buyer.getEmail());
                return buyerDAO.findByEmail(buyer.getEmail()).orElseThrow();
            } else {
                log.error("구매자 재가입 실패: email={}", buyer.getEmail());
                throw new BusinessException("재가입 처리에 실패했습니다.");
            }
        } else {
            // 4. 신규 회원가입
            log.info("신규 구매자 회원가입: email={}", buyer.getEmail());

            // 중복 체크
            if (buyerDAO.existsByEmail(buyer.getEmail())) {
                throw new BusinessException("이미 등록된 이메일입니다. 탈퇴 후 재가입은 불가능합니다.");
            }
            if (buyerDAO.existsByNickname(buyer.getNickname())) {
                throw new BusinessException("이미 사용중인 닉네임입니다.");
            }

            return buyerDAO.save(buyer);
        }
    }

    @Override
    public Optional<Buyer> findById(Long buyerId) {
        log.debug("구매자 조회: buyerId={}", buyerId);
        return buyerDAO.findById(buyerId);
    }

    @Override
    public Optional<Buyer> findByEmail(String email) {
        log.debug("구매자 이메일 조회: email={}", email);
        return buyerDAO.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        log.debug("이메일 중복 체크: email={}", email);
        return buyerDAO.existsByEmail(email) || sellerDAO.existsByEmail(email);
    }

    @Override
    public boolean existsByNickname(String nickname) {
        log.debug("닉네임 중복 체크: nickname={}", nickname);
        return buyerDAO.existsByNickname(nickname);
    }

    @Override
    public String getGubunInfo(Buyer buyer) {
        MemberGubun memberGubun = buyer.getMemberGubun();
        if (memberGubun == null) {
            memberGubun = MemberGubun.NEW;
        }

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
        log.info("구매자 정보 수정: buyerId={}", buyerId);

        // 수정하려는 닉네임이 다른 활성 회원과 중복되는지 확인
        if (buyer.getNickname() != null) {
            Optional<Buyer> existingBuyer = buyerDAO.findById(buyerId);
            if (existingBuyer.isPresent()) {
                String currentNickname = existingBuyer.get().getNickname();

                // 닉네임이 변경되는 경우에만 중복 체크
                if (!buyer.getNickname().equals(currentNickname)) {
                    if (buyerDAO.existsByNickname(buyer.getNickname())) {
                        log.warn("닉네임 중복: nickname={}", buyer.getNickname());
                        throw new BusinessException("이미 사용중인 닉네임입니다.");
                    }
                }
            }
        }

        int result = buyerDAO.update(buyerId, buyer);
        log.info("구매자 정보 수정 완료: buyerId={}, 수정된 행 수={}", buyerId, result);
        return result;
    }

    @Override
    public int withdrawWithReason(Long buyerId, String reason) {
        log.info("구매자 탈퇴 처리: buyerId={}, reason={}", buyerId, reason);

        // 구매자 존재 여부 확인
        Optional<Buyer> buyerOpt = buyerDAO.findById(buyerId);
        if (buyerOpt.isEmpty()) {
            log.warn("탈퇴 처리할 구매자를 찾을 수 없음: buyerId={}", buyerId);
            throw new BusinessException("탈퇴 처리할 회원을 찾을 수 없습니다.");
        }

        Buyer buyer = buyerOpt.get();

        // 이미 탈퇴한 회원인지 확인 (Service 메서드 사용)
        if (isWithdrawn(buyer)) {
            log.warn("이미 탈퇴한 구매자: buyerId={}", buyerId);
            throw new BusinessException("이미 탈퇴한 회원입니다.");
        }

        int result = buyerDAO.withdrawWithReason(buyerId, reason);
        log.info("구매자 탈퇴 처리 완료: buyerId={}, 처리된 행 수={}", buyerId, result);
        return result;
    }

    @Override
    public List<Buyer> findWithdrawnMembers() {
        log.debug("탈퇴 회원 목록 조회");
        return buyerDAO.findWithdrawnMembers();
    }

    @Override
    public List<Buyer> findAll() {
        log.debug("전체 구매자 목록 조회");
        return buyerDAO.findAll();
    }

    @Override
    public int reactivate(String email, String password) {
        log.info("탈퇴 회원 재활성화: email={}", email);

        // 재활성화 대상 확인
        Optional<Buyer> buyerOpt = buyerDAO.findByEmail(email);
        if (buyerOpt.isEmpty()) {
            log.warn("재활성화할 회원을 찾을 수 없음: email={}", email);
            throw new BusinessException("해당 이메일의 회원을 찾을 수 없습니다.");
        }

        Buyer buyer = buyerOpt.get();
        if (!isWithdrawn(buyer)) {
            log.warn("탈퇴하지 않은 회원의 재활성화 시도: email={}", email);
            throw new BusinessException("탈퇴하지 않은 회원입니다.");
        }

        int result = buyerDAO.reactivate(email, password);
        if (result > 0) {
            log.info("탈퇴 회원 재활성화 성공: email={}", email);
        } else {
            log.warn("탈퇴 회원 재활성화 실패: email={}", email);
        }

        return result;
    }

    @Override
    public int rejoin(Buyer buyer) {
        log.info("탈퇴 회원 재가입: email={}", buyer.getEmail());

        // 재가입 대상 확인
        Optional<Buyer> existingBuyerOpt = buyerDAO.findByEmail(buyer.getEmail());
        if (existingBuyerOpt.isEmpty()) {
            log.warn("재가입할 회원을 찾을 수 없음: email={}", buyer.getEmail());
            throw new BusinessException("해당 이메일의 회원을 찾을 수 없습니다.");
        }

        Buyer existingBuyer = existingBuyerOpt.get();
        if (!isWithdrawn(existingBuyer)) {
            log.warn("탈퇴하지 않은 회원의 재가입 시도: email={}", buyer.getEmail());
            throw new BusinessException("탈퇴하지 않은 회원입니다.");
        }

        // 닉네임 중복 체크 (활성 회원만)
        if (buyerDAO.existsByNickname(buyer.getNickname())) {
            log.warn("재가입 시 닉네임 중복: nickname={}", buyer.getNickname());
            throw new BusinessException("이미 사용중인 닉네임입니다.");
        }

        int result = buyerDAO.rejoin(buyer);
        if (result > 0) {
            log.info("탈퇴 회원 재가입 성공: email={}", buyer.getEmail());
        } else {
            log.warn("탈퇴 회원 재가입 실패: email={}", buyer.getEmail());
        }

        return result;
    }

    /**
     * 탈퇴 여부 확인
     */
    public boolean isWithdrawn(Buyer buyer) {
        return buyer != null && buyer.getWithdrawnAt() != null;
    }

    /**
     * 활성 상태 여부 확인
     */
    public boolean isActive(Buyer buyer) {
        return buyer != null &&
            buyer.getStatus() != null &&
            MemberStatus.ACTIVE.getCode().equals(buyer.getStatus()) &&
            !isWithdrawn(buyer);
    }

    /**
     * 로그인 가능 여부 확인
     */
    public boolean canLogin(Buyer buyer) {
        if (buyer == null || buyer.getStatus() == null) return false;

        try {
            MemberStatus memberStatus = MemberStatus.fromCode(buyer.getStatus());
            return memberStatus.canLogin() && !isWithdrawn(buyer);
        } catch (IllegalArgumentException e) {
            log.warn("잘못된 상태 코드: buyerId={}, status={}",
                buyer.getBuyerId(), buyer.getStatus());
            return false;
        }
    }

    /**
     * 상태 표시 문자열 반환
     */
    public String getStatusDisplay(Buyer buyer) {
        if (buyer == null || buyer.getStatus() == null) {
            return "알 수 없음";
        }
        try {
            return MemberStatus.fromCode(buyer.getStatus()).getCode();
        } catch (IllegalArgumentException e) {
            return "알 수 없음";
        }
    }

    /**
     * 로그인 검증 (이메일 + 상태 확인)
     */
    public boolean validateLogin(String email) {
        Optional<Buyer> buyerOpt = findByEmail(email);
        if (buyerOpt.isEmpty()) {
            return false;
        }

        return canLogin(buyerOpt.get());
        // 비밀번호 검증은 Controller에서 PasswordEncoder로 처리
    }

    /**
     * 구매자 대시보드용 상태 정보 생성
     */
    public Map<String, Object> createDashboardInfo(Buyer buyer) {
        if (buyer == null) return Map.of();

        return Map.of(
            "buyerId", buyer.getBuyerId(),
            "nickname", buyer.getNickname(),
            "email", buyer.getEmail(),
            "status", getStatusDisplay(buyer),
            "memberGubun", getGubunInfo(buyer),
            "canLogin", canLogin(buyer),
            "isWithdrawn", isWithdrawn(buyer)
        );
    }


}