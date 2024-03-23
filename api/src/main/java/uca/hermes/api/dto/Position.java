package uca.hermes.api.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Position {
    private Double latitude;
    private Double longitude;
}
