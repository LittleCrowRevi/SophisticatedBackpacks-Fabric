package net.p3pp3rf1y.sophisticatedbackpacks.core.api;

public interface IStorageWrapper {

    void setSaveHandler(Runnable saveHandler);

    default void setInventorySlotChangeHandler(Runnable slotChangeHandler) {
        //noop
    }

    ITrackedContentsItemHandler getInventoryForUpgradeProcessing();

    InventoryHandler getInventoryHandler();

    ITrackedContentsItemHandler getInventoryForInputOutput();

    default void setUpgradeCachesInvalidatedHandler(Runnable handler) {
        //noop
    }

    SettingsHandler getSettingsHandler();

    UpgradeHandler getUpgradeHandler();

    Optional<UUID> getContentsUuid();

    int getMainColor();

    int getAccentColor();

    Optional<Integer> getOpenTabId();

    void setOpenTabId(int openTabId);

    void removeOpenTabId();

    void setColors(int mainColor, int accentColor);

    void setSortBy(SortBy sortBy);

    SortBy getSortBy();

    void sort();

    void onContentsNbtUpdated();

    void refreshInventoryForUpgradeProcessing();

    void refreshInventoryForInputOutput();

    void setPersistent(boolean persistent);

    void fillWithLoot(Player playerEntity);

    RenderInfo getRenderInfo();

    void setColumnsTaken(int columnsTaken, boolean hasChanged);

    int getColumnsTaken();

    default int getNumberOfSlotRows() {
        return 0;
    }

    default Optional<IStorageFluidHandler> getFluidHandler() {
        return Optional.empty();
    }

    default Optional<IEnergyStorage> getEnergyStorage() {return Optional.empty();}

    default ItemStack getWrappedStorageStack() {
        return ItemStack.EMPTY;
    }

    default int getBaseStackSizeMultiplier() {
        return 1;
    }

    default void onInit() {
        getInventoryHandler().onInit();
    }
}
