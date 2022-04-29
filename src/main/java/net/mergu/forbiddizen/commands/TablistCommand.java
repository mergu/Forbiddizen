package net.mergu.forbiddizen.commands;

import com.comphenix.protocol.wrappers.*;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import net.mergu.forbiddizen.packets.WrapperPlayServerPlayerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TablistCommand extends AbstractCommand {

    public TablistCommand() {
        setName("old_tablist");
        setSyntax("old_tablist [id:<id>/<player>] (action:<action>) (name:<name>) (display_name:<name>) (skin_blob:<skin_blob>) (ping:<ping>) (gamemode:<gamemode>)");
        setRequiredArguments(1, -1);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesPrefix("id")) {
                if (arg.matchesArgumentType(PlayerTag.class)) {
                    scriptEntry.addObject("id", new ElementTag(arg.asType(PlayerTag.class).getUUID().toString()));
                }
                else {
                    scriptEntry.addObject("id", arg.asElement());
                }
            }
            else if (arg.matchesPrefix("name")) {
                scriptEntry.addObject("name", arg.asElement());
            }
            else if (arg.matchesPrefix("display_name")) {
                scriptEntry.addObject("display_name", arg.asElement());
            }
            else if (arg.matchesPrefix("skin_blob")) {
                scriptEntry.addObject("skin_blob", arg.asElement());
            }
            else if (arg.matchesPrefix("ping") && arg.matchesInteger()) {
                scriptEntry.addObject("ping", arg.asElement());
            }
            // ADD_PLAYER, REMOVE_PLAYER, UPDATE_LATENCY, UPDATE_GAME_MODE, UPDATE_DISPLAY_NAME
            else if (arg.matchesPrefix("action") && arg.matchesEnum(EnumWrappers.PlayerInfoAction.values())) {
                scriptEntry.addObject("action", arg.asElement());
            }
            // SURVIVAL, ADVENTURE, CREATIVE, SPECTATOR
            else if (arg.matchesPrefix("gamemode") && arg.matchesEnum(EnumWrappers.NativeGameMode.values())) {
                scriptEntry.addObject("gamemode", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must have an id");
        }
        scriptEntry.defaultObject("name", new ElementTag(""));
        scriptEntry.defaultObject("action", new ElementTag("add_player"));
        scriptEntry.defaultObject("ping", new ElementTag("0"));
        scriptEntry.defaultObject("gamemode", new ElementTag("survival"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag action = scriptEntry.getElement("action");
        ElementTag id = scriptEntry.getElement("id");
        ElementTag name = scriptEntry.getElement("name");
        ElementTag displayName = scriptEntry.getElement("display_name");
        ElementTag skinBlob = scriptEntry.getElement("skin_blob");
        ElementTag ping = scriptEntry.getElement("ping");
        ElementTag gamemode = scriptEntry.getElement("gamemode");

        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
        packet.setAction(EnumWrappers.PlayerInfoAction.valueOf(action.asString().toUpperCase()));

        WrappedGameProfile gameProfile = new WrappedGameProfile(UUID.fromString(id.asString()), name.asString());
        if (skinBlob != null && skinBlob.asString().contains(";")) {
            String[] splitBlob = skinBlob.asString().split(";", 2);
            gameProfile.getProperties().put("textures", new WrappedSignedProperty("textures", splitBlob[0], splitBlob[1]));
        }

        WrappedChatComponent wrappedChatComponent = null;
        if (displayName != null) {
            String displayJson = ComponentSerializer.toString(
                    FormattedTextHelper.parse(displayName.asString(), ChatColor.WHITE));
            wrappedChatComponent = WrappedChatComponent.fromJson(displayJson);
        }

        EnumWrappers.NativeGameMode nativeGameMode = EnumWrappers.NativeGameMode.valueOf(gamemode.asString().toUpperCase());

        PlayerInfoData playerInfoData = new PlayerInfoData(gameProfile, ping.asInt(), nativeGameMode, wrappedChatComponent);
        List<PlayerInfoData> playerInfoDataList = new ArrayList<>();
        playerInfoDataList.add(playerInfoData);

        packet.setData(playerInfoDataList);
        packet.sendPacket(Utilities.getEntryPlayer(scriptEntry).getPlayerEntity());
    }
}