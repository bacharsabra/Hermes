package uca.hermes.api.repo;

import uca.hermes.api.dao.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, String> {
    /*
    List<Place> findByLongitudeAndLatitude(double longitude, double latitude);

    @Query("SELECT p FROM Place p WHERE ST_DWithin(ST_SetSRID(ST_Point(p.longitude, p.latitude), 4326), ST_SetSRID(ST_Point(:longitude, :latitude), 4326), :radius)")
    List<Place> findByLocationWithinRadius(double longitude, double latitude, double radius);
    */

}