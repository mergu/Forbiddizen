package net.mergu.forbiddizen.events;

import com.denizenscript.denizen.paper.events.ServerListPingScriptEventPaperImpl;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.Bukkit;

import java.util.List;

public class PaperServerListPingScriptEvent extends ServerListPingScriptEventPaperImpl {

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("num_players:") && ArgumentHelper.matchesInteger(determination.substring("num_players:".length()))) {
            ((PaperServerListPingEvent) event).setNumPlayers(new ElementTag(determination.substring("num_players:".length())).asInt());
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }
}
