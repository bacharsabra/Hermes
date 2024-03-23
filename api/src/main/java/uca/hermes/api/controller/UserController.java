package uca.hermes.api.controller;

import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.WebRequest;
import uca.hermes.api.dao.User;
import uca.hermes.api.dto.Position;
import uca.hermes.api.dto.UserDTO;
import uca.hermes.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import java.security.Principal;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/basic-auth/api/users")
public class UserController {
    private final UserService service;
    private final MessageSource messageSource;

    @Autowired
    public UserController(UserService service, MessageSource messageSource) {
        this.service = service;
        this.messageSource = messageSource;
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Object> getAllUsers(WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Only the admin can view all users
        if (!authentication.getName().equals("admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        List<User> list = service.findAll();
        List<UserDTO> listDTO = new ArrayList<>();
        for(User user : list) {
            listDTO.add(user.toDTO(request.getHeader("host")));
        }
        return ResponseEntity.status(HttpStatus.OK).body(listDTO);
    }

    @RequestMapping(value = "/{username}", method = RequestMethod.GET)
    public ResponseEntity<Object> getUser(@PathVariable String username, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Only the user himself and the admin can view the user details
        if (!authentication.getName().equals(username) && !authentication.getName().equals("admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<User> user = service.findById(username);
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format(messageSource.getMessage("user.notFound", null, Locale.getDefault()), username));
        }
        return ResponseEntity.status(HttpStatus.OK).body(user.get().toDTO(request.getHeader("host")));
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Object> postUser(@RequestBody User user, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Only the admin can create  users
        if (!authentication.getName().equals("admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (service.existsById(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format(messageSource.getMessage("user.alreadyExists", null, Locale.getDefault()), user.getUsername()));
        }
        service.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(user.toDTO(request.getHeader("host")));
    }
    @PutMapping("/{username}/update-position")
    public ResponseEntity<?> updatePosition(@PathVariable String username, @RequestBody Position currentPosition, Principal authenticatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //Only the user himself and the admin can update the user's position
        if (!authentication.getName().equals(username) && !authentication.getName().equals("admin")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            service.updatePosition(username, currentPosition);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (UsernameNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{username}/change-password", method = RequestMethod.PATCH)
    public ResponseEntity<Object> changePassword(@PathVariable String username, @RequestBody String newPassword) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            //Only the user can change his password
            if (!authentication.getName().equals(username)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            service.changePassword(username, newPassword);
            return ResponseEntity.status(HttpStatus.OK).build();
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
