package com.example.backend.demand.service.impl;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.DemandStatus;
import com.example.backend.common.enums.UserStatus;
import com.example.backend.common.moderation.ContentModerationService;
import com.example.backend.common.moderation.ModerationResult;
import com.example.backend.demand.dto.DemandCreateDto;
import com.example.backend.demand.dto.DemandDto;
import com.example.backend.demand.dto.DemandSearchCriteria;
import com.example.backend.demand.dto.DemandUpdateDto;
import com.example.backend.demand.entity.Demand;
import com.example.backend.demand.entity.DemandImage;
import com.example.backend.demand.entity.Tag;
import com.example.backend.demand.pattern.factory.DemandFactory;
import com.example.backend.demand.repository.DemandImageRepository;
import com.example.backend.demand.repository.DemandRepository;
import com.example.backend.demand.repository.TagRepository;
import com.example.backend.demand.service.DemandService;
import com.example.backend.user.dto.UserPublicDto;
import com.example.backend.user.entity.User;
import com.example.backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DemandServiceImpl implements DemandService {

    private final DemandFactory demandFactory;
    private final DemandRepository demandRepository;
    private final DemandImageRepository demandImageRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final ContentModerationService contentModerationService;

    public DemandServiceImpl(DemandFactory demandFactory,
                             DemandRepository demandRepository,
                             DemandImageRepository demandImageRepository,
                             TagRepository tagRepository,
                             UserRepository userRepository,
                             ContentModerationService contentModerationService) {
        this.demandFactory = demandFactory;
        this.demandRepository = demandRepository;
        this.demandImageRepository = demandImageRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.contentModerationService = contentModerationService;
    }

    @Override
    public DemandDto createDemand(Long userId, DemandCreateDto req) {
        User publisher = requireActiveUser(userId);
        List<String> warnings = validateDemandTextAndReward(req);
        Demand demand = demandFactory.createDemand(req);
        demand.setPublisherId(publisher.getId());
        demandRepository.save(demand);
        saveImages(demand.getId(), req == null ? null : req.getImageUrls());
        DemandDto dto = toDto(demand);
        dto.setRiskWarnings(warnings);
        return dto;
    }

    @Override
    public List<DemandDto> search(DemandSearchCriteria criteria) {
        List<Demand> demands;
        if (criteria != null && criteria.getPublisherId() != null) {
            demands = demandRepository.findByPublisherIdOrderByCreatedAtDesc(criteria.getPublisherId());
        } else {
            demands = demandRepository.findAll();
        }
        if (criteria != null) {
            demands = demands.stream()
                    .filter(demand -> criteria.getCategory() == null || demand.getCategory() == criteria.getCategory())
                    .filter(demand -> criteria.getStatus() == null || demand.getStatus() == criteria.getStatus())
                    .filter(demand -> criteria.getUrgent() == null || demand.getUrgent().equals(criteria.getUrgent()))
                    .filter(demand -> isBlank(criteria.getLocationTag()) || containsIgnoreCase(demand.getLocationTag(), criteria.getLocationTag()))
                    .filter(demand -> isBlank(criteria.getKeyword())
                            || containsIgnoreCase(demand.getTitle(), criteria.getKeyword())
                            || containsIgnoreCase(demand.getDescription(), criteria.getKeyword()))
                    .collect(Collectors.toList());
            demands = sortDemands(demands, criteria.getSortBy(), criteria.getSortDir());
        }
        return demands.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public DemandDto getDetail(Long demandId) {
        Demand demand = requireDemand(demandId);
        return toDto(demand);
    }

    @Override
    public void updateStatus(Long demandId, DemandStatus status) {
        if (status == null) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "状态不能为空");
        }
        Demand demand = requireDemand(demandId);
        demand.setStatus(status);
        demandRepository.save(demand);
    }

    @Override
    public DemandDto update(Long demandId, Long userId, DemandUpdateDto req) {
        Demand demand = requireDemand(demandId);
        ensureOwner(userId, demand);
        if (!demand.isOpen()) {
            throw new BusinessException(ErrorCode.CONFLICT, "需求状态不是开放中");
        }

        if (req != null) {
            if (req.getTitle() != null) {
                demand.setTitle(req.getTitle());
            }
            if (req.getDescription() != null) {
                demand.setDescription(req.getDescription());
            }
            if (req.getCategory() != null) {
                demand.setCategory(req.getCategory());
            }
            if (req.getRewardAmount() != null) {
                demand.setRewardAmount(req.getRewardAmount());
            }
            if (req.getRewardType() != null) {
                demand.setRewardType(req.getRewardType());
            }
            if (req.getStatus() != null) {
                demand.setStatus(req.getStatus());
            }
            if (req.getUrgent() != null) {
                demand.setUrgent(req.getUrgent());
            }
            if (req.getUrgentDeadline() != null) {
                demand.setUrgentDeadline(req.getUrgentDeadline());
            }
            if (req.getLocationTag() != null) {
                demand.setLocationTag(req.getLocationTag());
            }
            if (req.getTagIds() != null) {
                updateImagesAndTags(demand.getId(), req.getTagIds(), req.getImageUrls());
            }
            List<String> warnings = validateDemandEntityTextAndReward(demand);
            validateUpdatedDemand(demand);
            demandRepository.save(demand);
            DemandDto dto = toDto(demand);
            dto.setRiskWarnings(warnings);
            return dto;
        }
        demandRepository.save(demand);
        return toDto(demand);
    }

    @Override
    public void cancel(Long demandId, Long userId) {
        Demand demand = requireDemand(demandId);
        ensureOwner(userId, demand);
        if (!demand.isOpen()) {
            throw new BusinessException(ErrorCode.CONFLICT, "需求状态不是开放中");
        }
        demand.cancel();
        demandRepository.save(demand);
    }

    @Override
    public void delete(Long demandId, Long userId) {
        Demand demand = requireDemand(demandId);
        ensureOwner(userId, demand);
        if (!demand.isOpen()) {
            throw new BusinessException(ErrorCode.CONFLICT, "需求已被接单，不能删除");
        }
        demandImageRepository.deleteByDemandId(demandId);
        demandRepository.deleteById(demandId);
    }

    private void validateUpdatedDemand(Demand demand) {
        DemandCreateDto validate = new DemandCreateDto();
        validate.setTitle(demand.getTitle());
        validate.setDescription(demand.getDescription());
        validate.setCategory(demand.getCategory());
        validate.setRewardAmount(demand.getRewardAmount());
        validate.setRewardType(demand.getRewardType());
        validate.setUrgent(demand.getUrgent());
        validate.setUrgentDeadline(demand.getUrgentDeadline());
        validate.setLocationTag(demand.getLocationTag());
        validate.setTagIds(getTagIdsForDemand(demand.getCategory()));
        validate.setImageUrls(getImageUrlsForDemand(demand.getId()));
        demandFactory.createDemand(validate);
    }

    private List<String> validateDemandTextAndReward(DemandCreateDto req) {
        if (req == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请求不能为空");
        }
        return validateDemandFields(req.getTitle(), req.getDescription(), req.getRewardAmount());
    }

    private List<String> validateDemandEntityTextAndReward(Demand demand) {
        return validateDemandFields(demand.getTitle(), demand.getDescription(), demand.getRewardAmount());
    }

    private List<String> validateDemandFields(String title, String description, Integer rewardAmount) {
        if (isBlank(title)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "标题不能为空");
        }
        if (isBlank(description)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "详细描述不能为空");
        }
        if (rewardAmount == null || rewardAmount < 0) {
            throw new BusinessException(ErrorCode.INVALID_PARAM, "报酬金额不能为负数");
        }
        List<String> warnings = new ArrayList<String>();
        ModerationResult titleResult = contentModerationService.assertDemandContentAllowed("标题", title);
        ModerationResult descriptionResult = contentModerationService.assertDemandContentAllowed("详细描述", description);
        warnings.addAll(titleResult.getWarnings());
        warnings.addAll(descriptionResult.getWarnings());
        return warnings.stream().distinct().collect(Collectors.toList());
    }

    private void updateImagesAndTags(Long demandId, List<Long> tagIds, List<String> imageUrls) {
        demandImageRepository.deleteByDemandId(demandId);
        saveImages(demandId, imageUrls);
    }

    private List<Long> getTagIdsForDemand(DemandCategory category) {
        if (category == DemandCategory.SKILL) {
            return tagRepository.findAll().stream().map(Tag::getId).collect(Collectors.toList());
        }
        return new ArrayList<Long>();
    }

    private List<String> getImageUrlsForDemand(Long demandId) {
        List<DemandImage> images = demandImageRepository.findByDemandIdOrderBySortOrderAsc(demandId);
        List<String> urls = new ArrayList<String>();
        for (DemandImage image : images) {
            urls.add(image.getImageUrl());
        }
        return urls;
    }

    private void saveImages(Long demandId, List<String> imageUrls) {
        if (imageUrls == null) {
            return;
        }
        for (int i = 0; i < imageUrls.size(); i++) {
            String imageUrl = imageUrls.get(i);
            if (isBlank(imageUrl)) {
                continue;
            }
            DemandImage image = new DemandImage();
            image.setDemandId(demandId);
            image.setImageUrl(imageUrl);
            image.setSortOrder(i);
            demandImageRepository.save(image);
        }
    }

    private Demand requireDemand(Long demandId) {
        if (demandId == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "需求ID不能为空");
        }
        return demandRepository.findById(demandId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DEMAND_NOT_FOUND));
    }

    private User requireActiveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.NOT_CERTIFIED);
        }
        return user;
    }

    private void ensureOwner(Long userId, Demand demand) {
        if (userId == null || demand.getPublisherId() == null || !demand.getPublisherId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    private List<Demand> sortDemands(List<Demand> demands, String sortBy, String sortDir) {
        Comparator<Demand> comparator;
        if ("rewardAmount".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Demand::getRewardAmount, Comparator.nullsLast(Integer::compareTo));
        } else if ("urgentDeadline".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparing(Demand::getUrgentDeadline, Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        } else {
            comparator = Comparator.comparing(Demand::getCreatedAt, Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        }
        if ("asc".equalsIgnoreCase(sortDir)) {
            return demands.stream().sorted(comparator).collect(Collectors.toList());
        }
        return demands.stream().sorted(comparator.reversed()).collect(Collectors.toList());
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (isBlank(source) || isBlank(keyword)) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private DemandDto toDto(Demand demand) {
        DemandDto dto = DemandDto.from(demand);
        if (demand != null) {
            userRepository.findById(demand.getPublisherId()).ifPresent(user -> dto.setPublisher(UserPublicDto.from(user)));
            dto.setImages(demandImageRepository.findByDemandIdOrderBySortOrderAsc(demand.getId()));
            dto.setTags(tagRepository.findAll());
        }
        return dto;
    }
}
