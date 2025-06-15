package fr.kenda.fasurvie.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

public class InventorySerialize {
    public static String serializeInventory(PlayerInventory inventory) throws Exception {
        ByteArrayOutputStream outputStream2 = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream2);
        dataOutput.writeInt(inventory.getSize());
        for (ItemStack item : inventory.getContents()) {
            dataOutput.writeObject(item);
        }
        dataOutput.close();
        return Base64.getEncoder().encodeToString(outputStream2.toByteArray());
    }

    public static ItemStack[] deserializeInventory(String base64) throws Exception {
        ByteArrayInputStream inputStream2 = new ByteArrayInputStream(Base64.getDecoder().decode(base64));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream2);
        int size = dataInput.readInt();
        ItemStack[] items = new ItemStack[size];
        for (int i = 0; i < size; ++i) {
            items[i] = (ItemStack) dataInput.readObject();
        }
        dataInput.close();
        return items;
    }
}

