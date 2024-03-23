package uca.hermes.api.dao;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.*;
import uca.hermes.api.dto.PlaceDTO;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import uca.hermes.api.dto.UserAccessDTO;
import uca.hermes.api.service.TagService;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EnableAutoConfiguration
@Builder
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String creator;
    @OneToMany
    @JoinColumn(name = "placeId")
    private List<UserPlaceAccess> userAccess;
    @OneToMany
    @JoinColumn(name = "placeId")
    private List<PlaceTag> tags;
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

    public UserAccessType getUserAccessType(String username, TagService tagService) {
        if (username.equals(this.getCreator()) || username.equals("admin")) {
            return UserAccessType.FULL;
        }

        UserAccessType tagAccessType = UserAccessType.NOT_AUTHORIZED;

        for (PlaceTag placeTag : tags) {
            Tag tag = tagService.findById(placeTag.getTagName()).get();
            UserAccessType currentAccessType = tag.getUserAccessType(username);
            if (currentAccessType.compareTo(tagAccessType) > 0) {
                tagAccessType = currentAccessType;
            }
        }

        UserAccessType placeAccessType = UserAccessType.NOT_AUTHORIZED;

        Optional<UserPlaceAccess> userPlaceAccess = userAccess.stream().filter(a -> a.getUsername().equals(username))
                .findFirst();
        if (userPlaceAccess.isPresent()) {
            if (userPlaceAccess.get().getCanEdit()) {
                placeAccessType = UserAccessType.FULL;
            } else {
                placeAccessType = UserAccessType.READ_ONLY;
            }
        }

        if (placeAccessType.compareTo(tagAccessType) > 0) {
            return placeAccessType;
        }

        return tagAccessType;
    }

    public PlaceDTO toDTO(String host) {
        List<String> tagsList = new ArrayList<>();
        for (PlaceTag tag : tags) {
            tagsList.add(tag.getTagName());
        }
        List<UserAccessDTO> userAccessDTOs = new ArrayList<>();
        for (UserPlaceAccess access : userAccess) {
            userAccessDTOs.add(access.toDTO());
        }
        String href = String.format("https://%s/basic-auth/places/%s", host, id);
        return new PlaceDTO(id, name, description, latitude, longitude, creator, userAccessDTOs, tagsList, updateTimestamp, href);
    }

    public String getImagePath(String imagesPath) {
        return String.format("%s/%s.jpg", imagesPath, id);
    }
}
