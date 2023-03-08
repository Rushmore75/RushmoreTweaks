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
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.google.gson.JsonSyntaxException;
import com.ibm.icu.text.MessageFormat;

import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;


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

        String message = MessageFormat.format(
            "{0} achieved {1}",
            event.getEntityPlayer().getName(),
            event.getAdvancement().getDisplayText().getUnformattedText()
            );

        sendMessageToDiscord(new PlayerMsg(new SentPlayer("Server"), message));
    }



    // TODO add death events & join/leave messages
    //===================================================
    //                  Send Messages 
    //===================================================

    /**
     * Push a message to the Discord server.
     * @param pMsg The player message. (UUID will be check!)
     */
    private static void sendMessageToDiscord(PlayerMsg pMsg) {
        if (!Base.serverUp) { return; }
        
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
        sendMessageToDiscord(pMsg);
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
        
        running = true;
        readDiscordMessages();
    }

}
