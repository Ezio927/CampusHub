package com.example.backend.demand.repository;

import com.example.backend.demand.entity.Tag;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class TagRepository {

    private final ConcurrentHashMap<Long, Tag> storage = new ConcurrentHashMap<Long, Tag>();
    private final ConcurrentHashMap<String, Long> nameIndex = new ConcurrentHashMap<String, Long>();
    private final AtomicLong idGenerator = new AtomicLong(1L);

    public TagRepository() {
        seed("location");
        seed("skill");
        seed("study");
        seed("secondhand");
    }

    public synchronized Tag save(Tag tag) {
        if (tag.getId() == null) {
            tag.setId(idGenerator.getAndIncrement());
        }
        storage.put(tag.getId(), tag);
        if (tag.getName() != null) {
            nameIndex.put(tag.getName(), tag.getId());
        }
        return tag;
    }

    public Optional<Tag> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public Optional<Tag> findByName(String name) {
        Long id = nameIndex.get(name);
        return id == null ? Optional.<Tag>empty() : findById(id);
    }

    public List<Tag> findByNameIn(List<String> names) {
        if (names == null) {
            return Collections.emptyList();
        }
        return names.stream()
                .map(this::findByName)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(ArrayList<Tag>::new));
    }

    public List<Tag> findAll() {
        return Collections.unmodifiableList(new ArrayList<Tag>(storage.values()));
    }

    private void seed(String name) {
        Tag tag = new Tag();
        tag.setName(name);
        save(tag);
    }
}
