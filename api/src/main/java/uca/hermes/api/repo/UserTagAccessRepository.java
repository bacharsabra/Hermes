package uca.hermes.api.repo;

import uca.hermes.api.dao.UserTagAccess;
import uca.hermes.api.dao.UserTagAccessKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserTagAccessRepository extends JpaRepository<UserTagAccess, UserTagAccessKey> {
    @Modifying
    @Query(value = "delete from UserTagAccess a where a.tagName = :tagName")
    void deleteByTagName(@Param("tagName") String tagName);
}
