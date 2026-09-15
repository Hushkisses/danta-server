package kr.danta.paper.facility;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.Objects;
import java.util.logging.Logger;

/** Applies pending DEV-082 visuals only when Minecraft loads the relevant chunk normally. */
public final class FacilityAppearanceListener implements Listener {
    private final FacilityAppearanceService service;
    private final Logger logger;

    public FacilityAppearanceListener(FacilityAppearanceService service, Logger logger) {
        this.service = Objects.requireNonNull(service);
        this.logger = Objects.requireNonNull(logger);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        int synced = service.syncLoadedChunk(event.getWorld(), event.getChunk().getX(), event.getChunk().getZ());
        if (synced > 0) logger.info("[DEV-082] facility visuals synced on chunk load: " + synced);
    }
}
