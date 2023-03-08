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
import com.basemod.base.util.UpdateServer;
import com.basemod.base.util.SentPlayer;
import com.basemod.base.util.UpdateServer.Status;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDeletedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamOwnerChangedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;

@EventBusSubscriber
public class TeamEventHandler {

	
	//=================================================
	// 				Listen for events	
	//=================================================
	// TODO streamline the messages together.
    @SubscribeEvent
	public static void onTeamJoin(ForgeTeamPlayerJoinedEvent event) {

		// Send out notification
		String message = MessageFormat.format(
			"{0} Joined {1}!",
			event.getPlayer().getName(),
			event.getTeam().getId()
			);
		DiscordRP.sendEverywhere(new PlayerMsg(new SentPlayer("Server"), message)); 
		
		updatePlayer(event.getPlayer(), event.getTeam(), Status.JOIN);
		
	}
	
	@SubscribeEvent
	public static void onTeamLeave(ForgeTeamPlayerLeftEvent event) {
		// this should filter out the event when it fires more
		// than once for a single event
		if (event.getTeam().getId() == "") { return; }

		// Send out the notification
		String message = MessageFormat.format(
			"{0} Left {1}!",
			event.getPlayer().getName(),
			event.getTeam().getId()
			);
		DiscordRP.sendEverywhere(new PlayerMsg(new SentPlayer("Server"), message));

		updatePlayer(event.getPlayer(), event.getTeam(), Status.LEAVE);	

	}

	@SubscribeEvent
	public static void onTeamDisband(ForgeTeamDeletedEvent event){

		String message = MessageFormat.format(
			"{0} Disbanded!",
			event.getTeam().getId()
			);
		DiscordRP.sendEverywhere(new PlayerMsg(new SentPlayer("Server"), message));

		updatePlayer(null, event.getTeam(), Status.DISBAND);

	}

	@SubscribeEvent
	public static void onTeamOwnerChange(ForgeTeamOwnerChangedEvent event) {

		String message = MessageFormat.format(
			"{0} Leadership changed from {1} to {2}!",
			event.getTeam().getId(),
			event.getOldOwner(),
			event.getTeam().getOwner()
			);
		DiscordRP.sendEverywhere(new PlayerMsg(new SentPlayer("Server"), message));

		updatePlayer(event.getOldOwner(), event.getTeam(), Status.RANK);
	
	}

	//=================================================
	// 				Notify the server
	//=================================================

	private static void updatePlayer(ForgePlayer player, ForgeTeam newTeam, Status status) {
		if (!Base.serverUp) { return; }
		/*
		 * Send the player and the new team so it can be updated in discord.
		 * Can also notify the server of new teams this way, just send it the team and if it doesn't see it create new one.
		 */
		String json = Base.gson.toJson(new UpdateServer(player, newTeam, status));
		try {
			HttpPost post = new HttpPost(Base.siteUri+"updatePlayer");
			post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

			CloseableHttpClient client = HttpClients.createDefault();
			client.execute(post);
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}



	}

}