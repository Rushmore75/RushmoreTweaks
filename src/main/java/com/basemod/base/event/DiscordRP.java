package com.basemod.base.event;

import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.google.gson.JsonSyntaxException;

import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;


@EventBusSubscriber
public class DiscordRP extends Thread {
   
    private boolean running = false; // This can probably go away

    //===================================================
    //                  Start Events 
    //===================================================
    @SubscribeEvent
    public static void chatEvent(ServerChatEvent event) {
        // !NOTE! Doesn't grab things like advancements

        if (!Base.serverUp) { return; }

        // perhaps a bit convoluted, but ultimately it should streamline things
        // on the server side.
        ForgePlayer player = Universe.get().getPlayer(event.getPlayer());
        PlayerMsg mp = new PlayerMsg(new SentPlayer(player, Universe.get()), event.getMessage()); 
        sendMessageToDiscord(mp);
    }
    // TODO add death events & achievements
    //===================================================
    //                  Stop Events 
    //===================================================




    
    /**
     * Push a message to the Discord server.
     * @param pMsg The player message. (UUID will be check!)
     */
    private static void sendMessageToDiscord(PlayerMsg pMsg) {
        String json = Base.gson.toJson(pMsg);

        try {
            // TODO this async
			HttpPost post = new HttpPost(Base.siteUri+"sentmessage");
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
    private static void sendMessageToMinecraft(PlayerMsg pMsg) {

        TextComponentString msg = new TextComponentString(
            "[§3"+pMsg.player.getName()+"§r] "+pMsg.msg
        );
        // FIXME i'm being rate limited or something
        Universe.get().server.getPlayerList().sendMessage(msg);
    }

    /**
     * Invokes both `sendMessageToMinecraft()` and
     * `sendMessageToDiscord()` sequentially.
     */
    public static void sendEverywhere(PlayerMsg pMsg) {
        sendMessageToMinecraft(pMsg);
        if (!Base.serverUp) { return; }
        sendMessageToDiscord(pMsg);
    }
    
    /**
    * Subscribe to the queue of Discord messages.
    * Note: This method will block!
    */
    public void readDiscordMessages() {
        
        try {
            HttpGet get = new HttpGet(Base.siteUri+"listenforchats/"+Universe.get().getUUID().toString());
            CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(get);

            // will block
            InputStream read = response.getEntity().getContent();
            byte[] buffer = new byte[1024];
            while (running) {
                int bytes = read.read(buffer);
                if (bytes > 1) {
                    StringBuilder sb = new StringBuilder();
                    for (byte b : buffer) {
                        sb.append((char) b);
                    }
                    // Clean up the front of the string
                    int index = sb.indexOf(":");
                    sb.delete(0, index+1);
                    String msg = sb.toString().trim();
                    Base.getLogger().info(msg);

                    try {
                        PlayerMsg playerMsg = Base.gson.fromJson(msg, PlayerMsg.class);
                        if (playerMsg == null) {
                            Base.getLogger().warn("[Non-Fatal]: PlayerMsg creation failed!");
                            continue;
                        }
                        sendMessageToMinecraft(playerMsg);
                    } catch (JsonSyntaxException e) {
                        Base.getLogger().entry(e.getStackTrace());
                    }
                }
            }
            
            client.close();
        } catch (IOException e) { e.printStackTrace(); }


    }

    @Override
    public void run() {
        // It would just cause errors to try
        // and read discord messages without
        // the bot actually being up.
        if (!Base.serverUp) { return; }
        running = true;
        readDiscordMessages();
    }

}
