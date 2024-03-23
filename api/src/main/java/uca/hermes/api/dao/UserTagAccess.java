package uca.hermes.api.dao;

import uca.hermes.api.dto.UserAccessDTO;
import jakarta.persistence.*;
import lombok.Builder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EnableAutoConfiguration
@Builder
@IdClass(UserTagAccessKey.class)
public class UserTagAccess {
    @Id
    private String username;
    @Id
    private String tagName;
    private Boolean canEdit;

    public UserAccessDTO toDTO() {
        return new UserAccessDTO(username, canEdit);
    }
}
