package uca.hermes.api;

import uca.hermes.api.controller.TagController;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(controllers = TagController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class TimeSeriesAPITests {
    @Autowired
    private MockMvc mockMvc;
    /*
    @MockBean
    private TagService service;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private TimeSeries timeSeries_Main;
    private List<TimeSeries> timeSeriesList_GET;

    private final String API_URL = "/app/api/timeseries";

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @BeforeEach
    public void init() {
        List<UserTagAccess> list = new ArrayList<>();
        list.add(new UserTagAccess("readonly", "1", false));
        list.add(new UserTagAccess("canedit", "1", true));
        timeSeries_Main = new TimeSeries("1", "Test1", "Description1", "admin", list, LocalDateTime.now());
        timeSeriesList_GET = new ArrayList<TimeSeries>();
        timeSeriesList_GET.add(new TimeSeries("1", "Test1", "Description1", "canedit", new ArrayList<>(), LocalDateTime.now()));
        list = new ArrayList<>();
        list.add(new UserTagAccess("canedit", "2", true));
        timeSeriesList_GET.add(new TimeSeries("2", "Test2", "Description2", "admin", list, LocalDateTime.now()));
        list = new ArrayList<>();
        list.add(new UserTagAccess("readonly", "3", false));
        timeSeriesList_GET.add(new TimeSeries("3", "Test3", "Description3", "admin", list, LocalDateTime.now()));
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetAllTimeSeriesAdmin() throws Exception {
        when(service.findAll()).thenReturn(timeSeriesList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(3)));
    }

    @Test
    @WithMockUser(username="canedit", roles="USER")
    void TestGetAllTimeSeriesCadEdit() throws Exception {
        when(service.findAll()).thenReturn(timeSeriesList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(2)));
    }

    @Test
    @WithMockUser(username="readonly", roles="USER")
    void TestGetAllTimeSeriesReadOnly() throws Exception {
        when(service.findAll()).thenReturn(timeSeriesList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL)
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()", CoreMatchers.is(1)));
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetTimeSeries() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        ResultActions resp = mockMvc.perform(get(API_URL + "/" + timeSeries_Main.getId())
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name", CoreMatchers.is(timeSeries_Main.getName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.description", CoreMatchers.is(timeSeries_Main.getDescription())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.creator", CoreMatchers.is(timeSeries_Main.getCreator())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userAccess[0].username", CoreMatchers.is(timeSeries_Main.getUserAccess().get(0).getUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userAccess[0].canEdit", CoreMatchers.is(timeSeries_Main.getUserAccess().get(0).getCanEdit())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userAccess[1].username", CoreMatchers.is(timeSeries_Main.getUserAccess().get(1).getUsername())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userAccess[1].canEdit", CoreMatchers.is(timeSeries_Main.getUserAccess().get(1).getCanEdit())));
    }

    @Test
    @WithMockUser(username="readonly1", roles="USER")
    void TestGetTimeSeriesForbidden() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        ResultActions resp = mockMvc.perform(get(API_URL + "/" + timeSeries_Main.getId())
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetTimeSeriesNotFound() throws Exception {
        when(service.findAll()).thenReturn(timeSeriesList_GET);

        ResultActions resp = mockMvc.perform(get(API_URL + "/notfound")
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestGetTimeSeriesNotModified() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        ResultActions resp = mockMvc.perform(get(API_URL + "/" + timeSeries_Main.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .header("If-Modified-Since", "Fri, 31 Dec 2123 00:00:00 GMT"));

        resp.andExpect(status().isNotModified());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPostTimeSeries() throws Exception {
        timeSeries_Main.setId(null);

        ResultActions resp = mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeSeries_Main)));

        resp.andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPostTimeSeriesWithId() throws Exception {
        ResultActions resp = mockMvc.perform(post(API_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeSeries_Main)));

        resp.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPutTimeSeries() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        timeSeries_Main.setId(null);
        timeSeries_Main.setCreator(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeSeries_Main)));

        resp.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPutTimeSeriesWithId() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        timeSeries_Main.setCreator(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeSeries_Main)));

        resp.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPutTimeSeriesWithCreator() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        timeSeries_Main.setId(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeSeries_Main)));

        resp.andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    public void TestPutTimeSeriesNotFound() throws Exception {
        when(service.findAll()).thenReturn(timeSeriesList_GET);

        timeSeries_Main.setId(null);
        timeSeries_Main.setCreator(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/notfound")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeSeries_Main)));

        resp.andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username="readonly", roles="USER")
    public void TestPutTimeSeriesForbidden() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        timeSeries_Main.setId(null);
        timeSeries_Main.setCreator(null);

        ResultActions resp = mockMvc.perform(put(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(timeSeries_Main)));

        resp.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestDeleteTimeSeries() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        ResultActions resp = mockMvc.perform(delete(API_URL + "/" + timeSeries_Main.getId())
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="readonly1", roles="USER")
    void TestDeleteTimeSeriesForbidden() throws Exception {
        when(service.findById(timeSeries_Main.getId())).thenReturn(Optional.of(timeSeries_Main));

        ResultActions resp = mockMvc.perform(delete(API_URL + "/1")
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username="admin", roles="ADMIN")
    void TestDeleteTimeSeriesNotFound() throws Exception {
        when(service.findAll()).thenReturn(timeSeriesList_GET);

        ResultActions resp = mockMvc.perform(delete(API_URL + "/notfound")
                .contentType(MediaType.APPLICATION_JSON));

        resp.andExpect(status().isOk());
    }
    */
}