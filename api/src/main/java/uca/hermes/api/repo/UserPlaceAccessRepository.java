package uca.hermes.api.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uca.hermes.api.dao.UserPlaceAccess;
import uca.hermes.api.dao.UserPlaceAccessKey;

public interface UserPlaceAccessRepository extends JpaRepository<UserPlaceAccess, UserPlaceAccessKey> {
    @Modifying
    @Query(value = "delete from UserPlaceAccess a where a.placeId = :placeId")
    void deleteByPlaceId(@Param("placeId") String placeId);
}
