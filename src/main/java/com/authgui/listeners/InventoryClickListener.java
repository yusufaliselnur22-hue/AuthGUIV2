package com.authgui.listeners;

import com.authgui.AuthGUIPlugin;
import com.authgui.AuthManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryClickListener implements Listener {

    private final AuthGUIPlugin plugin;

    public InventoryClickListener(AuthGUIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        // Check if this is the auth GUI
        String guiTitle = AuthGUIPlugin.colorize(plugin.getConfig().getString("gui.title", "&8AuthGUI"));
        String invTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        String expectedTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(net.kyori.adventure.text.Component.text(guiTitle));

        if (!invTitle.equals(expectedTitle)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() == null) return;
        if (event.getCurrentItem().getType().isAir()) return;

        boolean registered = plugin.getAuthManager().isRegistered(player.getUniqueId());
        int clickedSlot = event.getSlot();

        if (!registered) {
            int regSlot = plugin.getConfig().getInt("register-button.slot", 11);
            if (clickedSlot == regSlot) {
                player.closeInventory();
                startRegisterFlow(player);
            }
        } else {
            int loginSlot = plugin.getConfig().getInt("login-button.slot", 13);
            if (clickedSlot == loginSlot) {
                player.closeInventory();
                startLoginFlow(player);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        if (plugin.getAuthManager().isLoggedIn(player.getUniqueId())) return;

        AuthManager.InputState state = plugin.getAuthManager().getInputState(player);
        if (state != AuthManager.InputState.NONE) return;

        // If inventory closed while not logged in and not in chat input flow, reopen
        String guiTitle = AuthGUIPlugin.colorize(plugin.getConfig().getString("gui.title", "&8AuthGUI"));
        String invTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(event.getView().title());
        String expectedTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                .serialize(net.kyori.adventure.text.Component.text(guiTitle));

        if (invTitle.equals(expectedTitle)) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline() && !plugin.getAuthManager().isLoggedIn(player.getUniqueId())
                            && plugin.getAuthManager().getInputState(player) == AuthManager.InputState.NONE) {
                        plugin.getGuiManager().openAuthGUI(player);
                    }
                }
            }.runTaskLater(plugin, 2L);
        }
    }

    private void startRegisterFlow(Player player) {
        plugin.getAuthManager().setInputState(player, AuthManager.InputState.AWAITING_REGISTER_PASSWORD);
        String prefix = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.prefix", "&8[&bAuth&8] &r"));
        String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.register-prompt",
                "&aŞifrenizi girin. &7(Sohbete yazın, &c/q &7ile iptal edin)"));
        player.sendMessage(prefix + msg);
    }

    private void startLoginFlow(Player player) {
        plugin.getAuthManager().setInputState(player, AuthManager.InputState.AWAITING_LOGIN_PASSWORD);
        String prefix = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.prefix", "&8[&bAuth&8] &r"));
        String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.login-prompt",
                "&aŞifrenizi girin. &7(Sohbete yazın, &c/q &7ile iptal edin)"));
        player.sendMessage(prefix + msg);
    }
}
