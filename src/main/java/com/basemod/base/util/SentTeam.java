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


    public SentTeam(ForgeTeam team, Universe universe) {
        this.owner = new SentPlayer(team.owner, universe);
        this.id = team.getId();
        this.universeUuid = universe.getUUID();

        Map<SentPlayer, EnumTeamStatus> m = new HashMap<>();
            
        for (ForgePlayer player : team.players.keySet()) {
            EnumTeamStatus status = team.players.get(player); 
            m.put(new SentPlayer(player, universe), status);
        }
        this.players=m;
    }
}
