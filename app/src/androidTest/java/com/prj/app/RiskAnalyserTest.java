package com.prj.app;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.prj.app.logic.RiskAnalyser;
import com.prj.app.managers.DatabaseManager;
import com.prj.app.managers.PreferencesManager;
import com.prj.app.util.Scan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Risk Analysis operation with mocked input data.
 *
 * @see RiskAnalyser
 */
@RunWith(AndroidJUnit4.class)
public class RiskAnalyserTest {
    @Mock
    DatabaseManager databaseManager;

    @Mock
    PreferencesManager preferenceManager;

    Context appContext;

    @Before
    public void initMocks() {
        createMockDatabaseManager();
        createMockPreferencesManager();
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * By default, {@link DatabaseManager#getScanData()} returns value of {@link RiskAnalyserTest#getFakeLocalScans()}
     */
    private void createMockDatabaseManager() {
        databaseManager = mock(DatabaseManager.class);
        when(databaseManager.getScanData()).thenReturn(getFakeLocalScans());
    }

    /**
     * Default config is valid for use with the {@link RiskAnalyserTest#getFakeLocalScans()} and {@link RiskAnalyserTest#getFakeRemoteScans()}
     * generation methods.
     * <p>
     * The fake scans are two timestamps (CMin = 2), spaced three seconds apart (TMax = 3.0), 0.5 meters apart (DMax = 0.5).
     * Each timestamp has 2 separate Access Points (HMin = 2)
     * <p>
     * <p>
     * The default config is
     * <ul>
     *     <li>CMin = 2</li>
     *     <li>DMax = 0.5</li>
     *     <li>HMin = 2</li>
     *     <li>TMax = 3.0</li>
     * </ul>
     * </p>
     */
    private void createMockPreferencesManager() {
        preferenceManager = mock(PreferencesManager.class);

        when(preferenceManager.getCMin()).thenReturn(2);
        when(preferenceManager.getDMax()).thenReturn(0.5);
        when(preferenceManager.getHMin()).thenReturn(2);
        when(preferenceManager.getTMax()).thenReturn(3.0);
    }


    @Test
    public void emptyLocalScans() {
        when(databaseManager.getScanData()).thenReturn(new ArrayList<>());

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(getFakeRemoteScans()));
    }

    @Test
    public void emptyRemoteScans() {
        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(new JSONArray()));
    }

    @Test
    public void nullRemoteScans() {
        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(null));
    }

    @Test
    public void nullLocalScans() {
        when(databaseManager.getScanData()).thenReturn(null);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(getFakeRemoteScans()));
    }


    @Test
    public void validMatch() {
        when(databaseManager.getScanData()).thenReturn(getFakeLocalScans());

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        JSONArray array = getFakeRemoteScans();
        assertTrue(analyser.matchResult(array));
    }

    //#region Test Constants

    //#region CMin
    @Test
    public void invalidCMinTooGreat() {
        when(preferenceManager.getCMin()).thenReturn(3);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(getFakeRemoteScans()));
    }


    @Test
    public void validCMinTooSmall() {
        when(preferenceManager.getCMin()).thenReturn(-1);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertTrue(analyser.matchResult(getFakeRemoteScans()));
    }
    //#endregion

    //#region DMax
    @Test
    public void invalidDMaxTooSmall() {
        when(preferenceManager.getDMax()).thenReturn(0.1);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(getFakeRemoteScans()));
    }

    @Test
    public void validDMaxTooGreat() {
        when(preferenceManager.getDMax()).thenReturn(1.5);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertTrue(analyser.matchResult(getFakeRemoteScans()));
    }
    //#endregion

    //#region HMin
    @Test
    public void validHMinTooSmall() {
        when(preferenceManager.getHMin()).thenReturn(0);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertTrue(analyser.matchResult(getFakeRemoteScans()));
    }

    @Test
    public void invalidHMinTooGreat() {
        when(preferenceManager.getHMin()).thenReturn(3);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(getFakeRemoteScans()));
    }
    //#endregion

    //#region TMax
    @Test
    public void invalidTMaxTooSmall() {
        when(preferenceManager.getTMax()).thenReturn(2.0);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(getFakeRemoteScans()));
    }

    @Test
    public void validTMaxTooGreat() {
        when(preferenceManager.getTMax()).thenReturn(3.0);

        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertTrue(analyser.matchResult(getFakeRemoteScans()));
    }
    //#endregion

    //#endregion

    //#region Auxiliary methods

    /**
     * Generate a JSONArray of remote scans. Two timestamps groups of two scans each at distance 1.0m, three seconds apart.
     * They should be valid scans when compared to the remote scans returned by {@link RiskAnalyserTest#getFakeLocalScans()}
     *
     * @return a JSONArray of fake remote scans
     */
    public JSONArray getFakeRemoteScans() {
        JSONArray array = new JSONArray();

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                JSONObject scan = new JSONObject();
                try {
                    scan.put("b", "aa:bb:cc:dd:e" + j + i + 1);
                    scan.put("l", 1);
                    scan.put("t", getFakeIsoDate(i * 3));
                } catch (JSONException e) {
                    e.printStackTrace();
                    return null;
                }
                array.put(scan);
            }

        }

        return array;
    }

    /**
     * Generate a list of local scans. Two timestamps groups of two scans each at distance 1.5m, three seconds apart.
     * They should be valid scans when compared to the remote scans returned by {@link RiskAnalyserTest#getFakeRemoteScans}
     *
     * @return a list of fake local scans
     */
    public List<Scan> getFakeLocalScans() {
        ArrayList<Scan> result = new ArrayList<>();
        //scan at 1 meter
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Scan scan = new Scan("aa:bb:cc:dd:e" + j + i + 1, 1.5, getFakeDate(i * 3));
                result.add(scan);
            }

        }
        return result;
    }

    /**
     * Get String representing parseable ISO date. 1st of January 2021 at 01:01:xx.
     * Returns same date as {@link RiskAnalyserTest#getFakeDate(int)}
     *
     * @param offset the positive offset, in seconds, to add to the date
     * @return a String representing an ISO date.
     */
    private String getFakeIsoDate(int offset) {
        LocalDateTime dateTime = LocalDateTime.of(2021, Month.JANUARY, 1, 1, 1, offset, 1);

        final DateTimeFormatter formatter = DateTimeFormatter
                .ISO_DATE_TIME
                .withZone(ZoneOffset.UTC);
        return formatter.format(dateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * Get Date 1st of January 2021 at 01:01:xx.
     * Returns same date as {@link RiskAnalyserTest#getFakeIsoDate(int)}
     *
     * @param offset the positive offset, in seconds, to add to the date
     * @return a UTC Date.
     */
    private Date getFakeDate(int offset) {
        LocalDateTime dateTime = LocalDateTime.of(2021, Month.JANUARY, 1, 1, 1, offset, 1);
        return Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }

    //#endregion
}