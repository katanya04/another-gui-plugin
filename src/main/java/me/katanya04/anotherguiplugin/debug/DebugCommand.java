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
    ActionItem item1 = new ActionItem(new ItemStack(Material.BARRIER), event -> event.getPlayer().closeInventory(), "Barrier");
    ActionItem item2 = new ActionItem(new ItemStack(Material.BED), event -> event.getPlayer().getWorld().setTime(20000), "Bed");
    ActionItem item3 = new ActionItem(new ItemStack(Material.EGG), event -> event.getPlayer().sendMessage("huevo"), "Huevo");
    public ItemStack[] contents = new ItemStack[]{item1.toItemStack(), item2.toItemStack(), item3.toItemStack(), new ItemStack(Material.SKULL_ITEM), new ItemStack(Material.WOOL)};
    ChestMenu chestMenu1 = new ChestMenu("chestMenu1", contents, true, InventoryMenu.SaveOption.NONE, null, null, 2, 3);
    ChestMenu chestMenu2 = new ChestMenu("chestMenu2", new ItemStack[]{new ItemStack(Material.POISONOUS_POTATO), new ItemStack(Material.BAKED_POTATO)}, true, InventoryMenu.SaveOption.GLOBAL, null, null, 1);
    ActionItem playerSkull = new ActionItem(pl -> Utils.setName(new ItemStack(Material.SKULL_ITEM), pl != null ? pl.getName() : ""), null, "playerSkull");
    ChestMenu chestMenu3 = new ChestMenu("chestMenu3", new ItemStack[]{null, playerSkull.toItemStack()}, true, InventoryMenu.SaveOption.GLOBAL, chestMenu1, chestMenu2);
    AnvilMenu anvilMenu1 = new AnvilMenu("anvilMenu1", true, InventoryMenu.SaveOption.GLOBAL, new ItemStack(Material.PAPER), null);
    MenuItem<ChestMenu> menu1 = new MenuItem<>(new ItemStack(Material.CHEST), chestMenu1, "chestMenu1");
    MenuItem<ChestMenu> menu2 = new MenuItem<>(new ItemStack(Material.ENDER_CHEST), chestMenu2, "chestMenu2");
    MenuItem<ChestMenu> menu3 = new MenuItem<>(new ItemStack(Material.SKULL_ITEM), chestMenu3, "chestMenu3");
    MenuItem<AnvilMenu> menu4 = new MenuItem<>(new ItemStack(Material.ANVIL), anvilMenu1, "anvilMenu1");
    MenuItem<InventoryMenu> menuTest = new MenuItem<>(new ItemStack(Material.POTION), new InventoryMenu("brewing", true, InventoryMenu.SaveOption.NONE, InventoryType.BREWING, new ItemStack[4]), "menuTest");
    ChestMenu chestMenu4 = new ChestMenu("chestMenu4", new ItemStack[]{null, new ItemStack(Material.POISONOUS_POTATO), contents[2]}, true, InventoryMenu.SaveOption.INDIVIDUAL, anvilMenu1, menuTest.getMenu());
    MenuItem<ChestMenu> menu5 = new MenuItem<>(new ItemStack(Material.COMMAND), chestMenu4, "chestMenu4");
    BookMenu.Field contentsField1 = new BookMenu.Field("example", true, true);
    BookMenu.InventoryField contentsField2 = new BookMenu.InventoryField();
    BookMenu<?> bookMenu1 = new BookMenu<>(contentsField1);
    MenuItem<BookMenu<?>> menu6 = new MenuItem<>(new ItemStack(Material.BOOK), bookMenu1, "bookMenu1");
    BookMenu<?> bookMenuFromConfig = new BookMenu<>(ignored -> {
        BookMenu.Field root = BookMenu.Field.fromConfig(AnotherGUIPlugin.getStorage());
        root.getChildGivenData("menu-saves").getChildGivenData("chestMenu4").getChildren().forEach(o -> o.setIsModifiable(BookMenu.Field.ModifiableOption.YES));
        return root;
    });
    MenuItem<BookMenu<?>> menu7 = new MenuItem<>(new ItemStack(Material.BOOK), bookMenuFromConfig, "bookMenu2");
    ListItem listItem = new ListItem(arg -> Utils.setName(new ItemStack(Material.PAPER), "list: " + arg.getDisplayName()),
            Arrays.asList("a", "b", "c"), pl -> pl.getFoodLevel() % 3, true, "ListItem");
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0)
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

        contentsField2.setInvMenu(new ChestMenu("InvMenuInBookMenu", new ItemStack[]{new ItemStack(Material.CAKE), null}, true, InventoryMenu.SaveOption.GLOBAL, null, null));
        ((ChestMenu) contentsField2.getInvMenu()).setFillWithBarriers(true);

        contentsField1.addChild(new BookMenu.Field("inventory", false, false).addChild(contentsField2));
        player.getInventory().addItem(menu1.toItemStack(), menu2.toItemStack(), menu3.toItemStack(), menu4.toItemStack(),
                menuTest.toItemStack(), menu5.toItemStack(), menu6.toItemStack(), menu7.toItemStack(), listItem.toItemStack(player));


        return false;
    }
}
