package uca.hermes.api;

import uca.hermes.api.controller.CollectionController;
import uca.hermes.api.dao.Place;
import uca.hermes.api.dao.PlaceTag;
import uca.hermes.api.dao.Tag;
import uca.hermes.api.service.PlaceService;
import uca.hermes.api.service.TagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CollectionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class CollectionsAPITests {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TagService tagService;

    @MockBean
    private PlaceService placeService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private List<Tag> tagList;

    private Place place1;
    private Place place2;

    private final String API_URL = "/basic-auth/api/collections";

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    public void init() {
        tagList = new ArrayList<>();
        tagList.add(new Tag("tag1", new ArrayList<>(), "admin", LocalDateTime.now()));
        tagList.add(new Tag("tag2", new ArrayList<>(), "admin", LocalDateTime.now()));
        tagList.add(new Tag("tag3", new ArrayList<>(), "admin", LocalDateTime.now()));
        List<PlaceTag> placeTags = new ArrayList<>();
        placeTags.add(new PlaceTag("tag1", "1"));
        placeTags.add(new PlaceTag("tag2", "1"));
        place1 = new Place("1", "test 1", "description",
                10.0, 15.0, "admin", new ArrayList<>(),
                placeTags, LocalDateTime.now());
        placeTags = new ArrayList<>();
        placeTags.add(new PlaceTag("tag2", "2"));
        placeTags.add(new PlaceTag("tag3", "2"));
        place2 = new Place("2", "test 2", "description",
                10.0, 15.0, "admin", new ArrayList<>(),
                placeTags, LocalDateTime.now());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetAllCollections() throws Exception {
        when(tagService.findAll()).thenReturn(tagList);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(3)));
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetCollection() throws Exception {
        when(tagService.findAll()).thenReturn(tagList);
        when(tagService.findById(tagList.get(0).getTagName())).thenReturn(Optional.of(tagList.get(0)));
        when(tagService.findById(tagList.get(1).getTagName())).thenReturn(Optional.of(tagList.get(1)));
        when(tagService.findById(tagList.get(2).getTagName())).thenReturn(Optional.of(tagList.get(2)));
        when(placeService.findById("1")).thenReturn(Optional.of(place1));
        when(placeService.findById("2")).thenReturn(Optional.of(place2));
        List<Place> list = new ArrayList<>();
        list.add(place1);
        when(placeService.findByTagName("tag1")).thenReturn(list);
        list = new ArrayList<>();
        list.add(place1);
        list.add(place2);
        when(placeService.findByTagName("tag2")).thenReturn(list);

        ResultActions resp = mockMvc.perform(get(API_URL + "/tag1")
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.places.size()", CoreMatchers.is(1)));

        resp = mockMvc.perform(get(API_URL + "/tag2")
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.places.size()", CoreMatchers.is(2)));
    }
}