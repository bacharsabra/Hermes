package uca.hermes.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import uca.hermes.api.dao.Place;
import uca.hermes.api.dao.Tag;
import uca.hermes.api.dao.UserAccessType;
import uca.hermes.api.dto.CollectionDTO;
import uca.hermes.api.dto.PlaceDTO;
import uca.hermes.api.service.PlaceService;
import uca.hermes.api.service.TagService;
import uca.hermes.api.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class CollectionController {
    private final PlaceService placeService;
    private final TagService tagService;
    private final UserService userService;
    private final MessageSource messageSource;

    CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
            .noTransform()
            .mustRevalidate();

    @Autowired
    public CollectionController(PlaceService placeService, TagService tagService, UserService userService,
            MessageSource messageSource) {
        this.placeService = placeService;
        this.tagService = tagService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @RequestMapping(value = "/basic-auth/api/collections", method = RequestMethod.GET)
    public ResponseEntity<Object> getAllCollections_Basic(WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return getAllCollections(authentication.getName());
    }

    @RequestMapping(value = "/token/api/collections", method = RequestMethod.GET)
    public ResponseEntity<Object> getAllCollections_Token(WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return getAllCollections((String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> getAllCollections(String username) {
        List<Tag> list = tagService.findAll();
        List<String> listDTO = new ArrayList<>();
        for (Tag tag : list) {
            if (tag.getUserAccessType(username) != UserAccessType.NOT_AUTHORIZED) {
                listDTO.add(tag.getTagName());
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(listDTO);
    }

    @RequestMapping(value = "/basic-auth/api/collections/{tagName}", method = RequestMethod.GET)
    public ResponseEntity<Object> getCollection_Basic(@PathVariable String tagName, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return getCollection(tagName, request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/collections/{tagName}", method = RequestMethod.GET)
    public ResponseEntity<Object> getCollection_Token(@PathVariable String tagName, WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return getCollection(tagName, request, (String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> getCollection(String tagName, WebRequest request, String username) {
        Optional<Tag> tag = tagService.findById(tagName);
        if (tag.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format(messageSource.getMessage("tag.notFound", null, Locale.getDefault()), tagName));
        }

        if (tag.get().getUserAccessType(username) == UserAccessType.NOT_AUTHORIZED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format(messageSource.getMessage("tag.forbidden", null, Locale.getDefault()), tagName));
        }
        List<Place> places = placeService.findByTagName(tag.get().getTagName());
        List<PlaceDTO> placesDTO = new ArrayList<>();
        for (Place place : places) {
            placesDTO.add(place.toDTO(request.getHeader("host")));
        }
        CollectionDTO collection = new CollectionDTO(tag.get().getTagName(), placesDTO);
        return ResponseEntity.status(HttpStatus.OK).cacheControl(cacheControl).body(collection);
    }
}
