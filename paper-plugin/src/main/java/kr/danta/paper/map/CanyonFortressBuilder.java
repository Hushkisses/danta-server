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
        // Courtyard zoning: gardens and compact supply/market stalls break up the empty stone plane.
        garden(world, 752, -150, 766, -136);
        garden(world, 752, -104, 766, -90);
        garden(world, 814, -150, 828, -136);
        garden(world, 814, -104, 828, -90);
        stall(world, 760, -132); stall(world, 760, -108);
        stall(world, 820, -132); stall(world, 820, -108);

        // Secondary raised command tower over the core to create a stronger skyline.
        fill(world, 787, BASE_Y + 12, -129, 803, BASE_Y + 20, -111, Material.STONE_BRICKS);
        fill(world, 789, BASE_Y + 13, -127, 801, BASE_Y + 19, -113, Material.AIR);
        crenellateRect(world, 787, 803, -129, -111, BASE_Y + 21);
        set(world, 790, BASE_Y + 20, -120, Material.BLUE_BANNER);
        set(world, 800, BASE_Y + 20, -120, Material.BLUE_BANNER);

        // Blue banners make the fortress readable as a military objective.
        for (int z : new int[]{-152, -120, -88}) {
            set(world, 743, BASE_Y + 5, z, Material.BLUE_BANNER);
            set(world, 837, BASE_Y + 5, z, Material.BLUE_BANNER);
        }

        // Invisible LIGHT blocks provide gameplay lighting without torch clutter.
        // Ground lights are suspended two blocks above walking surfaces; wall-walk/keep lights sit above their floors.
        lightGrid(world, 750, 830, -160, -80, BASE_Y + 2, 12);
        lightGrid(world, 746, 834, -164, -76, BASE_Y + 11, 12);
        lightGrid(world, 782, 818, -142, -98, BASE_Y + 12, 10);
        lightGrid(world, 789, 801, -127, -113, BASE_Y + 16, 8);
        setLight(world, 790, BASE_Y + 4, -120);
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
    private void garden(World w,int x0,int z0,int x1,int z1){
        fill(w,x0,BASE_Y-1,z0,x1,BASE_Y-1,z1,Material.MOSS_BLOCK);
        for(int x=x0+2;x<x1;x+=4) for(int z=z0+2;z<z1;z+=4) set(w,x,BASE_Y,z,Material.OAK_LEAVES);
        for(int x=x0;x<=x1;x++){ set(w,x,BASE_Y-1,z0,Material.STONE_BRICKS); set(w,x,BASE_Y-1,z1,Material.STONE_BRICKS); }
        for(int z=z0;z<=z1;z++){ set(w,x0,BASE_Y-1,z,Material.STONE_BRICKS); set(w,x1,BASE_Y-1,z,Material.STONE_BRICKS); }
    }
    private void stall(World w,int x,int z){
        fill(w,x-3,BASE_Y,z-2,x+3,BASE_Y,z+2,Material.OAK_PLANKS);
        for(int dx : new int[]{-3,3}) for(int dz : new int[]{-2,2}) fill(w,x+dx,BASE_Y+1,z+dz,x+dx,BASE_Y+3,z+dz,Material.OAK_FENCE);
        fill(w,x-3,BASE_Y+4,z-2,x+3,BASE_Y+4,z+2,Material.BLUE_WOOL);
        fill(w,x-2,BASE_Y+4,z-2,x+2,BASE_Y+4,z+2,Material.WHITE_WOOL);
    }
    private void lightGrid(World w,int minX,int maxX,int minZ,int maxZ,int y,int step){
        for(int x=minX;x<=maxX;x+=step) for(int z=minZ;z<=maxZ;z+=step) setLight(w,x,y,z);
    }
    private void setLight(World w,int x,int y,int z){
        var block=w.getBlockAt(x,y,z);
        if(block.getType().isAir()) block.setType(Material.LIGHT,false);
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
