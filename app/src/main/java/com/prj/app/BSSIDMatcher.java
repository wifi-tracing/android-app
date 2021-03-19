package com.prj.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Pair;
import android.widget.TextView;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

@SuppressWarnings("FieldCanBeLocal")
@SuppressLint({"UseSwitchCompatOrMaterialCode", "SetTextI18n", "SimpleDateFormat"})

public class BSSIDMatcher {
    private final DatabaseManager databaseManager;
    private final TextView resultTextView;
    private final Context context;
    private double MAX_TIME_DIFFERENCE = 35; //in seconds
    private double MAX_DISTANCE_DIFFERENCE = 2; //in meters
    private double MIN_NUMBER_OF_NEAR_HOTSPOTS = 4; //number of hotspots needed to correctly triangulate referential position
    private double MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS = 4; //number of consecutive timestamps needed to confirm contact

    /**
     * Contains functions to fetch and match positive scans with locally stored data
     *
     * @param databaseManager a databaseManager object
     * @param resultTextView  a TextView object to modify to show user progress and results
     * @param context         application context
     */
    public BSSIDMatcher(DatabaseManager databaseManager, TextView resultTextView, Context context) {
        this.databaseManager = databaseManager;
        this.resultTextView = resultTextView;
        this.context = context;
    }

    /**
     * Get matching BSSIDs and show progress to the global TextView
     */
    public void getMatchingBSSIDs() {
        List<String> results = databaseManager.getScanBSSIDs();
        JSONObject jsonBody = new JSONObject();

        try {
            jsonBody.put("BSSIDs", new JSONArray(results));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (resultTextView != null) {
            resultTextView.setText("Checking matching BSSIDs");
        }
        String URL = VolleySingleton.API_URL + "scans/get/matchBSSID";
        sendMatchingBSSIDsPOST(URL, jsonBody);
    }

    /**
     * Send POST request to API to fetch scans containing the same BSSID as the ones locally stored
     *
     * @param URL
     * @param jsonBody
     */
    private void sendMatchingBSSIDsPOST(String URL, JSONObject jsonBody) {
        try {
            CustomJsonArrayRequest customJsonArrayRequest = new CustomJsonArrayRequest(Request.Method.POST, URL, jsonBody, this::matchResult, Throwable::printStackTrace);
            VolleySingleton.getInstance(context).getRequestQueue().add(customJsonArrayRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Match given JSONArray of scan data with local storage
     *
     * @param matchingScans a JSONArray of remote postive scans;
     */
    private void matchResult(JSONArray matchingScans) {
        List<Scan> localScans = databaseManager.getScanData();
        List<Scan> remoteScans = null;
        try {
            remoteScans = parseScans(matchingScans);
        } catch (JSONException jsonException) {
            jsonException.printStackTrace();

            if (resultTextView != null) {
                resultTextView.setText(
                        "There was an error. Could not parse incoming JSON payload correctly.");
            }
        }
        localScans = removeUnusedLocalScans(localScans, remoteScans);
        //sort by timestamp
        localScans.sort(Comparator.comparing(Scan::getTimestamp));
        localScans = removeNonConcurrentScans(localScans); //filter out scans that don't have at least 2 mins of the same bssid
        remoteScans.sort(Comparator.comparing(Scan::getTimestamp));
        //todo: change removeNonConcurrentScans to keep sorted Scans
        localScans.sort(Comparator.comparing(Scan::getTimestamp));

        Map<Scan, List<Scan>> map = getScanMap(localScans, remoteScans);
        TreeMap<Date, List<Scan>> timeMap = getDateMap(map);
        List<Pair<Date, List<Scan>>> positiveResultDates = getFirstPositiveResult(timeMap);

        if (positiveResultDates.size() != 0) {

            StringBuilder out = new StringBuilder();

            for (Pair<Date, List<Scan>> scan : positiveResultDates) {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                out.append("\n\n Found group at ").append(formatter.format(scan.second.get(0).getTimestamp()));
                for (Scan hotspot : scan.second) {
                    out.append("\n").append(hotspot.toString());
                }
            }
            if (resultTextView != null) {

                resultTextView.setText(
                        "Found a match! " + positiveResultDates.size() + "/" + MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS +
                                " consecutive scans of " + MIN_NUMBER_OF_NEAR_HOTSPOTS + " or more matching hotspots.\n\n" + out.toString());
            }

            NotificationManager.showExposureNotification(context);

        } else {
            if (resultTextView != null) {

                resultTextView.setText(
                        "Cross referenced " + remoteScans.size() + " scans.\n" +
                                "No matches found\n");
            }
        }

    }


    /**
     * Return an array of Date objects corresponding to timestamps of concurrent positive results, if there
     * are at least x subsequent ones
     *
     * @param timeMap the timeMap as returned by the getDateMap method
     * @return an array of Date objects, if there are positive contacts, empty if there are none
     */
    private List<Pair<Date, List<Scan>>> getFirstPositiveResult(TreeMap<Date, List<Scan>> timeMap) {
        List<Pair<Date, List<Scan>>> dateStorage = new ArrayList<>();

        for (Date timestamp : timeMap.keySet()) {
            if (dateStorage.size() == 0) {
                dateStorage.add(new Pair(timestamp, timeMap.get(timestamp)));
            } else {
                Date lastTimestamp = dateStorage.get(dateStorage.size() - 1).first;
                double timeDiff = Math.abs(lastTimestamp.getTime() - timestamp.getTime()) / 1000.0;
                if (timeDiff <= MAX_TIME_DIFFERENCE) {
                    dateStorage.add(new Pair(timestamp, timeMap.get(timestamp)));
                    if (dateStorage.size() >= MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS) {
                        return dateStorage;
                    }
                } else {
                    dateStorage.clear();
                    dateStorage.add(new Pair(timestamp, timeMap.get(timestamp)));
                }
            }
        }

        if (dateStorage.size() <= MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS) {
            dateStorage.clear();
        }
        return dateStorage;
    }

    /**
     * Get map of local scan timestamps, and the scans with that timestamp which contain a value equal
     * or higher of MINIMUM_NUMBER_OF_NEAR_HOTSPOTS.
     *
     * @param map the scan map as returned by getScanMap
     * @return the time map containing timestamps and the number of scan results in it which have positive contacts
     */
    private TreeMap<Date, List<Scan>> getDateMap(Map<Scan, List<Scan>> map) {
        Map<Date, List<Scan>> tempTimeMap = new HashMap<>(); //temporary time map to keep count  of scan results and positive values
        TreeMap<Date, List<Scan>> timeMap = new TreeMap<>();

        // for every local scan, identified by same timestamps, group all scan results that have at least one matching remote scan result
        for (Scan scan : map.keySet()) {
            if (map.get(scan) != null && Objects.requireNonNull(map.get(scan)).size() > 0) {
                //valid
                if (tempTimeMap.containsKey(scan.getTimestamp())) {
                    List<Scan> list = tempTimeMap.get(scan.getTimestamp());
                    if (list != null) {
                        list.add(scan);
                        if (list.size() >= MIN_NUMBER_OF_NEAR_HOTSPOTS) {
                            timeMap.put(scan.getTimestamp(), list);
                        }
                    } else {
                        list = new ArrayList<>();
                        list.add(scan);
                        tempTimeMap.put(scan.getTimestamp(), list);
                    }
                } else {
                    ArrayList<Scan> list = new ArrayList<>();
                    list.add(scan);
                    tempTimeMap.put(scan.getTimestamp(), list);
                }
            }
        }
        return timeMap;
    }


    /**
     * Create a map of scans that have matching remote counterparts. Keys are the local scans, values are lists
     * of remote scans that are within distance and time boundaries to be considered contacts
     *
     * @param localScans  a list of Scan objects which contains the local phone scans
     * @param remoteScans a list of Scan objects which contains the positive scans fetched from the server
     * @return local scans mapped to an array of remote scans that are within time and distance boundaries for contact
     */
    private Map<Scan, List<Scan>> getScanMap(List<Scan> localScans, List<Scan> remoteScans) {

        Map<Scan, List<Scan>> map = new HashMap<>();

        //map of scan result -> remote scan result that is near at a similar timestamp
        for (Scan localScan : localScans) {
            for (Scan remoteScan : remoteScans) {
                if (remoteScan.getBssid().equals(localScan.getBssid())) {
                    double differenceInSeconds = scanTimeDifference(remoteScan, localScan);
                    if (differenceInSeconds <= MAX_TIME_DIFFERENCE) {
                        double differenceInMeters = Math.abs(remoteScan.getDistance() - localScan.getDistance());
                        if (differenceInMeters <= MAX_DISTANCE_DIFFERENCE) {
                            if (map.containsKey(localScan)) {
                                map.get(localScan).add(remoteScan);
                            } else {
                                ArrayList<Scan> container = new ArrayList<>();
                                container.add(remoteScan);
                                map.put(localScan, container);
                            }
                        }
                    }
                }
            }
        }
        return map;
    }

    /**
     * Remove scans that are not considered useful for contact tracing. Specifically, remove scan objects
     * if they cannot be grouped in groups of 4 scans of 35 seconds each.
     * This methods assumes that the localScans parameter is already sorted by ascending timestamp, and that
     * scans have been registered with a gap of about 30/35 seconds
     *
     * @param localScans the array of Scan objects to filter
     * @return the filtered array of Scan objects
     */
    private List<Scan> removeNonConcurrentScans(List<Scan> localScans) {
        ArrayList<Scan> validScans = new ArrayList<>();
        Map<String, ArrayList<Scan>> storage = new HashMap<>();
        // we assume sorted by timestamp ascending
        for (int i = 0; i < localScans.size(); i++) {
            Scan current = localScans.get(i);
            if (storage.containsKey(current.getBssid())) {
                ArrayList<Scan> storageList = storage.get(current.getBssid());
                if (storageList != null) {
                    if (storageList.size() > 0) {
                        //get last element found for this bssid
                        Scan lastScan = storageList.get(storageList.size() - 1);

                        if (scanTimeDifference(lastScan, current) <= MAX_TIME_DIFFERENCE) {
                            storageList.add(current);
                        } else {
                            if (storageList.size() >= MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS) {
                                validScans.addAll(storageList);
                            }
                            storageList.clear();
                        }
                    } else {
                        storageList.add(current);
                    }
                } else {
                    ArrayList<Scan> scans = new ArrayList<>();
                    scans.add(current);
                    storage.put(current.getBssid(), scans);
                }
            } else {
                ArrayList<Scan> scans = new ArrayList<>();
                scans.add(current);
                storage.put(current.getBssid(), scans);
            }

        }
        for (ArrayList<Scan> scans : storage.values()) {
            if (scans.size() >= MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS) {
                validScans.addAll(scans);
            }
        }

        return validScans;
    }


    private double scanTimeDifference(Scan scan1, Scan scan2) {
        return Math.abs(scan1.getTimestamp().getTime() - scan2.getTimestamp().getTime()) / 1000.0;
    }

    /**
     * Parse a JSONArray of a specific format into Scan objects. The JSONArray object has to contain the following properties:
     * "b" : the BSSID of the scan
     * "l" : the distance, in meters, from the hotspot
     * "t" : the timestamp of the scan, in ISO format
     *
     * @param jsonArray the JSONArray object
     * @return a list of Scan objects
     * @throws JSONException if JSON objects are not properly formatted
     */
    public List<Scan> parseScans(JSONArray jsonArray) throws JSONException {
        List<Scan> result = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject scan = jsonArray.getJSONObject(i);
            Date timestamp = Date.from(Instant.parse(scan.getString("t")));
            result.add(new Scan(scan.getString("b"), scan.getDouble("l"), timestamp));
        }

        return result;
    }

    /**
     * Return localScans after filtering out scans whose BSSIDs are not present in remoteScans
     *
     * @param localScans  the scan list from which to remove scans
     * @param remoteScans the scan list to search for matching BSSIDs
     * @return a list of scans that have BSSIDs found in remote scans
     */
    private List<Scan> removeUnusedLocalScans(List<Scan> localScans, List<Scan> remoteScans) {
        return localScans.parallelStream().filter(scan -> containsString(remoteScans, scan)).collect(Collectors.toList());
    }

    /**
     * Check if the list of scans contains a specific BSSID
     *
     * @param list the list of scans to scout
     * @param scan a scan object whose BSSID we are looking for
     * @return true if the list contains a scan object with the specified BSSID
     */
    public boolean containsString(final List<Scan> list, final Scan scan) {
        return list.parallelStream().anyMatch(o -> o.getBssid().equals(scan.getBssid()));
    }

    public double getMAX_TIME_DIFFERENCE() {
        return MAX_TIME_DIFFERENCE;
    }

    public void setMAX_TIME_DIFFERENCE(double MAX_TIME_DIFFERENCE) {
        this.MAX_TIME_DIFFERENCE = MAX_TIME_DIFFERENCE;
    }

    public double getMAX_DISTANCE_DIFFERENCE() {
        return MAX_DISTANCE_DIFFERENCE;
    }

    public void setMAX_DISTANCE_DIFFERENCE(double MAX_DISTANCE_DIFFERENCE) {
        this.MAX_DISTANCE_DIFFERENCE = MAX_DISTANCE_DIFFERENCE;
    }

    public double getMIN_NUMBER_OF_NEAR_HOTSPOTS() {
        return MIN_NUMBER_OF_NEAR_HOTSPOTS;
    }

    public void setMIN_NUMBER_OF_NEAR_HOTSPOTS(double MIN_NUMBER_OF_NEAR_HOTSPOTS) {
        this.MIN_NUMBER_OF_NEAR_HOTSPOTS = MIN_NUMBER_OF_NEAR_HOTSPOTS;
    }

    public double getMIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS() {
        return MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS;
    }

    public void setMIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS(double MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS) {
        this.MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS = MIN_NUMBER_OF_CONSECUTIVE_TIMESTAMPS;
    }

}