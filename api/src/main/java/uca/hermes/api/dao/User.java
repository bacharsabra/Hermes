package uca.hermes.api.dao;

import uca.hermes.api.dto.UserDTO;
import jakarta.persistence.*;
import lombok.Builder;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EnableAutoConfiguration
@Builder
@Table(name = "USERS")
public class User {
    @Id
    private String username;
    private String password;
    private Boolean isToken;
    private LocalDateTime tokenExpiryDate;
    @OneToOne
    private Place currentPlace;

    public UserDTO toDTO(String host) {
        String href = "https://"+ host +"/basic-auth/users/" + username;
        return new UserDTO(username, isToken, tokenExpiryDate, href, (currentPlace == null) ? null : currentPlace.toDTO(host));
    }
}
