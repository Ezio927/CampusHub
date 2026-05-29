package com.example.backend.demand.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.backend.demand.entity.Tag;
import com.example.backend.demand.mapper.TagMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class TagRepository {

    private final TagMapper mapper;

    public TagRepository(TagMapper mapper) {
        this.mapper = mapper;
    }

    public Tag save(Tag tag) {
        if (tag.getId() == null) {
            mapper.insert(tag);
        } else {
            mapper.updateById(tag);
        }
        return tag;
    }

    public Optional<Tag> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }

    public Optional<Tag> findByName(String name) {
        return Optional.ofNullable(mapper.selectOne(
                new LambdaQueryWrapper<Tag>().eq(Tag::getName, name)));
    }

    public List<Tag> findByNameIn(List<String> names) {
        if (names == null || names.isEmpty()) return List.of();
        return mapper.selectList(
                new LambdaQueryWrapper<Tag>().in(Tag::getName, names));
    }

    public List<Tag> findAll() {
        return mapper.selectList(null);
    }

    public long count() {
        return mapper.selectCount(null);
    }
}
