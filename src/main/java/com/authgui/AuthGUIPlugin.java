package com.authgui;

import com.authgui.listeners.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class AuthGUIPlugin extends JavaPlugin {

    private static AuthGUIPlugin instance;
    private AuthManager authManager;
    private GUIManager guiManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        authManager = new AuthManager(this);
        guiManager = new GUIManager(this);

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerMoveListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerCommandListener(this), this);

        getLogger().info("AuthGUI etkinleştirildi!");
    }

    @Override
    public void onDisable() {
        if (authManager != null) {
            authManager.saveData();
        }
        getLogger().info("AuthGUI devre dışı bırakıldı.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("authgui")) {
            if (!sender.hasPermission("authgui.admin")) {
                sender.sendMessage(colorize(getConfig().getString("messages.prefix", "&8[&bAuth&8] &r")
                        + getConfig().getString("messages.reload-no-permission", "&cBu komutu kullanmak için izniniz yok!")));
                return true;
            }
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                authManager.reloadConfig();
                guiManager.reloadConfig();
                sender.sendMessage(colorize(getConfig().getString("messages.prefix", "&8[&bAuth&8] &r")
                        + getConfig().getString("messages.reload-success", "&aYapılandırma dosyası yeniden yüklendi!")));
                return true;
            }
            sender.sendMessage(colorize("&7Kullanım: /authgui reload"));
            return true;
        }
        return false;
    }

    public static AuthGUIPlugin getInstance() {
        return instance;
    }

    public AuthManager getAuthManager() {
        return authManager;
    }

    public GUIManager getGuiManager() {
        return guiManager;
    }

    public static String colorize(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }
}
