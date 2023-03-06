package com.basemod.base;

import com.feed_the_beast.ftblib.lib.command.CmdBase;
import com.feed_the_beast.ftblib.lib.data.Universe;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CmdGetUniverse extends CmdBase {

    public CmdGetUniverse() {
        super("getUniverse", Level.ALL);
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        String uuid = Universe.get().getUUID().toString();
        TextComponentString msg = new TextComponentString(uuid+" (Hover Me)");
        msg.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to put into chat bar, then CTRL+A, CTRL+C to copy.")));
        msg.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, uuid));
        msg.getStyle().setColor(TextFormatting.YELLOW);
        
        sender.sendMessage(msg);
    }
    
}
