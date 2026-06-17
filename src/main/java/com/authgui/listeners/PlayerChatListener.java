package com.authgui.listeners;

import com.authgui.AuthGUIPlugin;
import com.authgui.AuthManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitRunnable;

@SuppressWarnings("deprecation")
public class PlayerChatListener implements Listener {

    private final AuthGUIPlugin plugin;

    public PlayerChatListener(AuthGUIPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        AuthManager.InputState state = plugin.getAuthManager().getInputState(player);

        if (state == AuthManager.InputState.NONE) {
            // Block chat if not logged in
            if (!plugin.getAuthManager().isLoggedIn(player.getUniqueId())) {
                event.setCancelled(true);
                String prefix = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.prefix", "&8[&bAuth&8] &r"));
                String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.must-login",
                        "&cOyun alanına erişmek için giriş yapmalısınız!"));
                player.sendMessage(prefix + msg);
            }
            return;
        }

        event.setCancelled(true);
        String input = event.getMessage();

        // Allow cancellation
        if (input.equalsIgnoreCase("/q") || input.equalsIgnoreCase("q")) {
            plugin.getAuthManager().setInputState(player, AuthManager.InputState.NONE);
            plugin.getAuthManager().clearTempPassword(player);
            String prefix = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.prefix", "&8[&bAuth&8] &r"));
            String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.cancelled", "&eİşlem iptal edildi."));
            player.sendMessage(prefix + msg);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        plugin.getGuiManager().openAuthGUI(player);
                    }
                }
            }.runTaskLater(plugin, 2L);
            return;
        }

        String prefix = AuthGUIPlugin.colorize(plugin.getConfig().getString("messages.prefix", "&8[&bAuth&8] &r"));

        switch (state) {
            case AWAITING_REGISTER_PASSWORD -> {
                if (input.length() < 4) {
                    String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                            "messages.register-password-too-short",
                            "&c&l✘ Şifre en az 4 karakter olmalıdır!"));
                    player.sendMessage(prefix + msg);
                    return;
                }
                plugin.getAuthManager().setTempPassword(player, input);
                plugin.getAuthManager().setInputState(player, AuthManager.InputState.AWAITING_REGISTER_CONFIRM);
                String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                        "messages.register-confirm-prompt",
                        "&aŞifrenizi tekrar girin &7(onaylamak için):"));
                player.sendMessage(prefix + msg);
            }

            case AWAITING_REGISTER_CONFIRM -> {
                String tempPass = plugin.getAuthManager().getTempPassword(player);
                if (tempPass == null || !tempPass.equals(input)) {
                    String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                            "messages.register-password-mismatch",
                            "&c&l✘ Şifreler eşleşmiyor! Tekrar deneyin."));
                    player.sendMessage(prefix + msg);
                    plugin.getAuthManager().setInputState(player, AuthManager.InputState.AWAITING_REGISTER_PASSWORD);
                    plugin.getAuthManager().clearTempPassword(player);
                    String retryMsg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                            "messages.register-prompt",
                            "&aŞifrenizi girin. &7(Sohbete yazın, &c/q &7ile iptal edin)"));
                    player.sendMessage(prefix + retryMsg);
                } else {
                    plugin.getAuthManager().register(player.getUniqueId(), input);
                    plugin.getAuthManager().setInputState(player, AuthManager.InputState.NONE);
                    plugin.getAuthManager().clearTempPassword(player);
                    String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                            "messages.register-success",
                            "&a&l✔ Başarıyla kayıt oldunuz! Sunucuya hoş geldiniz."));
                    player.sendMessage(prefix + msg);
                }
            }

            case AWAITING_LOGIN_PASSWORD -> {
                if (plugin.getAuthManager().isLockedOut(player.getUniqueId())) {
                    String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                            "messages.login-too-many-attempts",
                            "&c&l✘ Çok fazla başarısız deneme! &75 saniye bekleyin."));
                    player.sendMessage(prefix + msg);
                    plugin.getAuthManager().setInputState(player, AuthManager.InputState.NONE);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (player.isOnline()) plugin.getGuiManager().openAuthGUI(player);
                        }
                    }.runTaskLater(plugin, 2L);
                    return;
                }

                boolean success = plugin.getAuthManager().login(player.getUniqueId(), input);
                if (success) {
                    plugin.getAuthManager().setInputState(player, AuthManager.InputState.NONE);
                    String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                            "messages.login-success", "&a&l✔ Başarıyla giriş yaptınız!"));
                    player.sendMessage(prefix + msg);
                } else {
                    String msg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                            "messages.login-wrong-password",
                            "&c&l✘ Yanlış şifre! Tekrar deneyin."));
                    player.sendMessage(prefix + msg);
                    if (!plugin.getAuthManager().isLockedOut(player.getUniqueId())) {
                        String retryMsg = AuthGUIPlugin.colorize(plugin.getConfig().getString(
                                "messages.login-prompt",
                                "&aŞifrenizi girin. &7(Sohbete yazın, &c/q &7ile iptal edin)"));
                        player.sendMessage(prefix + retryMsg);
                    } else {
                        plugin.getAuthManager().setInputState(player, AuthManager.InputState.NONE);
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (player.isOnline()) plugin.getGuiManager().openAuthGUI(player);
                            }
                        }.runTaskLater(plugin, 2L);
                    }
                }
            }

            default -> {}
        }
    }
}
