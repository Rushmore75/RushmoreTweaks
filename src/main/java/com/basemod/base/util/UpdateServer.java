package com.basemod.base.util;

import javax.annotation.Nullable;

import com.feed_the_beast.ftblib.events.team.ForgeTeamEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;

public class UpdateServer {
    public final Status status;
    public final PlayerUpdate receiver;
    public final PlayerUpdate sender;
    
    public UpdateServer(PlayerUpdate sender, PlayerUpdate receiver, Status status) {
        
        this.sender = sender;
        this.receiver = receiver;

        this.status = status;
    }
    
    public static class PlayerUpdate {
        public final SentPlayer player;
        public final SentTeam team;
        
        public PlayerUpdate(@Nullable ForgePlayer player, @Nullable ForgeTeam team) {
            
            this.player = player == null ? null : new SentPlayer(player); 
            this.team = team == null ? null : new SentTeam(team);            

        }
        
        public PlayerUpdate(SentPlayer player, SentTeam team) {
            this.player = player;
            this.team = team;
        }
    }
    
    // TODO, the sending of each event should be more streamlined
    
    public enum Status {
        // Team
        RANK,
        LEAVE,
        JOIN,
        DISBAND,
        
        SERVER_STOP,
        SERVER_START,

        PLAYER_LOGIN,
        PLAYER_LOGOUT,
        PLAYER_DEATH,

    } 

}
