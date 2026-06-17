package com.authgui.listeners;

import com.authgui.AuthGUIPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final AuthGUIPlugin plugin;

    public PlayerQuitListener(AuthGUIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getAuthManager().logout(event.getPlayer().getUniqueId());
    }
}
