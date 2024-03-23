package uca.hermes.api;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.ArgumentMatchers.any;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import uca.hermes.api.controller.PlaceController;
import uca.hermes.api.dao.Place;
import uca.hermes.api.dao.PlaceTag;
import uca.hermes.api.dto.PlaceDTO;
import uca.hermes.api.service.PlaceService;
import uca.hermes.api.service.TagService;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(PlaceController.class)
public class PlaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlaceService placeService;

    @MockBean
    private TagService tagService;

    @MockBean
    private MessageSource messageSource;

    @InjectMocks
    private PlaceController placeController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void getAllPlaces_ReturnsEmptyList_WhenNoPlacesExist() throws Exception {
        // Arrange
        when(placeService.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/basic-auth/api/places"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void getAllPlaces_NotModified_WhenCachedDataIsUpToDate() throws Exception {
        // Arrange
        // Mocking the service to return a list of places
        PlaceDTO placeDTO = PlaceDTO.builder()
                .name("Central Park")
                .description("A large public park in New York City.")
                .latitude(40.785091)
                .longitude(-73.968285)
                .creator("admin")
                .userAccess(new ArrayList<>()) // Assuming UserAccessDTO has been defined elsewhere
                .tags(Arrays.asList("Nature", "Public"))
                .updateTimestamp(LocalDateTime.now())
                .build();

        List<Place> places = Collections.singletonList(placeDTO.toEntity());
        when(placeService.findAll()).thenReturn(places);
        // Mocking the request to simulate If-Modified-Since header with a date after the last update timestamp
        LocalDateTime lastUpdate = places.get(0).getUpdateTimestamp();
        ZonedDateTime lastUpdateZoned = lastUpdate.atZone(ZoneId.of("GMT"));
        long ifModifiedSince = lastUpdateZoned.toInstant().toEpochMilli();

        // Act & Assert
        mockMvc.perform(get("/basic-auth/api/places").header("If-Modified-Since", ifModifiedSince))
                .andExpect(status().isNotModified());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void getAllPlaces_ReturnsEmptyList_WhenNoPlacesFound() throws Exception {
        Place place = new Place();
        place.setName("Central Park");
        place.setDescription("A large public park in New York City.");
        place.setLatitude(40.785091);
        place.setLongitude(-73.968285);
        place.setCreator("admin");
        place.setUserAccess(new ArrayList<>());
        place.setTags(List.of(new PlaceTag("Nature", "Public")));
        place.setUpdateTimestamp(LocalDateTime.now());

        List<Place> places = Collections.singletonList(place);

        when(placeService.findAll()).thenReturn(places);

        // Act
        mockMvc.perform(get("/basic-auth/api/places").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Central Park"))
                .andExpect(jsonPath("$[0].description").value("A large public park in New York City."))
                .andExpect(jsonPath("$[0].latitude").value(40.785091))
                .andExpect(jsonPath("$[0].longitude").value(-73.968285))
                .andExpect(jsonPath("$[0].creator").value("admin"));
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void getPlace_ReturnsPlaceDetails_WhenPlaceFound() throws Exception {
        // Arrange
        String placeId = "123";
        Place place = new Place();
                place.setId(placeId);
                place.setName("Central Park");
                place.setDescription("A large public park in New York City.");
                place.setLatitude(40.785091);
                place.setLongitude(-73.968285);
                place.setCreator("admin");
                place.setUserAccess(new ArrayList<>());
                place.setTags(List.of(new PlaceTag("Nature", "Public")));
                place.setUpdateTimestamp(LocalDateTime.now());

        when(placeService.findById(placeId)).thenReturn(Optional.of(place));

        // Act
        mockMvc.perform(get("/basic-auth/api/places/" + placeId).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(placeId))
                .andExpect(jsonPath("$.name").value("Central Park"))
                .andExpect(jsonPath("$.description").value("A large public park in New York City."))
                .andExpect(jsonPath("$.latitude").value(40.785091))
                .andExpect(jsonPath("$.longitude").value(-73.968285))
                .andExpect(jsonPath("$.creator").value("admin"));
    }

//    @Test
//    @WithMockUser(username="admin", roles="ADMIN")
//    public void getPlace_ReturnsNotFound_WhenPlaceDoesNotExist() {
//        String placeId = "nonExistentPlaceId";
//        Locale locale = Locale.ENGLISH;
//        when(placeService.findById(placeId)).thenReturn(Optional.empty());
//        try {
//            mockMvc.perform(get("/basic-auth/api/places/" + placeId).header(HttpHeaders.ACCEPT_LANGUAGE, locale.toLanguageTag()))
//                    .andExpect(status().isNotFound());
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void postPlace_CreatesNewPlace_Successfully() throws Exception {
        // Arrange
        PlaceDTO newPlaceDTO = PlaceDTO.builder()
                .name("New Place")
                .description("Description of the new place")
                .latitude(40.7128)
                .longitude(-74.0060)
                .userAccess(new ArrayList<>())
                .tags(Arrays.asList("Nature", "Public"))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        String newPlaceJson = objectMapper.writeValueAsString(newPlaceDTO);

        doNothing().when(placeService).save(any(Place.class));

        // Act & Assert
        mockMvc.perform(post("/basic-auth/api/places")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newPlaceJson))
                .andExpect(status().isCreated());
    }


//    @Test
//    @WithMockUser(username="admin", roles="ADMIN")
//    public void putPlace_UpdatesPlace_Successfully() throws Exception {
//        // Arrange
//        String placeId = "existingId";
//        Place existingPlace = new Place();
//        existingPlace.setId(placeId);
//        when(placeService.findById(placeId)).thenReturn(Optional.of(existingPlace));
//
//        PlaceDTO updatePlaceDTO = PlaceDTO.builder()
//                .name("Updated Park")
//                .description("An updated public park.")
//                .latitude(42.0)
//                .longitude(-75.0)
//                .creator("admin")
//                .build();
//        ObjectMapper objectMapper = new ObjectMapper();
//        String updatePlaceJson = objectMapper.writeValueAsString(updatePlaceDTO);
//
//        // Act & Assert
//        mockMvc.perform(put("/basic-auth/api/places/" + placeId)
//                        .with(csrf())
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(updatePlaceJson))
//                .andExpect(status().isOk());
//
//        verify(placeService, times(1)).save(any(Place.class));
//    }
//
//
//    @Test
//    @WithMockUser(username="admin", roles="ADMIN")
//    public void deletePlace_DeletesPlace_Successfully() throws Exception {
//        String placeId = "existingPlaceId";
//        when(placeService.findById(placeId)).thenReturn(Optional.of(new Place()));
//        doNothing().when(placeService).deleteById(placeId);
//
//        mockMvc.perform(delete("/basic-auth/api/places/" + placeId)
//                        .with(csrf()))
//                .andExpect(status().isOk());
//    }
}
