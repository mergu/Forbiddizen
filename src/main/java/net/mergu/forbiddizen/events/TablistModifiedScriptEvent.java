package net.mergu.forbiddizen.events;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import net.mergu.forbiddizen.packets.WrapperPlayServerPlayerInfo;

public class TablistModifiedScriptEvent extends BukkitScriptEvent {

    public static TablistModifiedScriptEvent instance;
    public PacketEvent event;
    public WrapperPlayServerPlayerInfo packet;
    public boolean enabled;

    public TablistModifiedScriptEvent() {
        instance = this;
        registerCouldMatcher("tablist modified");
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public boolean matches(ScriptPath path) {
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "TablistModified";
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "action":
                return new ElementTag(packet.getAction().name());
            case "player_data":
                ListTag list = new ListTag();
                for (PlayerInfoData infoData : packet.getData()) {
                    MapTag map = new MapTag();
                    map.putObject("ping", new ElementTag(infoData.getLatency()));
                    map.putObject("name", new ElementTag(infoData.getProfile().getName()));
                    map.putObject("gamemode", new ElementTag(CoreUtilities.toLowerCase(infoData.getGameMode().name())));
                    if (infoData.getProfile().getUUID() != null) {
                        map.putObject("uuid", new ElementTag(infoData.getProfile().getUUID().toString()));
                    }
                    if (infoData.getDisplayName() != null) {
                        map.putObject("display_name", new ElementTag(FormattedTextHelper.stringify(
                                ComponentSerializer.parse(infoData.getDisplayName().getJson()), ChatColor.WHITE)
                        ));
                    }
                    list.addObject(map);
                }
                return list;
        }
        return super.getContext(name);
    }

    @Override
    public void cancellationChanged() {
        if (cancelled) {
            event.setCancelled(true);
        }
    }

    @Override
    public void init() {
        enabled = true;
    }

    @Override
    public void destroy() {
        enabled = false;
    }

    public void handle(PacketEvent event) {
        this.event = event;
        this.packet = new WrapperPlayServerPlayerInfo(event.getPacket());
        cancelled = false;
        fire();
        //if (!cancelled) {
        //    event.setPacket(packet.getHandle());
        //}
    }
}