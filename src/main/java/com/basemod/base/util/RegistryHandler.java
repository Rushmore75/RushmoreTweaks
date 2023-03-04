package com.basemod.base.util;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerJoinedEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamPlayerLeftEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;

@EventBusSubscriber
public class RegistryHandler {

    @SubscribeEvent
	public static void onTeamJoinedEvent(ForgeTeamPlayerJoinedEvent event) {
		ForgePlayer player = event.getPlayer();
		ForgeTeam team = event.getTeam();

		System.out.println("Join");
		
	}
	
	@SubscribeEvent
	public static void onTeamLeaveEvent(ForgeTeamPlayerLeftEvent event) {
		ForgeTeam team = event.getTeam();
		System.out.println("Leave");
	}

}