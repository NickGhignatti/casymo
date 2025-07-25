package utils

import org.scalatest.funsuite.AnyFunSuite
import utils.Result.Failure
import utils.Result.Success

class TestResult extends AnyFunSuite:

  test("Success should be recognized as success"):
    val result = Success(42)
    assert(result.isSuccess)
    assert(!result.isFailure)

    result match
      case Success(value) => assert(value == 42)
      case Failure(_)     => fail("Expected Success, but got Failure")

  test("Failure should be recognized as failure"):
    val result = Failure("Error occurred")
    assert(!result.isSuccess)
    assert(result.isFailure)

    result match
      case Success(_)     => fail("Expected Failure, but got Success")
      case Failure(error) => assert(error == "Error occurred")

  test("map should transform Success value"):
    val successResult = Success(10)
    val mappedResult = successResult.map(_ * 2)
    assert(mappedResult == Success(20))

  test("map should return Failure unchanged"):
    val failureResult = Failure("An error")
    val mappedResult = failureResult.map(_ => 42)
    assert(mappedResult == Failure("An error"))

  test("flatMap should chain computations for Success"):
    val successResult = Success(5)
    val flatMappedResult = successResult.flatMap(x => Success(x + 5))
    assert(flatMappedResult == Success(10))

  test("flatMap should return Failure unchanged"):
    val failureResult = Failure("An error")
    val flatMappedResult = failureResult.flatMap(_ => Success(5))
    assert(flatMappedResult == Failure("An error"))

  test("getOrElse should return value for Success"):
    val successResult = Success(100)
    assert(successResult.getOrElse(0) == 100)

  test("getOrElse should return default for Failure"):
    val failureResult = Failure("Something went wrong")
    assert(failureResult.getOrElse(42) == 42)

  test("option should return None for Failure"):
    val failureResult = Failure("Something went wrong")
    assert(failureResult.option().isEmpty)

  test("option should return Some for Success"):
    val failureResult = Success("All right")
    assert(failureResult.option().isDefined)
