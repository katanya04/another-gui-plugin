package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.Utils.ReflectionMethods;
import me.katanya04.anotherguiplugin.Utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class InventoryMenu implements Menu<Inventory>, InventoryHolder {
    protected Inventory contents; //on openMenu, a copy of this inventory is opened
    protected String name;
    protected boolean canInteract;
    protected boolean saveChanges;
    protected Set<Integer> protectedSlots;
    protected Consumer<InventoryClickEvent> onClickBehaviour;
    protected Consumer<InventoryCloseEvent> onCloseBehaviour;
    public InventoryMenu(String name, boolean canInteract, boolean saveChanges, Map<Integer, ItemStack> contents, InventoryType invType) {
        this(name, canInteract, saveChanges, Utils.mapToArray(contents, true), invType);
    }
    public InventoryMenu(String name, boolean canInteract, boolean saveChanges, Inventory contents) {
        this(name, canInteract, saveChanges, contents.getContents(), contents.getType());
    }
    public InventoryMenu(String name, boolean canInteract, boolean saveChanges, ItemStack[] contents, InventoryType invType) {
        this.name = name;
        this.canInteract = canInteract;
        this.saveChanges = saveChanges;
        protectedSlots = new HashSet<>();
        this.contents = newInventory(contents, invType, name);

        //default implementation
        onClickBehaviour = (e -> {
            if (!this.canInteract || this.protectedSlots.contains(e.getSlot()))
                e.setCancelled(true);
        });
        onCloseBehaviour = (e -> {
            if (this.saveChanges)
                setContents(e.getInventory());
        });
    }

    @Override
    public void clear() {
        this.contents.clear();
    }

    @Override
    public Inventory getContents() {
        return getInventory();
    }

    @Override
    public void openMenu(Player player) {
        Inventory inv = getInventory();
        player.openInventory(inv);
        if (this.contents.getType() != InventoryType.CHEST) {
            ReflectionMethods.setContents(player, inv);
        }
    }

    @Override
    public Inventory getInventory() {
        return newInventory(this.contents);
    }

    protected Inventory newInventory(Inventory inv) {
        return newInventory(inv.getContents(), inv.getType(), inv.getName());
    }

    protected Inventory newInventory(ItemStack[] contents, InventoryType invType, String name) {
        Inventory inv;
        if (Objects.requireNonNull(invType) == InventoryType.CHEST) {
            int size = Utils.ceilToMultipleOfNine(contents.length);
            int maxSize = 54;
            inv = Bukkit.createInventory(this, Math.min(size, maxSize), name);
        } else
            inv = Bukkit.createInventory(this, invType);
        inv.setContents(contents);
        return inv;
    }

    public static InventoryMenu getAnotherPlayersMenu(Player player) {
        InventoryHolder inv = player.getOpenInventory().getTopInventory().getHolder();
        if (inv instanceof InventoryMenu)
            return (InventoryMenu) inv;
        else
            return null;
    }

    public void setOnCloseBehaviour(Consumer<InventoryCloseEvent> onCloseBehaviour) {
        this.onCloseBehaviour = onCloseBehaviour;
    }

    public void setOnClickBehaviour(Consumer<InventoryClickEvent> onClickBehaviour) {
        this.onClickBehaviour = onClickBehaviour;
    }

    public Set<Integer> getProtectedSlots() {
        return protectedSlots;
    }

    public void setProtectedSlots(Set<Integer> protectedSlots) {
        this.protectedSlots = protectedSlots;
    }

    public void addProtectedSlots(int... slots) {
        this.protectedSlots.addAll(Arrays.stream(slots).boxed().collect(Collectors.toSet()));
    }

    public void setContents(Inventory contents) {
        this.contents = contents;
    }

    public boolean canInteract() {
        return canInteract;
    }

    public boolean areChangesSaved() {
        return saveChanges;
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (e.getClickedInventory() != null && e.getInventory().getHolder() instanceof InventoryMenu) {
                ((InventoryMenu) e.getInventory().getHolder()).onClickBehaviour.accept(e);
            }
        }
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            if (e.getInventory().getHolder() instanceof InventoryMenu) {
                ((InventoryMenu) e.getInventory().getHolder()).onCloseBehaviour.accept(e);
            }
        }
    }
}
