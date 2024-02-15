package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.InventoryMenuPlugin;
import me.katanya04.anotherguiplugin.Utils.ReflectionMethods;
import me.katanya04.anotherguiplugin.Utils.TreeNode;
import me.katanya04.anotherguiplugin.Utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class BookMenu implements Menu<BookMenu.Field> {
    protected static final int MAX_LENGTH = 15;
    protected final Field contents;
    public enum ActionType {REMOVE, MODIFY, ADD}
    protected CommandFunction commandFunction;
    protected BiFunction<TextComponent, ActionType, TextComponent[]> hoverFunction;
    public BookMenu (Field contents) {
        this.contents = contents;
    }

    @Override
    public void openMenu(Player player) {
        ReflectionMethods.openBook(player, getBook());
    }

    @Override
    public Field getContents() {
        return contents;
    }

    @Override
    public void clear() {
        contents.clear();
    }

    protected static String getColorOfValue(String value) {
        if (value.equalsIgnoreCase("true"))
            return "§a";
        if (value.equalsIgnoreCase("false"))
            return "§c";
        if (Utils.isInteger(value))
            return "§6";
        if (Utils.isDouble(value))
            return "§3";
        else
            return "§8";
    }

    protected ItemStack getBook() {
        return ReflectionMethods.getBook(buildPagesFromField(contents, "root"));
    }

    public void setCommandOnAction(CommandFunction commandFunction) {
        this.commandFunction = commandFunction;
    }
    public void setHoverFunction(BiFunction<TextComponent, ActionType, TextComponent[]> hoverFunction) {
        this.hoverFunction = hoverFunction;
    }
    @FunctionalInterface
    public interface CommandFunction {
        String apply(TextComponent entry, ActionType actionType, String path);
    }

    protected void addClickEvent(TextComponent text, ActionType actionType, String path) {
        String command;
        if (commandFunction != null && (command = commandFunction.apply(text, actionType, path)) != null)
            text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
    }

    protected void addHoverEvent(TextComponent text, ActionType actionType) {
        TextComponent[] hoverText;
        if (hoverFunction != null && (hoverText = hoverFunction.apply(text, actionType)) != null)
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
    }

    protected String fitToLine(String text) {
        return text.length() <= MAX_LENGTH ? text : text.substring(0, Integer.max(0, MAX_LENGTH - 3)) + "...";
    }

    protected List<TextComponent> buildPagesFromField(Field entries, String path) {
        List<TextComponent> toret = new ArrayList<>();
        String entryValue = entries.getData();
        TextComponent key = new TextComponent();
        toret.add(key);
        if (entries.numChildren() > 0) { //the field contains more fields
            entryValue = "§o" + entryValue + "§r§0:";
            if (entries.numChildren() > 1)
                entryValue += "\n";
            for (TreeNode<String, Field> entry : entries.getChildren())
                toret.addAll(buildPagesFromField(entry.cast(), path + "." + entries.getData()));
        } else { //the field is the value
            entryValue = getColorOfValue(entryValue) + entryValue + "\n§r";
            addClickEvent(key, ActionType.MODIFY, path);
            addHoverEvent(key, ActionType.MODIFY);
        }
        key.setText(fitToLine(entryValue));
        if (entries.isRemovableFromBook()) {
            addClickEvent(key, ActionType.REMOVE, path);
            addHoverEvent(key, ActionType.REMOVE);
        }
        if (entries.canAddMoreFields()) {
            TextComponent addEntry = new TextComponent("- §2[+]§r§0\n");
            addClickEvent(addEntry, ActionType.ADD, path);
            addHoverEvent(addEntry, ActionType.ADD);
            toret.add(addEntry);
        }
        return toret;
    }

    protected void modifyConfigString(Player player, String path, Field field) {
        AnvilMenu anvilMenu = new AnvilMenu(
                "Modify " + path.split("\\.")[path.split("\\.").length - 1],
                Utils.setName(new ItemStack(Material.PAPER), field.getData()),
                null);
        anvilMenu.setOnClickBehaviour(event -> {
            if (event.getClickedInventory().getType() != InventoryType.ANVIL)
                return;
            event.setCancelled(true);
            if (event.getRawSlot() == 2)
                field.setValue(event.getClickedInventory().getItem(2).getItemMeta().getDisplayName());
            Bukkit.getScheduler().runTaskLater(InventoryMenuPlugin.plugin, () -> {
                player.closeInventory();
                BookMenu.this.openMenu(player);
            }, 1L);
        });
        anvilMenu.setOnCloseBehaviour(event -> BookMenu.this.openMenu(player));
        anvilMenu.openMenu(player);
    }

    public static class Field extends TreeNode<String, Field> {
        private Function<Object, Boolean> validCheckFunction;
        private boolean removableFromBook;
        private boolean canAddMoreFields;
        public Field(String value, boolean removableFromBook, boolean canAddMoreFields) {
            super(value);
            this.removableFromBook = removableFromBook;
            this.canAddMoreFields = canAddMoreFields;
        }
        public void setValidCheckFunction(Function<Object, Boolean> validCheckFunction) {
            this.validCheckFunction = validCheckFunction;
        }
        public boolean replaceEntry(Field newValue, int n) {
            if (validCheckFunction == null || validCheckFunction.apply(newValue)) {
                super.setChild(newValue, n);
                return true;
            }
            return false;
        }
        public void clear() {
            super.children.clear();
        }
        public boolean isRemovableFromBook() {
            return removableFromBook;
        }
        public boolean canAddMoreFields() {
            return canAddMoreFields;
        }
        public void setCanAddMoreFields(boolean canAddMoreFields) {
            this.canAddMoreFields = canAddMoreFields;
        }
        public void setRemovableFromBook(boolean removableFromBook) {
            this.removableFromBook = removableFromBook;
        }
    }
}