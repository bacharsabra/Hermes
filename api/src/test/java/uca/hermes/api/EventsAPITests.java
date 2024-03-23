package uca.hermes.api;

import uca.hermes.api.controller.PlaceController;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(controllers = PlaceController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class EventsAPITests {
    @Autowired
    private MockMvc mockMvc;
    /*
    @MockBean
    private PlaceService service;

    @MockBean
    private TagService tagService;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private TimeSeries timeSeries_Main;
    private Place place_Main;
    private List<Place> placeList_GET;

    private final String API_URL = "/app/api/timeseries/1/events";

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    public void init() {
        List<Tag> list = new ArrayList<>();
        list.add(new Tag("tag 1", "1"));
        list.add(new Tag("tag 2", "1"));
        place_Main = new Place("1", "1", LocalDateTime.now(), "1", "Comment1", list, LocalDateTime.now());
        placeList_GET = new ArrayList<Place>();
        placeList_GET.add(new Place("1", "1", LocalDateTime.now(), "1", "Comment1", list, LocalDateTime.now()));
        list = new ArrayList<>();
        list.add(new Tag("tag 1", "2"));
        list.add(new Tag("tag 2", "2"));
        placeList_GET.add(new Place("2", "1", LocalDateTime.now(), "1", "Comment2", list, LocalDateTime.now()));
        list = new ArrayList<>();
        list.add(new Tag("tag 3", "3"));
        list.add(new Tag("tag 4", "3"));
        placeList_GET.add(new Place("3", "1", LocalDateTime.now(), "1", "Comment2", list, LocalDateTime.now()));

        List<UserTagAccess> accessList = new ArrayList<>();
        accessList.add(new UserTagAccess("readonly", "1", false));
        accessList.add(new UserTagAccess("canedit", "1", true));
        timeSeries_Main = new TimeSeries("1", "Test1", "Description1", "admin", accessList, LocalDateTime.now());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetAllEvents() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findByTimeSeriesId("1")).thenReturn(placeList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(3)));
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetAllEventsByTag() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findByTimeSeriesId("1")).thenReturn(placeList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON).param("tag", "tag 3"));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    @WithMockUser(username="user1", roles="USER")
    void TestGetAllEventsForbidden() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findByTimeSeriesId("1")).thenReturn(placeList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetAllEventsNotFound() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findByTimeSeriesId("1")).thenReturn(placeList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL.replace('1', '2'))
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetAllEventsNotModified() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findByTimeSeriesId("1")).thenReturn(placeList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .header("If-Modified-Since", "Fri, 31 Dec 2123 00:00:00 GMT"));

        resp.andExpect(status().isNotModified());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetEvent() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));

        ResultActions resp = mockMvc.perform(get(API_URL + "/" + place_Main.getId())
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.date", CoreMatchers.is(objectMapper.writeValueAsString(place_Main.getDate()).substring(1, 28))))
                .andExpect(MockMvcResultMatchers.jsonPath("$.comment", CoreMatchers.is(place_Main.getComment())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tags[0]", CoreMatchers.is(place_Main.getTags().get(0).getTagName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.tags[1]", CoreMatchers.is(place_Main.getTags().get(1).getTagName())));
    }

    @Test
    @WithMockUser(username="user1", roles="USER")
    void TestGetEventForbidden() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));

        ResultActions resp = mockMvc.perform(get(API_URL + "/" + place_Main.getId())
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetEventNotFound() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findAll()).thenReturn(placeList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL + "/notfound")
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetEventNotModified() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));

        ResultActions resp = mockMvc.perform(get(API_URL + "/" + place_Main.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("If-Modified-Since", "Fri, 31 Dec 2123 00:00:00 GMT"));

        resp.andExpect(status().isNotModified());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPostEvent() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        place_Main.setId(null);
        place_Main.setTimeSeriesId(null);

        ResultActions resp = mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPostEventWithId() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        place_Main.setTimeSeriesId(null);

        ResultActions resp = mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPostEventWithTimeSeriesId() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        place_Main.setId(null);

        ResultActions resp = mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="readonly", roles="USER")
    public void TestPostEventForbidden() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        place_Main.setId(null);
        place_Main.setTimeSeriesId(null);

        ResultActions resp = mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPostEventNotFound() throws Exception {
        when(service.findAll()).thenReturn(placeList_GET);

        place_Main.setId(null);
        place_Main.setTimeSeriesId(null);

        ResultActions resp = mockMvc.perform(post(API_URL.replace('1', '2'))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPutEvent() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));
        when(service.existsByTimeSeriesIdAndId(place_Main.getTimeSeriesId(), place_Main.getId())).thenReturn(true);

        place_Main.setId(null);
        place_Main.setTimeSeriesId(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPutEventWithId() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));
        when(service.existsByTimeSeriesIdAndId(place_Main.getTimeSeriesId(), place_Main.getId())).thenReturn(true);

        place_Main.setTimeSeriesId(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPutEventWithTimeSeriesId() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));
        when(service.existsByTimeSeriesIdAndId(place_Main.getTimeSeriesId(), place_Main.getId())).thenReturn(true);

        place_Main.setId(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPutEventNotFound() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findAll()).thenReturn(placeList_GET);

        place_Main.setId(null);
        place_Main.setTimeSeriesId(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/notfound")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="readonly", roles="USER")
    public void TestPutEventForbidden() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));
        when(service.existsByTimeSeriesIdAndId(place_Main.getTimeSeriesId(), place_Main.getId())).thenReturn(true);

        place_Main.setId(null);
        place_Main.setTimeSeriesId(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(place_Main.toDTO())));

        resp.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestDeleteEvent() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));
        when(service.existsByTimeSeriesIdAndId(place_Main.getTimeSeriesId(), place_Main.getId())).thenReturn(true);

        ResultActions resp = mockMvc.perform(delete(API_URL + "/" + place_Main.getId())
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="readonly", roles="USER")
    void TestDeleteEventForbidden() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findById(place_Main.getId())).thenReturn(Optional.of(place_Main));
        when(service.existsByTimeSeriesIdAndId(place_Main.getTimeSeriesId(), place_Main.getId())).thenReturn(true);

        ResultActions resp = mockMvc.perform(delete(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestDeleteEventNotFound() throws Exception {
        when(tagService.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));
        when(service.findAll()).thenReturn(placeList_GET);

        ResultActions resp = mockMvc.perform(delete(API_URL + "/notfound")
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk());
    }
    */
}