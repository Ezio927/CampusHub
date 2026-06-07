package com.example.backend.user.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.NotificationType;
import com.example.backend.common.enums.QuotaRecordType;
import com.example.backend.common.enums.RewardType;
import com.example.backend.notification.service.NotificationService;
import com.example.backend.user.dto.QuotaAccountDto;
import com.example.backend.user.dto.QuotaRecordDto;
import com.example.backend.user.entity.QuotaRecord;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.QuotaRecordRepository;
import com.example.backend.user.repository.UserRepository;
import com.example.backend.user.service.QuotaService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuotaServiceImpl implements QuotaService {

    private final UserRepository userRepository;
    private final QuotaRecordRepository quotaRecordRepository;
    private final NotificationService notificationService;

    public QuotaServiceImpl(UserRepository userRepository,
                            QuotaRecordRepository quotaRecordRepository,
                            NotificationService notificationService) {
        this.userRepository = userRepository;
        this.quotaRecordRepository = quotaRecordRepository;
        this.notificationService = notificationService;
    }

    @Override
    public QuotaAccountDto getAccount(Long userId) {
        User user = requireUser(userId);
        return QuotaAccountDto.from(user, listRecords(userId));
    }

    @Override
    public List<QuotaRecordDto> listRecords(Long userId) {
        return quotaRecordRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(QuotaRecordDto::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public QuotaRecordDto checkIn(Long userId) {
        if (hasRecordToday(userId, QuotaRecordType.DAILY_CHECK_IN)) {
            throw new BusinessException(ErrorCode.CONFLICT, "今日已签到");
        }
        User user = requireUser(userId);
        user.addQuotaAvailable(DAILY_CHECK_IN_REWARD);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.DAILY_CHECK_IN, DAILY_CHECK_IN_REWARD,
                null, null, userId, "每日签到奖励");
        notifyQuotaChanged(userId, "额度分增加 +" + DAILY_CHECK_IN_REWARD, record.getReason(), null);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto grantCertificationReward(Long userId, Long operatorId) {
        if (quotaRecordRepository.existsByUserIdAndType(userId, QuotaRecordType.CERT_REWARD)) {
            return null;
        }
        User user = requireUser(userId);
        user.addQuotaAvailable(CERTIFICATION_REWARD);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.CERT_REWARD, CERTIFICATION_REWARD,
                null, null, operatorId, "实名认证通过奖励");
        notifyQuotaChanged(userId, "额度分增加 +" + CERTIFICATION_REWARD, record.getReason(), null);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto grantPhoneReward(Long userId) {
        if (quotaRecordRepository.existsByUserIdAndType(userId, QuotaRecordType.PHONE_BIND_REWARD)) {
            return null;
        }
        User user = requireUser(userId);
        if (isBlank(user.getPhone())) {
            return null;
        }
        user.addQuotaAvailable(PHONE_BIND_REWARD);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.PHONE_BIND_REWARD, PHONE_BIND_REWARD,
                null, null, userId, "填写手机号奖励");
        notifyQuotaChanged(userId, "额度分增加 +" + PHONE_BIND_REWARD, record.getReason(), null);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto grantEmailReward(Long userId) {
        if (quotaRecordRepository.existsByUserIdAndType(userId, QuotaRecordType.EMAIL_BIND_REWARD)) {
            return null;
        }
        User user = requireUser(userId);
        if (isBlank(user.getEmail())) {
            return null;
        }
        user.addQuotaAvailable(EMAIL_BIND_REWARD);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.EMAIL_BIND_REWARD, EMAIL_BIND_REWARD,
                null, null, userId, "填写邮箱奖励");
        notifyQuotaChanged(userId, "额度分增加 +" + EMAIL_BIND_REWARD, record.getReason(), null);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto rewardGoodReview(Long userId, Long orderId) {
        if (orderId == null
                || quotaRecordRepository.existsByUserIdAndRelatedOrderIdAndType(userId, orderId, QuotaRecordType.GOOD_REVIEW_REWARD)) {
            return null;
        }
        User user = requireUser(userId);
        user.addQuotaAvailable(GOOD_REVIEW_REWARD);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.GOOD_REVIEW_REWARD, GOOD_REVIEW_REWARD,
                null, orderId, userId, "收获好评奖励");
        notifyQuotaChanged(userId, "额度分增加 +" + GOOD_REVIEW_REWARD, record.getReason(), orderId);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto rewardDailyOrderCompletion(Long userId, Long orderId) {
        if (orderId == null
                || quotaRecordRepository.existsByUserIdAndRelatedOrderIdAndType(userId, orderId, QuotaRecordType.DAILY_ORDER_REWARD)
                || hasRecordToday(userId, QuotaRecordType.DAILY_ORDER_REWARD)) {
            return null;
        }
        User user = requireUser(userId);
        user.addQuotaAvailable(DAILY_ORDER_REWARD);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.DAILY_ORDER_REWARD, DAILY_ORDER_REWARD,
                null, orderId, userId, "每日首单完成奖励");
        notifyQuotaChanged(userId, "额度分增加 +" + DAILY_ORDER_REWARD, record.getReason(), orderId);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto adjustByAdmin(Long userId, int amount, String reason, Long adminId) {
        if (amount == 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "调整值不能为0");
        }
        User user = requireUser(userId);
        if (amount > 0) {
            user.addQuotaAvailable(amount);
            userRepository.save(user);
            QuotaRecord record = record(user, QuotaRecordType.ADMIN_GRANT, amount,
                    null, null, adminId, isBlank(reason) ? "管理员发放额度分" : reason);
            notifyQuotaChanged(userId, "额度分增加 +" + amount, record.getReason(), null);
            return QuotaRecordDto.from(record);
        }
        int deduct = Math.abs(amount);
        ensureAvailable(user, deduct);
        user.deductQuotaAvailable(deduct);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.ADMIN_DEDUCT, -deduct,
                null, null, adminId, isBlank(reason) ? "管理员扣除额度分" : reason);
        notifyQuotaChanged(userId, "额度分扣除 " + deduct, record.getReason(), null);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto freezeDemandReward(Long userId, Long demandId, RewardType rewardType, Integer rewardAmount) {
        int amount = pointAmount(rewardType, rewardAmount);
        if (amount <= 0) {
            return null;
        }
        User user = requireUser(userId);
        ensureAvailable(user, amount);
        user.freezeQuota(amount);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.DEMAND_REWARD_FREEZE, -amount,
                demandId, null, userId, "发布额度分需求冻结报酬");
        notifyQuotaChanged(userId, "额度分冻结 " + amount, record.getReason(), demandId);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public void adjustDemandRewardFreeze(Long userId, Long demandId,
                                         RewardType oldType, Integer oldAmount,
                                         RewardType newType, Integer newAmount) {
        int oldPointAmount = pointAmount(oldType, oldAmount);
        int newPointAmount = pointAmount(newType, newAmount);
        int diff = newPointAmount - oldPointAmount;
        if (diff == 0) {
            return;
        }
        User user = requireUser(userId);
        if (diff > 0) {
            ensureAvailable(user, diff);
            user.freezeQuota(diff);
            userRepository.save(user);
            QuotaRecord record = record(user, QuotaRecordType.DEMAND_REWARD_ADJUST, -diff,
                    demandId, null, userId, "调整额度分报酬，追加冻结");
            notifyQuotaChanged(userId, "额度分追加冻结 " + diff, record.getReason(), demandId);
            return;
        }
        int release = Math.min(-diff, user.getQuotaFrozen());
        if (release <= 0) {
            return;
        }
        user.releaseQuota(release);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.DEMAND_REWARD_ADJUST, release,
                demandId, null, userId, "调整额度分报酬，释放冻结");
        notifyQuotaChanged(userId, "额度分释放 " + release, record.getReason(), demandId);
    }

    @Override
    @Transactional
    public QuotaRecordDto refundDemandReward(Long userId, Long demandId, RewardType rewardType, Integer rewardAmount, String reason) {
        int amount = pointAmount(rewardType, rewardAmount);
        if (amount <= 0) {
            return null;
        }
        User user = requireUser(userId);
        int release = Math.min(amount, user.getQuotaFrozen());
        if (release <= 0) {
            return null;
        }
        user.releaseQuota(release);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.DEMAND_REWARD_REFUND, release,
                demandId, null, userId, isBlank(reason) ? "需求取消或删除，退回冻结报酬" : reason);
        notifyQuotaChanged(userId, "额度分退回 " + release, record.getReason(), demandId);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public void payDemandReward(Long publisherId, Long acceptorId, Long demandId, Long orderId,
                                RewardType rewardType, Integer rewardAmount) {
        int amount = pointAmount(rewardType, rewardAmount);
        if (amount <= 0 || orderId == null) {
            return;
        }
        if (quotaRecordRepository.existsByUserIdAndRelatedOrderIdAndType(acceptorId, orderId, QuotaRecordType.ORDER_REWARD_EARN)) {
            return;
        }
        User publisher = requireUser(publisherId);
        if (publisher.getQuotaFrozen() < amount) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT, "冻结额度分不足，无法结算订单报酬");
        }
        publisher.consumeFrozenQuota(amount);
        userRepository.save(publisher);
        QuotaRecord payRecord = record(publisher, QuotaRecordType.ORDER_REWARD_PAY, -amount,
                demandId, orderId, publisherId, "订单完成，支付额度分报酬");

        User acceptor = requireUser(acceptorId);
        acceptor.addQuotaAvailable(amount);
        userRepository.save(acceptor);
        QuotaRecord earnRecord = record(acceptor, QuotaRecordType.ORDER_REWARD_EARN, amount,
                demandId, orderId, publisherId, "订单完成，获得额度分报酬");
        notifyQuotaChanged(publisherId, "额度分支付 " + amount, payRecord.getReason(), orderId);
        notifyQuotaChanged(acceptorId, "额度分获得 +" + amount, earnRecord.getReason(), orderId);
    }

    @Override
    @Transactional
    public QuotaRecordDto freezeOrderDeposit(Long userId, Long orderId, Integer amount) {
        int safeAmount = safeAmount(amount);
        if (safeAmount <= 0) {
            return null;
        }
        User user = requireUser(userId);
        ensureAvailable(user, safeAmount);
        user.freezeQuota(safeAmount);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.ORDER_DEPOSIT_FREEZE, -safeAmount,
                null, orderId, userId, "低信用接单保证金冻结");
        notifyQuotaChanged(userId, "额度分保证金冻结 " + safeAmount, record.getReason(), orderId);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto refundOrderDeposit(Long userId, Long orderId, Integer amount, String reason) {
        int safeAmount = safeAmount(amount);
        if (safeAmount <= 0 || orderId == null
                || quotaRecordRepository.existsByUserIdAndRelatedOrderIdAndType(userId, orderId, QuotaRecordType.ORDER_DEPOSIT_REFUND)
                || quotaRecordRepository.existsByUserIdAndRelatedOrderIdAndType(userId, orderId, QuotaRecordType.ORDER_DEPOSIT_PENALTY)) {
            return null;
        }
        User user = requireUser(userId);
        int release = Math.min(safeAmount, user.getQuotaFrozen());
        if (release <= 0) {
            return null;
        }
        user.releaseQuota(release);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.ORDER_DEPOSIT_REFUND, release,
                null, orderId, userId, isBlank(reason) ? "接单保证金退回" : reason);
        notifyQuotaChanged(userId, "额度分保证金退回 " + release, record.getReason(), orderId);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto deductOrderDeposit(Long userId, Long orderId, Integer amount, String reason) {
        int safeAmount = safeAmount(amount);
        if (safeAmount <= 0 || orderId == null
                || quotaRecordRepository.existsByUserIdAndRelatedOrderIdAndType(userId, orderId, QuotaRecordType.ORDER_DEPOSIT_PENALTY)) {
            return null;
        }
        User user = requireUser(userId);
        int fromFrozen = Math.min(safeAmount, user.getQuotaFrozen());
        if (fromFrozen > 0) {
            user.consumeFrozenQuota(fromFrozen);
        }
        int remain = safeAmount - fromFrozen;
        int fromAvailable = Math.min(remain, user.getQuotaAvailable());
        if (fromAvailable > 0) {
            user.deductQuotaAvailable(fromAvailable);
        }
        int actual = fromFrozen + fromAvailable;
        if (actual <= 0) {
            return null;
        }
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.ORDER_DEPOSIT_PENALTY, -actual,
                null, orderId, userId, isBlank(reason) ? "接单保证金扣除" : reason);
        notifyQuotaChanged(userId, "额度分扣除 " + actual, record.getReason(), orderId);
        return QuotaRecordDto.from(record);
    }

    @Override
    @Transactional
    public QuotaRecordDto deductLateCancelPenalty(Long userId, Long orderId, Integer amount, String reason) {
        int safeAmount = safeAmount(amount);
        if (safeAmount <= 0) {
            return null;
        }
        User user = requireUser(userId);
        int actual = Math.min(safeAmount, user.getQuotaAvailable());
        if (actual <= 0) {
            return null;
        }
        user.deductQuotaAvailable(actual);
        userRepository.save(user);
        QuotaRecord record = record(user, QuotaRecordType.CANCEL_PENALTY, -actual,
                null, orderId, userId, isBlank(reason) ? "逾期取消订单扣除额度分" : reason);
        notifyQuotaChanged(userId, "额度分扣除 " + actual, record.getReason(), orderId);
        return QuotaRecordDto.from(record);
    }

    private QuotaRecord record(User user, QuotaRecordType type, int changeAmount,
                               Long demandId, Long orderId, Long operatorId, String reason) {
        QuotaRecord record = new QuotaRecord();
        record.setUserId(user.getId());
        record.setType(type);
        record.setChangeAmount(changeAmount);
        record.setAvailableAfter(user.getQuotaAvailable());
        record.setFrozenAfter(user.getQuotaFrozen());
        record.setRelatedDemandId(demandId);
        record.setRelatedOrderId(orderId);
        record.setOperatorId(operatorId);
        record.setReason(reason);
        return quotaRecordRepository.save(record);
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private void ensureAvailable(User user, int amount) {
        if (user.getQuotaAvailable() < amount) {
            throw new BusinessException(ErrorCode.QUOTA_INSUFFICIENT,
                    "额度分不足：需要 " + amount + "，当前可用 " + user.getQuotaAvailable());
        }
    }

    private int pointAmount(RewardType rewardType, Integer rewardAmount) {
        if (rewardType != RewardType.POINT) {
            return 0;
        }
        return safeAmount(rewardAmount);
    }

    private int safeAmount(Integer amount) {
        return amount == null || amount <= 0 ? 0 : amount;
    }

    private void notifyQuotaChanged(Long userId, String title, String content, Long relatedId) {
        notificationService.createNotification(userId, NotificationType.QUOTA_CHANGED, title, content, relatedId);
    }

    private boolean hasRecordToday(Long userId, QuotaRecordType type) {
        LocalDate today = LocalDate.now();
        return quotaRecordRepository.existsByUserIdAndTypeAndCreatedAtBetween(userId, type,
                today.atStartOfDay(), today.plusDays(1).atStartOfDay());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
