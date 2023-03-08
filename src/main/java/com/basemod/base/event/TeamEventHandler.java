package com.basemod.base.event;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.IOException;
import java.text.MessageFormat;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.basemod.base.Base;
import com.basemod.base.util.PlayerMsg;
import com.basemod.base.util.SentPlayer;
import com.basemod.base.util.SentTeam;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;

@EventBusSubscriber
public class TeamEventHandler {

    @SubscribeEvent
	public static void onTeamJoinedEvent(ForgeTeamPlayerJoinedEvent event) {
		
		if (!Base.serverUp) { return; }

		ForgePlayer player = event.getPlayer();
		ForgeTeam team = event.getTeam();
		
		String player_json = Base.gson.toJson(new SentPlayer(player, event.getUniverse()));
		String team_json = Base.gson.toJson(new SentTeam(team, event.getUniverse()));	

		try {
			HttpPost post = new HttpPost(Base.siteUri+"teamjoin");
			// TODO test this, do they need to be combined? Or can we get away with not
			// post.setEntity(new StringEntity(player_json, ContentType.APPLICATION_JSON));
			post.setEntity(new StringEntity(team_json, ContentType.APPLICATION_JSON));

			CloseableHttpClient client = HttpClients.createDefault();
			client.execute(post);
			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		// Send out notification
		String join = MessageFormat.format("{0} joined {1}!", player.getName(), team.getTitle());
		DiscordRP.sendEverywhere(new PlayerMsg(new SentPlayer("Server", Universe.get()), join)); 
	}
	
	@SubscribeEvent
	public static void onTeamLeaveEvent(ForgeTeamPlayerLeftEvent event) {

		if (!Base.serverUp) { return; }

		// NOTE: this event seems to double-fire when it should only
		// fire once. Not my fault but might be relevant information.
		String player_json = Base.gson.toJson( new SentPlayer(event.getPlayer(), event.getUniverse()) );

		try {
			HttpPost post = new HttpPost(Base.siteUri+"teamleave");
			post.setEntity(new StringEntity(player_json, ContentType.APPLICATION_JSON));

			CloseableHttpClient client = HttpClients.createDefault();
			client.execute(post);
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Send out the notification
		String join = MessageFormat.format("{0} joined {1}!", event.getPlayer().getName(), event.getTeam().getTitle());
		DiscordRP.sendEverywhere(new PlayerMsg(new SentPlayer("Server", Universe.get()), join));
	}


}