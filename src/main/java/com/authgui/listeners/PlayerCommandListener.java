package com.authgui.listeners;

import com.authgui.AuthGUIPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class PlayerCommandListener implements Listener {

    private final AuthGUIPlugin plugin;

    public PlayerCommandListener(AuthGUIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (plugin.getAuthManager().isLoggedIn(player.getUniqueId())) return;

        // Allow /q for cancellation during input flow
        String cmd = event.getMessage().toLowerCase().trim();
        if (cmd.equals("/q")) return;

        event.setCancelled(true);
        String prefix = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.prefix", "&8[&bAuth&8] &r"));
        String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.must-login",
                "&cOyun alanına erişmek için giriş yapmalısınız!"));
        player.sendMessage(prefix + msg);
    }
}
