package mod.journeycreative.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.journeycreative.screen.TrashcanInventory;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.datafixers.util.Pair;
import mod.journeycreative.JourneyCreative;
import mod.journeycreative.networking.JourneyClientNetworking;
import mod.journeycreative.networking.PlayerClientUnlocksData;
import mod.journeycreative.networking.TrashcanServerStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.HotbarManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.SessionSearchTrees;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.inventory.Hotbar;
import net.minecraft.client.searchtree.SearchTree;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/*
 * This file is part of Journey Creative.
 * * Potions of this code are derived from Minecraft source code.
 * Intellectual Property of Mojang AB. All rights reserved.
 *
 * All modifications, additions, and custom logic are licensed under the MIT License.
 * Full license text can be found in the LICENSE file at the root of this project.
 */
@OnlyIn(Dist.CLIENT)
public class JourneyInventoryScreen extends AbstractContainerScreen<JourneyInventoryScreen.JourneyScreenHandler> {
    public static final WidgetSprites JOURNEY_BUTTON_TEXTURES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "journey_button"),
            ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "journey_button_highlighted"));

    private static final ResourceLocation SCROLLER_TEXTURE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_TEXTURE = ResourceLocation.withDefaultNamespace("container/creative_inventory/scroller_disabled");
    private static final ResourceLocation[] TAB_TOP_UNSELECTED_TEXTURES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_unselected_7")};
    private static final ResourceLocation[] TAB_TOP_SELECTED_TEXTURES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_top_selected_7")};
    private static final ResourceLocation[] TAB_BOTTOM_UNSELECTED_TEXTURES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_unselected_7")};
    private static final ResourceLocation[] TAB_BOTTOM_SELECTED_TEXTURES = new ResourceLocation[]{ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_1"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_2"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_3"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_4"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_5"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_6"), ResourceLocation.withDefaultNamespace("container/creative_inventory/tab_bottom_selected_7")};
    private static final ResourceLocation JOURNEY_INVENTORY_TEXTURE = ResourceLocation.fromNamespaceAndPath(JourneyCreative.MODID, "textures/gui/gui_journey_inventory.png");
    private static final int ROWS_COUNT = 5;
    private static final int COLUMNS_COUNT = 9;
    private static final int TAB_WIDTH = 26;
    private static final int TAB_HEIGHT = 32;
    private static final int SCROLLBAR_WIDTH = 12;
    private static final int SCROLLBAR_HEIGHT = 15;
    private SimpleContainer INVENTORY;
    private static final Component DELETE_ITEM_SLOT_TEXT = Component.translatable("inventory.binSlot");
    static CreativeModeTab selectedTab = CreativeModeTabs.getDefaultTab();
    private float scrollPosition;
    private boolean scrolling;
    private EditBox searchBox;
    @Nullable
    private List<Slot> slots;
    @Nullable
    private Slot deleteItemSlot;
    private Slot pickupSlot;
    private JourneyInventoryListener listener;
    private boolean ignoreTypedCharacter;
    private boolean lastClickOutsideBounds;
    private final Set<TagKey<Item>> searchResultTags = new HashSet();
    private final boolean operatorTabEnabled;
    private final List<net.neoforged.neoforge.client.gui.CreativeTabsScreenPage> pages = new java.util.ArrayList<>();
    private net.neoforged.neoforge.client.gui.CreativeTabsScreenPage currentPage = new net.neoforged.neoforge.client.gui.CreativeTabsScreenPage(new java.util.ArrayList<>());

    public JourneyInventoryScreen(LocalPlayer player, FeatureFlagSet enabledFeatures, boolean operatorTabEnabled) {
        super(new JourneyScreenHandler(player), player.getInventory(), CommonComponents.EMPTY);
        player.containerMenu = this.menu;
        this.imageHeight = 136;
        this.imageWidth = 195;
        this.operatorTabEnabled = operatorTabEnabled;
        INVENTORY = this.menu.INVENTORY;
    }

    private boolean shouldShowOperatorTab(Player player) {
        return false;
    }

    private void updateDisplayParameters(FeatureFlagSet enabledFeatures, boolean showOperatorTab, HolderLookup.Provider registries) {
        ClientPacketListener clientPlayNetworkHandler = this.minecraft.getConnection();

        if (this.populateDisplay(clientPlayNetworkHandler != null ? clientPlayNetworkHandler.searchTrees() : null, enabledFeatures, showOperatorTab, registries)) {
            Iterator var5 = CreativeModeTabs.allTabs().iterator();

            while(true) {
                while(true) {
                    CreativeModeTab itemGroup;
                    Collection collection;
                    do {
                        if (!var5.hasNext()) {
                            return;
                        }

                        itemGroup = (CreativeModeTab)var5.next();
                        collection = itemGroup.getDisplayItems();
                    } while(itemGroup != selectedTab);

                    if (itemGroup.getType() == CreativeModeTab.Type.CATEGORY && collection.isEmpty()) {
                        this.setSelectedTab(CreativeModeTabs.getDefaultTab());
                    } else {
                        collection = filterUnlockedItems(collection);
                        this.refreshSelectedTab(collection);
                    }
                }
            }
        }

    }

    private boolean populateDisplay(@Nullable SessionSearchTrees searchManager, FeatureFlagSet enabledFeatures, boolean showOperatorTab, HolderLookup.Provider registries) {
        if (!CreativeModeTabs.tryRebuildTabContents(enabledFeatures, showOperatorTab, registries)) {
            return false;
        } else {
            if (searchManager != null) {
                List<ItemStack> list = List.copyOf(CreativeModeTabs.searchTab().getDisplayItems());
                searchManager.updateCreativeTooltips(registries, list);
                searchManager.updateCreativeTags(list);
            }

            return true;
        }
    }

    private void refreshSelectedTab(Collection<ItemStack> displayStacks) {
        int i = ((JourneyScreenHandler) this.menu).getRow(this.scrollPosition);
        ((JourneyScreenHandler) this.menu).itemList.clear();
        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.search();
        } else {
            ((JourneyScreenHandler) this.menu).itemList.addAll(displayStacks);
        }

        this.scrollPosition = ((JourneyScreenHandler) this.menu).getScrollPosition(i);
        ((JourneyScreenHandler) this.menu).scrollItems(this.scrollPosition);
    }

    public void containerTick() {
        super.containerTick();
        if (this.minecraft != null) {
            LocalPlayer clientPlayerEntity = this.minecraft.player;
            if (clientPlayerEntity != null) {
                this.updateDisplayParameters(clientPlayerEntity.connection.enabledFeatures(), this.shouldShowOperatorTab(clientPlayerEntity), clientPlayerEntity.level().registryAccess());
            }
        }
    }

    protected void slotClicked(@Nullable Slot slot, int slotId, int button, ClickType actionType) {
        if (this.isCreativeInventorySlot(slot)) {
            this.searchBox.moveCursorToEnd(false);
            this.searchBox.setHighlightPos(0);
        }

        boolean bl = actionType == ClickType.QUICK_MOVE;
        actionType = slotId == -999 && actionType == ClickType.PICKUP ? ClickType.THROW : actionType; // If pickup action, then throw
        ItemStack itemStack;
        if (slot == null && selectedTab.getType() != CreativeModeTab.Type.INVENTORY && actionType != ClickType.QUICK_CRAFT) { // click outside inventory
            if (!((JourneyScreenHandler) this.menu).getCarried().isEmpty() && this.lastClickOutsideBounds) {
                if (button == 0) {
                    this.minecraft.player.drop(((JourneyScreenHandler) this.menu).getCarried(), true);
                    JourneyClientNetworking.dropJourneyStack(((JourneyScreenHandler) this.menu).getCarried(), this.minecraft.player);
                    ((JourneyScreenHandler) this.menu).setCarried(ItemStack.EMPTY);
                }

                if (button == 1) {
                    itemStack = ((JourneyScreenHandler) this.menu).getCarried().split(1);
                    this.minecraft.player.drop(itemStack, true);
                    JourneyClientNetworking.dropJourneyStack(itemStack, this.minecraft.player);
                }
            }
        } else {
            if (slot != null && !slot.mayPickup(this.minecraft.player)) {
                return;
            }

            if (slot == this.deleteItemSlot && bl && this.deleteItemSlot.hasItem()) { // shift click delete item slot
                ItemStack tcStack = this.deleteItemSlot.getItem();
                boolean ret = this.menu.insertItemTrashcan(tcStack, 9, 46, false);
                if(ret) {
                    for (int k = 9; k < 45; ++k) {
                        Slot s = ((JourneyScreenHandler) this.menu).getSlot(k);
                        JourneyClientNetworking.clickJourneyStack(s.getItem(), ((JourneySlot) s).slot.index);
                    }
                    JourneyClientNetworking.sendTrashcanUpdate(tcStack);
                }
            } else {
                ItemStack itemStack2;
                if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) { // player inventory visible
                    if (slot == this.deleteItemSlot) { // click delete item slot
                        ItemStack cursorStack = this.menu.getCarried().copy();
                        if (cursorStack.isEmpty() && this.deleteItemSlot.hasItem()) {
                            itemStack2 = this.deleteItemSlot.getItem();
                            ((JourneyScreenHandler) this.menu).setCarried(itemStack2);
                            JourneyClientNetworking.sendTrashcanUpdate(ItemStack.EMPTY);
                        } else {
                            JourneyClientNetworking.sendTrashcanUpdate(cursorStack);
                            ((JourneyScreenHandler) this.menu).setCarried(ItemStack.EMPTY);
                        }
                    } else if (bl && slot != null && slot.hasItem()) { // shift click slot.
                        itemStack2 = slot.getItem();
                        JourneyClientNetworking.sendTrashcanUpdate(itemStack2);
                        slot.setByPlayer(ItemStack.EMPTY);
                        JourneyClientNetworking.clickJourneyStack(ItemStack.EMPTY, ((JourneySlot) slot).slot.index);
                    } else if (actionType == ClickType.THROW && slot != null && slot.hasItem()) {
                        itemStack = slot.remove(button == 0 ? 1 : slot.getItem().getMaxStackSize());
                        itemStack2 = slot.getItem();
                        this.minecraft.player.drop(itemStack, true);
                        JourneyClientNetworking.dropJourneyStack(itemStack, this.minecraft.player);
                        JourneyClientNetworking.clickJourneyStack(itemStack2, ((JourneySlot) slot).slot.index);
                    } else if (actionType == ClickType.THROW && slotId == -999 && !((JourneyScreenHandler) this.menu).getCarried().isEmpty()) {
                        this.minecraft.player.drop(((JourneyScreenHandler) this.menu).getCarried(), true);
                        JourneyClientNetworking.dropJourneyStack(((JourneyScreenHandler) this.menu).getCarried(), this.minecraft.player);
                        ((JourneyScreenHandler) this.menu).setCarried(ItemStack.EMPTY);
                    } else {
                        this.minecraft.player.inventoryMenu.clicked(slot == null ? slotId : ((JourneySlot) slot).slot.index, button, actionType, this.minecraft.player);
                    }
                    for (int k = 9; k < 45; ++k) {
                        Slot s = ((JourneyScreenHandler) this.menu).getSlot(k);
                        JourneyClientNetworking.clickJourneyStack(s.getItem(), ((JourneySlot) s).slot.index);
                    }
                } else {
                    ItemStack itemStack3;
                    if (actionType != ClickType.QUICK_CRAFT && slot.container == INVENTORY) {
                        itemStack = ((JourneyScreenHandler) this.menu).getCarried();
                        itemStack2 = slot.getItem();
                        if (actionType == ClickType.SWAP) {
                            if (!itemStack2.isEmpty() && this.minecraft.player.getInventory().getItem(button).isEmpty()) {
                                this.minecraft.player.getInventory().setItem(button, itemStack2.copyWithCount(itemStack2.getMaxStackSize()));
                                this.minecraft.player.inventoryMenu.broadcastChanges();
                            }

                            return;
                        }

                        if (actionType == ClickType.CLONE) {
                            if (((JourneyScreenHandler) this.menu).getCarried().isEmpty() && slot.hasItem()) {
                                itemStack3 = slot.getItem();
                                ((JourneyScreenHandler) this.menu).setCarried(itemStack3.copyWithCount(itemStack3.getMaxStackSize()));
                            }

                            return;
                        }

                        if (actionType == ClickType.THROW) {
                            if (!itemStack2.isEmpty()) {
                                itemStack3 = itemStack2.copyWithCount(button == 0 ? 1 : itemStack2.getMaxStackSize());
                                this.minecraft.player.drop(itemStack3, true);
                                JourneyClientNetworking.dropJourneyStack(itemStack3, this.minecraft.player);
                            }

                            return;
                        }

                        if (!itemStack.isEmpty() && !itemStack2.isEmpty() && ItemStack.isSameItemSameComponents(itemStack, itemStack2)) {
                            if (button == 0) {
                                if (bl) {
                                    itemStack.setCount(itemStack.getMaxStackSize());
                                } else if (itemStack.getCount() < itemStack.getMaxStackSize()) {
                                    itemStack.grow(1);
                                }
                            } else {
                                itemStack.shrink(1);
                            }
                        } else if (!itemStack2.isEmpty() && itemStack.isEmpty()) {
                            int j = bl ? itemStack2.getMaxStackSize() : itemStack2.getCount();
                            ((JourneyScreenHandler) this.menu).setCarried(itemStack2.copyWithCount(j));
                        } else if (button == 0) {
                            ((JourneyScreenHandler) this.menu).setCarried(ItemStack.EMPTY);
                        } else if (!((JourneyScreenHandler) this.menu).getCarried().isEmpty()) {
                            ((JourneyScreenHandler) this.menu).getCarried().shrink(1);
                        }
                    } else if (this.menu != null) {
                        itemStack = slot == null ? ItemStack.EMPTY : ((JourneyScreenHandler) this.menu).getSlot(slot.index).getItem();
                        ((JourneyScreenHandler) this.menu).clicked(slot == null ? slotId : slot.index, button, actionType, this.minecraft.player);
                        int k;
                        if (AbstractContainerMenu.getQuickcraftHeader(button) == 2) {
                            for (k = 0; k < 9; ++k) {
                                JourneyClientNetworking.clickJourneyStack(((JourneyScreenHandler) this.menu).getSlot(45 + k).getItem(), 36 + k);
                            }
                        } else if (slot != null && Inventory.isHotbarSlot(slot.getContainerSlot()) && selectedTab.getType() != CreativeModeTab.Type.INVENTORY) {
                            if (actionType == ClickType.THROW && !itemStack.isEmpty() && !((JourneyScreenHandler) this.menu).getCarried().isEmpty()) {
                                k = button == 0 ? 1 : itemStack.getCount();
                                itemStack3 = itemStack.copyWithCount(k);
                                itemStack.shrink(k);
                                this.minecraft.player.drop(itemStack3, true);
                                JourneyClientNetworking.dropJourneyStack(itemStack3, this.minecraft.player);
                            }

                            this.minecraft.player.inventoryMenu.broadcastChanges();
                        }
                    }
                }
            }
        }
    }

    private boolean isCreativeInventorySlot(@Nullable Slot slot) {
        return slot != null && slot.container == INVENTORY;
    }

    @Override
    protected  void init() {
        super.init();
        this.pages.clear();
        int tabIndex = 0;
        List<CreativeModeTab> currentPage = new java.util.ArrayList<>();

        var featureFlags = Minecraft.getInstance().player.connection.enabledFeatures();
        var registries = Minecraft.getInstance().player.connection.registryAccess();
        for (CreativeModeTab tab : net.neoforged.neoforge.common.CreativeModeTabRegistry.getSortedCreativeModeTabs()) {
            // This forces the tab to generate its internal item list
            tab.buildContents(new CreativeModeTab.ItemDisplayParameters(featureFlags, operatorTabEnabled, registries));
        }

        for (CreativeModeTab sortedCreativeModeTab : net.neoforged.neoforge.common.CreativeModeTabRegistry.getSortedCreativeModeTabs()
                .stream().filter(CreativeModeTab::hasAnyItems).toList()) {
            currentPage.add(sortedCreativeModeTab);
            tabIndex++;
            if (tabIndex == 10) {
                this.pages.add(new net.neoforged.neoforge.client.gui.CreativeTabsScreenPage(currentPage));
                currentPage = new java.util.ArrayList<>();
                tabIndex = 0;
            }
        }
        if (tabIndex != 0) {
            this.pages.add(new net.neoforged.neoforge.client.gui.CreativeTabsScreenPage(currentPage));
        }
        if (this.pages.isEmpty()) {
            this.currentPage = new net.neoforged.neoforge.client.gui.CreativeTabsScreenPage(new java.util.ArrayList<>());
        } else {
            this.currentPage = this.pages.get(0);
        }
        if (this.pages.size() > 1) {
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal("<"), b -> setCurrentPage(this.pages.get(Math.max(this.pages.indexOf(this.currentPage) - 1, 0)))).pos(leftPos,  topPos - 50).size(20, 20).build());
            addRenderableWidget(net.minecraft.client.gui.components.Button.builder(Component.literal(">"), b -> setCurrentPage(this.pages.get(Math.min(this.pages.indexOf(this.currentPage) + 1, this.pages.size() - 1)))).pos(leftPos + imageWidth - 20, topPos - 50).size(20, 20).build());
        }
        this.currentPage = this.pages.stream().filter(page -> page.getVisibleTabs().contains(selectedTab)).findFirst().orElse(this.currentPage);
        if (!this.currentPage.getVisibleTabs().contains(selectedTab)) {
            selectedTab = this.currentPage.getVisibleTabs().get(0);
        }
        Font var10003 = this.font;
        int var10004 = this.leftPos + 82;
        int var10005 = this.topPos + 6;
        Objects.requireNonNull(this.font);
        this.searchBox = new EditBox(var10003, var10004, var10005, 80, 9, Component.translatable("itemGroup.search"));
        this.searchBox.setMaxLength(50);
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(false);
        this.searchBox.setTextColor(-1);
        this.addWidget(this.searchBox);
        CreativeModeTab itemGroup = selectedTab;
        selectedTab = CreativeModeTabs.getDefaultTab();
        this.setSelectedTab(itemGroup);
        this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        this.listener = new JourneyInventoryListener(this.minecraft);
        this.minecraft.player.inventoryMenu.addSlotListener(this.listener);
        if (!selectedTab.shouldDisplay()) {
            this.setSelectedTab(CreativeModeTabs.getDefaultTab());
        }

        ScreenPosition survivalButtonPos = new ScreenPosition(this.leftPos + 149, this.topPos + this.imageHeight + 5);
        this.addRenderableWidget(
                new ImageButton(survivalButtonPos.x(), survivalButtonPos.y(), 20, 18, JourneyInventoryScreen.JOURNEY_BUTTON_TEXTURES, (button) -> {
                    Minecraft.getInstance().setScreen(new InventoryScreen(Minecraft.getInstance().player));
                })
        );
    }

    public void resize(Minecraft client, int width, int height) {
        int i = ((JourneyScreenHandler) this.menu).getRow(this.scrollPosition);
        String string  = this.searchBox.getValue();
        this.init(client, width, height);
        this.searchBox.setValue(string);
        if (!this.searchBox.getValue().isEmpty()) {
            this.search();
        }

        this.scrollPosition = ((JourneyScreenHandler) this.menu).getScrollPosition(i);
        ((JourneyScreenHandler) this.menu).scrollItems(this.scrollPosition);
    }

    public void removed() {
        super.removed();
        if (this.minecraft.player != null && this.minecraft.player.getInventory() != null) {
            this.minecraft.player.inventoryMenu.removeSlotListener(this.listener);
        }
    }

    public boolean charTyped(char chr, int modifiers) {
        if (this.ignoreTypedCharacter) {
            return false;
        } else if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            return false;
        } else {
            String string = this.searchBox.getValue();
            if (this.searchBox.charTyped(chr, modifiers)) {
                if (!Objects.equals(string, this.searchBox.getValue())) {
                    this.search();
                }

                return true;
            } else {
                return false;
            }
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        if (selectedTab.getType() != CreativeModeTab.Type.SEARCH) {
            if (this.minecraft.options.keyChat.matches(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                this.setSelectedTab(CreativeModeTabs.searchTab());
                return true;
            } else {
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
        } else {
            boolean bl = !this.isCreativeInventorySlot(this.hoveredSlot) || this.hoveredSlot.hasItem();
            boolean bl2 = InputConstants.getKey(keyCode, scanCode).getNumericKeyValue().isPresent();
            if (bl && bl2 && this.checkHotbarKeyPressed(keyCode, scanCode)) {
                this.ignoreTypedCharacter = true;
                return true;
            } else {
                String string = this.searchBox.getValue();
                if (this.searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                    if (!Objects.equals(string, this.searchBox.getValue())) {
                        this.search();
                    }

                    return true;
                } else {
                    return this.searchBox.isFocused() && this.searchBox.isVisible() && keyCode != 256 || super.keyPressed(keyCode, scanCode, modifiers);
                }
            }
        }
    }

    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        this.ignoreTypedCharacter = false;
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    private void search() {
        ((JourneyScreenHandler) this.menu).itemList.clear();
        this.searchResultTags.clear();
        String searchString = this.searchBox.getValue();
        if (searchString.isEmpty()) {
            Collection<ItemStack> filteredItems = filterUnlockedItems(selectedTab.getDisplayItems());
            ((JourneyScreenHandler) this.menu).itemList.addAll(filteredItems);
        } else {
            ClientPacketListener clientPlayNetworkHandler = this.minecraft.getConnection();
            if (clientPlayNetworkHandler != null) {
                SessionSearchTrees searchManager = clientPlayNetworkHandler.searchTrees();
                SearchTree searchProvider;
                if (searchString.startsWith("#")) {
                    searchString = searchString.substring(1);
                    searchProvider = searchManager.creativeTagSearch();
                    this.searchForTags(searchString);
                } else {
                    searchProvider = searchManager.creativeNameSearch();
                }

                Collection<ItemStack> filteredItems = filterUnlockedItems((Collection<ItemStack>) searchProvider.search(searchString.toLowerCase(Locale.ROOT)));
                ((JourneyScreenHandler) this.menu).itemList.addAll(filteredItems);
            }
        }

        this.scrollPosition = 0.0F;
        ((JourneyScreenHandler) this.menu).scrollItems(0.0F);
    }

    private void searchForTags(String id) {
        int i = id.indexOf(58);
        Predicate<ResourceLocation> predicate;
        if (i == -1) {
            predicate = (ResourceLocation idx) -> {
                return idx.getPath().contains(id);
            };
        } else {
            String string = id.substring(0, i).trim();
            String string2 = id.substring(i+1).trim();
            predicate = (ResourceLocation idx) -> {
                return idx.getNamespace().contains(string) && idx.getPath().contains(string2);
            };
        }

        Stream var10000 = BuiltInRegistries.ITEM.getTagNames().filter((tag) -> {
            return predicate.test(tag.location());
        });
        Set var10001 = this.searchResultTags;
        Objects.requireNonNull(var10001);
        var10000.forEach(var10001::add);
    }

    protected void renderLabels(GuiGraphics context, int mouseX, int mouseY) {
        if (selectedTab.showTitle()) {
            context.drawString(this.font, selectedTab.getDisplayName(), 8, 6, -12566464, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double d = mouseX - (double) this.leftPos;
            double e = mouseY - (double) this.topPos;
            Iterator var10 = currentPage.getVisibleTabs().iterator();

            while (var10.hasNext()) {
                CreativeModeTab itemGroup = (CreativeModeTab) var10.next();
                if (itemGroup.getType() == CreativeModeTab.Type.HOTBAR) continue;
                if (BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(itemGroup)
                        .map(key -> key == CreativeModeTabs.OP_BLOCKS)
                        .orElse(false)) continue;

                if (this.isClickInTab(itemGroup, d, e)) {
                    return true;
                }
            }

            if (selectedTab.getType() != CreativeModeTab.Type.INVENTORY && this.isClickInScrollbar(mouseX, mouseY)) {
                this.scrolling = this.hasScrollbar();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            double d = mouseX - (double) this.leftPos;
            double e = mouseY - (double) this.topPos;
            this.scrolling = false;
            Iterator var10 = currentPage.getVisibleTabs().iterator();

            while (var10.hasNext()) {
                CreativeModeTab itemGroup = (CreativeModeTab) var10.next();
                if (itemGroup.getType() == CreativeModeTab.Type.HOTBAR) continue;
                if (BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(itemGroup)
                        .map(key -> key == CreativeModeTabs.OP_BLOCKS)
                        .orElse(false)) continue;
                if (this.isClickInTab(itemGroup, d, e)) {
                    this.setSelectedTab(itemGroup);
                    return true;
                }
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    private boolean hasScrollbar() {
        return selectedTab.canScroll() && ((JourneyScreenHandler) this.menu).shouldShowScrollbar();
    }

    private Collection<ItemStack> filterUnlockedItems(Collection<ItemStack> unfilteredItems) {
        Collection<ItemStack> filtered = ItemStackLinkedSet.createTypeAndComponentsSet();
        for (ItemStack itemStack : unfilteredItems) {
            if (PlayerClientUnlocksData.isUnlocked(itemStack)) {
                filtered.add(itemStack.copy());
            }
        }

        return filtered;
    }

    private void setSelectedTab(CreativeModeTab group) {
        CreativeModeTab itemGroup = selectedTab;
        selectedTab = group;
        this.quickCraftSlots.clear();
        ((JourneyScreenHandler) this.menu).itemList.clear();
        this.clearDraggingState();
        int i;
        int j;
        if (selectedTab.getType() == CreativeModeTab.Type.CATEGORY) {
            Collection<ItemStack> filteredItems = filterUnlockedItems(selectedTab.getDisplayItems());
            ((JourneyScreenHandler) this.menu).itemList.addAll(filteredItems);
        }

        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            AbstractContainerMenu screenHandler = this.minecraft.player.inventoryMenu;
            if (this.slots == null) {
                this.slots = ImmutableList.copyOf(((JourneyScreenHandler)this.menu).slots);
            }

            ((JourneyScreenHandler) this.menu).slots.clear();

            for (i = 0; i < screenHandler.slots.size(); ++i) {
                int n;
                int k;
                int l;
                int m;
                if (i >= 5 && i < 9) {
                    k = i - 5;
                    l = k / 2;
                    m = k % 2;
                    n = 54 + l * 54;
                    j = 6 + m * 27;
                } else if (i >= 0 && i < 5) {
                    n = -2000;
                    j = -2000;
                } else if (i == 45) {
                    n = 35;
                    j = 20;
                } else {
                    k = i - 9;
                    l = k % 9;
                    m = k / 9;
                    n = 9 + l * 18;
                    if (i >= 36) {
                        j = 112;
                    } else {
                        j = 54 + m * 18;
                    }
                }

                Slot slot = new JourneySlot((Slot) screenHandler.slots.get(i), i, n, j);
                ((JourneyScreenHandler) this.menu).slots.add(slot);
            }

            this.deleteItemSlot = new Slot(this.menu.trashcanInventory, 0, 173, 112);
            ((JourneyScreenHandler) this.menu).slots.add(this.deleteItemSlot);
        } else if (itemGroup.getType() == CreativeModeTab.Type.INVENTORY) {
            ((JourneyScreenHandler) this.menu).slots.clear();
            ((JourneyScreenHandler) this.menu).slots.addAll(this.slots);
            this.slots = null;
        }

        if (selectedTab.getType() == CreativeModeTab.Type.SEARCH) {
            this.searchBox.setVisible(true);
            this.searchBox.setCanLoseFocus(false);
            this.searchBox.setFocused(true);
            if (itemGroup != group) {
                this.searchBox.setValue("");
            }

            this.search();
        } else {
            this.searchBox.setVisible(false);
            this.searchBox.setCanLoseFocus(true);
            this.searchBox.setFocused(false);
            this.searchBox.setValue("");
        }

        this.scrollPosition = 0.0F;
        ((JourneyScreenHandler) this.menu).scrollItems(0.0F);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        } else if (!this.hasScrollbar()) {
            return false;
        } else {
            this.scrollPosition = ((JourneyScreenHandler) this.menu).getScrollPosition(this.scrollPosition, verticalAmount);
            ((JourneyScreenHandler) this.menu).scrollItems(this.scrollPosition);
            return true;
        }
    }

    protected boolean hasClickedOutside(double mouseX, double mouseY, int left, int top, int button) {
        boolean bl = mouseX < (double)left || mouseY < (double)top || mouseX >= (double)(left + this.imageWidth) || mouseY >= (double)(top + this.imageHeight);
        this.lastClickOutsideBounds = bl && !this.isClickInTab(selectedTab, mouseX, mouseY);
        return this.lastClickOutsideBounds;
    }

    protected boolean isClickInScrollbar(double mouseX, double mouseY) {
        int i = this.leftPos;
        int j = this.topPos;
        int k = i + 175;
        int l = j + 18;
        int m = k + 14;
        int n = l + 112;
        return mouseX >= (double) k && mouseY >= (double) l && mouseX < (double) m && mouseY < (double) n;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.scrolling) {
            int i = this.topPos + 18;
            int j = i + 112;
            this.scrollPosition = ((float) mouseY - (float) i - 7.5F) / ((float) (j - i) - 15.0F);
            this.scrollPosition = Mth.clamp(this.scrollPosition, 0.0F, 1.0F);
            ((JourneyScreenHandler) this.menu).scrollItems(this.scrollPosition);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
        super.render(context, mouseX, mouseY, deltaTicks);

        if (this.deleteItemSlot != null &&
                selectedTab.getType() == CreativeModeTab.Type.INVENTORY &&
                this.isHovering(this.deleteItemSlot.x, this.deleteItemSlot.y, 16, 16, (double) mouseX, (double) mouseY)) {
            context.renderTooltip(this.font, DELETE_ITEM_SLOT_TEXT, mouseX, mouseY);
        }

        Iterator var5 = currentPage.getVisibleTabs().iterator();

        if (this.pages.size() != 1) {
            Component page = Component.literal(String.format("%d / %d", this.pages.indexOf(this.currentPage) + 1, this.pages.size()));
            context.pose().pushPose();
            context.pose().translate(0F, 0F, 300F);
            context.drawString(font, page.getVisualOrderText(), leftPos + (imageWidth / 2) - (font.width(page) / 2), topPos - 44, -1);
            context.pose().popPose();
        }

        while (var5.hasNext()) {
            CreativeModeTab itemGroup = (CreativeModeTab)var5.next();
            if (itemGroup.getType() == CreativeModeTab.Type.HOTBAR) continue;
            if (BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(itemGroup)
                    .map(key -> key == CreativeModeTabs.OP_BLOCKS)
                    .orElse(false)) continue;

            if (this.renderTabTooltipIfHovered(context, itemGroup, mouseX, mouseY)) {
                break;
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        this.renderTooltip(context, mouseX, mouseY);
    }

    public List<Component> getTooltipFromContainerItem(ItemStack stack) {
        boolean bl = this.hoveredSlot != null && this.hoveredSlot instanceof JourneyInventoryScreen.LockableSlot;
        boolean bl2 = selectedTab.getType() == CreativeModeTab.Type.CATEGORY;
        boolean bl3 = selectedTab.getType() == CreativeModeTab.Type.SEARCH;
        TooltipFlag.Default default_ = this.minecraft.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
        TooltipFlag tooltipType = bl ? default_.asCreative() : default_;
        List<Component> list = stack.getTooltipLines(Item.TooltipContext.of(this.minecraft.level), this.minecraft.player, tooltipType);
        if (list.isEmpty()) {
            return list;
        } else if (bl2 && bl) {
            return list;
        } else {
            List<Component> list2 = Lists.newArrayList(list);
            if (bl3 && bl) {
                this.searchResultTags.forEach((tagKey) -> {
                    if (stack.is(tagKey)) {
                        list2.add(1, Component.literal("#" + String.valueOf(tagKey.location())).withStyle(ChatFormatting.DARK_PURPLE));
                    }

                });
            }

            int i = 1;
            Iterator var10 = CreativeModeTabs.tabs().iterator();

            while(var10.hasNext()) {
                CreativeModeTab itemGroup = (CreativeModeTab) var10.next();
                if (itemGroup.getType() != CreativeModeTab.Type.SEARCH && itemGroup.contains(stack)) {
                    list2.add(i++, itemGroup.getDisplayName().copy().withStyle(ChatFormatting.BLUE));
                }
            }

            return list2;
        }
    }

    protected void renderBg(GuiGraphics context, float deltaTicks, int mouseX, int mouseY) {
        Iterator var5 = currentPage.getVisibleTabs().iterator();

        while(var5.hasNext()) {
            CreativeModeTab itemGroup = (CreativeModeTab) var5.next();
            if (itemGroup.getType() == CreativeModeTab.Type.HOTBAR) continue;
            if (BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(itemGroup)
                    .map(key -> key == CreativeModeTabs.OP_BLOCKS)
                    .orElse(false)) continue;
            if (itemGroup != selectedTab) {
                this.renderTabIcon(context, itemGroup);
            }
        }

        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            context.blit(JourneyInventoryScreen.JOURNEY_INVENTORY_TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        } else {
            context.blit(selectedTab.getBackgroundTexture(), this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        }
        this.searchBox.render(context, mouseX, mouseY, deltaTicks);
        int i = this.leftPos + 175;
        int j = this.topPos + 18;
        int k = j + 112;
        if (selectedTab.canScroll()) {
            ResourceLocation identifier = this.hasScrollbar() ? SCROLLER_TEXTURE : SCROLLER_DISABLED_TEXTURE;
            context.blitSprite(identifier, i, j + (int)((float)(k - j - 17) * this.scrollPosition), 12, 15);
        }

        if (currentPage.getVisibleTabs().contains(selectedTab)) {
            this.renderTabIcon(context, selectedTab);
        }
        if (selectedTab.getType() == CreativeModeTab.Type.INVENTORY) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(context, this.leftPos + 73, this.topPos + 6, this.leftPos + 105, this.topPos + 49, 20, 0.0625F, (float)mouseX, (float)mouseY, this.minecraft.player);
        }
    }

    private int getTabX(CreativeModeTab group) {
        int i = this.currentPage.getColumn(group);
        int k = 27 * i;
        if (group.isAlignedRight()) {
            k = this.imageWidth - 27 * (7 - i) + 1;
        }

        return k;
    }

    private int getTabY(CreativeModeTab group) {
        int i = 0;
        if (this.currentPage.isTop(group)) {
            i -= 32;
        } else {
            i += this.imageHeight;
        }

        return i;
    }

    protected boolean isClickInTab(CreativeModeTab group, double mouseX, double mouseY) {
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        return mouseX >= (double) i && mouseX <= (double) (i + 26) && mouseY >= (double) j && mouseY <= (double) (j + 32);
    }

    protected boolean renderTabTooltipIfHovered(GuiGraphics context, CreativeModeTab group, int mouseX, int mouseY) {
        int i = this.getTabX(group);
        int j = this.getTabY(group);
        if (this.isHovering(i + 3, j + 3, 21, 27, (double) mouseX, (double) mouseY)) {
            context.renderTooltip(this.font, group.getDisplayName(), mouseX, mouseY);
            return true;
        } else {
            return false;
        }
    }

    protected void renderTabIcon(GuiGraphics context, CreativeModeTab group) {
        boolean bl = group == selectedTab;
        boolean bl2 = currentPage.isTop(group);
        int i = group.column();
        int j = this.leftPos + this.getTabX(group);
        int k = this.topPos - (bl2 ? 28 : -(this.imageHeight - 4));
        ResourceLocation[] identifiers;
        if (bl2) {
            identifiers = bl ? TAB_TOP_SELECTED_TEXTURES : TAB_TOP_UNSELECTED_TEXTURES;
        } else {
            identifiers = bl ? TAB_BOTTOM_SELECTED_TEXTURES : TAB_BOTTOM_UNSELECTED_TEXTURES;
        }

        context.blitSprite(identifiers[Mth.clamp(i, 0, identifiers.length)], j, k, 26, 32);
        context.pose().pushPose();
        context.pose().translate(0.0F, 0.0F, 100.0F);
        j += 5;
        k += 8 + (bl2 ? 1 : -1);
        ItemStack itemStack = group.getIconItem();
        context.renderItem(itemStack, j, k);
        context.renderItemDecorations(this.font, itemStack, j, k);
        context.pose().popPose();
    }

    public boolean isInventoryTabSelected() {
        return selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
    }

    public static void onHotbarKeyPress(Minecraft client, int index, boolean restore, boolean save) {
        LocalPlayer clientPlayerEntity = client.player;
        RegistryAccess dynamicRegistryManager = clientPlayerEntity.level().registryAccess();
        HotbarManager hotbarStorage = client.getHotbarManager();
        Hotbar hotbarStorageEntry = hotbarStorage.get(index);
        if (restore) {
            List<ItemStack> list = hotbarStorageEntry.load(dynamicRegistryManager);

            for(int i = 0; i < Inventory.getSelectionSize(); ++i) {
                ItemStack itemStack = (ItemStack)list.get(i);
                clientPlayerEntity.getInventory().setItem(i, itemStack);
                JourneyClientNetworking.clickJourneyStack(itemStack, 36 + i);
            }

            clientPlayerEntity.inventoryMenu.broadcastChanges();
        } else if (save) {
            hotbarStorageEntry.storeFrom(clientPlayerEntity.getInventory(), dynamicRegistryManager);
            Component text = client.options.keyHotbarSlots[index].getTranslatedKeyMessage();
            Component text2 = client.options.keyLoadHotbarActivator.getTranslatedKeyMessage();
            Component text3 = Component.translatable("inventory.hotbarSaved", new Object[]{text2, text});
            client.gui.setOverlayMessage(text3, false);
            client.getNarrator().sayNow(text3);
            hotbarStorage.save();
        }

    }

    public net.neoforged.neoforge.client.gui.CreativeTabsScreenPage getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(net.neoforged.neoforge.client.gui.CreativeTabsScreenPage currentPage) {
        this.currentPage = currentPage;
    }

    public Slot getDeleteItemSlot() {
        return this.deleteItemSlot;
    }

    @OnlyIn(Dist.CLIENT)
    public static class JourneyScreenHandler extends AbstractContainerMenu {
        public final NonNullList<ItemStack> itemList = NonNullList.create();
        private final AbstractContainerMenu parent;
        private TrashcanInventory trashcanInventory;
        private SimpleContainer INVENTORY;

        public JourneyScreenHandler(Player player) {
            super((MenuType) null, 0);
            this.parent = player.inventoryMenu;
            Inventory playerInventory = player.getInventory();
            INVENTORY = new SimpleContainer(45);

            for(int i = 0; i < 5; ++i) {
                for(int j = 0; j < 9; ++j) {
                    this.addSlot(new JourneyInventoryScreen.LockableSlot(INVENTORY, i * 9 + j, 9 + j * 18, 18 + i * 18));
                }
            }

            this.addPlayerHotbarSlots(playerInventory, 9, 112);
            this.scrollItems(0.0F);
            this.trashcanInventory = TrashcanServerStorage.get(player);
        }

        public boolean stillValid(Player player) {
            return true;
        }

        protected int getOverflowRows() {
            return Mth.positiveCeilDiv(this.itemList.size(), 9) - 5;
        }

        protected int getRow(float scroll) {
            return Math.max((int)((double)(scroll * (float)this.getOverflowRows()) + 0.5), 0);
        }

        protected float getScrollPosition(int row) {
            return Mth.clamp((float)row / (float)this.getOverflowRows(), 0.0F, 1.0F);
        }

        protected float getScrollPosition(float current, double amount) {
            return Mth.clamp(current - (float)(amount / (double)this.getOverflowRows()), 0.0F, 1.0F);
        }

        public void scrollItems(float position) {
            int i = this.getRow(position);

            for(int j = 0; j < 5; ++j) {
                for(int k = 0; k < 9; ++k) {
                    int l = k + (j + i) * 9;
                    if (l >= 0 && l < this.itemList.size()) {
                        INVENTORY.setItem(k + j * 9, (ItemStack)this.itemList.get(l).copy());
                    } else {
                        INVENTORY.setItem(k + j * 9, ItemStack.EMPTY);
                    }
                }
            }
        }

        public boolean shouldShowScrollbar() {
            return this.itemList.size() > 45;
        }

        public ItemStack quickMoveStack(Player player, int slot) {
            if (slot >= this.slots.size() - 9 && slot < this.slots.size()) {
                Slot slot2 = (Slot)this.slots.get(slot);
                if (slot2 != null && slot2.hasItem()) {
                    slot2.setByPlayer(ItemStack.EMPTY);
                }
            }

            return ItemStack.EMPTY;
        }

        public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
            return slot.container != this.INVENTORY;
        }

        public boolean canDragTo(Slot slot) {
            return slot.container != this.INVENTORY;
        }

        public ItemStack getCarried() {
            return this.parent.getCarried();
        }

        public void setCarried(ItemStack stack) {
            this.parent.setCarried(stack);
        }

        @Override
        public void setItem(int slot, int revision, ItemStack stack) {
            if (slot < 45 && JourneyInventoryScreen.selectedTab.getType() != CreativeModeTab.Type.INVENTORY) {
                this.parent.setItem(slot, revision, stack);
                return;
            }
            super.setItem(slot, revision, stack);
        }

        protected boolean insertItemTrashcan(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
            boolean bl = false;
            int i = startIndex;
            if (fromLast) {
                i = endIndex - 1;
            }

            Slot slot;
            ItemStack itemStack;
            int j;
            if (stack.isStackable()) {
                while(!stack.isEmpty()) {
                    if (fromLast) {
                        if (i < startIndex) {
                            break;
                        }
                    } else if (i >= endIndex) {
                        break;
                    }

                    slot = (Slot)this.slots.get(i);
                    itemStack = slot.getItem();
                    if (!itemStack.isEmpty() && ItemStack.isSameItemSameComponents(stack, itemStack)) {
                        j = itemStack.getCount() + stack.getCount();
                        int k = slot.getMaxStackSize(itemStack);
                        if (j <= k) {
                            stack.setCount(0);
                            itemStack.setCount(j);
                            slot.setChanged();
                            bl = true;
                        } else if (itemStack.getCount() < k) {
                            stack.shrink(k - itemStack.getCount());
                            itemStack.setCount(k);
                            slot.setChanged();
                            bl = true;
                        }
                    }

                    if (fromLast) {
                        --i;
                    } else {
                        ++i;
                    }
                }
            }

            if (!stack.isEmpty()) {
                if (fromLast) {
                    i = endIndex - 1;
                } else {
                    i = startIndex;
                }

                while(true) {
                    if (fromLast) {
                        if (i < startIndex) {
                            break;
                        }
                    } else if (i >= endIndex) {
                        break;
                    }

                    slot = (Slot)this.slots.get(i);
                    itemStack = slot.getItem();
                    if (itemStack.isEmpty() && slot.mayPlace(stack)) {
                        j = slot.getMaxStackSize(stack);
                        slot.setByPlayer(stack.split(Math.min(stack.getCount(), j)));
                        slot.setChanged();
                        bl = true;
                        break;
                    }

                    if (fromLast) {
                        --i;
                    } else {
                        ++i;
                    }
                }
            }

            return bl;
        }

        @Override
        public void removed(Player player) {
            super.removed(player);

            ItemStack cursorStack = this.getCarried();

            if (!cursorStack.isEmpty()) {
                player.drop(cursorStack, true);
                JourneyClientNetworking.dropJourneyStack(cursorStack, (LocalPlayer) player);
                this.setCarried(ItemStack.EMPTY);
            }
        }

        private void addPlayerHotbarSlots(Inventory playerInventory, int x, int y) {
            int m;

            // Player Hotbar
            for (m = 0; m < 9; ++m) {
                this.addSlot(new Slot(playerInventory, m, x + m * 18, y));
            }
        }

    }

    @OnlyIn(Dist.CLIENT)
    private static class JourneySlot extends Slot {
        final Slot slot;

        public JourneySlot(Slot slot, int invSlot, int x, int y) {
            super(slot.container, invSlot, x, y);
            this.slot = slot;
        }

        public void onTake(Player player, ItemStack stack) {
            this.slot.onTake(player, stack);
        }

        public boolean mayPlace(ItemStack stack) {
            return this.slot.mayPlace(stack);
        }

        public ItemStack getItem() {
            return this.slot.getItem();
        }

        public boolean hasItem() {
            return this.slot.hasItem();
        }

        public void setByPlayer(ItemStack stack, ItemStack previousStack) {
            this.slot.setByPlayer(stack, previousStack);
        }

        public void set(ItemStack stack) {
            this.slot.set(stack);
        }

        public void setChanged() {
            this.slot.setChanged();
        }

        public int getMaxStackSize() {
            return this.slot.getMaxStackSize();
        }

        public int getMaxStackSize(ItemStack stack) {
            return this.slot.getMaxStackSize(stack);
        }

        @Nullable
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return this.slot.getNoItemIcon();
        }

        public ItemStack remove(int amount) {
            return this.slot.remove(amount);
        }

        public boolean isActive() {
            return this.slot.isActive();
        }

        public boolean mayPickup(Player playerEntity) {
            return this.slot.mayPickup(playerEntity);
        }
    }

    @OnlyIn(Dist.CLIENT)
    static class LockableSlot extends Slot {
        public LockableSlot(Container inventory, int i, int j, int k) {
            super(inventory, i, j, k);
        }

        public boolean mayPickup(Player playerEntity) {
            ItemStack itemStack = this.getItem();
            if (super.mayPickup(playerEntity) && !itemStack.isEmpty()) {
                return itemStack.isItemEnabled(playerEntity.level().enabledFeatures()) && !itemStack.has(DataComponents.CREATIVE_SLOT_LOCK);
            } else {
                return itemStack.isEmpty();
            }
        }
    }
}

