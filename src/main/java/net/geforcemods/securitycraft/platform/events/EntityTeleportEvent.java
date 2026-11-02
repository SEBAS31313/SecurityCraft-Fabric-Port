package net.geforcemods.securitycraft.platform.events;

import net.geforcemods.securitycraft.platform.GameplayEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Cancelable teleport hook; subtypes preserve the Rift Stabilizer's independent filters. */
public class EntityTeleportEvent extends GameplayEvent {
    private final Entity entity;
    private final Vec3 target;
    private final Vec3 previous;
    public EntityTeleportEvent(Entity entity,double x,double y,double z){this.entity=entity;this.target=new Vec3(x,y,z);this.previous=entity.position();}
    public Entity getEntity(){return entity;} public Vec3 getTarget(){return target;} public Vec3 getPrev(){return previous;}
    public double getPrevX(){return previous.x;} public double getPrevY(){return previous.y;} public double getPrevZ(){return previous.z;}
    public static class EnderEntity extends EntityTeleportEvent {public EnderEntity(Entity e,double x,double y,double z){super(e,x,y,z);}}
    public static class EnderPearl extends EntityTeleportEvent {public EnderPearl(Entity e,double x,double y,double z){super(e,x,y,z);}}
    public static class ItemConsumption extends EntityTeleportEvent {public ItemConsumption(Entity e,double x,double y,double z){super(e,x,y,z);}}
    public static class TeleportCommand extends EntityTeleportEvent {public TeleportCommand(Entity e,double x,double y,double z){super(e,x,y,z);}}
    public static class SpreadPlayersCommand extends EntityTeleportEvent {public SpreadPlayersCommand(Entity e,double x,double y,double z){super(e,x,y,z);}}
}
