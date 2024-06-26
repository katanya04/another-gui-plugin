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
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MinecraftFont;

import java.util.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * A menu on a written book, whose contents are interacted with via commands fired on book click events. The contents ({@link Field})
 * follow a tree structure, where each node represents a string (or an {@link InventoryMenu}, see {@link InventoryField})
 * <p>
 * Contents are created when opening the book, and saved on a cache. The cache is a LinkedHashMap, with keys of type {@link T},
 * these keys are created from the player that opens the book, using the toCacheKey function on the player.
 *
 * @param <T> the cache key type
 */
public class BookMenu<T> implements Menu<BookMenu.Field> {
    protected static List<BookMenu<?>> books = new ArrayList<>();
    protected static final String command = "/menus bookModify %d %s %s";
    protected static int ids = 0;

    public enum ActionType {REMOVE, MODIFY, ADD}

    protected Function<Player, T> toCacheKey;
    protected Function<T, Field> generateContents;
    protected LinkedHashMap<T, Field> contentsCache;
    public int cacheMaxSize;
    protected BiFunction<TextComponent, ActionType, TextComponent[]> hoverFunction;
    protected int id;
    protected int spacesPerIndentationLevel;

    public BookMenu(Field contents) {
        this(ignored -> contents);
    }

    public BookMenu(Function<T, Field> contents) {
        this.generateContents = contents;
        this.id = ids++;
        this.spacesPerIndentationLevel = 1;
        books.add(this);
        this.cacheMaxSize = 50;
        contentsCache = new LinkedHashMap<T, Field>() {
            @Override
            protected boolean removeEldestEntry(Map.Entry<T, Field> eldest) {
                return size() > cacheMaxSize;
            }
        };
        toCacheKey = pl -> null;

        //default implementation
        this.hoverFunction = (text, action) -> new TextComponent[]{new TextComponent("Click to " + action.name().toLowerCase())};
    }

    public static BookMenu<?> getBookById(int id) {
        List<BookMenu<?>> bookList = books.stream().filter(o -> o.id == id).collect(Collectors.toList());
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
        ReflectionMethods.openBook(player, getBook(player));
    }

    @Override
    public void setContents(Field generateContents) {
        this.generateContents = ignored -> generateContents;
    }

    @Override
    public Field getContents() {
        return generateContents.apply(null);
    }

    @Override
    public void clear() {
        generateContents = ignored -> null;
    }

    public void setGenerateContents(Function<T, Field> generateContents) {
        this.generateContents = generateContents;
    }

    public void setToCacheKey(Function<Player, T> toCacheKey) {
        this.toCacheKey = toCacheKey;
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

    protected Field getContentsFromPlayer(Player player) { //check generates contents / gets from cache twice after modification
        return getContentsFromPlayer(player, false);
    }

    protected Field getContentsFromPlayer(Player player, boolean useCacheAnyway) { //check generates contents / gets from cache twice after modification
        T cacheKey = toCacheKey.apply(player);
        Field contents = contentsCache.get(cacheKey);
        if (contents == null || (!contents.shouldUseCache && !useCacheAnyway)) {
            contents = generateContents.apply(cacheKey);
            contentsCache.put(cacheKey, contents);
        }
        return contents;
    }

    @Override
    public void setParent(Menu<?> parent) {}

    @Override
    public Menu<?> getParent() {
        return null;
    }

    protected ItemStack getBook(Player player) {
        Field contents = getContentsFromPlayer(player);
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
        return font.getWidth(ChatColor.stripColor(text)) > font.getWidth("LLLLLLLLLLLLLLLLLLL");
    }

    protected String fitToLine(String text) {
        StringBuilder textSb = new StringBuilder(text);
        if (!isTextLargerThanMax(textSb.toString()))
            return text;
        textSb.append("...\n");
        int index = textSb.length() - 5;
        do {
            textSb.deleteCharAt(index--);
        } while (textSb.charAt(index) == '§' || isTextLargerThanMax(textSb.toString()) && index > 0);
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
        boolean isRoot = entries.getParent() == null;
        boolean isLeafNode = entries.numChildren() == 0;
        int indentationLevel;
        if (isLeafNode && !isRoot && entries.getParent().numChildren() == 1) // key : value (no new line, no indentation)
            indentationLevel = 0;
        else
            indentationLevel = path.split("\\.").length - 2;
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
            for (Field entry : entries.getChildren())
                toret.addAll(buildPagesFromField(entry, path + "." + entry.getId()));
        } else { //the field is the value
            entryValue = (entries instanceof InventoryField ? getIndentation(indentationLevel) + InventoryField.OPEN_INV_MSG
                    : getColorOfValue(entryValue.trim()) + entryValue) + "§r§0";
            if (entries.isRemovableFromBook() && !isRoot)
                toret.add(getRemoveButton(path));
        }
        if (entries.isModifiable()) {
            addClickEvent(key, ActionType.MODIFY, path);
            addHoverEvent(key, ActionType.MODIFY);
        }
        if (entries.canAddMoreFields()) {
            toret.add(getAddButton(path));
            toret.add(new TextComponent("\n"));
        } else if (isLeafNode)
            toret.add(new TextComponent("\n"));
        key.setText(fitToLine(entryValue));
        return toret;
    }

    public static class Field extends TreeNode<String, Field> {
        private Predicate<String> validCheckFunction;
        private boolean removableFromBook;
        private boolean canAddMoreFields;
        public enum ModifiableOption {YES, NO, ONLY_IF_LEAF}
        private ModifiableOption isModifiable;
        private int maxNumberOfFields;
        private String modifyPermission;
        private Consumer<Field> onModifyValue;
        private Consumer<Field> onAddChildren;
        private Consumer<Field> onRemoveChildren;
        private Consumer<Field> onModifyChildrenValue;
        private Field childTemplate;
        private boolean shouldUseCache;
        private boolean openMenuOnCreate;
        private static final String FIELD_PATH = "FieldPath";
        private static final AnvilMenu anvilMenu = new AnvilMenu("anvilMenu", false, InventoryMenu.SaveOption.NONE, null, null);

        static {
            anvilMenu.setOnClickBehaviour(event -> {
                String path = ReflectionMethods.getNBT(event.getInventory().getItem(0), FIELD_PATH);
                int bookId;
                try {
                    bookId = Integer.parseInt(path.split("/")[0]);
                } catch (NumberFormatException ex) {
                    return;
                }
                BookMenu<?> book = getBookById(bookId);
                if (book == null)
                    return;
                Field field = book.getContentsFromPlayer((Player) event.getWhoClicked(), true).getNodeFromPath(path.split("/")[1]);
                if (event.getRawSlot() == 2) {
                    if (event.getClickedInventory().getItem(2) == null)
                        return;
                    String value = event.getClickedInventory().getItem(2).getItemMeta().getDisplayName();
                    if (field.validCheckFunction == null || field.checkIfValid(value)) {
                        field.setValue(value);
                        field.triggerUpdate();
                        event.getClickedInventory().setItem(1, null);
                        event.getWhoClicked().closeInventory();
                    } else {
                        Player p = ((Player) event.getWhoClicked());
                        p.playSound(p.getLocation(), Sound.VILLAGER_NO, 1.f, 1.f);
                    }
                } else if (event.getRawSlot() == 1 && event.getClickedInventory().getItem(1) != null && event.getClickedInventory().getItem(1).getType() != Material.AIR) {
                    Field parent_ = field.getParent();
                    Field f = parent_.getChild(parent_.numChildren() - 2);
                    parent_.removeChild(field);
                    field = new Field(f);
                    parent_.addChild(field);
                    event.getClickedInventory().setItem(1, null);
                    event.getWhoClicked().closeInventory();
                }
            });
        }

        private Field() {
            this("");
        }

        public Field(String value) {
            this(value, false, false);
        }

        public Field(String value, Consumer<Field> onModifyValue, Predicate<String> validCheckFunction) {
            this(value, false, false);
            this.onModifyValue = onModifyValue;
            this.validCheckFunction = validCheckFunction;
            this.openMenuOnCreate = true;
        }

        public Field(String value, boolean removableFromBook, boolean canAddMoreFields) {
            this(value, removableFromBook, canAddMoreFields, -1);
        }

        public Field(String value, boolean removableFromBook, boolean canAddMoreFields, int maxNumberOfFields) {
            this(value, removableFromBook, canAddMoreFields, maxNumberOfFields, "");
        }

        public Field(String value, boolean removableFromBook, boolean canAddMoreFields, int maxNumberOfFields, String permission) {
            super(value);
            this.removableFromBook = removableFromBook;
            this.canAddMoreFields = canAddMoreFields;
            this.maxNumberOfFields = maxNumberOfFields;
            this.modifyPermission = permission;
            this.isModifiable = ModifiableOption.ONLY_IF_LEAF;
            this.shouldUseCache = true;
            this.openMenuOnCreate = true;
        }

        public Field(Field field) {
            super(field);
            this.validCheckFunction = field.validCheckFunction;
            this.removableFromBook = field.removableFromBook;
            this.canAddMoreFields = field.canAddMoreFields;
            this.maxNumberOfFields = field.maxNumberOfFields;
            this.modifyPermission = field.modifyPermission;
            this.onModifyValue = field.onModifyValue;
            this.onModifyChildrenValue = field.onModifyChildrenValue;
            this.isModifiable = field.isModifiable;
            this.childTemplate = field.childTemplate;
            this.shouldUseCache = field.shouldUseCache;
            this.onAddChildren = field.onAddChildren;
            this.onRemoveChildren = field.onRemoveChildren;
            this.openMenuOnCreate = field.openMenuOnCreate;
            recursiveCopyChildren(this, field);
        }

        @Override
        protected void recursiveCopyChildren(Field destination, Field sender) {
            destination.clear();
            for (Field node : sender.getChildren()) {
                destination.addChild(new Field(node));
            }
        }

        public boolean checkIfValid(String value) {
            return validCheckFunction == null || validCheckFunction.test(value);
        }

        protected String getPath(String bookId) {
            return bookId != null ? bookId + "/" + getPath() : getPath();
        }

        public void setValidCheckFunction(Predicate<String> validCheckFunction) {
            this.validCheckFunction = validCheckFunction;
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

        public Field getChildTemplate() {
            return childTemplate;
        }

        public boolean shouldUseCache() {
            return shouldUseCache;
        }

        public Consumer<Field> getOnAddChildren() {
            return onAddChildren;
        }

        public Consumer<Field> getOnRemoveChildren() {
            return onRemoveChildren;
        }

        public boolean isOpenMenuOnCreate() {
            return openMenuOnCreate;
        }

        public void setOpenMenuOnCreate(boolean openMenuOnCreate) {
            this.openMenuOnCreate = openMenuOnCreate;
        }

        public void setOnRemoveChildren(Consumer<Field> onRemoveChildren) {
            this.onRemoveChildren = onRemoveChildren;
        }

        public void setOnAddChildren(Consumer<Field> onAddChildren) {
            this.onAddChildren = onAddChildren;
        }

        public void setShouldUseCache(boolean shouldUseCache) {
            this.shouldUseCache = shouldUseCache;
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

        public void setOnModifyValue(Consumer<Field> onModifyValue) {
            this.onModifyValue = onModifyValue;
        }

        public void setOnModifyChildrenValue(Consumer<Field> onModifyChildrenValue) {
            this.onModifyChildrenValue = onModifyChildrenValue;
        }

        public Consumer<Field> getOnModifyChildrenValue() {
            return onModifyChildrenValue;
        }

        public void setIsModifiable(ModifiableOption isModifiable) {
            this.isModifiable = isModifiable;
        }

        public void setChildTemplate(Field childTemplate) {
            this.childTemplate = childTemplate;
        }

        @Override
        public Field addChild(Field child) {
            super.addChild(child);
            if (this.onAddChildren != null)
                this.onAddChildren.accept(child);
            triggerUpdate();
            return this;
        }

        @Override
        public boolean removeChild(Field node) {
            boolean toret = super.removeChild(node);
            if (this.onRemoveChildren != null)
                this.onRemoveChildren.accept(node);
            triggerUpdate();
            return toret;
        }

        public Field createChild() {
            Field toret;
            if (getChildTemplate() != null)
                toret = new Field(getChildTemplate());
            else
                toret = new Field("none");
            this.addChild(toret);
            return toret;
        }

        public InventoryMenu getMenu(String bookID) {
            anvilMenu.setContents(new ItemStack[]{
                    ReflectionMethods.setItemNBT(Utils.setName(new ItemStack(Material.IRON_SPADE),
                                    this.getData().isEmpty() ? "New value" : this.getData()), Field.FIELD_PATH,
                            this.getPath(bookID)), this.getParent() != null &&
                    this.getParent().numChildren() > 1 ? Utils.setName(new ItemStack(
                    Material.IRON_SPADE), "Copy from previous field") : null
            });
            return getAnvilMenu();
        }

        private AnvilMenu getAnvilMenu() {
            return anvilMenu;
        }

        public boolean isModifiable() {
            switch (isModifiable) {
                case YES:
                    return true;
                case ONLY_IF_LEAF:
                    return this.numChildren() == 0;
                default:
                    return false;
            }
        }

        public void triggerUpdate() {
            if (this.onModifyValue != null)
                this.onModifyValue.accept(this);
            Field parent = this.getParent();
            while (parent != null) {
                if (parent.onModifyChildrenValue != null)
                    parent.onModifyChildrenValue.accept(this);
                parent = parent.getParent();
            }
        }
    }

    public static class InventoryField extends Field {
        private InventoryMenu invMenu;
        public static final String OPEN_INV_MSG = "[Open menu]";

        public InventoryField(InventoryField inventoryField) {
            super(inventoryField);
            this.invMenu = inventoryField.invMenu;
        }

        public InventoryField(int slots, String name) {
            this(new ChestMenu(name, new ItemStack[slots], true, InventoryMenu.SaveOption.GLOBAL, false));
            ((ChestMenu) this.invMenu).setFillWithBarriers(true);
        }

        public InventoryField(InventoryMenu invMenu) {
            this(invMenu, null);
        }

        public InventoryField(InventoryMenu invMenu, Consumer<Field> onModifyValue) {
            super(OPEN_INV_MSG);
            setInvMenu(invMenu);
            setOnModifyValue(onModifyValue);
        }

        @Override
        public InventoryMenu getMenu(String bookID) {
            return getInvMenu();
        }

        public void setInvMenu(InventoryMenu invMenu) {
            this.invMenu = invMenu;
        }

        public InventoryMenu getInvMenu() {
            return invMenu;
        }

        @Override
        public Field addChild(Field child) {
            throw new RuntimeException("Inventory fields cannot have children");
        }

        public static InventoryField fromConfig(String name, Object itemsObj) {
            ItemStack[] items = Utils.getCollectionOfItems(itemsObj);
            if (items == null)
                items = new ItemStack[0];
            ChestMenu menu = new ChestMenu(name, items, true, InventoryMenu.SaveOption.GLOBAL, false);
            menu.setFillWithBarriers(true);
            InventoryField invField = new InventoryField(menu);
            menu.setOnCloseBehaviour(event -> invField.triggerUpdate());
            return invField;
        }

        @Override
        public boolean canAddMoreFields() {
            return false;
        }
    }

    public static class ConfigField extends Field {
        private boolean isRoot;
        private ConfigurationSection configurationSection;

        private ConfigField(String data) {
            super(data);
        }

        private ConfigField(Field field) {
            super(field);
        }

        @Override
        protected void recursiveCopyChildren(Field destination, Field sender) {
            destination.clear();
            for (Field node : sender.getChildren())
                destination.addChild(node instanceof InventoryField ? new InventoryField((InventoryField) node) : new ConfigField(node));
        }

        @Override
        public Field createChild() {
            ConfigField toret;
            if (getChildTemplate() != null)
                toret = new ConfigField(getChildTemplate());
            else
                toret = new ConfigField("none");
            this.addChild(toret);
            return toret;
        }

        @Override
        public void setOnModifyChildrenValue(Consumer<Field> onModifyChildrenValue) {
            if (!isRoot)
                super.setOnModifyChildrenValue(onModifyChildrenValue);
            else {
                Consumer<Field> defaultUpdate = field -> this.toConfig(this.configurationSection);
                super.setOnModifyChildrenValue(defaultUpdate.andThen(onModifyChildrenValue));
            }
        }

        public static ConfigField fromConfig(ConfigurationSection config) {
            ConfigField root = new ConfigField(config.getName());
            root.setShouldUseCache(false);
            root.isRoot = true;
            root.configurationSection = config;
            root.updateStructure();
            root.setOnModifyChildrenValue(field -> root.toConfig(config));
            return root;
        }

        public void updateStructure() {
            this.clear();
            for (Map.Entry<String, Object> entry : this.configurationSection.getValues(false).entrySet()) {
                ConfigField currentEntry;
                if (entry.getValue() instanceof ConfigurationSection) {
                    ConfigurationSection value = (ConfigurationSection) entry.getValue();
                    currentEntry = new ConfigField(value.getName());
                    currentEntry.configurationSection = value;
                    currentEntry.updateStructure();
                } else if (Utils.getCollectionOfItems(entry.getValue()) != null) {
                    currentEntry = new ConfigField(entry.getKey());
                    currentEntry.addChild(InventoryField.fromConfig(entry.getKey(), entry.getValue()));
                } else if (entry.getValue() instanceof Collection<?>) {
                    currentEntry = new ConfigField(entry.getKey());
                    Collection<?> value = (Collection<?>) entry.getValue();
                    List<Field> children = new ArrayList<>();
                    for (Object obj : value) {
                        if (Utils.getCollectionOfItems(obj) != null)
                            children.add(InventoryField.fromConfig(currentEntry.data, obj));
                        else
                            children.add(new ConfigField(obj.toString()));
                    }
                    currentEntry.addChildren(children);
                } else {
                    currentEntry = new ConfigField(entry.getKey());
                    currentEntry.addChild(new ConfigField(entry.getValue().toString()));
                }
                this.addChild(currentEntry);
            }
        }

        public ConfigurationSection toConfig(ConfigurationSection config) {
            Utils.clearConfSection(config);
            toConfig(config, "");
            return config;
        }

        public void toConfig(ConfigurationSection config, String path) {
            if (this.isLeaf())
                return;
            int leafChildren = this.getChildrenByPredicate(TreeNode::isLeaf, false).size();
            for (Field node : this.getChildren()) {
                if (!(node instanceof ConfigField) && !(node instanceof InventoryField))
                    return;
                if (node.isLeaf()) {
                    if (path.isEmpty())
                        continue;
                    Object data = node instanceof InventoryField ? ((InventoryField) node).getInvMenu().getContentsItemArray()
                            : node.getData();
                    if (leafChildren == 1)
                        config.set(path, data);
                    else {
                        List<Object> list = config.getList(path) == null ? new ArrayList<>() : config.getList(path)
                                .stream().map(o -> o == null ? null : (Object) o).collect(Collectors.toList());
                        list.add(data);
                        config.set(path, list);
                    }
                } else if (node instanceof ConfigField)
                    ((ConfigField) node).toConfig(config, path.isEmpty() ? node.getData() : path + "." + node.getData());
            }
        }

        public Field getChild(String path) {
            String[] pathSplit = path.split("\\.");
            Field field = this;
            for (String s : pathSplit) {
                field = field.getFirstChildGivenData(s);
                if (field == null)
                    return null;
            }
            return field;
        }

        public Field getChildCreateIfNotExists(String path, String defaultValue) {
            Field child = this.getChild(path);
            if (child == null) {
                configurationSection.set(path, defaultValue);
                this.updateStructure();
                child = this.getChild(path);
            }
            return child;
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
            BookMenu<?> book = getBookById(id);
            if (book == null)
                return false;
            Field field = book.getContentsFromPlayer(player).getNodeFromPath(args[3]);
            if (field == null)
                return false;
            ActionType action;
            try {
                action = ActionType.valueOf(args[2]);
            } catch (IllegalArgumentException ex) {
                return false;
            }
            if (field.modifyPermission != null && !commandSender.hasPermission(field.modifyPermission))
                return false;
            switch (action) {
                case REMOVE:
                    Field parent = field.getParent();
                    parent.removeChild(field);
                    book.openMenu(player);
                    break;
                case ADD:
                    field = field.createChild();
                case MODIFY:
                    InventoryMenu invMenu = field.getMenu(String.valueOf(id));
                    invMenu.setParent(book);
                    if (action != ActionType.ADD || field.isOpenMenuOnCreate())
                        invMenu.openMenu(player);
                    else
                        book.openMenu(player);
                    break;
            }
            return false;
        }
    }
}