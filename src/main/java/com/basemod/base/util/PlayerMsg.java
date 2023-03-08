package com.basemod.base.util;

import net.minecraft.entity.player.EntityPlayer;

public class PlayerMsg {
    public final SentPlayer player;
    public final String msg;

    public PlayerMsg(SentPlayer player, String msg) {
        this.player = player;
        this.msg = msg;
    }

    public PlayerMsg(EntityPlayer player, String msg) {
        this.msg = msg;
        this.player = new SentPlayer(player);
    }
   
    public static PlayerMsg sendAsServer(String msg) {
        return new PlayerMsg(new SentPlayer("Server"), msg);
    }

}

