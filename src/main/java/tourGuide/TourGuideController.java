package tourGuide;

import java.util.*;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jsoniter.output.JsonStream;

import gpsUtil.location.VisitedLocation;
import tourGuide.distance.Distance;
import tourGuide.helper.InternalTestHelper;
import tourGuide.service.GpsUtilService;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tourGuide.user.User;
import tripPricer.Provider;

@RestController
public class TourGuideController {
    @Autowired
    TourGuideService tourGuideService;
    @Autowired
    GpsUtilService gpsUtilService;
    @Autowired
    UserService userService;

    @RequestMapping("/")
    public String index() {
        return "Greetings from TourGuide!";
    }

    @RequestMapping("/getLocation")
    public String getLocation(@RequestParam(name = "userName") String userName) {
        User user = userService.getUser(userName);
        VisitedLocation visitedLocation = userService.getUserLocation(user);
        return JsonStream.serialize(visitedLocation.location);
    }

    @RequestMapping("/getNearbyAttractions")
    public String getNearbyAttractions(@RequestParam(name ="userName") String userName) throws InterruptedException {
        User user = userService.getUser(userName);
        VisitedLocation visitedLocation= userService.getUserLocation(user);
        List<Distance> attractions = gpsUtilService.getNearByAttractions(visitedLocation);
        return JsonStream.serialize(attractions);
    }

    @RequestMapping("/getRewards")
    public String getRewards(@RequestParam String userName) throws InterruptedException {
        User user = userService.getUser(userName);
        return JsonStream.serialize(userService.getUserRewards(user));
    }

    @RequestMapping("/getAllCurrentLocations")
    public String getAllCurrentLocations() {
        return JsonStream.serialize(userService.getAllLocationsUser());
    }

    @RequestMapping("/getTripDeals")
    public String getTripDeals(@RequestParam String userName) throws InterruptedException {
        User user = userService.getUser(userName);
        List<Provider> providers = tourGuideService.getTripDeals(user);
        return JsonStream.serialize(providers);
    }
}