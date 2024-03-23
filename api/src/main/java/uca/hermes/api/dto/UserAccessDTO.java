package uca.hermes.api.dto;

import uca.hermes.api.dao.UserPlaceAccess;
import uca.hermes.api.dao.UserTagAccess;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserAccessDTO {
    private String username;
    private Boolean canEdit;

    public UserTagAccess toTagEntity(String tagName) {
        return new UserTagAccess(username, tagName, canEdit);
    }

    public UserPlaceAccess toPlaceEntity(String placeId) {
        return new UserPlaceAccess(username, placeId, canEdit);
    }
}
