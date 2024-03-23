package uca.hermes.api.dao;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@EnableAutoConfiguration
@Builder
@IdClass(PlaceTagKey.class)
public class PlaceTag {
    @Id
    private String tagName;
    @Id
    private String placeId;
}
