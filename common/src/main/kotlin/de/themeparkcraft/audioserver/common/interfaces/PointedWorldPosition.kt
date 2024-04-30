package de.themeparkcraft.audioserver.common.interfaces

/**
 * Represents a pointed position in the world, specified by its x, y, z coordinates, the world it belongs to,
 * and the yaw and pitch rotations.
 *
 * @see WorldPosition
 */
class PointedWorldPosition(
    x: Double, y: Double, z: Double, world: String,
    /**
     * The yaw rotation of a pointed world position.
     *
     * Yaw is the rotation around the vertical axis in degrees.
     * A yaw of 0 degrees indicates that the pointed position is facing north,
     * while a positive yaw rotates the position clockwise and a negative yaw rotates it counterclockwise.
     *
     * The value is represented as a floating-point number.
     *
     * @see PointedWorldPosition
     * @see WorldPosition
     */
    val yaw: Float,
    /**
     * The pitch of a pointed world position.
     *
     * The pitch represents the vertical rotation in degrees of a pointed world position.
     * It indicates the up or down angle of the pointed object with respect to the horizon.
     *
     * @see PointedWorldPosition
     * @see WorldPosition
     */
    val pitch: Float
) : WorldPosition(x, y, z, world)