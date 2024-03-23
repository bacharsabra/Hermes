package uca.hermes.api.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import uca.hermes.api.dao.User;
import uca.hermes.api.dto.Position;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();

    Optional<User> findById(String id);

    boolean existsById(String id);

    void save(User user);

    void deleteById(String id);

    void updatePosition(String username, Position currentPosition);
    
    public ResponseEntity<Object> validateToken(WebRequest request);

    void changePassword(String id, String newPassword);
}
