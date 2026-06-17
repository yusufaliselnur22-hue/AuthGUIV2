package com.authgui.listeners;

import com.authgui.AuthGUIPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinListener implements Listener {

    private final AuthGUIPlugin plugin;

    public PlayerJoinListener(AuthGUIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();

        // Open GUI on next tick so inventory is ready
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    plugin.getGuiManager().openAuthGUI(player);
                }
            }
        }.runTaskLater(plugin, 5L);
    }
}
