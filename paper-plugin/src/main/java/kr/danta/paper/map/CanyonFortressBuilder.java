package kr.danta.paper.map;

import org.bukkit.Material;
import org.bukkit.World;

/**
 * DEV-112A generated physical test fortress for canyon_fort.
 * Deliberately deterministic so it can later be replaced/exported as an NBT asset.
 */
public final class CanyonFortressBuilder {
    public static final int CENTER_X = 790, BASE_Y = 78, CENTER_Z = -120;

    public BuildResult build(World world) {
        // Clear only the authored vertical volume; preserve terrain below the battlefield.
        fill(world, 730, BASE_Y, -180, 850, BASE_Y + 28, -60, Material.AIR);

        // Courtyard floor and broad outer platform.
        fill(world, 738, BASE_Y - 1, -172, 842, BASE_Y - 1, -68, Material.STONE_BRICKS);

        // Outer 96x96 curtain wall, 9 high / 3 thick.
        wallRect(world, 742, 838, -168, -72, BASE_Y, BASE_Y + 8, 3);
        // West-facing outer gate opening centered on DEV-112 outer gate anchor.
        fill(world, 740, BASE_Y, -124, 746, BASE_Y + 6, -116, Material.AIR);
        arch(world, 742, BASE_Y + 7, -124, -116);

        // Four outer towers.
        tower(world, 742, -168, 6, 14);
        tower(world, 742, -72, 6, 14);
        tower(world, 838, -168, 6, 14);
        tower(world, 838, -72, 6, 14);

        // Inner keep wall, west gate aligned with inner objective.
        wallRect(world, 778, 822, -146, -94, BASE_Y, BASE_Y + 9, 2);
        fill(world, 776, BASE_Y, -123, 781, BASE_Y + 6, -117, Material.AIR);
        arch(world, 778, BASE_Y + 7, -123, -117);

        // Central keep/core building.
        fill(world, 784, BASE_Y, -132, 806, BASE_Y + 11, -108, Material.STONE_BRICKS);
        fill(world, 786, BASE_Y + 1, -130, 804, BASE_Y + 9, -110, Material.AIR);
        // Entrance and battlement roof.
        fill(world, 783, BASE_Y, -122, 787, BASE_Y + 5, -118, Material.AIR);
        crenellateRect(world, 784, 806, -132, -108, BASE_Y + 12);

        // Core dais exactly at DEV-112 core anchor.
        fill(world, 787, BASE_Y, -123, 793, BASE_Y, -117, Material.POLISHED_ANDESITE);
        world.getBlockAt(790, 80, -120).setType(Material.BEACON, false);

        // Main assault road and courtyard guide.
        fill(world, 730, BASE_Y - 1, -123, 790, BASE_Y - 1, -117, Material.SMOOTH_STONE);
        // Lighting for test usability.
        for (int x = 750; x <= 830; x += 16) {
            world.getBlockAt(x, BASE_Y, -160).setType(Material.TORCH, false);
            world.getBlockAt(x, BASE_Y, -80).setType(Material.TORCH, false);
        }
        return new BuildResult(CENTER_X, BASE_Y, CENTER_Z);
    }

    private void wallRect(World w,int minX,int maxX,int minZ,int maxZ,int y0,int y1,int thick){
        fill(w,minX,y0,minZ,maxX,y1,minZ+thick-1,Material.STONE_BRICKS);
        fill(w,minX,y0,maxZ-thick+1,maxX,y1,maxZ,Material.STONE_BRICKS);
        fill(w,minX,y0,minZ,minX+thick-1,y1,maxZ,Material.STONE_BRICKS);
        fill(w,maxX-thick+1,y0,minZ,maxX,y1,maxZ,Material.STONE_BRICKS);
        crenellateRect(w,minX,maxX,minZ,maxZ,y1+1);
    }
    private void crenellateRect(World w,int minX,int maxX,int minZ,int maxZ,int y){
        for(int x=minX;x<=maxX;x+=2){ set(w,x,y,minZ,Material.STONE_BRICK_WALL); set(w,x,y,maxZ,Material.STONE_BRICK_WALL); }
        for(int z=minZ;z<=maxZ;z+=2){ set(w,minX,y,z,Material.STONE_BRICK_WALL); set(w,maxX,y,z,Material.STONE_BRICK_WALL); }
    }
    private void tower(World w,int x,int z,int r,int height){
        fill(w,x-r,BASE_Y,z-r,x+r,BASE_Y+height,z+r,Material.STONE_BRICKS);
        fill(w,x-r+2,BASE_Y+1,z-r+2,x+r-2,BASE_Y+height-1,z+r-2,Material.AIR);
        crenellateRect(w,x-r,x+r,z-r,z+r,BASE_Y+height+1);
    }
    private void arch(World w,int x,int y,int minZ,int maxZ){
        fill(w,x-1,y,minZ,x+3,y+1,maxZ,Material.STONE_BRICKS);
    }
    private void fill(World w,int x0,int y0,int z0,int x1,int y1,int z1,Material m){
        int minCx=Math.floorDiv(Math.min(x0,x1),16), maxCx=Math.floorDiv(Math.max(x0,x1),16);
        int minCz=Math.floorDiv(Math.min(z0,z1),16), maxCz=Math.floorDiv(Math.max(z0,z1),16);
        for(int cx=minCx;cx<=maxCx;cx++) for(int cz=minCz;cz<=maxCz;cz++) w.getChunkAt(cx,cz).load();
        for(int x=Math.min(x0,x1);x<=Math.max(x0,x1);x++)
            for(int y=Math.min(y0,y1);y<=Math.max(y0,y1);y++)
                for(int z=Math.min(z0,z1);z<=Math.max(z0,z1);z++) w.getBlockAt(x,y,z).setType(m,false);
    }
    private void set(World w,int x,int y,int z,Material m){ w.getBlockAt(x,y,z).setType(m,false); }
    public record BuildResult(int centerX,int baseY,int centerZ){}
}
