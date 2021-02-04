package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.distance.Distance;
import tourGuide.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GpsUtilService{

    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

    private GpsUtil gpsUtil = new GpsUtil();

    private RewardCentral rewardCentral = new RewardCentral();
    private RewardsService rewardsService = new RewardsService(gpsUtil,rewardCentral);
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

    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        List<Attraction> nearbyAttractions = new ArrayList<>();
        for(Attraction attraction : gpsUtil.getAttractions()) {
            if(rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
                nearbyAttractions.add(attraction);
            }
        }
        return nearbyAttractions;
    }

    //
    public List<Distance> getNearByAttractionsTest(VisitedLocation visitedLocation){
        List<Distance> nearByAttractions = new ArrayList<>();
        List<Attraction>attractions = gpsUtil.getAttractions();

            attractions.parallelStream().forEach(attraction1 -> {
               double distance = calculDistance(new Location(attraction1.latitude, attraction1.longitude), visitedLocation.location);
                int point = rewardCentral.getAttractionRewardPoints(attraction1.attractionId,visitedLocation.userId);
                nearByAttractions.add(new Distance(attraction1.attractionName, attraction1.latitude, attraction1.longitude,
                        visitedLocation.location.latitude,visitedLocation.location.longitude, distance,point));
            });
        Collections.sort(nearByAttractions, Distance.distanceComparator);
        return nearByAttractions.subList(0,5);
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
