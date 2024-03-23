package uca.hermes.api.service;

import uca.hermes.api.dao.Tag;

import java.util.List;
import java.util.Optional;

public interface TagService {
    List<Tag> findAll();

    Optional<Tag> findById(String id);

    boolean existsById(String id);

    void save(Tag tag);
}
