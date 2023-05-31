package ru.quenteez.utils;

import java.util.List;

public class SimpleColorSet extends ColorSet
{

    public SimpleColorSet(List<ColorThreshold> list)
    {
        layers = list;
    }

    @Override
    public String toString()
    {
        if (this.layers.isEmpty())
        {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (ColorThreshold layer : this.layers)
        {
            stringBuilder.append("#" + Integer.toHexString(layer.getColor() | 0x1000000).substring(1).toUpperCase());
            stringBuilder.append(',').append(' ');
            stringBuilder.append(layer.maxNoise);
            stringBuilder.append(',').append(' ');
        }
        // Delete last ", "
        stringBuilder.deleteCharAt(stringBuilder.length() - 2);
        return stringBuilder.toString();
    }
}
