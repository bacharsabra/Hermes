package uca.hermes.api.service.impl;

import uca.hermes.api.dao.Tag;
import uca.hermes.api.dao.UserTagAccess;
import uca.hermes.api.repo.TagRepository;
import uca.hermes.api.repo.UserTagAccessRepository;
import uca.hermes.api.service.TagService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TagServiceImpl implements TagService {
    private final TagRepository repository;
    private final UserTagAccessRepository accessRepository;

    @Autowired
    public TagServiceImpl(TagRepository repository, UserTagAccessRepository accessRepository) {
        this.repository = repository;
        this.accessRepository = accessRepository;
    }

    @Override
    public List<Tag> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Tag> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public void save(Tag tag) {
        List<UserTagAccess> accessList = tag.getUserAccess();
        if (tag.getTagName() != null) {
            accessRepository.deleteByTagName(tag.getTagName());
        }
        tag.setUserAccess(new ArrayList<>());
        repository.save(tag);
        for (UserTagAccess access : accessList) {
            access.setTagName(tag.getTagName());
            accessRepository.save(access);
        }
    }
}
