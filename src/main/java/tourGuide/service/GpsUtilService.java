package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.distance.Distance;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class GpsUtilService{

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    ExecutorService executorService = Executors.newFixedThreadPool(100);
    private GpsUtil gpsUtil = new GpsUtil();

    private RewardCentral rewardCentral = new RewardCentral();
    private RewardsService rewardsService ;//= new RewardsService(gpsUtil,rewardCentral);
    private UserService userService = new UserService(gpsUtil,rewardsService);

    List<Attraction> attractions = new ArrayList<>();

    public GpsUtilService( GpsUtil gpsUtil,RewardsService rewardsService) {
        this.rewardsService = rewardsService;
        this.gpsUtil = gpsUtil;
    }

    public Attraction addAttraction(Attraction attraction){
        if(attraction!=null){
            attractions.add(attraction);
            return attraction;
        }
        return null;
    }

    public List<Attraction>getAttractions(){
        for(Attraction attraction : gpsUtil.getAttractions()){
            attractions.add(attraction);
        }
        return attractions;
    }

    public Attraction getAttractionByName(String name){
        for (Attraction attraction : gpsUtil.getAttractions()) {
            if(attraction.attractionName.equals(name)){
                return attraction;
            }
        }
        return null;
    }

    public List<Distance> getNearByAttractions(VisitedLocation visitedLocation){
        List<Distance> nearByAttractions = new ArrayList<>();
        List<Attraction>attractions = gpsUtil.getAttractions();
        User user =userService.getAllUsers().get(0);

        attractions.parallelStream().forEach(attraction -> {
            double distance = calculDistance(new Location(attraction.latitude, attraction.longitude), visitedLocation.location);
            int point = rewardsService.getRewardPoints(attraction, user);

            user.addDistance(new Distance(attraction.attractionName, attraction.latitude, attraction.longitude,
                    visitedLocation.location.latitude,visitedLocation.location.longitude, distance,point));
        });
        Collections.sort(user.getDistances(), Distance.distanceComparator);
        return user.getDistances().subList(0,5);
    }

    public Double calculDistance(Location loc1, Location loc2){
        double lat1 = Math.toRadians(loc1.latitude);
        double lon1 = Math.toRadians(loc1.longitude);
        double lat2 = Math.toRadians(loc2.latitude);
        double lon2 = Math.toRadians(loc2.longitude);

        double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

        double nauticalMiles = 60 * Math.toDegrees(angle);
        double statuteMiles = STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
        return statuteMiles;
    }
}
