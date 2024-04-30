package de.themeparkcraft.audioserver.minecraft.extensions

import de.themeparkcraft.audioserver.common.interfaces.PointedWorldPosition
import org.bukkit.entity.Player

/**
 * Represents the world position of a player, including the x, y, z coordinates, the world name, yaw rotation, and pitch rotation.
 *
 * The [PointedWorldPosition] class extends the [WorldPosition] class and adds yaw and pitch rotations, which define the direction the player is facing.
 *
 * @property x The x-coordinate of the player's position in the world.
 * @property y The y-coordinate of the player's position in the world.
 * @property z The z-coordinate of the player's position in the world.
 * @property world The name of the world the player is in.
 * @property yaw The yaw rotation of the player's position.
 * @property pitch The pitch rotation of the player's position.
 *
 * @see WorldPosition
 * @see PointedWorldPosition
 */
val Player.worldPosition: PointedWorldPosition
    get() = PointedWorldPosition(location.x, location.y, location.z, location.world.name, location.yaw, location.pitch)