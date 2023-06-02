package ru.quenteez.network;

import net.minecraft.network.PacketByteBuf;

import java.util.Map;

public class PacketSyncBiomeSettings {
    private Map<String, BiomeSettingSyncWrapper> syncMap;
    public PacketSyncBiomeSettings() {
        this.syncMap = OTGClientSyncManager.getSyncedData();
    }

    public static PacketSyncBiomeSettings decodeSpigot(PacketByteBuf buffer) {
        PacketSyncBiomeSettings packet = new PacketSyncBiomeSettings();
        int size = buffer.readInt();  // Читаем размер списка биомов
        String dirtyPreset = buffer.readString();  // Читаем имя пресета
        String regex = ".*?([a-zA-Z_]+).*";
        String preset = dirtyPreset.replaceAll(regex, "$1");
        for (int i = size; i > 0; i--) {
            if (buffer.readableBytes() < 2) {  // Проверяем наличие достаточного количества байтов для чтения имени биома
                break;
            }
            String biomeName = buffer.readString();  // Читаем имя биома
            if (biomeName.isEmpty()) {
                continue;  // Пропускаем пустое имя биома и переходим к следующей итерации цикла
            }
            String key = "otg" + ":" + preset + "." + biomeName;
//            Identifier key = new Identifier("otg", preset + "." + biomeName);
            BiomeSettingSyncWrapper wrapper = new BiomeSettingSyncWrapper(buffer);
            packet.syncMap.putIfAbsent(key, wrapper);
        }
        OTGClientSyncManager.setSyncedData(packet.syncMap);
        return packet;
    }

}
