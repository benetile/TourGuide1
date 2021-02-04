package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class RewardsService{
    private static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;

	// proximity in miles
    private int defaultProximityBuffer = 10;
	private int proximityBuffer = defaultProximityBuffer;
	private int attractionProximityRange = 200;
	private  GpsUtil gpsUtil ;
	private RewardCentral rewardsCentral = new RewardCentral();
	private UserService userService;
	private int internalTestHelper =InternalTestHelper.getInternalUserNumber();

	private Logger logger = LoggerFactory.getLogger(RewardsService.class);
	//Create a CompletableFuture
	CompletableFuture<User> completableFuture = new CompletableFuture<>();

	public RewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}


	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}
	
	public void setDefaultProximityBuffer() {
		proximityBuffer = defaultProximityBuffer;
	}
	
	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction> attractions = gpsUtil.getAttractions();

		for(int i=0;i<userLocations.size();i++){
			VisitedLocation visitedLocation = userLocations.get(i);
			for(Attraction attraction : attractions) {
				if(user.getUserRewards().stream().filter(r -> r.attraction.attractionName.equals(attraction.attractionName)).count() == 0) {
					if(nearAttraction(visitedLocation, attraction)) {
							user.addUserReward(new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}
	}

	public void myThreadEssaie(User user) throws InterruptedException {
		int cpu = Runtime.getRuntime().availableProcessors();
		ExecutorService poolExecutor = Executors.newFixedThreadPool(cpu);
		Runnable runTask = () -> {
				calculateRewards(user);
				//sleep(100);
		};
		poolExecutor.submit(runTask);
		poolExecutor.shutdown();
	}

	public void  asynchroCalculateRewardsTest(User user) throws InterruptedException {
		ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
		for (int i=0;i<internalTestHelper;i++) {
			myThreadEssaie(user);
			}
			sleep(100);
	}
	public void asynchroCalculateRewards(List<User> users){
		users.parallelStream().forEach(user -> calculateRewards(user));

		ExecutorService poolExecutorService = Executors.newFixedThreadPool(5);
		logger.info("test du runnable de " + users.size() + " users.");
		poolExecutorService.execute(()->{
			for (User user : users){
				try {
					myThreadEssaie(user);
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void sleep(int time){
		try {
			Thread.sleep(time);
		}catch (InterruptedException e){
			e.printStackTrace();
		}
	}
	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {

		return getDistance(attraction, location) > attractionProximityRange ? false : true;
	}
	
	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return getDistance(attraction, visitedLocation.location) > proximityBuffer ? false : true;
	}
	
	public int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}
	
	public double getDistance(Location loc1, Location loc2) {
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
