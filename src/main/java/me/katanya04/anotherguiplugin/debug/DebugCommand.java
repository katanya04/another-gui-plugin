package me.katanya04.anotherguiplugin.debug;

import me.katanya04.anotherguiplugin.AnotherGUIPlugin;
import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import me.katanya04.anotherguiplugin.actionItems.ListItem;
import me.katanya04.anotherguiplugin.actionItems.MenuItem;
import me.katanya04.anotherguiplugin.menu.AnvilMenu;
import me.katanya04.anotherguiplugin.menu.BookMenu;
import me.katanya04.anotherguiplugin.menu.ChestMenu;
import me.katanya04.anotherguiplugin.menu.InventoryMenu;
import me.katanya04.anotherguiplugin.utils.Utils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class DebugCommand implements CommandExecutor {
    static ActionItem item1 = new ActionItem(new ItemStack(Material.BARRIER), event -> event.getPlayer().closeInventory(), "Barrier");
    static ActionItem item2 = new ActionItem(new ItemStack(Material.BED), event -> event.getPlayer().getWorld().setTime(20000), "Bed");
    static ActionItem item3 = new ActionItem(new ItemStack(Material.EGG), event -> event.getPlayer().sendMessage("huevo"), "Huevo");
    static ItemStack[] contents = new ItemStack[]{item1.toItemStack(), item2.toItemStack(), item3.toItemStack(), new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.WOOL)};
    static ChestMenu chestMenu1 = new ChestMenu("chestMenu1", contents, true, InventoryMenu.SaveOption.NONE, null, null, 2, 3);
    static ChestMenu chestMenu2 = new ChestMenu("chestMenu2", new ItemStack[]{new ItemStack(Material.POISONOUS_POTATO), new ItemStack(Material.BAKED_POTATO)}, true, InventoryMenu.SaveOption.GLOBAL, null, null, 1);
    static ActionItem playerSkull = new ActionItem(pl -> Utils.setName(new ItemStack(Material.SKULL_ITEM), pl != null ? pl.getName() : ""), null, "playerSkull");
    static ChestMenu chestMenu3 = new ChestMenu("chestMenu3", new ItemStack[]{null, playerSkull.toItemStack()}, true, InventoryMenu.SaveOption.GLOBAL, chestMenu1, chestMenu2);
    static AnvilMenu anvilMenu1 = new AnvilMenu("anvilMenu1", true, InventoryMenu.SaveOption.GLOBAL, new ItemStack(Material.PAPER), null);
    static MenuItem<ChestMenu> menu1 = new MenuItem<>(new ItemStack(Material.CHEST), chestMenu1, "chestMenu1");
    static MenuItem<ChestMenu> menu2 = new MenuItem<>(new ItemStack(Material.ENDER_CHEST), chestMenu2, "chestMenu2");
    static MenuItem<ChestMenu> menu3 = new MenuItem<>(new ItemStack(Material.SKULL_ITEM), chestMenu3, "chestMenu3");
    static MenuItem<AnvilMenu> menu4 = new MenuItem<>(new ItemStack(Material.ANVIL), anvilMenu1, "anvilMenu1");
    static MenuItem<InventoryMenu> menuTest = new MenuItem<>(new ItemStack(Material.POTION), new InventoryMenu("brewing", true, InventoryMenu.SaveOption.NONE, InventoryType.BREWING, new ItemStack[4]), "menuTest");
    static ChestMenu chestMenu4 = new ChestMenu("chestMenu4", new ItemStack[]{null, new ItemStack(Material.POISONOUS_POTATO), contents[2], null, null}, true, InventoryMenu.SaveOption.INDIVIDUAL, anvilMenu1, menuTest.getMenu());
    static {
        chestMenu4.setFillWithBarriers(true);
    }
    static MenuItem<ChestMenu> menu5 = new MenuItem<>(new ItemStack(Material.COMMAND), chestMenu4, "chestMenu4");
    static BookMenu.Field contentsField1 = new BookMenu.Field("example", true, true);
    static BookMenu.InventoryField contentsField2 = new BookMenu.InventoryField(new ChestMenu("InvMenuInBookMenu", new ItemStack[]{new ItemStack(Material.CAKE), null}, true, InventoryMenu.SaveOption.GLOBAL, null, null));
    static {
        ((ChestMenu) contentsField2.getInvMenu()).setFillWithBarriers(true);
    }
    static BookMenu<?> bookMenu1 = new BookMenu<>(contentsField1);
    static MenuItem<BookMenu<?>> menu6 = new MenuItem<>(new ItemStack(Material.BOOK), bookMenu1, "bookMenu1");
    static BookMenu<?> bookMenuFromConfig = new BookMenu<>(ignored -> {
        BookMenu.Field root = BookMenu.ConfigField.fromConfig(AnotherGUIPlugin.getStorage());
        if (root.getFirstChildGivenData("menu-saves") != null && root.getFirstChildGivenData("menu-saves").getFirstChildGivenData("chestMenu4") != null) {
            root.getFirstChildGivenData("menu-saves").getFirstChildGivenData("chestMenu4").getChildren().forEach(o -> o.setIsModifiable(BookMenu.Field.ModifiableOption.YES));
            root.getChildrenByPredicate(node -> node instanceof BookMenu.InventoryField, true).forEach(o -> {
                InventoryMenu inv = ((BookMenu.InventoryField) o).getInvMenu();
                if (inv instanceof ChestMenu)
                    ((ChestMenu) inv).setFillWithBarriers(true);
            });
        }
        root.applyToChildren(o -> o.setCanAddMoreFields(true), true);
        root.applyToChildren(o -> o.setRemovableFromBook(true), true);
        return root;
    });
    static MenuItem<BookMenu<?>> menu7 = new MenuItem<>(new ItemStack(Material.BOOK), bookMenuFromConfig, "bookMenu2");
    static ListItem listItem = new ListItem(arg -> Utils.setName(new ItemStack(Material.PAPER), "list: " + arg.getDisplayName()),
            Arrays.asList("a", "b", "c"), pl -> pl.getFoodLevel() % 3, true, "ListItem");
    static MenuItem<ChestMenu> menu8 = new MenuItem<>(new ItemStack(Material.COMMAND_MINECART), new ChestMenu("menu8",
            new ItemStack[27], true, InventoryMenu.SaveOption.INDIVIDUAL, null, null), "menu8");
    static MenuItem<ChestMenu> menu9 = new MenuItem<>(new ItemStack(Material.COMMAND_MINECART), new ChestMenu("menu9",
            new ItemStack[1], true, InventoryMenu.SaveOption.INDIVIDUAL, null, null), "menu9");
    static {
        menu9.getMenu().setFillWithBarriers(true);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 || !sender.hasPermission("AnotherGUIPlugin.menusDebug"))
            return false;
        Player player = (Player) sender;
        anvilMenu1.setOnClickBehaviour(event -> {
            if (event.getRawSlot() == 2 && event.getCurrentItem().getType() != Material.AIR) {
                event.getWhoClicked().sendMessage(event.getCurrentItem().getItemMeta().getDisplayName());
                event.getWhoClicked().closeInventory();
            }
        });
        contentsField1.addChild(new BookMenu.Field("4", false, true));
        contentsField1.addChild(new BookMenu.Field("this has children", true, false));
        contentsField1.getChild(1).addChild(new BookMenu.Field("this one", false, false));
        contentsField1.getChild(1).addChild(new BookMenu.Field("and this one", true, true));

        contentsField1.addChild(new BookMenu.Field("inventory", false, false).addChild(contentsField2));
        player.getInventory().addItem(menu1.toItemStack(), menu2.toItemStack(), menu3.toItemStack(), menu4.toItemStack(),
                menuTest.toItemStack(), menu5.toItemStack(), menu6.toItemStack(), menu7.toItemStack(), listItem.toItemStack(player),
                menu8.toItemStack(), menu9.toItemStack());


        return false;
    }
}
