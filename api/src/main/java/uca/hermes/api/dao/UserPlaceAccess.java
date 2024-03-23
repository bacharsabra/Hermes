package uca.hermes.api.dao;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import uca.hermes.api.dto.UserAccessDTO;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EnableAutoConfiguration
@Builder
@IdClass(UserPlaceAccessKey.class)
public class UserPlaceAccess {
    @Id
    private String username;
    @Id
    private String placeId;
    private Boolean canEdit;

    public UserAccessDTO toDTO() {
        return new UserAccessDTO(username, canEdit);
    }
}
