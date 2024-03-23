package uca.hermes.api.dto;

import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserDTO {
    private String username;
    private Boolean isToken;
    private LocalDateTime tokenExpiryDate;
    private String href;
    private PlaceDTO place;
}
