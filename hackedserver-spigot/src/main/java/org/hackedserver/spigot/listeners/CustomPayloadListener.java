package org.hackedserver.spigot.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import io.netty.buffer.ByteBuf;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.nio.charset.StandardCharsets;

import org.bukkit.entity.Player;
import org.hackedserver.core.HackedPlayer;
import org.hackedserver.core.HackedServer;
import org.hackedserver.core.config.Action;
import org.hackedserver.core.config.GenericCheck;
import org.hackedserver.spigot.HackedServerPlugin;
import org.hackedserver.spigot.utils.logs.ActionManager;

public class CustomPayloadListener {

    private final ProtocolManager protocolManager;
    private final PacketAdapter adapter;

    public CustomPayloadListener(ProtocolManager protocolManager, HackedServerPlugin plugin) {
        this.protocolManager = protocolManager;
        this.adapter = new PacketAdapter(plugin, ListenerPriority.NORMAL, PacketType.Play.Client.CUSTOM_PAYLOAD) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                StructureModifier<Object> modifier = packet.getModifier();
                if (modifier.size() < 2)
                    return;
                ByteBuf buf = (ByteBuf) modifier.read(1);
                String channel = packet.getMinecraftKeys().read(0).getFullKey();
                HackedPlayer hackedPlayer = HackedServer.getPlayer(player.getUniqueId());
                String message = buf.toString(StandardCharsets.UTF_8);
                for (GenericCheck check : HackedServer.getChecks())
                    if (check.pass(channel, message)) {
                        hackedPlayer.addGenericCheck(check);
                        for (Action action : check.getActions()) {
                            ActionManager.performActions(action, player, false, Placeholder.unparsed("player",
                                    player.getName()), Placeholder.parsed("name", check.getName()));
                        }
                    }
            }
        };
    }

    public void register() {
        protocolManager.addPacketListener(adapter);
    }

    public void unregister() {
        protocolManager.removePacketListener(adapter);
    }
}