package uca.hermes.api.dto;
import uca.hermes.api.dao.Tag;
import uca.hermes.api.dao.UserTagAccess;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class TagDTO {
    private String tagName;
    private List<UserAccessDTO> userAccess;
    private String creator;
    private LocalDateTime updateTimestamp;
    private String href;

    public Tag toEntity() {
        List<UserTagAccess> userAccessEntities = new ArrayList<>();
        for (UserAccessDTO access : userAccess) {
            userAccessEntities.add(access.toTagEntity(tagName));
        }
        return new Tag(tagName, userAccessEntities, creator, updateTimestamp);
    }
}
