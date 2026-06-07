package com.example.backend.demand.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.demand.entity.DemandImage;
import com.example.backend.demand.mapper.DemandImageMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class DemandImageRepository {

    private final DemandImageMapper mapper;

    public DemandImageRepository(DemandImageMapper mapper) {
        this.mapper = mapper;
    }

    public DemandImage save(DemandImage img) {
        if (img.getId() == null) {
            mapper.insert(img);
        } else {
            mapper.updateById(img);
        }
        return img;
    }

    public Optional<DemandImage> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public List<DemandImage> findByDemandIdOrderBySortOrderAsc(Long demandId) {
        return mapper.selectList(
                new LambdaQueryWrapper<DemandImage>()
                        .eq(DemandImage::getDemandId, demandId)
                        .orderByAsc(DemandImage::getSortOrder));
    }

    public void deleteByDemandId(Long demandId) {
        mapper.delete(new LambdaQueryWrapper<DemandImage>()
                .eq(DemandImage::getDemandId, demandId));
    }
}
