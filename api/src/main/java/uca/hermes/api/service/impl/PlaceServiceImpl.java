package uca.hermes.api.service.impl;

import uca.hermes.api.dao.Place;
import uca.hermes.api.dao.PlaceTag;
import uca.hermes.api.dao.Tag;
import uca.hermes.api.dao.UserPlaceAccess;
import uca.hermes.api.repo.PlaceRepository;
import uca.hermes.api.repo.PlaceTagRepository;
import uca.hermes.api.repo.TagRepository;
import uca.hermes.api.repo.UserPlaceAccessRepository;
import uca.hermes.api.service.PlaceService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PlaceServiceImpl implements PlaceService {
    private final PlaceRepository repository;
    private final PlaceTagRepository placeTagRepository;
    private final TagRepository tagRepository;
    private final UserPlaceAccessRepository accessRepository;

    @Autowired
    public PlaceServiceImpl(PlaceRepository repository, TagRepository tagRepository, PlaceTagRepository placeTagRepository, UserPlaceAccessRepository accessRepository) {
        this.repository = repository;
        this.tagRepository = tagRepository;
        this.placeTagRepository = placeTagRepository;
        this.accessRepository = accessRepository;
    }

    @Override
    public List<Place> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<Place> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }


    @Override
    @Transactional
    public void save(Place place) {
        List<PlaceTag> tagsList = place.getTags();
        List<UserPlaceAccess> accessList = place.getUserAccess();
        if (place.getId() != null) {
            placeTagRepository.deleteByPlaceId(place.getId());
            accessRepository.deleteByPlaceId(place.getId());
        }
        place.setTags(new ArrayList<>());
        place.setUserAccess(new ArrayList<>());
        repository.save(place);
        //Add the all tag by default
        if (tagsList.stream().filter(t -> t.getTagName().equals("all")).count() == 0) {
            tagsList.add(new PlaceTag("all", place.getId()));
        }
        for (PlaceTag tag : tagsList) {
            if (!tagRepository.existsById(tag.getTagName())) {
                tagRepository.save(new Tag(tag.getTagName(), null, place.getCreator(), place.getUpdateTimestamp()));
            }
            tag.setPlaceId(place.getId());
            placeTagRepository.save(tag);
        }
        for (UserPlaceAccess access : accessList) {
            access.setPlaceId(place.getId());
            accessRepository.save(access);
        }
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        placeTagRepository.deleteByPlaceId(id);
        accessRepository.deleteByPlaceId(id);
        repository.deleteById(id);
    }

    @Override
    public List<Place> findByTagName(String tagName) {
        return placeTagRepository.findPlacesByTagName(tagName);
    }
    /*
    @Override
    public List<Place> findByLongitudeAndLatitude(double longitude, double latitude) {
        return new ArrayList<>(repository.findByLongitudeAndLatitude(longitude, latitude));
    }

    @Override
    public List<Place> findByLongitudeAndLatitudeWithinRadius(double longitude, double latitude, double radius) {
        return repository.findByLocationWithinRadius(longitude, latitude, radius);
    }
    */
}
