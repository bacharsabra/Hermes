package uca.hermes.api.dto;

import uca.hermes.api.dao.Place;
import uca.hermes.api.dao.PlaceTag;
import lombok.*;
import uca.hermes.api.dao.UserPlaceAccess;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class PlaceDTO {
    private String id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private String creator;
    private List<UserAccessDTO> userAccess;
    private List<String> tags;
    private LocalDateTime updateTimestamp;
    private String href;

    public Place toEntity() {
        List<PlaceTag> tagsList = new ArrayList<>();
        for (String tag : tags) {
            tagsList.add(new PlaceTag(tag, id));
        }
        List<UserPlaceAccess> userAccessEntities = new ArrayList<>();
        for (UserAccessDTO access : userAccess) {
            userAccessEntities.add(access.toPlaceEntity(id));
        }
        return new Place(id, name, description, latitude, longitude, creator, userAccessEntities, tagsList,
                updateTimestamp);
    }
}
