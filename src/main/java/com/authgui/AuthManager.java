package com.authgui;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

public class AuthManager {

    private final AuthGUIPlugin plugin;
    private File dataFile;
    private FileConfiguration dataConfig;

    // UUID -> hashed password
    private final Map<UUID, String> passwords = new HashMap<>();
    // UUID -> salt
    private final Map<UUID, String> salts = new HashMap<>();
    // UUID -> logged in state
    private final Set<UUID> loggedIn = new HashSet<>();
    // UUID -> failed attempt count
    private final Map<UUID, Integer> failedAttempts = new HashMap<>();
    // UUID -> lockout end time
    private final Map<UUID, Long> lockoutTimes = new HashMap<>();

    // Chat input states
    public enum InputState { NONE, AWAITING_REGISTER_PASSWORD, AWAITING_REGISTER_CONFIRM, AWAITING_LOGIN_PASSWORD }
    private final Map<UUID, InputState> inputStates = new HashMap<>();
    private final Map<UUID, String> tempPasswords = new HashMap<>();

    private int maxAttempts;
    private int lockoutDuration;

    public AuthManager(AuthGUIPlugin plugin) {
        this.plugin = plugin;
        loadData();
        reloadConfig();
    }

    public void reloadConfig() {
        maxAttempts = plugin.getConfig().getInt("security.max-login-attempts", 3);
        lockoutDuration = plugin.getConfig().getInt("security.lockout-duration", 5);
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("playerdata.yml oluşturulamadı: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (dataConfig.getConfigurationSection("players") != null) {
            for (String uuidStr : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String hash = dataConfig.getString("players." + uuidStr + ".password");
                    String salt = dataConfig.getString("players." + uuidStr + ".salt");
                    if (hash != null && salt != null) {
                        passwords.put(uuid, hash);
                        salts.put(uuid, salt);
                    }
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }

    public void saveData() {
        for (Map.Entry<UUID, String> entry : passwords.entrySet()) {
            String uuidStr = entry.getKey().toString();
            dataConfig.set("players." + uuidStr + ".password", entry.getValue());
            dataConfig.set("players." + uuidStr + ".salt", salts.get(entry.getKey()));
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("playerdata.yml kaydedilemedi: " + e.getMessage());
        }
    }

    public boolean isRegistered(UUID uuid) {
        return passwords.containsKey(uuid);
    }

    public boolean isLoggedIn(UUID uuid) {
        return loggedIn.contains(uuid);
    }

    public void register(UUID uuid, String password) {
        String salt = generateSalt();
        String hash = hashPassword(password, salt);
        passwords.put(uuid, hash);
        salts.put(uuid, salt);
        loggedIn.add(uuid);
        failedAttempts.remove(uuid);
        saveData();
    }

    public boolean login(UUID uuid, String password) {
        if (isLockedOut(uuid)) return false;
        String salt = salts.get(uuid);
        String hash = hashPassword(password, salt);
        if (hash != null && hash.equals(passwords.get(uuid))) {
            loggedIn.add(uuid);
            failedAttempts.remove(uuid);
            lockoutTimes.remove(uuid);
            return true;
        } else {
            int attempts = failedAttempts.getOrDefault(uuid, 0) + 1;
            failedAttempts.put(uuid, attempts);
            if (attempts >= maxAttempts) {
                lockoutTimes.put(uuid, System.currentTimeMillis() + (lockoutDuration * 1000L));
                failedAttempts.put(uuid, 0);
            }
            return false;
        }
    }

    public boolean isLockedOut(UUID uuid) {
        Long lockoutEnd = lockoutTimes.get(uuid);
        if (lockoutEnd == null) return false;
        if (System.currentTimeMillis() >= lockoutEnd) {
            lockoutTimes.remove(uuid);
            return false;
        }
        return true;
    }

    public void logout(UUID uuid) {
        loggedIn.remove(uuid);
        inputStates.remove(uuid);
        tempPasswords.remove(uuid);
        failedAttempts.remove(uuid);
    }

    public InputState getInputState(Player player) {
        return inputStates.getOrDefault(player.getUniqueId(), InputState.NONE);
    }

    public void setInputState(Player player, InputState state) {
        inputStates.put(player.getUniqueId(), state);
    }

    public void setTempPassword(Player player, String password) {
        tempPasswords.put(player.getUniqueId(), password);
    }

    public String getTempPassword(Player player) {
        return tempPasswords.get(player.getUniqueId());
    }

    public void clearTempPassword(Player player) {
        tempPasswords.remove(player.getUniqueId());
    }

    private String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashed) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            plugin.getLogger().severe("SHA-256 hash hatası: " + e.getMessage());
            return null;
        }
    }
}
