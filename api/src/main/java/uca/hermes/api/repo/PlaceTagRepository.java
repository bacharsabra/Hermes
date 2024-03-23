package uca.hermes.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uca.hermes.api.dao.Place;
import uca.hermes.api.dao.PlaceTagKey;
import uca.hermes.api.dao.PlaceTag;

import java.util.List;

public interface PlaceTagRepository extends JpaRepository<PlaceTag, PlaceTagKey> {
    @Modifying
    @Query(value = "delete from PlaceTag p where p.placeId = :placeId")
    void deleteByPlaceId(@Param("placeId") String placeId);

    @Query(value = "select p from Place p join PlaceTag t on p.id = t.placeId where t.tagName = :tagName")
    List<Place> findPlacesByTagName(String tagName);
}
