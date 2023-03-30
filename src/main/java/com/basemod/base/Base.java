package com.basemod.base;

import com.basemod.base.event.DiscordRP;
import com.basemod.base.util.UpdateServer;
import com.basemod.base.util.UpdateServer.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;

import org.apache.logging.log4j.Logger;

@Mod(modid = Base.MOD_ID, name = Base.NAME, version = Base.VERSION, serverSideOnly = Base.SERVER_SIDE)
public class Base {

    public final static Gson gson = new GsonBuilder()
        .serializeNulls()
        .create();
    public final static String siteUri = "http://127.0.0.1:8000/";
    public static final String MOD_ID = "rushmoretweaks";
    public static final String NAME = "Rushmore Tweaks";
    public static final String VERSION = "1.0.0";
    public static final boolean SERVER_SIDE = true; 

    public static Logger getLogger() { return logger; } 
    public static void setLogger(Logger er) { logger = er; }

    @Mod.Instance(MOD_ID)
    public static Base instance;
    private static Logger logger; // used to print messages to our console output
    
    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        Base.setLogger(event.getModLog());
    }

    /** This is the final initialization event. Register actions from other mods here*/
    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
        DiscordRP.checkServer();
        // ClientCommandHandler.instance.registerCommand(new CmdGetUniverse());
    }
   
    @Mod.EventHandler
    public static void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new CmdGetUniverse());
    }
    
    @Mod.EventHandler
    public static void serverStarted(FMLServerStartedEvent event) {
        Base.getLogger().info("Server starting...");
        DiscordRP.sendEventToDiscord(
            new UpdateServer(null, null, Status.SERVER_START)
            );       
        // FMLServerAboutToStartEvent <- Universe Instance loaded
        // FMLServerStartingEvent 
        // FMLServerStartedEvent <- Universe data loaded from disk
    
        new DiscordRP().start();
    }

    @Mod.EventHandler
    public static void serverStop(FMLServerStoppedEvent event) {
        DiscordRP.sendEventToDiscord(
                new UpdateServer(null, null, Status.SERVER_STOP));
    }


}
