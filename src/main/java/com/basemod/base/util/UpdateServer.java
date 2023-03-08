package com.basemod.base.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;

public class UpdateServer {
    public final SentPlayer player;
    public final SentTeam team;
    public final Status status;
    
    public UpdateServer(ForgePlayer player, ForgeTeam team, Status status) {
        if (player == null) {
            this.player = null; 
        } else {
            this.player = new SentPlayer(player, Universe.get());
        }

        this.team = new SentTeam(team, Universe.get());
        this.status = status;
    }
    
    public enum Status {
        /** Promoted / Demoted */
        RANK,
        /** Leaves a team */
        LEAVE,
        /** Joins a team */
        JOIN,
        /** The team disbanded */
        DISBAND

    } 

}
