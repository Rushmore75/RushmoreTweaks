package com.basemod.base;

import com.basemod.base.event.DiscordRP;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.logging.log4j.Logger;

@Mod(modid = Base.MOD_ID, name = Base.NAME, version = Base.VERSION)
public class Base {

    public final static Gson gson = new GsonBuilder()
        .serializeNulls()
        .create();
    public final static String siteUri = "http://127.0.0.1:8000/";
    public static final String MOD_ID = "rushmoretweaks";
    public static final String NAME = "Rushmore Tweaks";
    public static final String VERSION = "1.0.0";
    // public static final String ACEPTED_VERSIONS = "[1.12.2]";

    public static boolean serverUp = true;
    
    public static void checkServer() {
         try {
            URL url = new URL(siteUri+"version");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            
            InputStream res = con.getInputStream();
            int b = 0;
            StringBuilder sb = new StringBuilder();
            while ((b = res.read()) != -1) {
                sb.append((char)b);
            }
            res.close(); 

            logger.info(sb.toString());
            // TODO finish version check

        } catch (IOException e) {
            serverUp = false;
            logger.warn("Unable to connect to external server, this pretty much invalidates the mod.");
        }       
        if (serverUp) {
            logger.info("Connected to server!");
        }
    }

    public static Logger getLogger() { return logger; } 

    @Instance
    public static Base instance;
    private static Logger logger; // used to print messages to our console output

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
    }

    /** This is the final initialization event. Register actions from other mods here*/
    @EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
        checkServer();
        // ClientCommandHandler.instance.registerCommand(new CmdGetUniverse());
    }
   
    @EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CmdGetUniverse());
    }
    
    @EventHandler
    public static void serverStarted(FMLServerStartedEvent event) {
        logger.info("Server starting...");
        // TODO @Subscribe(priority=EventPriority.LOWEST)
        // what is subscribe vs event handler?
        
        // Event order:
        // FMLServerAboutToStartEvent <- Universe Instance loaded
        // FMLServerStartingEvent 
        // FMLServerStartedEvent <- Universe data loaded from disk
    
        // Start discord transport
        new DiscordRP().start();

    }
    
}
