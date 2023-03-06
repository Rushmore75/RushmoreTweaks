package com.basemod.base.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;

public class SentPlayer {
    String uuid;
    String playerName;
    final String universeUuid;
    

    private static int fakePlayerCount = 0;

    public String getName() {
        return playerName;
    }

    public SentPlayer(ForgePlayer player, Universe universe) {

        universeUuid = universe.getUUID().toString();
        try {
            playerName = player.getName();
            uuid = player.getId().toString();
            // catching the error instead of preventing it 
            // isn't the best practice.
            // TODO player NPE here sometimes
        } catch (NullPointerException e) {
            // Use the string instead of uuid
            // so it works with unofficial accounts
            uuid = "fakeplayer_uuid_"+fakePlayerCount;
            playerName = "fakeplayer_"+fakePlayerCount;
            fakePlayerCount++;
        }

    }
}


