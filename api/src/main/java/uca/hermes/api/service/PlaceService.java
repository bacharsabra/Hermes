package uca.hermes.api.service;

import uca.hermes.api.dao.Place;

import java.util.List;
import java.util.Optional;

public interface PlaceService {
    List<Place> findAll();

    Optional<Place> findById(String id);

    boolean existsById(String id);

    void save(Place place);

    void deleteById(String id);

    List<Place> findByTagName(String tagName);

    /*
    List<Place> findByLongitudeAndLatitude(double longitude, double latitude);

    List<Place> findByLongitudeAndLatitudeWithinRadius(double longitude, double latitude, double radius);
    */
}
