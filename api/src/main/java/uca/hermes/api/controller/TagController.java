package uca.hermes.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import uca.hermes.api.dao.Tag;
import uca.hermes.api.dao.UserAccessType;
import uca.hermes.api.dto.TagDTO;
import uca.hermes.api.service.TagService;
import uca.hermes.api.service.UserService;
import uca.hermes.api.util.StringUtilities;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class TagController {
    private final TagService service;
    private final UserService userService;

    private final MessageSource messageSource;

    CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
            .noTransform()
            .mustRevalidate();

    @Autowired
    public TagController(TagService service, UserService userService, MessageSource messageSource) {
        this.service = service;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    @RequestMapping(value = "/basic-auth/api/tags", method = RequestMethod.GET)
    public ResponseEntity<Object> getAllTags_Basic(WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getAllTags(request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/tags", method = RequestMethod.GET)
    public ResponseEntity<Object> getAllTags_Token(WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return getAllTags(request, (String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> getAllTags(WebRequest request, String username) {
        List<Tag> list = service.findAll();
        List<TagDTO> listDTO = new ArrayList<>();
        for (Tag tag : list) {
            if (tag.getUserAccessType(username) != UserAccessType.NOT_AUTHORIZED) {
                listDTO.add(tag.toDTO(request.getHeader("host")));
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(listDTO);
    }

    @RequestMapping(value = "/basic-auth/api/tags/{tagName}", method = RequestMethod.GET)
    public ResponseEntity<Object> getTag_Basic(@PathVariable String tagName, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getTag(tagName, request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/tags/{tagName}", method = RequestMethod.GET)
    public ResponseEntity<Object> getTag_Token(@PathVariable String tagName, WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return getTag(tagName, request, (String) tokenValidationResult.getBody());
    }

    public ResponseEntity<Object> getTag(String tagName, WebRequest request, String username) {
        Optional<Tag> tag = service.findById(tagName);
        if (tag.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format(messageSource.getMessage("tag.notFound", null, Locale.getDefault()), tagName));

        }

        if (tag.get().getUserAccessType(username) == UserAccessType.NOT_AUTHORIZED) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format(messageSource.getMessage("tag.forbidden", null, Locale.getDefault()), tagName));
        }

        // Handle If-Modified-Since HTTP header
        if (request.checkNotModified(tag.get().getUpdateTimestamp()
                .atZone(ZoneId.of("GMT")).toInstant().toEpochMilli())) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        return ResponseEntity.status(HttpStatus.OK).cacheControl(cacheControl).body(tag.get().toDTO(request.getHeader("host")));
    }

    @RequestMapping(value = "/basic-auth/api/tags", method = RequestMethod.POST)
    public ResponseEntity<Object> postTag_Basic(@RequestBody TagDTO tag, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return postTag(tag, request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/tags", method = RequestMethod.POST)
    public ResponseEntity<Object> postTag_Token(@RequestBody TagDTO tag, WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return postTag(tag, request, (String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> postTag(TagDTO tag, WebRequest request, String username) {
        tag.setCreator(username);

        Tag savedTag = tag.toEntity();
        service.save(savedTag);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTag.toDTO(request.getHeader("host")));
    }

    @RequestMapping(value = "/basic-auth/api/tags/{tagName}", method = RequestMethod.PUT)
    public ResponseEntity<Object> putTag_Basic(@PathVariable String tagName, @RequestBody TagDTO tag, WebRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return putTag(tagName, tag, request, authentication.getName());
    }

    @RequestMapping(value = "/token/api/tags/{tagName}", method = RequestMethod.PUT)
    public ResponseEntity<Object> putTag_Token(@PathVariable String tagName, @RequestBody TagDTO tag,
            WebRequest request) {
        ResponseEntity<Object> tokenValidationResult = userService.validateToken(request);
        if (tokenValidationResult.getStatusCode() != HttpStatus.OK) {
            return tokenValidationResult;
        }
        return putTag(tagName, tag, request, (String) tokenValidationResult.getBody());
    }

    private ResponseEntity<Object> putTag(String tagName, TagDTO tag, WebRequest request, String username) {
        Optional<Tag> oldTag = service.findById(tagName);
        if (oldTag.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(String.format(messageSource.getMessage("tag.notFound", null, Locale.getDefault()), tagName));
        }

        if (oldTag.get().getUserAccessType(username) != UserAccessType.FULL) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(String.format(messageSource.getMessage("tag.forbidden", null, Locale.getDefault()), tagName));
        }

        if (!StringUtilities.isBlankOrNull(tag.getTagName())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String
                    .format(messageSource.getMessage("tag.updateNotAllowed", null, Locale.getDefault()), tagName));
        }
        tag.setTagName(tagName);

        if (!StringUtilities.isBlankOrNull(tag.getCreator())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(String.format(
                    messageSource.getMessage("tag.creatorUpdateNotAllowed", null, Locale.getDefault()), tagName));
        }
        tag.setCreator(oldTag.get().getCreator());

        Tag savedTag = tag.toEntity();
        service.save(savedTag);
        return ResponseEntity.status(HttpStatus.OK).body(savedTag.toDTO(request.getHeader("host")));
    }
}
