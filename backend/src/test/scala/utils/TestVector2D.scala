package utils

import org.scalatest.funsuite.AnyFunSuite

class TestVector2D extends AnyFunSuite:

  val tolerance = 1e-6

  extension (v: Vector2D)
    def almostEqual(other: Vector2D): Boolean =
      Math.abs(v.x - other.x) < tolerance && Math.abs(v.y - other.y) < tolerance

  test("addition of two vectors"):
    val v1 = Vector2D(1.0, 2.0)
    val v2 = Vector2D(3.0, 4.0)
    val result = v1 + v2
    assert(result === Vector2D(4.0, 6.0))

  test("subtraction of two vectors"):
    val v1 = Vector2D(5.0, 7.0)
    val v2 = Vector2D(2.0, 3.0)
    assert((v1 - v2) === Vector2D(3.0, 4.0))

  test("multiplication by scalar"):
    val v = Vector2D(2.0, 3.0)
    assert((v * 4.0) === Vector2D(8.0, 12.0))

  test("division by scalar"):
    val v = Vector2D(8.0, 12.0)
    assert((v / 4.0) === Vector2D(2.0, 3.0))

  test("dot product of two vectors"):
    val v1 = Vector2D(1.0, 2.0)
    val v2 = Vector2D(3.0, 4.0)
    assert(Math.abs((v1 dot v2) - 11.0) < tolerance)

  test("inversion of direction"):
    val v = Vector2D(5.0, -8.0)
    assert(-v === Vector2D(-5.0, 8.0))

  test("normalization of a vector"):
    val v = Vector2D(3.0, 4.0)
    val normalized = v.normalize
    assert(normalized === Vector2D(0.6, 0.8))

  test("equality of identical vectors"):
    val v1 = Vector2D(1.0, 2.0)
    val v2 = Vector2D(1.0, 2.0)
    assert(v1 === v2)

  test("Vector apply should return Vector.zero"):
    val v = Vector2D()

    assert(v == Vector2D.zero)
