package net.blockventuremc.audioserver.common.interfaces

import kotlinx.serialization.Serializable

/**
 * Represents a position in the world, specified by its x, y, z coordinates and the world it belongs to.
 *
 * @property x The x coordinate of the position.
 * @property y The y coordinate of the position.
 * @property z The z coordinate of the position.
 * @property world The world the position belongs to.
 */
@Serializable
open class WorldPosition(
    /**
     * Represents a double value for the x coordinate of a position in the world.
     */
    val x: Double,
    /**
     * Represents the y coordinate of a position in a world.
     *
     * The value of [y] represents the vertical position of a [WorldPosition] in a world.
     *
     * @see WorldPosition
     * @see WorldPosition.y
     */
    val y: Double,
    /**
     * Represents the z coordinate of a position in the world.
     */
    val z: Double,
    /**
     * Represents the name of the world.
     */
    val world: String
): RabbitSendable {

    override fun toString(): String {
        return "WorldPosition(x=$x, y=$y, z=$z, world='$world')"
    }

}
