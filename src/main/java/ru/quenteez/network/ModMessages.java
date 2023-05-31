package ru.quenteez.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.Identifier;
import ru.quenteez.OTGMain;

public class ModMessages {
    public static final Identifier SPIGOT_ID = new Identifier(OTGMain.MOD_ID, "spigot");
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(SPIGOT_ID, SpigotS2CPacket::receive);
    }
}
