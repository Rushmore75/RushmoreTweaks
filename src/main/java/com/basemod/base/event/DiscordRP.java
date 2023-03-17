package com.basemod.base.event;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ForkJoinPool;

import com.basemod.base.Base;
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
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

@EventBusSubscriber
public class DiscordRP extends Thread {

    private static String universeUuid = null;

    private boolean running = false; // This can probably go away

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

    @EventHandler
    public static void serverStart(FMLServerStartedEvent event) {
        Base.getLogger().info("Server starting, releasing message.");
        sendEventToDiscord(
                new UpdateServer(null, null, Status.SERVER_START));
    }

    @EventHandler
    public static void serverStop(FMLServerStoppedEvent event) {
        sendEventToDiscord(
                new UpdateServer(null, null, Status.SERVER_STOP));
    }

    // ===================================================
    // Send Messages
    // ===================================================

    public static void sendEventToDiscord(UpdateServer update) {
        if (!Base.serverUp) {
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
        if (!Base.serverUp) {
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
        if (!Base.serverUp) {
            return;
        }

        try {
            URL url = new URL(Base.siteUri + "listenforchats/" + Universe.get().getUUID().toString());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            // BufferedInputStream read = new BufferedInputStream(con.getInputStream());
            InputStream read = con.getInputStream();

            // will block
            while (running) {
                byte[] buffer = new byte[1024];
                int bytes = 0;
                StringBuilder sb = new StringBuilder();

                // Read into buffer
                do {
                    bytes = read.read(buffer);
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
                    Base.getLogger().warn("'"+msg+"'");

                    try {
                        // FIXME the player's name doesn't get deseialized.
                        PlayerMsg playerMsg = Base.gson.fromJson(msg, PlayerMsg.class);
                        Base.getLogger().info(playerMsg.toString());
                        sendMessageToMinecraft(playerMsg);
                    } catch (JsonSyntaxException e) { Base.getLogger().error(e.getCause()); }
                }
            }
            read.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        running = true;
        readDiscordMessages();
    }

}
