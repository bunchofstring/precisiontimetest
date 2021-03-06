package com.bunchofstring.test.netcon;

import com.bunchofstring.test.CoreUtils;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NetworkConditioner {

    private static final Logger LOGGER = Logger.getLogger(NetworkConditioner.class.getSimpleName());

    private static final int CONNECTION_STATE_CHANGE_TIMEOUT_MILLISECONDS = 10_000;
    private static final int TRAILING_DELAY_MILLISECONDS = 1000;
    private static final int INITIAL_BACKOFF = 100;

    private final NetworkStateProvider nsp;
    private final boolean isWifiEnabledAtStart;
    private final boolean isDataEnabledAtStart;

    public NetworkConditioner() throws Exception {
        grantWiFiControlPrivileges();
        nsp = new NetworkStateProvider();
        isWifiEnabledAtStart = nsp.isWifiEnabled();
        isDataEnabledAtStart = nsp.isDataEnabled();
    }

    public void reset() throws Exception {
        LOGGER.log(Level.INFO, "reset()");
        setWifiEnabled(isWifiEnabledAtStart);
        setDataEnabled(isDataEnabledAtStart);
    }

    public void setDataEnabled(final boolean enable) throws Exception {
        LOGGER.log(Level.INFO, "setDataEnabled(...) "+enable);
        final String state = enable ? "enable" : "disable";
        CoreUtils.executeShellCommand("svc data "+state);
    }

    /*
    //Prepare
    adb shell
    su

    //(Simulate weak connection (via high packet loss)
    iptables -A INPUT -m statistic --mode random --probability 0.99 -j DROP

    //Restore healthy connection (by reverting the simulation rule)
    iptables -D INPUT -m statistic --mode random --probability 0.99 -j DROP
    */

    public void setWifiEnabled(final boolean enable) throws Exception {
        LOGGER.log(Level.INFO, "setWifiEnabled(...) "+enable);
        final String state = enable ? "enable" : "disable";
        CoreUtils.executeShellCommand("svc wifi "+state);
        if(!enable) {
            awaitDisconnection();
        }
    }

    public void pairConnectWifiNetwork(final String ssid) throws Exception {
        LOGGER.log(Level.INFO, "pairConnectWifiNetwork(...) "+ssid);
        CoreUtils.executeShellCommand("cmd wifi connect-network "+ssid+" open");
        awaitConnection();
    }

    public void pairConnectWifiNetwork(final String ssid, final String pass) throws Exception {
        LOGGER.log(Level.INFO, "pairConnectWifiNetwork(...) "+ssid);
        CoreUtils.executeShellCommand("cmd wifi connect-network "+ssid+" wpa2 "+pass);
        awaitConnection();
    }

    private void grantWiFiControlPrivileges() throws Exception {
        LOGGER.log(Level.INFO, "grantWiFiControlPrivileges()");
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.ACCESS_NETWORK_STATE");
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.ACCESS_WIFI_STATE");
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.CHANGE_WIFI_STATE");

        //Android 11
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.CHANGE_NETWORK_STATE");
        CoreUtils.grantPermission("com.bunchofstring.precisiontime.test.test","android.permission.ACCESS_FINE_LOCATION");
    }

    private void awaitConnection() throws Exception {
        LOGGER.log(Level.INFO, "awaitConnection()");
        final long start = new Date().getTime();
        int backoffWait = INITIAL_BACKOFF;
        while(new Date().getTime() - start < CONNECTION_STATE_CHANGE_TIMEOUT_MILLISECONDS){
            LOGGER.log(Level.INFO, "awaitConnection() busy wait");
            if(nsp.isWifiConnected()) {
                Thread.sleep(TRAILING_DELAY_MILLISECONDS);
                return;
            }
            Thread.sleep(backoffWait);
            backoffWait *= 2;
        }
    }

    private void awaitDisconnection() throws Exception {
        LOGGER.log(Level.INFO, "awaitDisconnection()");
        final long start = new Date().getTime();
        int backoffWait = INITIAL_BACKOFF;
        while(new Date().getTime() - start < CONNECTION_STATE_CHANGE_TIMEOUT_MILLISECONDS){
            LOGGER.log(Level.INFO, "awaitDisconnection() busy wait");
            if(nsp.isWifiDisconnectedAndStable()) {
                Thread.sleep(TRAILING_DELAY_MILLISECONDS);
                return;
            }
            Thread.sleep(backoffWait);
            backoffWait *= 2;
        }
    }
}
