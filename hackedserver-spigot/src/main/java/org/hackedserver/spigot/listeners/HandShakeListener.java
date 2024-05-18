package org.hackedserver.spigot.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import org.bukkit.entity.Player;
import org.hackedserver.core.HackedServer;
import org.hackedserver.core.config.Action;
import org.hackedserver.core.config.GenericCheck;
import org.hackedserver.spigot.HackedServerPlugin;
import org.hackedserver.spigot.utils.logs.ActionManager;

public class HandShakeListener {

    private final ProtocolManager protocolManager;
    private final PacketAdapter adapter;

    public HandShakeListener(ProtocolManager protocolManager, HackedServerPlugin plugin) {
        this.protocolManager = protocolManager;
        this.adapter = new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Handshake.Client.SET_PROTOCOL) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                PacketContainer packet = event.getPacket();
                StructureModifier<Object> modifier = packet.getModifier();
                Integer modifierSize = modifier.size();
                if (modifierSize != 4)
                    return;
                String type = packet.getModifier().read(3).toString();
                if (!type.equals("LOGIN"))
                    return;
                // Loop on every field
                for (int i = 0; i < modifier.size(); i++) {
                    Object buf = packet.getModifier().read(i);
                    String message = buf.toString();
                    for (GenericCheck check : HackedServer.getChecks())
                        if (check.softPass(message)) {
                            for (Action action : check.getActions()) {
                                ActionManager.performActions(action, player, true, Placeholder.unparsed("player",
                                        player.getName()), Placeholder.parsed("name", check.getName()));
                            }
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