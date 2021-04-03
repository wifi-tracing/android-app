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

    private void createMockDatabaseManager() {
        databaseManager = mock(DatabaseManager.class);
        when(databaseManager.getScanBSSIDs()).thenReturn(null);
    }

    private void createMockPreferencesManager() {
        preferenceManager = mock(PreferencesManager.class);
    }


    @Test
    public void emptyLocalScans() {
        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(getFakeRemoteScans()));
    }

    @Test
    public void invalidRemoteScans() {
        when(databaseManager.getScanData()).thenReturn(new ArrayList<>());
        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        assertFalse(analyser.matchResult(null));
    }

    @Test
    public void validMatch() {
        DatabaseManager databaseManager = mock(DatabaseManager.class);
        when(databaseManager.getScanData()).thenReturn(getFakeLocalScans());


        when(preferenceManager.getCMin()).thenReturn(0);
        when(preferenceManager.getDMax()).thenReturn(0.0);
        when(preferenceManager.getHMin()).thenReturn(2);
        when(preferenceManager.getTMax()).thenReturn(3.0);
        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        JSONArray array = getFakeRemoteScans();
        assertTrue(analyser.matchResult(array));
    }

    @Test
    public void inValidMatch() {
        DatabaseManager databaseManager = mock(DatabaseManager.class);
        when(databaseManager.getScanData()).thenReturn(getFakeLocalScans());
        when(preferenceManager.getCMin()).thenReturn(2);
        when(preferenceManager.getDMax()).thenReturn(100.0);
        when(preferenceManager.getHMin()).thenReturn(0);
        when(preferenceManager.getTMax()).thenReturn(3.0);
        RiskAnalyser analyser = new RiskAnalyser(databaseManager, preferenceManager, null, appContext);
        JSONArray array = getFakeRemoteScans();
        assertFalse(analyser.matchResult(array));
    }

    public JSONArray getFakeRemoteScans() {
        JSONArray array = new JSONArray();

        for (int i = 0; i < 1; i++) {
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

    public List<Scan> getFakeLocalScans() {
        ArrayList<Scan> result = new ArrayList<>();
        //scan at 1 meter
        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 2; j++) {
                Scan scan = new Scan("aa:bb:cc:dd:e" + j + i + 1, 1.0, getFakeDate(i * 3));
                result.add(scan);
            }

        }
        return result;
    }

    private String getFakeIsoDate(int offset) {
        LocalDateTime dateTime = LocalDateTime.of(2021, Month.JANUARY, 1, 1, 1, offset, 1);

        final DateTimeFormatter formatter = DateTimeFormatter
                .ISO_DATE_TIME
                .withZone(ZoneOffset.UTC);
        return formatter.format(dateTime.toInstant(ZoneOffset.UTC));
    }

    private Date getFakeDate(int offset) {
        LocalDateTime dateTime = LocalDateTime.of(2021, Month.JANUARY, 1, 1, 1, offset, 1);
        return Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }
}