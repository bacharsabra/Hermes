package uca.hermes.api.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class CollectionDTO {
    private String tagName;
    private List<PlaceDTO> places;
}
