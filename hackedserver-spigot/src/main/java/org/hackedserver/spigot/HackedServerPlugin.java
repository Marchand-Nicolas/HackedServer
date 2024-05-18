package org.hackedserver.spigot;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.hackedserver.core.config.ConfigsManager;
import org.hackedserver.core.config.Message;
import org.hackedserver.spigot.commands.CommandsManager;
import org.hackedserver.spigot.listeners.CustomPayloadListener;
import org.hackedserver.spigot.listeners.HackedPlayerListeners;
import org.hackedserver.spigot.listeners.HandShakeListener;
import org.hackedserver.spigot.utils.logs.Logs;

public class HackedServerPlugin extends JavaPlugin {

    private ProtocolManager protocolManager;
    private CustomPayloadListener customPayloadListener;
    private HandShakeListener handShakeListener;
    private BukkitAudiences audiences;
    private static HackedServerPlugin instance;

    public HackedServerPlugin() throws NoSuchFieldException, IllegalAccessException {
        instance = this;
        Logs.enableFilter(this);
        ConfigsManager.init(Logs.getLogger(), getDataFolder());
    }

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(true));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        audiences = BukkitAudiences.create(this);
        Logs.onEnable(audiences);
        new Metrics(this, 2008);
        Bukkit.getPluginManager().registerEvents(new HackedPlayerListeners(), this);
        protocolManager = ProtocolLibrary.getProtocolManager();
        customPayloadListener = new CustomPayloadListener(protocolManager, this);
        customPayloadListener.register();
        handShakeListener = new HandShakeListener(protocolManager, this);
        handShakeListener.register();
        new CommandsManager(this, audiences).loadCommands();
        Logs.logComponent(Message.PLUGIN_LOADED.toComponent());
    }

    @Override
    public void onDisable() {
        customPayloadListener.unregister();
        handShakeListener.unregister();
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    public static HackedServerPlugin get() {
        return instance;
    }
}
