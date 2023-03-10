package com.basemod.base.event;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.text.MessageFormat;

import com.basemod.base.util.PlayerMsg;
import com.basemod.base.util.UpdateServer;
import com.basemod.base.util.UpdateServer.PlayerUpdate;
import com.basemod.base.util.UpdateServer.Status;
import com.feed_the_beast.ftblib.events.team.ForgeTeamDeletedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamOwnerChangedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;

@EventBusSubscriber
public class TeamEventHandler {

	
	//=================================================
	// 				Listen for events	
	//=================================================
    @SubscribeEvent
	public static void onTeamJoin(ForgeTeamPlayerJoinedEvent event) {

		// Send out notification
		String message = MessageFormat.format(
			"{0} Joined {1}!",
			event.getPlayer().getName(),
			event.getTeam().getId()
			);
		DiscordRP.sendMessageToMinecraft(PlayerMsg.sendAsServer(message)); 

		DiscordRP.sendEventToDiscord(
			new UpdateServer(
				new PlayerUpdate(event.getPlayer(), event.getTeam()),
				null,
				Status.JOIN
			)	
		);
		
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
		DiscordRP.sendMessageToMinecraft(PlayerMsg.sendAsServer(message));

		DiscordRP.sendEventToDiscord(
			new UpdateServer(
				new PlayerUpdate(event.getPlayer(), event.getTeam()),
				null,
				Status.LEAVE	
			)	
		);

	}

	@SubscribeEvent
	public static void onTeamDisband(ForgeTeamDeletedEvent event){

		String message = MessageFormat.format(
			"{0} Disbanded!",
			event.getTeam().getId()
			);
		DiscordRP.sendMessageToMinecraft(PlayerMsg.sendAsServer(message));

		DiscordRP.sendEventToDiscord(
			new UpdateServer(
				new PlayerUpdate(null, event.getTeam()),	
			null,
			Status.DISBAND
			)
		);

	}

	@SubscribeEvent
	public static void onTeamOwnerChange(ForgeTeamOwnerChangedEvent event) {

		String message = MessageFormat.format(
			"{0} Leadership changed from {1} to {2}!",
			event.getTeam().getId(),
			event.getOldOwner(),
			event.getTeam().getOwner()
			);
		DiscordRP.sendMessageToMinecraft(PlayerMsg.sendAsServer(message));

		DiscordRP.sendEventToDiscord(
			new UpdateServer(
				new PlayerUpdate(event.getTeam().getOwner(), event.getTeam()),
				new PlayerUpdate(event.getOldOwner(), event.getTeam()),
				Status.RANK
				)
		);
	}

}