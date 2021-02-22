package tourGuide.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserPreferences;
import tourGuide.user.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

@Service
public class TourGuideService {
	private Logger logger = LoggerFactory.getLogger(TourGuideService.class);
	private final GpsUtil gpsUtil;
	private final RewardsService rewardsService;
	private final TripPricer tripPricer = new TripPricer();
	private UserService userService = new UserService(new GpsUtil(),new RewardsService(new GpsUtil(), new RewardCentral()));

	public TourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
		this.gpsUtil = gpsUtil;
		this.rewardsService = rewardsService;

	}

	public List<Provider> getTripDeals(User user) throws InterruptedException {
		List<UserReward> userReward = userService.getUserRewards(user);
		int cumulatativeRewardPoints = userReward.stream().mapToInt(i->i.getRewardPoints()).sum();
		UserPreferences userPreference = user.getUserPreferences();
		List<Provider> providers = new ArrayList<>();
		for (Attraction attraction : gpsUtil.getAttractions()){
			String uuid = String.valueOf(attraction.attractionId);
			providers = tripPricer.getPrice(tripPricerApiKey,UUID.fromString(uuid),userPreference.getNumberOfAdults(), userPreference.getNumberOfChildren(),
					userPreference.getTripDuration(),cumulatativeRewardPoints);
			user.setTripDeals(providers);

			System.out.println(cumulatativeRewardPoints);
		}
		return providers;
	}
	/**********************************************************************************
	 *
	 * Methods Below: For Internal Testing
	 *
	 **********************************************************************************/
	private static final String tripPricerApiKey = "test-server-api-key";
	// Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
	private final Map<String, User> internalUserMap = new HashMap<>();

}
