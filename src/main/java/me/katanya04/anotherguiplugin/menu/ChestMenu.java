package me.katanya04.anotherguiplugin.menu;

import me.katanya04.anotherguiplugin.events.AfterUpdateOpenMenuEvent;
import me.katanya04.anotherguiplugin.events.ChangePageChestMenuEvent;
import me.katanya04.anotherguiplugin.events.BeforeUpdateOpenMenuEvent;
import me.katanya04.anotherguiplugin.utils.Utils;
import me.katanya04.anotherguiplugin.actionItems.ActionItem;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An InventoryMenu that supports scrolling between pages. Similar behaviour could be manually added to the InventoryMenu
 * class, but this one already provides it ready to use
 */
public class ChestMenu extends InventoryMenu {
    protected static final ItemStack BARRIER = Utils.setName(new ItemStack(Material.BARRIER), " ");
    protected static final ActionItem<?> nextPage = new ActionItem<>(Utils.setName(new ItemStack(Material.ARROW), "Next Page"),
            event -> {
                ChestMenu currentChestMenu = (ChestMenu) event.getInv().getHolder();
                if (currentChestMenu == null)
                    return;
                Player p = event.getPlayer();
                ChangePageChestMenuEvent changePage = new ChangePageChestMenuEvent(currentChestMenu, p, event.getInv(), currentChestMenu.getPageNum(p) - 1, currentChestMenu.getPageNum(p));
                Bukkit.getPluginManager().callEvent(changePage);
                if (!changePage.isCancelled()) {
                    currentChestMenu.nextPage(p);
                    updateInvContents(p);
                }
            }, "nextPage");
    protected static final ActionItem<?> previousPage = new ActionItem<>(Utils.setName(new ItemStack(Material.ARROW), "Previous Page"),
            event -> {
                ChestMenu currentChestMenu = (ChestMenu) event.getInv().getHolder();
                if (currentChestMenu == null)
                    return;
                Player p = event.getPlayer();
                ChangePageChestMenuEvent changePage = new ChangePageChestMenuEvent(currentChestMenu, p, event.getInv(), currentChestMenu.getPageNum(p) + 1, currentChestMenu.getPageNum(p));
                Bukkit.getPluginManager().callEvent(changePage);
                if (!changePage.isCancelled()) {
                    currentChestMenu.previousPage(event.getPlayer());
                    updateInvContents(event.getPlayer());
                }
            }, "previousPage");
    private final HashMap<String, Integer> currentlyOpenPage;
    private boolean fillWithBarriers;
    private boolean addPagesToFit;
    private Consumer<ChangePageChestMenuEvent> onChangePage;

    public ChestMenu(String name, Map<Integer, ItemStack> contents) {
        this(name, Utils.mapToArray(contents, true));
    }

    public ChestMenu(String name, ItemStack[] contents) {
        this(name, ignored -> contents);
    }

    public ChestMenu(String name, ItemStack[] contents, boolean canInteract, SaveOption saveOption, boolean addPagesToFit, int... protectedSlots) {
        this(name, ignored -> contents, canInteract, saveOption, addPagesToFit, protectedSlots);
    }

    public ChestMenu(String name, Function<Player, ItemStack[]> contents) {
        this(name, contents, false, SaveOption.NONE, true);
    }

    public ChestMenu(String name, Function<Player, ItemStack[]> contents, boolean canInteract, SaveOption saveOption, boolean addPagesToFit, int... protectedSlots) {
        super(name, canInteract, saveOption, InventoryType.CHEST, contents);
        addProtectedSlots(protectedSlots);
        this.addPagesToFit = addPagesToFit;
        this.currentlyOpenPage = new HashMap<>();
    }

    private int getPageNum(Player player) {
        return Optional.ofNullable(this.currentlyOpenPage.get(player.getName())).orElse(0);
    }

    private void nextPage(Player player) {
        this.currentlyOpenPage.put(player.getName(), getPageNum(player) + 1);
    }

    private void previousPage(Player player) {
        this.currentlyOpenPage.put(player.getName(), getPageNum(player) - 1);
    }

    private int maxSizePage(int page, int pages) {
        if (pages == 1) return MAX_CHEST_SIZE;
        if (page == 0 || page == pages - 1) return MAX_CHEST_SIZE - 1;
        return MAX_CHEST_SIZE - 2;
    }

    private ItemStack[] getContentsPage(ItemStack[] allContents, int pageNum) {
        int maxSize = this.maxSizePage(pageNum, getNumPages(allContents.length));
        return Arrays.copyOfRange(allContents, maxSize * pageNum,
                Math.min(allContents.length, maxSize * pageNum + maxSize));
    }

    private ItemStack[] getContentsPage(Player player) {
        return getContentsPage(contents.apply(player), getPageNum(player));
    }

    public static void updateInvContents(Player player) {
        InventoryMenu menu = getAnotherPlayersMenu(player);
        if (!(menu instanceof ChestMenu))
            return;
        player.getOpenInventory().getTopInventory().setContents(menu.newInventory(player).getContents());
    }

    public boolean isFillWithBarriers() {
        return fillWithBarriers;
    }

    public void setFillWithBarriers(boolean fillWithBarriers) {
        this.fillWithBarriers = fillWithBarriers;
    }

    public boolean isAddPagesToFit() {
        return addPagesToFit;
    }

    public void setAddPagesToFit(boolean addPagesToFit) {
        this.addPagesToFit = addPagesToFit;
    }

    private void setContentsInInv(ItemStack[] contents, Inventory inv, boolean hasPreviousPage) {
        if (!hasPreviousPage)
            inv.setContents(contents);
        else {
            int i, invSize = inv.getSize();
            for (i = 0; i < invSize - 9 && i < contents.length; i++)
                inv.setItem(i, contents[i]);
            for (; i < invSize && i < contents.length; i++)
                inv.setItem(i + 1, contents[i]);
        }
    }

    private int getNumPages(int contentSize) {
        if (contentSize < MAX_CHEST_SIZE)
            return 1;
        int toret = 1;
        contentSize -= MAX_CHEST_SIZE - 1;
        while (contentSize > MAX_CHEST_SIZE - 1) {
            toret++;
            contentSize -= MAX_CHEST_SIZE - 2;
        }
        return ++toret;
    }

    @Override
    public void openMenu(Player player) {
        this.currentlyOpenPage.remove(player.getName());
        super.openMenu(player);
    }

    @Override
    protected Inventory newInventory(Player player) {
        ItemStack[] allContents = getItemContents(player);
        int pageNum = getPageNum(player);
        int contentSize = allContents.length;
        int numPages = getNumPages(contentSize);
        int inventorySize = Utils.ceilToMultipleOfNine(contentSize);
        inventorySize = Math.min(inventorySize, MAX_CHEST_SIZE);
        Inventory toret = Bukkit.createInventory(this, inventorySize, GUIName);

        ItemStack[] contentsInThisPage = this.getContentsPage(allContents, pageNum);
        setContentsInInv(contentsInThisPage, toret, pageNum > 0);

        if (fillWithBarriers) {
            int i = contentsInThisPage.length;
            while (i < inventorySize) {
                this.addProtectedSlots(i);
                toret.setItem(i++, BARRIER);
            }
        }
        if (numPages != 1 && pageNum != numPages - 1)
            toret.setItem(inventorySize - 1, nextPage.toItemStack());
        if (numPages != 1 && pageNum != 0)
            toret.setItem(inventorySize - 9, previousPage.toItemStack());
        return toret;
    }

    private int getStartingPosOfPage(int pageNum) {
        int pos = 0;
        while (pageNum > 0) {
            pos += pageNum == 1 ? MAX_CHEST_SIZE - 1 : MAX_CHEST_SIZE - 2;
            pageNum--;
        }
        return pos;
    }

    private ItemStack[] updateAPageFromContents(Inventory contents, Player player) {
        ItemStack[] allContents = this.contents.apply(player);
        int allContentsSize = allContents.length;
        ItemStack[] thisPage = getWithoutUIElements(contents);
        int startingPosition = getStartingPosOfPage(getPageNum(player));
        int thisPageLastIndex = startingPosition + thisPage.length;
        if (thisPageLastIndex > allContentsSize) {
            ItemStack[] padding = new ItemStack[thisPageLastIndex - allContentsSize];
            allContents = (ItemStack[]) ArrayUtils.addAll(allContents, padding);
        }
        System.arraycopy(thisPage, 0, allContents, startingPosition, thisPage.length);
        return allContents;
    }

    @Override
    protected void globalSave(Inventory contents, Player player) {
        ItemStack[] allContents = updateAPageFromContents(contents, player);
        this.setContents(ignored -> allContents);
        updateOpenMenus(contents, player);
    }

    @Override
    public void updateOpenMenus(Inventory contents, Player player) {
        this.currentlyOpenCopies.stream()
                .filter(o -> !contents.equals(o) && this.currentlyOpenPage.get(o.getViewers().get(0).getName()) == getPageNum(player))
                .forEach(inv -> {
                    BeforeUpdateOpenMenuEvent event = new BeforeUpdateOpenMenuEvent(this, inv, contents.getContents());
                    Bukkit.getPluginManager().callEvent(event);
                    if (!event.isCancelled())
                        inv.setContents(contents.getContents());
                    Bukkit.getPluginManager().callEvent(new AfterUpdateOpenMenuEvent(this, inv));
                });
    }

    @Override
    public void updateOpenMenus() {
        this.currentlyOpenCopies.forEach(inv -> {
            ItemStack[] newContents = getContentsPage((Player) inv.getViewers().get(0));
            BeforeUpdateOpenMenuEvent event = new BeforeUpdateOpenMenuEvent(this, inv, newContents);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled())
                inv.setContents(newContents);
            Bukkit.getPluginManager().callEvent(new AfterUpdateOpenMenuEvent(this, inv));
        });
    }

    @Override
    protected void saveToFile(Inventory contents, Player player, boolean saveToDisk) {
        ItemStack[] allContents = updateAPageFromContents(contents, player);
        this.saveFile.set("menu-saves." + GUIName + "." + Utils.getPlayerUUID(player.getName()), allContents);
        if (saveToDisk)
            this.saveFile.saveConfig();
    }

    public ItemStack[] getWithoutUIElements(Inventory contents) {
        return getWithoutUIElements(contents, Integer.MAX_VALUE);
    }

    public ItemStack[] getWithoutUIElements(Inventory contents, int numItems) {
        return numItems == 0 ? new ItemStack[0] :
                Arrays.stream(contents.getContents()).filter(ChestMenu::isNotUIElement).limit(numItems).toArray(ItemStack[]::new);
    }

    private static boolean isNotUIElement(ItemStack item) {
        return !(nextPage.isThisActionItem(item) || previousPage.isThisActionItem(item) || BARRIER.equals(item));
    }

    public void setOnChangePage(Consumer<ChangePageChestMenuEvent> onChangePage) {
        this.onChangePage = onChangePage;
    }

    public static class EventListener implements Listener {
        @EventHandler
        public void onChangePage(ChangePageChestMenuEvent e) {
            ChestMenu chestMenu = e.getChestMenu();
            if (chestMenu.onChangePage != null)
                chestMenu.onChangePage.accept(e);
        }
    }
}