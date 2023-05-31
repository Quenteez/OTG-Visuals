package ru.quenteez;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import ru.quenteez.network.ModMessages;

import org.apache.logging.log4j.Logger;

public class OTGMain implements ClientModInitializer {
    public static final String MOD_ID = "otg";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static String currentBiome;

    @Override
    public void onInitializeClient() {
        ModMessages.registerS2CPackets();
    }
}
