package com.basemod.base.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import com.feed_the_beast.ftblib.lib.data.ForgePlayer;
import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftblib.lib.data.Universe;
import com.feed_the_beast.ftblib.lib.math.ChunkDimPos;
import com.feed_the_beast.ftbutilities.data.ClaimedChunk;
import com.feed_the_beast.ftbutilities.data.ClaimedChunks;
import com.basemod.base.Base;
import com.feed_the_beast.ftblib.events.team.ForgeTeamConfigEvent;
import com.feed_the_beast.ftblib.events.team.ForgeTeamConfigSavedEvent;

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
    
    @SubscribeEvent
    public static void configSave(ForgeTeamConfigSavedEvent event) {
        Base.getLogger().info("Config Save");
        Base.getLogger().info(event.getConfig().toString());
        event.getConfig().getValue("desc");
        event.getConfig().getValue("ftblib");
        /*
        {
            ftbutilities={
                explosions=false,
                blocks_edit=ally,
                blocks_interact=ally,
                attack_entities=ally,
                use_items=ally
            }, 
            ftblib={
                display={
                    color=magenta,
                    fake_player_status=ally, 
                    title=conig, 
                    desc=
                }, 
                free_to_join=false
            }
        } 
        */
    } 
    
    private static void chunkTest(ForgePlayer player) {
        // Universe.get().getTeams().stream().
        ChunkDimPos pos = new ChunkDimPos(player.getPlayer());
        ClaimedChunk cc = ClaimedChunks.instance.getChunk(pos);
        ForgeTeam ft = ClaimedChunks.instance.getChunkTeam(pos);

        // cc.getData().
        
    }


}
