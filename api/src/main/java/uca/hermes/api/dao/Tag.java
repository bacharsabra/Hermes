package uca.hermes.api.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import uca.hermes.api.dto.TagDTO;
import uca.hermes.api.dto.UserAccessDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EnableAutoConfiguration
@Builder
public class Tag {
    @Id
    private String tagName;
    @OneToMany
    @JoinColumn(name = "tagName")
    private List<UserTagAccess> userAccess;
    private String creator;
    @UpdateTimestamp
    private LocalDateTime updateTimestamp;

    public UserAccessType getUserAccessType(String username) {
        if (username.equals(this.getCreator()) || username.equals("admin")) {
            return UserAccessType.FULL;
        }

        Optional<UserTagAccess> userTagAccess = userAccess.stream().filter(a -> a.getUsername().equals(username)).findFirst();
        if (userTagAccess.isPresent()) {
            if (userTagAccess.get().getCanEdit()) {
                return UserAccessType.FULL;
            } else {
                return UserAccessType.READ_ONLY;
            }
        }

        return UserAccessType.NOT_AUTHORIZED;
    }

    public TagDTO toDTO(String host) {
        List<UserAccessDTO> userAccessDTOs = new ArrayList<>();
        for (UserTagAccess access : userAccess) {
            userAccessDTOs.add(access.toDTO());
        }
        String href = "https://"+ host +"/basic-auth/tags/" + tagName;
        return new TagDTO(tagName, userAccessDTOs, creator, updateTimestamp, href);
    }
}
