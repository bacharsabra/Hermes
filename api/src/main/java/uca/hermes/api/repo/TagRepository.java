package uca.hermes.api.repo;

import uca.hermes.api.dao.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, String> {
}
