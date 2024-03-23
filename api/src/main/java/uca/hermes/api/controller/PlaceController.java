package uca.hermes.api.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import uca.hermes.api.dao.Place;
import uca.hermes.api.dao.UserAccessType;
import uca.hermes.api.dto.PlaceDTO;
import uca.hermes.api.dto.SearchQuery;
import uca.hermes.api.service.PlaceService;
import uca.hermes.api.service.TagService;
import uca.hermes.api.service.UserService;
import uca.hermes.api.util.StringUtilities;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class PlaceController {
    private final PlaceService service;
    private final TagService tagService;
    private final UserService userService;

    private final MessageSource messageSource;

    @Value("${hermes.images-path}")
    private String imagesPath;

    CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
            .noTransform()
            .mustRevalidate();

    @Autowired
    public PlaceController(PlaceService service, TagService tagService, UserService userService,
            MessageSource messageSource) {
        this.service = service;
        this.tagService = tagService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @RequestMapping(value = "/basic-auth/api/places", method = RequestMethod.GET)
    public ResponseEntity<Object> getAllPlaces_Basic(WebRequest request, @RequestParam String searchString,
                                                     @RequestParam String[] tagsList) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setSearchString(searchString);
        searchQuery.setTagsList(tagsList);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return getAllPlaces(request, searchQuery, authentication.getName());
    }

    @RequestMapping(value = "/token/api/places", method = RequestMethod.GET)
    public ResponseEntity<Object> getAllPlaces_Token(WebRequest request, @RequestParam String searchString,
                                                     @RequestParam String[] tagsList) {
        SearchQuery searchQuery = new SearchQuery();
        searchQuery.setSearchString(searchString);
        searchQuery.setTagsList(tagsList);
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return getAllPlaces(request, searchQuery, (String)tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> getAllPlaces(WebRequest request, SearchQuery searchQuery, String username) {
        List<Place> list = service.findAll();
        // Handle If-Modified-Since HTTP header
        if (!list.isEmpty() && request.checkNotModified(list.stream()
                .map(Place::getUpdateTimestamp).max(LocalDateTime::compareTo).get()
                .atZone(ZoneId.of("GMT")).toInstant().toEpochMilli())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        if(searchQuery != null) {
            searchQuery.normalize();
        } else {
            searchQuery = new SearchQuery();
        }
        List<PlaceDTO> listDTO = new ArrayList<>();
        for(Place place : list) {
            if(place.getUserAccessType(username, tagService) == UserAccessType.NOT_AUTHORIZED) {
                continue;
            }
            if (!searchQuery.checkPlace(place)) {
                continue;
            }
            listDTO.add(place.toDTO(request.getHeader("host")));
        }
        return ResponseEntity.status(HttpStatus.OK).cacheControl(cacheControl).body(listDTO);
    }

    @RequestMapping(value = "/basic-auth/api/places/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> getPlace_Basic(@PathVariable String id, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getPlace(id, request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/places/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> getPlace_Token(@PathVariable String id, WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return getPlace(id, request, (String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> getPlace(String id, WebRequest request, String username) {
        Optional<Place> place = service.findById(id);
        if (place.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format(messageSource.getMessage("place.notFound", null, Locale.getDefault()), id));
        }

        if (place.get().getUserAccessType(username, tagService) == UserAccessType.NOT_AUTHORIZED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format(messageSource.getMessage("place.forbidden", null, Locale.getDefault()), id));
        }

        // Handle If-Modified-Since HTTP header
        if (request.checkNotModified(place.get().getUpdateTimestamp()
                .atZone(ZoneId.of("GMT")).toInstant().toEpochMilli())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        return ResponseEntity.status(HttpStatus.OK).cacheControl(cacheControl).body(place.get().toDTO(request.getHeader("host")));
    }

    @RequestMapping(value = "/basic-auth/api/places", method = RequestMethod.POST)
    public ResponseEntity<Object> postPlace_Basic(@RequestBody PlaceDTO place, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return postPlace(place, request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/places", method = RequestMethod.POST)
    public ResponseEntity<Object> postPlace_Token(@RequestBody PlaceDTO place, WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return postPlace(place, request, (String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> postPlace(PlaceDTO place, WebRequest request, String username) {
        if (!StringUtilities.isBlankOrNull(place.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(String.format(messageSource.getMessage("place.idAutoGenerated", null, Locale.getDefault())));
        }

        place.setCreator(username);

        Place savedPlace = place.toEntity();
        service.save(savedPlace);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedPlace.toDTO(request.getHeader("host")));
    }

    @RequestMapping(value = "/basic-auth/api/places/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Object> putPlace_Basic(@PathVariable String id, @RequestBody PlaceDTO place, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return putPlace(id, place, request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/places/{id}", method = RequestMethod.PUT)
    public ResponseEntity<Object> putPlace_Token(@PathVariable String id, @RequestBody PlaceDTO place,
            WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return putPlace(id, place, request, (String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> putPlace(String id, PlaceDTO place, WebRequest request, String username) {
        Optional<Place> oldPlace = service.findById(id);
        if (oldPlace.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format(messageSource.getMessage("place.notFound", null, Locale.getDefault()), id));
        }

        if (oldPlace.get().getUserAccessType(username, tagService) != UserAccessType.FULL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format(messageSource.getMessage("place.forbidden", null, Locale.getDefault()), id));
        }

        if (!StringUtilities.isBlankOrNull(place.getId())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    String.format(messageSource.getMessage("place.updateNotAllowed", null, Locale.getDefault()), id));
        }
        place.setId(id);

        if (!StringUtilities.isBlankOrNull(place.getCreator())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String
                    .format(messageSource.getMessage("place.creatorUpdateNotAllowed", null, Locale.getDefault()), id));
        }
        place.setCreator(oldPlace.get().getCreator());

        Place savedPlace = place.toEntity();
        service.save(savedPlace);
        return ResponseEntity.status(HttpStatus.OK).body(savedPlace.toDTO(request.getHeader("host")));
    }

    @RequestMapping(value = "/basic-auth/api/places/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> deletePlace_Basic(@PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return deletePlace(id, authentication.getName());

    }

    @RequestMapping(value = "/token/api/places/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> deletePlace_Token(@PathVariable String id, WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return deletePlace(id, (String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> deletePlace(String id, String username) {
        Optional<Place> oldPlace = service.findById(id);
        if (oldPlace.isEmpty()) {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(String.format(messageSource.getMessage("place.notFound", null, Locale.getDefault()), id));
        }

        if (oldPlace.get().getUserAccessType(username, tagService) != UserAccessType.FULL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    String.format(messageSource.getMessage("place.editForbidden", null, Locale.getDefault()), id));
        }

        service.deleteById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(String.format(messageSource.getMessage("place.deleted", null, Locale.getDefault()), id));
    }

    @RequestMapping(value = "/basic-auth/api/places/{id}/image", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Object> getImage_Basic(@PathVariable String id, WebRequest request)
            throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getImage(id, request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/places/{id}/image", method = RequestMethod.GET)
    public @ResponseBody ResponseEntity<Object> getImage_Token(@PathVariable String id, WebRequest request)
            throws IOException {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return getImage(id, request, (String) tokenValidationResult.getBody());
    }

    private @ResponseBody ResponseEntity<Object> getImage(String id, WebRequest request, String username)
            throws IOException {
        Optional<Place> place = service.findById(id);
        if (place.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format(messageSource.getMessage("place.notFound", null, Locale.getDefault()), id));
        }

        if (place.get().getUserAccessType(username, tagService) == UserAccessType.NOT_AUTHORIZED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format(messageSource.getMessage("place.forbidden", null, Locale.getDefault()), id));
        }

        // Handle If-Modified-Since HTTP header
        if (request.checkNotModified(place.get().getUpdateTimestamp()
                .atZone(ZoneId.of("GMT")).toInstant().toEpochMilli())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        File file = new File(place.get().getImagePath(imagesPath));
        if (!file.exists()) {
            file = ResourceUtils.getFile("classpath:static/default.jpg");
        }
        InputStream in = new FileInputStream(file);
        return ResponseEntity.status(HttpStatus.OK).cacheControl(cacheControl).body(IOUtils.toByteArray(in));
    }

    @RequestMapping(value = "/basic-auth/api/places/{id}/image", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Object> saveImage_Basic(@PathVariable String id,
            @RequestParam("file") MultipartFile file) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return saveImage(id, file, authentication.getName());
    }

    @RequestMapping(value = "/token/api/places/{id}/image", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<Object> saveImage_Token(@PathVariable String id,
            @RequestParam("file") MultipartFile file, WebRequest request) throws IOException {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return saveImage(id, file, (String) tokenValidationResult.getBody());
    }

    private @ResponseBody ResponseEntity<Object> saveImage(String id, MultipartFile file, String username)
            throws IOException {
        Optional<Place> place = service.findById(id);
        if (place.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format(messageSource.getMessage("place.notFound", null, Locale.getDefault()), id));
        }

        if (place.get().getUserAccessType(username, tagService) != UserAccessType.FULL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                    String.format(messageSource.getMessage("place.editForbidden", null, Locale.getDefault()), id));
        }

        try {
            Files.copy(file.getInputStream(), Path.of(place.get().getImagePath(imagesPath)));
            return ResponseEntity.status(HttpStatus.OK).body("Uploaded the image successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(String
                    .format(messageSource.getMessage("image.uploadError", null, Locale.getDefault()), e.getMessage()));
        }
    }

    /*
    //TODO [said] move this to the original getAllPlaces method and just add the parameters there.
    //TODO [said] make sure that you modify both token and basic-auth
    @RequestMapping(method = RequestMethod.GET, value = "/searchByLongitudeAndLatitude")
    public ResponseEntity<Object> searchByLongitudeAndLatitude(@RequestParam double longitude, @RequestParam double latitude, WebRequest request) {
        List<Place> places = service.findByLongitudeAndLatitude(longitude, latitude);
        List<PlaceDTO> placeDTOs = new ArrayList<>();
        for(Place place : places) {
            placeDTOs.add(place.toDTO(request.getHeader("host")));
        }
        return ResponseEntity.status(HttpStatus.OK).body(placeDTOs);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/searchByLongitudeAndLatitude")
    public ResponseEntity<Object> searchByLongitudeAndLatitudeClose(
            @RequestParam double longitude,
            @RequestParam double latitude,
            @RequestParam(defaultValue = "1000") double radius,
            WebRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        List<Place> places = service.findByLongitudeAndLatitudeWithinRadius(longitude, latitude, radius);
        List<PlaceDTO> placeDTOs = new ArrayList<>();
        for(Place place : places) {
            if(place.getUserAccessType(authentication.getName(), tagService) != UserAccessType.NOT_AUTHORIZED) {
                placeDTOs.add(place.toDTO(request.getHeader("host")));
            }
        }

        if (!placeDTOs.isEmpty()) {
            long lastUpdateTimestamp = placeDTOs.stream()
                    .map(PlaceDTO::getUpdateTimestamp)
                    .max(LocalDateTime::compareTo)
                    .get()
                    .atZone(ZoneId.of("GMT"))
                    .toInstant()
                    .toEpochMilli();

            if (request.checkNotModified(lastUpdateTimestamp)) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
            }
        }

        return ResponseEntity.ok().cacheControl(cacheControl).body(placeDTOs);
    }
    */
}
