/*
 *  * Copyright © Wynntils - 2021.
 */

package com.wynntils.core.framework.instances.data;

import com.wynntils.core.framework.enums.ClassType;
import com.wynntils.core.framework.instances.containers.PlayerData;
import com.wynntils.core.utils.StringUtils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextFormatting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionBarData extends PlayerData {

    private static final Pattern ACTIONBAR_PATTERN = Pattern.compile("(?:§❤ *([0-9]+)/([0-9]+))?.*? {2,}(?:§([LR])§-(?:§([LR])§-§([LR])?)?)?.*".replace("§", "(?:§[0-9a-fklmnor])*"));

    private String lastActionBar;
    private String specialActionBar = null;

    public ActionBarData() { }

    public void updateActionBar(String actionBar) {
        CharacterData characterData = get(CharacterData.class);
        SpellData spellData = get(SpellData.class);
        EntityPlayerSP player = getPlayer();

        if (characterData.getCurrentClass() == ClassType.NONE) return;

        // Avoid useless processing
        if (this.lastActionBar == null || !this.lastActionBar.equals(actionBar)) {
            this.lastActionBar = actionBar;

            if (actionBar.contains("|") || actionBar.contains("_")) {
                specialActionBar = StringUtils.getCutString(actionBar, "    ", "    " + TextFormatting.AQUA, false);
            } else {
                specialActionBar = null;
            }

            Matcher match = ACTIONBAR_PATTERN.matcher(actionBar);

            if (match.matches()) {
                if (match.group(1) != null) {
                    characterData.setHealth(Integer.parseInt(match.group(1)));
                    characterData.setMaxHealth(Integer.parseInt(match.group(2)));
                }

                if (match.group(3) != null) {
                    int size;
                    for (size = 1; size < 3; ++size) {
                        if (match.group(size + 3) == null) break;
                    }

                    boolean[] lastSpell = new boolean[size];
                    for (int i = 0; i < size; ++i) {
                        lastSpell[i] = match.group(i + 3).charAt(0) == 'R' ? SpellData.SPELL_RIGHT : SpellData.SPELL_LEFT;
                    }

                    spellData.setLastSpell(lastSpell);
                }
            }
        }

        characterData.setLevel(player.experienceLevel);
        characterData.setExperiencePercentage(player.experience);
    }

    public String getSpecialActionBar() {
        return specialActionBar;
    }

    public String getLastActionBar() {
        return lastActionBar;
    }

}
