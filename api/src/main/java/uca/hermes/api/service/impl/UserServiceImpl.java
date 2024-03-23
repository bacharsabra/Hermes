package uca.hermes.api.service.impl;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import uca.hermes.api.dao.MyUserDetails;
import uca.hermes.api.dao.Place;
import uca.hermes.api.dao.User;
import uca.hermes.api.dto.Position;
import uca.hermes.api.repo.UserRepository;
import uca.hermes.api.service.PlaceService;
import uca.hermes.api.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final PlaceService placeService;
    private final MessageSource messageSource;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder, MessageSource messageSource, PlaceService placeService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.messageSource = messageSource;
        this.placeService = placeService;
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public Optional<User> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    @Transactional
    public void save(User user) {
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        repository.save(user);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository.findById(username)
                .map(user -> new MyUserDetails(user))
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Username %s was not found", username)));
    }

    @Override
    @Transactional
    public void updatePosition(String username, Position currentPosition) {
        Optional<User> optionalUser = repository.findById(username);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            Place currentPlace = user.getCurrentPlace();
            if (currentPlace == null) {
                currentPlace = new Place(null, username + " current position", null, currentPosition.getLatitude(), currentPosition.getLongitude(), username, new ArrayList<>(), new ArrayList<>(), null);
            } else {
                currentPlace.setLatitude(currentPosition.getLatitude());
                currentPlace.setLongitude(currentPosition.getLongitude());
            }
            placeService.save(currentPlace);
            user.setCurrentPlace(currentPlace);
            repository.save(user);
        } else {
            throw new UsernameNotFoundException(String.format(messageSource.getMessage("user.notFound", null, Locale.getDefault()), username));
        }
    }

    @Override
    public ResponseEntity<Object> validateToken(WebRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        Optional<User> optionalUser = repository.findById(token);
        if (optionalUser.isEmpty()) {
            //TODO [said] after the merge, make this message a resource
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageSource.getMessage("token.invalid", null, Locale.getDefault()));
        }
        User user = optionalUser.get();
        if (!user.getIsToken() || user.getTokenExpiryDate().compareTo(LocalDateTime.now()) < 0) {
            //TODO [said] after the merge, make this message a resource
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(messageSource.getMessage("token.invalid", null, Locale.getDefault()));
        }
        return ResponseEntity.status(HttpStatus.OK).body(token);
    }

    //TODO [said] search for | String.format(" | and replace these messages with resources
    @Override
    public void changePassword(String username, String newPassword) {
        Optional<User> optionalUser = repository.findById(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            repository.save(user);
        } else {
            throw new UsernameNotFoundException(String.format(messageSource.getMessage("user.notFound", null, Locale.getDefault()), username));
        }
    }

}
