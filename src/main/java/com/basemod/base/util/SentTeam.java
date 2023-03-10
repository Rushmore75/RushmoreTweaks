package com.basemod.base.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.feed_the_beast.ftblib.lib.EnumTeamStatus;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;

public class SentTeam {
    
    final SentPlayer owner; 
    final String id;
    final UUID universeUuid;
    final Map<SentPlayer, EnumTeamStatus> players;


    public SentTeam(ForgeTeam team) {
        // The only time owner should be null is if the team is fake, like "singleplayer"
        this.owner = team.owner == null ? new SentPlayer("Server") : new SentPlayer(team.owner);
        this.id = team.getId();
        this.universeUuid = Universe.get().getUUID();

        Map<SentPlayer, EnumTeamStatus> m = new HashMap<>();
            
        for (ForgePlayer player : team.players.keySet()) {
            EnumTeamStatus status = team.players.get(player); 
            m.put(new SentPlayer(player), status);
        }
        this.players=m;
    }
}
