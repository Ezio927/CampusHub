package com.example.backend.demand.pattern.factory;

import com.example.backend.common.BusinessException;
import com.example.backend.common.ErrorCode;
import com.example.backend.common.enums.DemandCategory;
import com.example.backend.common.enums.RewardType;
import com.example.backend.demand.dto.DemandCreateDto;
import com.example.backend.demand.entity.Demand;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

class DemandFactoryValidatorsTest {

    private DemandFactory demandFactory;

    @BeforeEach
    void setUp() {
        demandFactory = new DemandFactory(
                new ErrandValidator(),
                new SkillValidator(),
                new StudyValidator(),
                new SecondhandValidator(),
                new OtherValidator()
        );
    }

    @Test
    void shouldRejectNullCategory() {
        DemandCreateDto dto = buildDto("test", null);
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> demandFactory.createDemand(dto)).getCode());
    }

    @Test
    void errandValidatorShouldRequireLocationTag() {
        DemandCreateDto dto = buildDto("errand task", DemandCategory.ERRAND);
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> demandFactory.createDemand(dto)).getCode());

        dto.setLocationTag("campus gate");
        Demand demand = demandFactory.createDemand(dto);
        Assertions.assertNotNull(demand);
        Assertions.assertEquals(DemandCategory.ERRAND, demand.getCategory());
    }

    @Test
    void skillValidatorShouldCreateWithoutTags() {
        DemandCreateDto dto = buildDto("java help", DemandCategory.SKILL);
        dto.setLocationTag("library");
        Demand demand = demandFactory.createDemand(dto);
        Assertions.assertNotNull(demand);
        Assertions.assertEquals(DemandCategory.SKILL, demand.getCategory());
    }

    @Test
    void studyValidatorShouldRequireDeadline() {
        DemandCreateDto dto = buildDto("math study", DemandCategory.STUDY);
        dto.setLocationTag("classroom");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> demandFactory.createDemand(dto)).getCode());

        dto.setUrgentDeadline(LocalDateTime.now().plusDays(1));
        Demand demand = demandFactory.createDemand(dto);
        Assertions.assertNotNull(demand);
    }

    @Test
    void secondhandValidatorShouldRequireImages() {
        DemandCreateDto dto = buildDto("used book", DemandCategory.SECONDHAND);
        dto.setLocationTag("dorm");
        Assertions.assertEquals(ErrorCode.BAD_REQUEST.getCode(),
                Assertions.assertThrows(BusinessException.class,
                        () -> demandFactory.createDemand(dto)).getCode());

        dto.setImageUrls(List.of("http://img.jpg"));
        Demand demand = demandFactory.createDemand(dto);
        Assertions.assertNotNull(demand);
    }

    @Test
    void shouldCreateDemandWithCorrectFields() {
        DemandCreateDto dto = buildDto("urgent errand", DemandCategory.ERRAND);
        dto.setLocationTag("gate");
        dto.setRewardAmount(50);
        dto.setRewardType(RewardType.MONEY);
        dto.setUrgent(true);
        dto.setDescription("urgent delivery");

        Demand demand = demandFactory.createDemand(dto);
        Assertions.assertEquals("urgent errand", demand.getTitle());
        Assertions.assertEquals(50, demand.getRewardAmount());
        Assertions.assertEquals(RewardType.MONEY, demand.getRewardType());
        Assertions.assertTrue(demand.getUrgent());
        Assertions.assertEquals("gate", demand.getLocationTag());
    }

    @Test
    void shouldHandleNullUrgentAsFalse() {
        DemandCreateDto dto = buildDto("test", DemandCategory.ERRAND);
        dto.setLocationTag("gate");

        Demand demand = demandFactory.createDemand(dto);
        Assertions.assertFalse(demand.getUrgent());
    }

    @Test
    void otherValidatorShouldAcceptWithoutExtraFields() {
        DemandCreateDto dto = buildDto("misc task", DemandCategory.OTHER);
        Demand demand = demandFactory.createDemand(dto);
        Assertions.assertNotNull(demand);
        Assertions.assertEquals(DemandCategory.OTHER, demand.getCategory());
    }

    @Test
    void shouldRejectNegativeRewardAmount() {
        DemandCreateDto dto = buildDto("test", DemandCategory.ERRAND);
        dto.setLocationTag("gate");
        dto.setRewardAmount(-1);

        BusinessException ex = Assertions.assertThrows(BusinessException.class,
                () -> demandFactory.createDemand(dto));
        Assertions.assertEquals(ErrorCode.INVALID_PARAM.getCode(), ex.getCode());
    }

    private DemandCreateDto buildDto(String title, DemandCategory category) {
        DemandCreateDto dto = new DemandCreateDto();
        dto.setTitle(title);
        dto.setDescription("test description");
        dto.setCategory(category);
        dto.setRewardAmount(20);
        dto.setRewardType(RewardType.MONEY);
        return dto;
    }
}
