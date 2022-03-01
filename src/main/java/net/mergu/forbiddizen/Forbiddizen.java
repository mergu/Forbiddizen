package net.mergu.forbiddizen;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import net.mergu.forbiddizen.commands.TablistCommand;
import net.mergu.forbiddizen.events.PaperServerListPingScriptEvent;
import net.mergu.forbiddizen.events.TablistModifiedScriptEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class Forbiddizen extends JavaPlugin {

    public static Forbiddizen instance;
    public static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        protocolManager.addPacketListener(new PacketAdapter(this, ListenerPriority.NORMAL, PacketType.Play.Server.PLAYER_INFO) {
            @Override
            public void onPacketSending(PacketEvent event) {
                if (event.getPacketType() == PacketType.Play.Server.PLAYER_INFO
                        && TablistModifiedScriptEvent.instance.enabled) {
                    TablistModifiedScriptEvent.instance.handle(event);
                }
            }
        });
        try {
            DenizenCore.commandRegistry.registerCommand(TablistCommand.class);
            ScriptEvent.events.remove(ScriptEvent.eventLookup.get("serverlistping"));
            ScriptEvent.registerScriptEvent(new PaperServerListPingScriptEvent());
            ScriptEvent.registerScriptEvent(new TablistModifiedScriptEvent());
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll(this);
    }
}