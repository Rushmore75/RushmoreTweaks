package com.basemod.base.event;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.basemod.base.Base;
import com.basemod.base.util.PlayerMsg;
import com.basemod.base.util.SentPlayer;
import com.basemod.base.util.UpdateServer;
import com.basemod.base.util.UpdateServer.PlayerUpdate;
import com.basemod.base.util.UpdateServer.Status;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.google.gson.JsonSyntaxException;
import com.ibm.icu.text.MessageFormat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;


@EventBusSubscriber
public class DiscordRP extends Thread {
   
    private boolean running = false; // This can probably go away

    //===================================================
    //                  Watch Events 
    //===================================================
    @SubscribeEvent
    public static void chatEvent(ServerChatEvent event) {

        PlayerMsg mp = new PlayerMsg(new SentPlayer(event.getPlayer()), event.getMessage()); 
        sendMessageToDiscord(mp);
        
    }

    @SubscribeEvent
    public static void advancementEvent(AdvancementEvent event) {

        if (event.getAdvancement().getDisplayText().toString().contains("recipes")) { return; }


        String message = MessageFormat.format(
            "{0} achieved {1}",
            event.getEntityPlayer().getName(),
            
            event.getAdvancement().getDisplayText().getUnformattedText()
            );

        sendMessageToDiscord(PlayerMsg.sendAsServer(message));
    }
    
    @SubscribeEvent
    public static void playerLogin(PlayerEvent.PlayerLoggedInEvent event) {

        sendEventToDiscord(
            new UpdateServer(
                new PlayerUpdate(
                    Universe.get().getPlayer(event.player),null
                    ),
                null, Status.PLAYER_LOGIN
            )
        );
    }
    
    @SubscribeEvent
    public static void playerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        // FIXME the universe is gone by the time this event happens...
        sendEventToDiscord(
            new UpdateServer(
                null,
                null,
                Status.PLAYER_LOGOUT
            )
        );
    }

    @SubscribeEvent
    public static void playerDeath(LivingDeathEvent event) {
        
        if (!(event.getEntity() instanceof EntityPlayer)) { return; }
        EntityPlayer player = (EntityPlayer) event.getEntity();

        Entity cause = event.getSource().getTrueSource();
        String name = cause == null ? event.getSource().getDamageType() : cause.getName(); 

        sendEventToDiscord(
            new UpdateServer(
                new PlayerUpdate(
                    Universe.get().getPlayer(player),null
                ),
                new PlayerUpdate(
                    new SentPlayer(name), null
                ),
                Status.PLAYER_DEATH
            )
        );
    }

    @EventHandler
    public static void serverStart(FMLServerStartedEvent event) {
        sendEventToDiscord(
            new UpdateServer(null, null, Status.SERVER_START)
        );
    } 

    @EventHandler
    public static void serverStop(FMLServerStoppedEvent event) {
        sendEventToDiscord(
            new UpdateServer(null, null, Status.SERVER_STOP)
        );
    } 

    //===================================================
    //                  Send Messages 
    //===================================================

    public static void sendEventToDiscord(UpdateServer update) {
        if (!Base.serverUp) { return; }
        
        String json = Base.gson.toJson(update);
        sendToDiscord(json, "sendevent/"+Universe.get().getUUID().toString());
   }
    /**
     * Push a message to the Discord server.
     * @param pMsg The player message. (UUID will be check!)
     */
    private static void sendMessageToDiscord(PlayerMsg pMsg) {
        if (!Base.serverUp) { return; }
        
        String json = Base.gson.toJson(pMsg);
        sendToDiscord(json, "sentmessage");
    }

    private static void sendToDiscord(String json, String path) {
        try {
            // TODO this async
			HttpPost post = new HttpPost(Base.siteUri+path);
			post.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

			CloseableHttpClient client = HttpClients.createDefault();
			client.execute(post);
			client.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Push a message to minecraft.
     */
    public static void sendMessageToMinecraft(PlayerMsg pMsg) {

        String msg =  "[§3"+pMsg.player.getName()+"§r] "+pMsg.msg;
        Universe.get().server.getPlayerList().sendMessage(new TextComponentString(msg));
        
    }

    
    /**
    * Subscribe to the queue of Discord messages.
    * Note: This method will block!
    */
    public void readDiscordMessages() {
        if (!Base.serverUp) { return; }

        try {
            HttpGet get = new HttpGet(Base.siteUri+"listenforchats/"+Universe.get().getUUID().toString());
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(get);

            // will block
            InputStream read = response.getEntity().getContent();
            byte[] buffer = new byte[1024];            
            while (running) {
                // Read into buffer
                int bytes = read.read(buffer);
                if (bytes > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : buffer) {
                        sb.append((char) b);
                    }
                    // Clean up the front of the string
                    int index = sb.indexOf(":");
                    sb.delete(0, index+1);
                    String msg = sb.toString().trim();

                    try {
                        PlayerMsg playerMsg = Base.gson.fromJson(msg, PlayerMsg.class);
                        if (playerMsg == null) {
                            Base.getLogger().warn("[Non-Fatal]: PlayerMsg creation failed!");
                            continue;
                        }
                        // might need to build a queue that empties based on
                        // time passage
                        sendMessageToMinecraft(playerMsg);
                    } catch (JsonSyntaxException e) {
                        Base.getLogger().error(e.getStackTrace());
                    }
                    // Get a new buffer to read into
                    buffer = new byte[1024];
                }
            }
            
            client.close();
        } catch (IOException e) { e.printStackTrace(); }


    }

    @Override
    public void run() {
        
        running = true;
        readDiscordMessages();
    }

}
