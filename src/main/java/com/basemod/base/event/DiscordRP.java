package com.basemod.base.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;

import com.basemod.base.Base;
import com.basemod.base.CmdGetUniverse;
import com.basemod.base.util.PlayerMsg;
import com.basemod.base.util.SentPlayer;
import com.basemod.base.util.UpdateServer;
import com.basemod.base.util.UpdateServer.PlayerUpdate;
import com.basemod.base.util.UpdateServer.Status;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.google.gson.JsonSyntaxException;
import com.ibm.icu.text.MessageFormat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@Mod.EventBusSubscriber(modid = Base.MOD_ID)
public class DiscordRP extends Thread {

    private static String universeUuid = null;
    private static boolean serverUp = true;

    // ===================================================
    // Watch Events
    // ===================================================
    @SubscribeEvent
    public static void chatEvent(ServerChatEvent event) {

        PlayerMsg mp = new PlayerMsg(new SentPlayer(event.getPlayer()), event.getMessage());
        sendMessageToDiscord(mp);

    }

    @SubscribeEvent
    public static void advancementEvent(AdvancementEvent event) {

        if (event.getAdvancement().getDisplayText().toString().contains("recipes")) {
            return;
        }

        String message = MessageFormat.format(
                "{0} achieved {1}",
                event.getEntityPlayer().getName(),

                event.getAdvancement().getDisplayText().getUnformattedText());

        sendMessageToDiscord(PlayerMsg.sendAsServer(message));
    }

    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        sendEventToDiscord(
                new UpdateServer(
                        new PlayerUpdate(
                                Universe.get().getPlayer(event.player), null),
                        null, Status.PLAYER_LOGIN));
    }

    @SubscribeEvent
    public static void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        sendEventToDiscord(
                new UpdateServer(
                        new PlayerUpdate(new SentPlayer(event.player), null),
                        null,
                        Status.PLAYER_LOGOUT));
    }

    @SubscribeEvent
    public static void playerDeath(LivingDeathEvent event) {

        if (!(event.getEntity() instanceof EntityPlayer)) {
            return;
        }
        EntityPlayer player = (EntityPlayer) event.getEntity();

        Entity cause = event.getSource().getTrueSource();
        String name = cause == null ? event.getSource().getDamageType() : cause.getName();

        sendEventToDiscord(
                new UpdateServer(
                        new PlayerUpdate(
                                Universe.get().getPlayer(player), null),
                        new PlayerUpdate(
                                new SentPlayer(name), null),
                        Status.PLAYER_DEATH));
    }

    // ===================================================
    // Send Messages
    // ===================================================

    public static void sendEventToDiscord(UpdateServer update) {
        if (!serverUp) {
            return;
        }

        if (universeUuid == null) {
            // Don't spam the universe call, might have performance improvements
            // but is here so npes don't happen
            universeUuid = Universe.get().getUUID().toString();
        }
        String json = Base.gson.toJson(update);
        sendToDiscord(json, "sendevent/" + universeUuid);
    }

    /**
     * Push a message to the Discord server.
     * 
     * @param pMsg The player message. (UUID will be check!)
     */
    private static void sendMessageToDiscord(PlayerMsg pMsg) {
        if (!serverUp) {
            Base.getLogger().warn("Server isn't up.");
            return;
        }

        String json = Base.gson.toJson(pMsg);
        sendToDiscord(json, "sentmessage");
    }

    private static void sendToDiscord(String json, String path) {

        try {
            URL url = new URL(Base.siteUri + path);

            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");

            con.setDoOutput(true);

            // Do the http request async
            ForkJoinPool.commonPool().execute(() -> {
                try {
                    OutputStream os = con.getOutputStream();
                    os.write(json.getBytes());
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    Base.getLogger().error(e.getStackTrace());
                }
            });
        } catch (IOException e) {
            Base.getLogger().error(e.getStackTrace());
        }

    }

    /**
     * Push a message to minecraft.
     */
    public static void sendMessageToMinecraft(PlayerMsg pMsg) {

        String msg = "[§3" + pMsg.player.getName() + "§r] " + pMsg.msg;
        Universe.get().server.getPlayerList().sendMessage(new TextComponentString(msg));

    }

    /**
     * Subscribe to the queue of Discord messages.
     * Note: This method will block!
     */
    public void readDiscordMessages() {
        if (!serverUp) {
            return;
        }
        InputStream read = null; 
        try {
            URL url = new URL(Base.siteUri + "listenforchats/" + Universe.get().getUUID().toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            read = con.getInputStream();
        } catch (IOException e) { e.printStackTrace(); }

        if (read == null) {
            Base.getLogger().error("Reading from webserver failed");
            return;
        }
        // will block
        while (true) {
            byte[] buffer = new byte[1024];
            int bytes = 0;
            StringBuilder sb = new StringBuilder();
            do {
                // Read into buffer
                try { bytes = read.read(buffer); } catch (IOException e) { e.printStackTrace(); }
                
                for (Byte i : buffer) { sb.append((char) i.byteValue()); }
                // clean buffer
                buffer = new byte[1024];
            } while (bytes > 2);

            if (bytes > 0) {
                int index = sb.indexOf("data:");
                while (index!=-1) {
                    sb.delete(index, index+5);
                    index = sb.indexOf("data:");
                }
                String msg = sb.toString().trim();
                if (msg.equals(":")) {
                    // The server sends these as heartbeats (or something.)
                    continue;
                }

                Base.getLogger().warn(msg);
                try {
                    // FIXME the player's name doesn't get deseialized.
                    PlayerMsg playerMsg = Base.gson.fromJson(msg, PlayerMsg.class);
                    Base.getLogger().info(playerMsg.toString());
                    sendMessageToMinecraft(playerMsg);
                } catch (JsonSyntaxException e) { Base.getLogger().error(e.getCause()); }
            }
        }
    }

    public static void checkServer() {
         try {
            URL url = new URL(Base.siteUri+"version");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            
            InputStream res = con.getInputStream();
            int b = 0;
            StringBuilder sb = new StringBuilder();
            while ((b = res.read()) != -1) {
                sb.append((char)b);
            }
            res.close(); 

            Base.getLogger().info(sb.toString());
            // TODO finish version check

        } catch (IOException e) {
            serverUp = false;
            Base.getLogger().warn("Unable to connect to external server, this pretty much invalidates the mod.");
        }       
        if (serverUp) {
            Base.getLogger().info("Connected to server!");
        }
    }

    @Override
    public void run() {
        readDiscordMessages();
    }

}
