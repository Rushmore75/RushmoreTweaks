package com.basemod.base.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.Universe;

@EventBusSubscriber
public class RuleEnforcer {

    @SubscribeEvent
	public static void playerDeath(LivingDeathEvent event) {
       
        // is the dieing entity a player
		if (event.getEntity() instanceof EntityPlayer) {
            // is the killer a player
            if (event.getSource().getTrueSource() instanceof EntityPlayer) {
                EntityPlayer victim = (EntityPlayer) event.getEntity();
                EntityPlayer attacker = (EntityPlayer) event.getSource().getTrueSource();
                // victim.getUniqueID();
                // victim.getPersistentID();
                // 
                ForgePlayer fp_victim = Universe.get().getPlayer(victim.getGameProfile());
                ForgePlayer fp_attacker = Universe.get().getPlayer(attacker.getGameProfile());
                // TODO impl rules as described in discord
            }           
		}
	} 
}
