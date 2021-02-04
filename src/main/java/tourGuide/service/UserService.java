package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tourGuide.helper.InternalTestHelper;
import tourGuide.tracker.Tracker;
import tourGuide.user.User;
import tourGuide.user.UserReward;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class UserService{

    private Logger logger = LoggerFactory.getLogger(UserService.class);
    public GpsUtil gpsUtil;
    public RewardsService rewardsService;
    boolean testMode = true;
    public final Tracker tracker;

    ExecutorService executorService = Executors.newFixedThreadPool(4);
    UUID userId;
    Location location1;
    Date timeVisited1;

    public UserService(GpsUtil gpsUtil, RewardsService rewardsService){
        this.gpsUtil = gpsUtil;
        this.rewardsService = rewardsService;
        if(testMode) {
            logger.info("TestMode enabled");
            logger.debug("Initializing users");
            initializeInternalUsers();
            logger.debug("Finished initializing users");
        }

        tracker = new Tracker(this);
        addShutDownHook();
    }

    private List<VisitedLocation> visitedLocations = new ArrayList<>();
    private final Map<String, User> internalUserMap = new HashMap<>();

    public void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone = "000";
            String email = userName + "@tourGuide.com";
            User user = new User(UUID.randomUUID(), userName, phone, email);
            generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        logger.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }
    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                tracker.stopTracking();
            }
        });
    }

    public VisitedLocation trackUserLocation(User user) {
        //generateUserLocationHistory(user);
        VisitedLocation visitedLocation = getUserLocation(user);
        //user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }


    public VisitedLocation getUserLocation(User user) {
        return user.getLastVisitedLocation();
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public User getUserById(UUID idUser){
        return internalUserMap.get(idUser);
    }

    public List<User> getAllUsers() {
        return internalUserMap.values().stream().collect(Collectors.toList());
    }

    public void addUser(User user) {
        if(!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }
    public List<Map<String,Location>> getAllLocationsUser(){
        List<VisitedLocation> currentLocations = new ArrayList<>();
        List<Map<String,Location>> testCurrentList = new ArrayList<>();
        Map<String,Location> maptest = new HashMap<>();
        VisitedLocation firstVisiteLocation = new VisitedLocation(userId,location1,timeVisited1);
        VisitedLocation lastVisiteLocation = new VisitedLocation(userId,location1,timeVisited1);
        for (User user : getAllUsers()) {
            for (VisitedLocation visitedLocation: user.getVisitedLocations()) {
                    firstVisiteLocation = visitedLocation;
                    for(VisitedLocation visitedLocation1 : user.getVisitedLocations()){
                        if(firstVisiteLocation.timeVisited.after(visitedLocation1.timeVisited)){
                            lastVisiteLocation = firstVisiteLocation;
                        }
                    }
            }
            maptest.put(String.valueOf(lastVisiteLocation.userId),lastVisiteLocation.location);
            currentLocations.add(lastVisiteLocation);
        }
        testCurrentList.add(maptest);
        return testCurrentList;
    }
    public void generateUserLocationHistory(User user) {
        IntStream.range(0, 3).forEach(i-> {
            user.addToVisitedLocations(new VisitedLocation(user.getUserId(), new Location(generateRandomLatitude(), generateRandomLongitude()), getRandomTime()));
        });

    }
    public double generateRandomLongitude() {
        double leftLimit = -180;
        double rightLimit = 180;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    public double generateRandomLatitude() {
        double leftLimit = -85.05112878;
        double rightLimit = 85.05112878;
        return leftLimit + new Random().nextDouble() * (rightLimit - leftLimit);
    }

    public Date getRandomTime() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(new Random().nextInt(30));
        return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * A revoir
     */

    public void asynchroTrackUserLocation(List<User> users){
        users.parallelStream()
                .forEach(user -> trackUserLocation(user));
    }

    public void asynchroExecutor() throws InterruptedException {
        executorService.shutdown();
        executorService.awaitTermination(1,TimeUnit.SECONDS);
    }

    Runnable task =()->{
        List <User> users = getAllUsers();
        for (User user : users) {
            trackUserLocation(user);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("Thread  interrupted.");
            }
        }
    };




}
