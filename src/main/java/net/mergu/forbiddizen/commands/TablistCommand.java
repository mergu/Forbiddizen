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
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import net.mergu.forbiddizen.packets.WrapperPlayServerPlayerInfo;

import java.util.Arrays;
import java.util.UUID;

public class TablistCommand extends AbstractCommand {

    public TablistCommand() {
        setName("tablist");
        setSyntax("tablist (remove) [id:<id>/<player>] (name:<name>) (display_name:<name>) (skin_blob:<skin_blob>) (ping:<ping>)");
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
            else if (arg.matches("remove")) {
                scriptEntry.addObject("action", arg.asElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("id")) {
            throw new InvalidArgumentsException("Must have an id");
        }
        scriptEntry.defaultObject("name", new ElementTag(""));
        scriptEntry.defaultObject("action", new ElementTag("add"));
        scriptEntry.defaultObject("ping", new ElementTag("0"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag action = scriptEntry.getElement("action");
        ElementTag id = scriptEntry.getElement("id");
        ElementTag name = scriptEntry.getElement("name");
        ElementTag displayName = scriptEntry.getElement("display_name");
        ElementTag skinBlob = scriptEntry.getElement("skin_blob");
        ElementTag ping = scriptEntry.getElement("ping");

        WrapperPlayServerPlayerInfo packet = new WrapperPlayServerPlayerInfo();
        if (CoreUtilities.equalsIgnoreCase(action.asString(), "add")) {
            packet.setAction(EnumWrappers.PlayerInfoAction.ADD_PLAYER);
        }
        else if (CoreUtilities.equalsIgnoreCase(action.asString(), "remove")) {
            packet.setAction(EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
        }

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

        PlayerInfoData playerInfoData = new PlayerInfoData(
                gameProfile, ping.asInt(), EnumWrappers.NativeGameMode.SURVIVAL, wrappedChatComponent);
        packet.setData(Arrays.asList(playerInfoData));
        packet.sendPacket(Utilities.getEntryPlayer(scriptEntry).getPlayerEntity());
    }
}
