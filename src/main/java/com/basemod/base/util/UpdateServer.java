package com.basemod.base.util;

import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;

public class UpdateServer {
    public final SentPlayer player;
    public final SentTeam team;
    public final Status status;
    
    public UpdateServer(ForgePlayer player, ForgeTeam team, Status status) {
        if (player == null) {
            this.player = null; 
        } else {
            this.player = new SentPlayer(player);
        }

        this.team = new SentTeam(team);
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
