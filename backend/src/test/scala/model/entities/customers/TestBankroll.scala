package model.entities.customers

import org.scalatest.funsuite.AnyFunSuite

class TestBankroll extends AnyFunSuite:

  test("creating a class with a positive Bankroll should store the amount"):
    val mock = Customer().withBankroll(100.0)
    assert(mock.bankroll === 100.0)

  test(
    "creating a class with a negative Bankroll should throw IllegalArgumentException"
  ):
    val negativeBr = -50.0
    val ex = intercept[IllegalArgumentException] {
      Customer().withBankroll(negativeBr)
    }

    assert(
      ex.getMessage === s"requirement failed: Bankroll amount must be positive, instead is $negativeBr"
    )

  test(
    "Modifying the Bankroll to a new positive amount should update the amount"
  ):
    val mock = Customer().withBankroll(100.0)
    val updatedMock = mock.updateBankroll(-20.0)
    assert(updatedMock.bankroll === 100.0 - 20.0)

  test(
    "Modifying the Bankroll to a negative amount should throw IllegalArgumentException"
  ):
    val startValue = 100.0
    val mock = Customer().withBankroll(startValue)
    val netLoss = -110.0
    val ex = intercept[IllegalArgumentException] {
      val updatedMock = mock.updateBankroll(netLoss)
    }
    assert(
      ex.getMessage === s"requirement failed: Bankroll amount must be positive, instead is ${startValue + netLoss}"
    )

  test("bankroll ratio should work as expected"):
    val c = Customer().withBankroll(1000.0).updateBankroll(500.0)
    assert(c.bankrollRatio == 1.5)
