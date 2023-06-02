package ru.quenteez;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import ru.quenteez.network.ModMessages;

import ru.quenteez.network.OTGClientSyncManager;

public class OTGMain implements ClientModInitializer {
    public static final String MOD_ID = "otg";

    @Override
    public void onInitializeClient() {
        ModMessages.registerS2CPackets();
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            OTGClientSyncManager.getSyncedData().clear();
        });
    }
}
