package tourGuide;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tourGuide.service.GpsUtilService;
import tourGuide.service.TourGuideService;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
public class TourGuideControllerTest {

    private User user;
    private UserPreferences userPreferences;

    @Autowired
    MockMvc mockMvc;

    @Autowired
    WebApplicationContext wac;

    @Autowired
    GpsUtilService gpsUtilService;

    @Autowired
    TourGuideService tourGuideService;


    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }


    @Test
    public void getAccueil() throws Exception {
        // Arrange & Act
        mockMvc.perform(get("/"))
                // Assert
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void getAllLocation() throws Exception {

        // Arrange & Act
        mockMvc.perform(get("/getAllCurrentLocations"))
                // Assert
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void getNearbyAttractions() throws Exception {

        // Arrange & Act
        mockMvc.perform(post("/getNearbyAttractions?userName=internalUser1"))
                // Assert
                .andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

    }
}
