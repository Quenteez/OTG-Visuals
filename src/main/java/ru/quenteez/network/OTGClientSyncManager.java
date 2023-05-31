package ru.quenteez.network;

import java.util.HashMap;
import java.util.Map;

public class OTGClientSyncManager
{
    private static Map<String, BiomeSettingSyncWrapper> syncedData = new HashMap<>();
    
    public static Map<String, BiomeSettingSyncWrapper> getSyncedData() {
        return OTGClientSyncManager.syncedData;
    }

    public static void setSyncedData(Map<String, BiomeSettingSyncWrapper> syncedData) {
        OTGClientSyncManager.syncedData = syncedData;
    }
}
