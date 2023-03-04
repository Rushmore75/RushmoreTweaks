package com.basemod.base;

import com.basemod.base.util.Resource;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = Resource.MOD_ID, name = Resource.NAME, version = Resource.VERSION)
public class Base {

    @Instance
    public static Base instance;
    private static Logger logger; // used to print messages to our console output


    /** This is the final initialization event. Register actions from other mods here*/
    @EventHandler
    public static void postInit(FMLPostInitializationEvent event) {
    }
}