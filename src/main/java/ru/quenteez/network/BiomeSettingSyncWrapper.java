package ru.quenteez.network;

import java.util.List;
import java.util.ArrayList;
import net.minecraft.network.PacketByteBuf;
import ru.quenteez.utils.ColorSet;
import ru.quenteez.utils.ColorThreshold;
import ru.quenteez.utils.SimpleColorSet;

public class BiomeSettingSyncWrapper
{
    private final float fogDensity;
    private final ColorSet grassColorControl;
    private final ColorSet foliageColorControl;
    private final ColorSet waterColorControl;

    public BiomeSettingSyncWrapper(PacketByteBuf buffer) {
        this.fogDensity = buffer.readFloat();
        byte size;

        List<ColorThreshold> grassColors = new ArrayList<>();
        size = buffer.readByte();
        for (int i = size; i > 0; i--) {
            if (buffer.readableBytes() < 8) { // Проверяем, достаточно ли данных для чтения двух значений типа int и float
                break; // Прекращаем цикл, если нет достаточного количества байт
            }
            grassColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
        }
        this.grassColorControl = new SimpleColorSet(grassColors);

        List<ColorThreshold> foliageColors = new ArrayList<>();
        size = buffer.readByte();
        for (int i = size; i > 0; i--) {
            if (buffer.readableBytes() < 8) { // Проверяем, достаточно ли данных для чтения двух значений типа int и float
                break; // Прекращаем цикл, если нет достаточного количества байт
            }
            foliageColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
        }
        this.foliageColorControl = new SimpleColorSet(foliageColors);

        List<ColorThreshold> waterColors = new ArrayList<>();
        size = buffer.readByte();
        for (int i = size; i > 0; i--) {
            if (buffer.readableBytes() < 8) { // Проверяем, достаточно ли данных для чтения двух значений типа int и float
                break; // Прекращаем цикл, если нет достаточного количества байт
            }
            waterColors.add(new ColorThreshold(buffer.readInt(), buffer.readFloat()));
        }
        this.waterColorControl = new SimpleColorSet(waterColors);
    }
    
    public float getFogDensity() {
        return this.fogDensity;
    }
    
    public ColorSet getGrassColorControl() {
        return this.grassColorControl;
    }
    
    public ColorSet getFoliageColorControl() {
        return this.foliageColorControl;
    }
    
    public ColorSet getWaterColorControl() {
        return this.waterColorControl;
    }
}
