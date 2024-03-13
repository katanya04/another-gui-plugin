package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import me.katanya04.anotherguiplugin.utils.ReflectionMethods;
import me.katanya04.anotherguiplugin.utils.TreeNode;
import me.katanya04.anotherguiplugin.utils.Utils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MinecraftFont;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BookMenu implements Menu<BookMenu.Field> {
    protected static List<BookMenu> books = new ArrayList<>();
    protected static int ids = 0;
    protected Field contents;
    public enum ActionType {REMOVE, MODIFY, ADD}
    protected static final String command = "/menus bookModify %d %s %s";
    protected BiFunction<TextComponent, ActionType, TextComponent[]> hoverFunction;
    protected int id;
    protected int spacesPerIndentationLevel;
    public BookMenu (Field contents) {
        this.contents = contents;
        this.id = ids++;
        this.spacesPerIndentationLevel = 1;
        books.add(this);

        //default implementation
        this.hoverFunction = (text, action) -> new TextComponent[]{new TextComponent("Click to " + action.name().toLowerCase())};
    }

    public static BookMenu getBookById(int id) {
        List<BookMenu> bookList = books.stream().filter(o -> o.id == id).collect(Collectors.toList());
        if (bookList.isEmpty())
            return null;
        if (bookList.size() > 1)
            AnotherGUIPlugin.getLog().log(Level.WARNING, "Multiple BookMenus with same id");
        return bookList.get(0);
    }

    public void setSpacesPerIndentationLevel(int spacesPerIndentationLevel) {
        this.spacesPerIndentationLevel = spacesPerIndentationLevel;
    }

    @Override
    public void openMenu(Player player) {
        ReflectionMethods.openBook(player, getBook());
    }

    @Override
    public void setContents(Field contents) {
        this.contents = contents;
    }

    @Override
    public Field getContents() {
        return contents;
    }

    @Override
    public void clear() {
        contents.clear();
    }

    public void unregister() {
        books.remove(this);
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
        return ReflectionMethods.getBook(buildPagesFromField(contents, contents.getId()));
    }
    public void setHoverFunction(BiFunction<TextComponent, ActionType, TextComponent[]> hoverFunction) {
        this.hoverFunction = hoverFunction;
    }

    protected void addClickEvent(TextComponent text, ActionType actionType, String path) {
        text.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, String.format(command, id, actionType.name(), path)));
    }

    protected void addHoverEvent(TextComponent text, ActionType actionType) {
        TextComponent[] hoverText;
        if (hoverFunction != null && (hoverText = hoverFunction.apply(text, actionType)) != null)
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
    }

    public static boolean isTextLargerThanMax(String text) {
        final MinecraftFont font = new MinecraftFont();
        return font.getWidth(text) > font.getWidth("LLLLLLLLLLLLLLLLLLL");
    }

    protected String fitToLine(String text) {
        StringBuilder textSb = new StringBuilder(text);
        StringBuilder rawTextSb = new StringBuilder(ChatColor.stripColor(text));
        if (!isTextLargerThanMax(rawTextSb.toString()))
            return text;
        textSb.append("...\n");
        rawTextSb.append("...\n");
        do {
            textSb.deleteCharAt(textSb.length() - 5);
            rawTextSb.deleteCharAt(textSb.length() - 5);
        } while (isTextLargerThanMax(rawTextSb.toString()));
        return textSb.toString();
    }

    protected TextComponent getRemoveButton(String path) {
        TextComponent removeEntry = new TextComponent("§4[-]§r§0 ");
        addClickEvent(removeEntry, ActionType.REMOVE, path);
        addHoverEvent(removeEntry, ActionType.REMOVE);
        return removeEntry;
    }

    protected TextComponent getAddButton(String path) {
        TextComponent addEntry = new TextComponent("§2[+]§r§0 ");
        addClickEvent(addEntry, ActionType.ADD, path);
        addHoverEvent(addEntry, ActionType.ADD);
        return addEntry;
    }

    protected String getIndentation(int indentationLevel) {
        StringBuilder sb = new StringBuilder();
        int spaces = this.spacesPerIndentationLevel;
        while (indentationLevel-- > 0) {
            while (spaces-- > 0)
                sb.append(" ");
            spaces = this.spacesPerIndentationLevel;
        }
        return sb.toString();
    }

    protected List<TextComponent> buildPagesFromField(Field entries, String path) {
        int indentationLevel = path.split("\\.").length - 2;
        boolean isRoot = entries.getParent() == null;
        boolean isLeafNode = entries.numChildren() == 0;
        List<TextComponent> toret = new ArrayList<>();
        String entryValue = getIndentation(indentationLevel) + entries.getData();
        TextComponent key = new TextComponent();
        if (!isRoot)
            toret.add(key);
        if (!isLeafNode) { //the field contains more fields
            entryValue = "§o" + entryValue + "§r§0: ";
            if (entries.isRemovableFromBook() && !isRoot)
                toret.add(getRemoveButton(path));
            if (entries.numChildren() > 1 || (entries.numChildren() == 1 && entries.getChild(0).numChildren() > 0) && !isRoot)
                toret.add(new TextComponent("\n"));
            for (TreeNode<String, Field> entry : entries.getChildren())
                toret.addAll(buildPagesFromField(entry.cast(), path + "." + entry.cast().getId()));
        } else { //the field is the value
            if (entries.onModify != Field.OnModifyActions.NONE) {
                entryValue = getColorOfValue(entryValue) + (entries.getOnModify() == Field.OnModifyActions.OPEN_INV ? Field.OPEN_INV_MSG : entryValue) + "§r§0";
                addClickEvent(key, ActionType.MODIFY, path);
                addHoverEvent(key, ActionType.MODIFY);
            }
            if (entries.isRemovableFromBook() && !isRoot)
                toret.add(getRemoveButton(path));
        }
        if (entries.canAddMoreFields())
            toret.add(getAddButton(path));
        key.setText(fitToLine(entryValue));
        toret.add(new TextComponent("\n"));
        return toret;
    }

    public static class Field extends TreeNode<String, Field> {
        private Function<Object, Boolean> validCheckFunction;
        public enum OnModifyActions{NONE, OPEN_ANVIL, OPEN_INV, CUSTOM}
        private OnModifyActions onModify;
        private Supplier<Boolean> onModifyCustomBhv;
        private boolean removableFromBook;
        private boolean canAddMoreFields;
        private int maxNumberOfFields;
        private String modifyPermission;
        private InventoryMenu invMenu;
        private static final String FIELD_PATH = "FieldPath";
        public static final String OPEN_INV_MSG = "[Open menu]";
        private static final AnvilMenu anvilMenu = new AnvilMenu("anvilMenu", false, InventoryMenu.SaveOption.NONE, null, null);
        static {
            anvilMenu.setOnClickBehaviour(event -> {
                String path = Utils.getNBT(event.getInventory().getItem(0), FIELD_PATH);
                int bookId;
                try {
                    bookId = Integer.parseInt(path.split("/")[0]);
                } catch (NumberFormatException ex) {
                    return;
                }
                BookMenu book = getBookById(bookId);
                if (book == null)
                    return;
                Field field = book.contents.getNodeFromPath(path.split("/")[1]).cast();
                if (event.getRawSlot() == 2) {
                    field.setValue(event.getClickedInventory().getItem(2).getItemMeta().getDisplayName());
                    event.getClickedInventory().setItem(1, null);
                    event.getWhoClicked().closeInventory();
                }
                else if (event.getRawSlot() == 1 && event.getClickedInventory().getItem(1) != null && event.getClickedInventory().getItem(1).getType() != Material.AIR) {
                    Field parent_ = field.getParent().cast();
                    Field f = parent_.getChild(parent_.numChildren() - 2).cast();
                    field.copyFrom(f);
                    event.getClickedInventory().setItem(1, null);
                    event.getWhoClicked().closeInventory();
                }
            });
        }
        private Field() {
            this("", false, false);
        }
        public Field(String value, boolean removableFromBook, boolean canAddMoreFields) {
            this(value, removableFromBook, canAddMoreFields, -1);
        }
        public Field(String value, boolean removableFromBook, boolean canAddMoreFields, int maxNumberOfFields) {
            this(value, removableFromBook, canAddMoreFields, maxNumberOfFields, "");
        }
        public Field(String value, boolean removableFromBook, boolean canAddMoreFields, int maxNumberOfFields, String permission) {
            this(value, removableFromBook, canAddMoreFields, maxNumberOfFields, permission, OnModifyActions.OPEN_ANVIL);
        }
        public Field(String value, boolean removableFromBook, boolean canAddMoreFields, int maxNumberOfFields, String permission, OnModifyActions onModify) {
            super(value);
            this.removableFromBook = removableFromBook;
            this.canAddMoreFields = canAddMoreFields;
            this.maxNumberOfFields = maxNumberOfFields;
            this.modifyPermission = permission;
            this.onModify = onModify;
        }
        protected void copyFrom(Field field) {
            this.data = field.data;
            this.validCheckFunction = field.validCheckFunction;
            this.onModify = field.onModify;
            this.onModifyCustomBhv = field.onModifyCustomBhv;
            this.removableFromBook = field.removableFromBook;
            this.canAddMoreFields = field.canAddMoreFields;
            this.maxNumberOfFields = field.maxNumberOfFields;
            this.modifyPermission = field.modifyPermission;
            this.invMenu = field.invMenu;
            recursiveCopyChildren(this, field);
        }
        protected void recursiveCopyChildren(Field destination, Field sender) {
            int i = 0;
            for (Field node : sender.getChildrenField()) {
                if (destination.numChildren() <= i)
                    destination.addChild(new Field());
                destination.getChild(i).cast().copyFrom(node);
                i++;
            }
        }
        protected String getPath(String bookId) {
            return  bookId != null ? bookId + "/" + getPath() : getPath();
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
            return canAddMoreFields && (maxNumberOfFields < 0 || numChildren() < maxNumberOfFields);
        }
        public int getMaxNumberOfFields() {
            return maxNumberOfFields;
        }
        public String getModifyPermission() {
            return modifyPermission;
        }
        public OnModifyActions getOnModify() {
            return onModify;
        }
        public List<Field> getChildrenField() {
            return children.stream().map(o -> (Field) o).collect(Collectors.toList());
        }
        public void setCanAddMoreFields(boolean canAddMoreFields) {
            this.canAddMoreFields = canAddMoreFields;
        }
        public void setRemovableFromBook(boolean removableFromBook) {
            this.removableFromBook = removableFromBook;
        }
        public void setMaxNumberOfFields(int maxNumberOfFields) {
            this.maxNumberOfFields = maxNumberOfFields;
        }
        public void setModifyPermission(String modifyPermission) {
            this.modifyPermission = modifyPermission;
        }
        public void setOnModify(OnModifyActions onModify) {
            this.onModify = onModify;
        }
        public void setOnModifyCustomBhv(Supplier<Boolean> onModifyCustomBhv) {
            this.onModifyCustomBhv = onModifyCustomBhv;
        }
        public void setInvMenu(InventoryMenu invMenu) {
            this.invMenu = invMenu;
        }
        public InventoryMenu getMenu() {
            switch (onModify) {
                case OPEN_ANVIL:
                    return getAnvilMenu();
                case OPEN_INV:
                    return getInvMenu();
                case CUSTOM:
                    onModifyCustomBhv.get();
                    return null;
                default:
                    return null;
            }
        }
        private AnvilMenu getAnvilMenu() {
            return anvilMenu;
        }

        public InventoryMenu getInvMenu() {
            return invMenu;
        }
    }
    public static class CommandListener implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
            if (!(commandSender instanceof Player))
                return false;
            Player player = (Player) commandSender;
            if (args.length != BookMenu.command.split(" ").length - 1)
                return false;
            if (!args[0].equals("bookModify"))
                return false;
            int id;
            try {
                id = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                return false;
            }
            BookMenu book = getBookById(id);
            if (book == null)
                return false;
            Field field = book.contents.getNodeFromPath(args[3]).cast();
            if (field == null)
                return false;
            ActionType action;
            try {
                action = ActionType.valueOf(args[2]);
            } catch (IllegalArgumentException ex) {
                return false;
            }
            if (!commandSender.hasPermission(field.modifyPermission))
                return false;
            switch (action) {
                case MODIFY:
                    InventoryMenu invMenu = field.getMenu();
                    if (invMenu == null)
                        return false;
                    invMenu.setParent(book);
                    invMenu.setContents(
                            new ItemStack[]{Utils.setItemNBT(Utils.setName(new ItemStack(Material.IRON_SPADE),
                                    field.getData().isEmpty() ? "New value" : field.getData()), Field.FIELD_PATH,
                                    field.getPath(String.valueOf(id))), field.getParent() != null &&
                                    field.getParent().numChildren() > 1 ? Utils.setName(new ItemStack(
                                            Material.IRON_SPADE), "Copy from previous field") : null
                    });
                    break;
                case REMOVE:
                    Field parent = field.getParent().cast();
                    parent.removeChild(field);
                    book.openMenu(player);
                    break;
                case ADD:
                    Field newField = new Field("", true, true);
                    field.addChild(newField);
                    InventoryMenu invMenu_ = newField.getMenu();
                    if (invMenu_ != null) {
                        invMenu_.setParent(book);
                        invMenu_.setContents(
                                new ItemStack[]{Utils.setItemNBT(Utils.setName(new ItemStack(Material.IRON_SPADE),
                                                "New value"), Field.FIELD_PATH, newField.getPath(String.valueOf(id))),
                                newField.getParent().numChildren() > 1 ? Utils.setName(new ItemStack(Material.IRON_SPADE),
                                        "Copy from previous field") : null
                        });
                    }
                    break;
            }
            return false;
        }
    }
}