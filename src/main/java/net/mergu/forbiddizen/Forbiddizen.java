package net.mergu.forbiddizen;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.events.ScriptEvent;
import net.mergu.forbiddizen.commands.TablistCommand;
import net.mergu.forbiddizen.events.PaperServerListPingScriptEvent;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

public final class Forbiddizen extends JavaPlugin {

    public static Forbiddizen instance;
    public static ProtocolManager protocolManager;

    @Override
    public void onEnable() {
        instance = this;
        protocolManager = ProtocolLibrary.getProtocolManager();
        try {
            DenizenCore.commandRegistry.registerCommand(TablistCommand.class);
            ScriptEvent.events.remove(ScriptEvent.eventLookup.get("serverlistping"));
            ScriptEvent.registerScriptEvent(new PaperServerListPingScriptEvent());
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
