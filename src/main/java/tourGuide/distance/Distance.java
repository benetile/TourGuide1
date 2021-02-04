package tourGuide.distance;

import gpsUtil.location.Location;
import tourGuide.user.UserReward;

import java.util.Comparator;

public class Distance extends Location {
    String attractionName;
    double latitudeUser;
    double longituteUser;
    double distance;
    int pointReward;

    public Distance( String attractionName,double latitude, double longitude, double latitudeUser, double longituteUser, double distance,int pointReward) {
        super(latitude, longitude);
        this.attractionName = attractionName;
        this.latitudeUser = latitudeUser;
        this.longituteUser = longituteUser;
        this.distance = distance;
        this.pointReward = pointReward;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public double getLatitudeUser() {
        return latitudeUser;
    }

    public void setLatitudeUser(double latitudeUser) {
        this.latitudeUser = latitudeUser;
    }

    public double getLongituteUser() {
        return longituteUser;
    }

    public void setLongituteUser(double longituteUser) {
        this.longituteUser = longituteUser;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
    public int getPointReward() {
        return pointReward;
    }

    public void setPointReward(int pointReward) {
        this.pointReward = pointReward;
    }

    public static Comparator<Distance> distanceComparator = new Comparator<Distance>() {
        @Override
        public int compare(Distance o1, Distance o2) {
            return (int) (o1.getDistance() - o2.getDistance());
        }
    };

}
