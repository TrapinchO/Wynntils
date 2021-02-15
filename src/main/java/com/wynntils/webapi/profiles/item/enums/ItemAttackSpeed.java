/*
 *  * Copyright © Wynntils - 2018 - 2021.
 */

package com.wynntils.webapi.profiles.item.enums;

import net.minecraft.util.text.TextFormatting;

public enum ItemAttackSpeed {

    SUPER_FAST("Super Fast Attack Speed", 3),
    VERY_FAST("Very Fast Attack Speed", 2),
    FAST("Fast Attack Speed", 1),
    NORMAL("Normal Attack Speed", 0),
    SLOW("Slow Attack Speed", -1),
    VERY_SLOW("Very Slow Attack Speed", -2),
    SUPER_SLOW("Super Slow Attack Speed", -3);

    String name;
    int offset;

    ItemAttackSpeed(String name, int offset) {
        this.name = name;
        this.offset = offset;
    }

    public String getName() {
        return name;
    }

    public int getOffset() {
        return offset;
    }

    public String asLore() {
        return TextFormatting.GRAY + name;
    }

}
