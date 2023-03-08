package com.basemod.base.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;

import net.minecraft.entity.player.EntityPlayer;

public class SentPlayer {
    final String uuid;
    final String playerName;
    final String universeUuid;

    public String getName() {
        return playerName;
    }

    public SentPlayer(String name) {
        universeUuid = Universe.get().getUUID().toString();
        playerName = name;
        uuid = universeUuid;
    }

    public SentPlayer(EntityPlayer player) {
        uuid = player.getUniqueID().toString();
        playerName = player.getName();
        universeUuid = Universe.get().getUUID().toString();

    }

    public SentPlayer(ForgePlayer player) {
        universeUuid = Universe.get().getUUID().toString();
        playerName = player.getName();
        uuid = player.getId().toString();
    }
}


