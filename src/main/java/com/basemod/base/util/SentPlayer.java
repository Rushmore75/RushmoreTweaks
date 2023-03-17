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

    public SentPlayer(String playerName) {
        universeUuid = Universe.get().getUUID().toString();
        this.playerName = playerName;
        uuid = universeUuid;
    }

    public SentPlayer(EntityPlayer player) {
        uuid = player.getUniqueID().toString();
        playerName = player.getName();
        universeUuid = Universe.get().getUUID().toString();

    }

    public SentPlayer(ForgePlayer player) {
        uuid = player.getId().toString();
        playerName = player.getName();
        universeUuid = Universe.get().getUUID().toString();
    }
}


