package uca.hermes.api.repo;

import uca.hermes.api.dao.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {

}