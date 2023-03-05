package com.basemod.base.event;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.basemod.base.Base;
import com.basemod.base.util.SentPlayer;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;

import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.event.ServerChatEvent;

// import com.feed_the_beast.ftblib.lib.net.*; // contains message sending methods

@EventBusSubscriber
public class DiscordRP {
   
    static class PlayerMsg {
        SentPlayer player;
        String msg;

        PlayerMsg(SentPlayer player, String msg) {
            this.player = player;
            this.msg = msg;
        }
    }

    @SubscribeEvent
    public static void format(ServerChatEvent event) {
        if (!Base.serverUp) { return; }

        // perhaps a bit convoluted, but ultimately it should streamline things
        // on the server side.
        ForgePlayer player = Universe.get().getPlayer(event.getPlayer());
        PlayerMsg mp = new PlayerMsg(new SentPlayer(player, Universe.get()), event.getMessage()); 
        String json = Base.gson.toJson(mp);

        try {
			HttpPost post = new HttpPost(Base.siteUri+"sentmessage");
			post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

			CloseableHttpClient client = HttpClients.createDefault();
			client.execute(post);
			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
    }

}
