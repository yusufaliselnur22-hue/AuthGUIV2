package com.authgui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {

    private final AuthGUIPlugin plugin;

    public GUIManager(AuthGUIPlugin plugin) {
        this.plugin = plugin;
    }

    public void reloadConfig() {
        // Config reloaded via plugin.getConfig()
    }

    public void openAuthGUI(Player player) {
        boolean registered = plugin.getAuthManager().isRegistered(player.getUniqueId());

        int rows = plugin.getConfig().getInt("gui.rows", 3);
        String title = AuthGUIPlugin.colorize(plugin.getConfig().getString("gui.title", "&8AuthGUI"));

        Inventory inv = Bukkit.createInventory(null, rows * 9, net.kyori.adventure.text.Component.text(
                AuthGUIPlugin.colorize(plugin.getConfig().getString("gui.title", "&8AuthGUI"))
        ));

        // Fill with filler if enabled
        if (plugin.getConfig().getBoolean("filler.enabled", true)) {
            Material fillerMat = getMaterial(plugin.getConfig().getString("filler.material", "GRAY_STAINED_GLASS_PANE"));
            String fillerName = plugin.getConfig().getString("filler.name", " ");
            ItemStack filler = createItem(fillerMat, AuthGUIPlugin.colorize(fillerName), null);
            for (int i = 0; i < rows * 9; i++) {
                inv.setItem(i, filler);
            }
        }

        if (!registered) {
            // Show Register button
            String regName = plugin.getConfig().getString("register-button.name", "&a&l✦ Kayıt Ol");
            Material regMat = getMaterial(plugin.getConfig().getString("register-button.material", "EMERALD"));
            int regSlot = plugin.getConfig().getInt("register-button.slot", 11);
            List<String> regLore = plugin.getConfig().getStringList("register-button.lore");
            inv.setItem(regSlot, createItem(regMat, AuthGUIPlugin.colorize(regName), colorizeList(regLore)));
        } else {
            // Show Login button
            String loginName = plugin.getConfig().getString("login-button.name", "&b&l⚡ Giriş Yap");
            Material loginMat = getMaterial(plugin.getConfig().getString("login-button.material", "NETHER_STAR"));
            int loginSlot = plugin.getConfig().getInt("login-button.slot", 13);
            List<String> loginLore = plugin.getConfig().getStringList("login-button.lore");
            inv.setItem(loginSlot, createItem(loginMat, AuthGUIPlugin.colorize(loginName), colorizeList(loginLore)));
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(net.kyori.adventure.text.Component.text(name)
                    .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
            if (lore != null && !lore.isEmpty()) {
                List<net.kyori.adventure.text.Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(net.kyori.adventure.text.Component.text(line)
                            .decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
                }
                meta.lore(loreComponents);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private Material getMaterial(String name) {
        try {
            return Material.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Geçersiz material adı: " + name + " — STONE kullanılıyor.");
            return Material.STONE;
        }
    }

    private List<String> colorizeList(List<String> list) {
        List<String> result = new ArrayList<>();
        for (String s : list) {
            result.add(AuthGUIPlugin.colorize(s));
        }
        return result;
    }

    public boolean isAuthGUI(Inventory inventory) {
        if (inventory == null) return false;
        String guiTitle = AuthGUIPlugin.colorize(plugin.getConfig().getString("gui.title", "&8AuthGUI"));
        if (inventory.getViewers().isEmpty()) return false;
        // Check via the title stored in the inventory
        for (org.bukkit.entity.HumanEntity viewer : inventory.getViewers()) {
            if (viewer instanceof Player p) {
                String openTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(p.getOpenInventory().title());
                String expectedTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText()
                        .serialize(net.kyori.adventure.text.Component.text(guiTitle));
                return openTitle.equals(expectedTitle);
            }
        }
        return false;
    }
}
