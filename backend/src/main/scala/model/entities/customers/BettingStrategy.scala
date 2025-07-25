package model.entities.customers

import model.entities.customers.CustState.Idle
import model.entities.customers.CustState.Playing
import model.entities.games._

trait HasBetStrategy[T <: HasBetStrategy[T] & Bankroll[T] & CustomerState[T]]:
  this: T =>
  val betStrategy: BettingStrategy[T]

  def placeBet(): Bet = betStrategy.placeBet(this)

  def updateAfter(result: Double): T =
    withBetStrategy(betStrategy.updateAfter(this, result))

  def changeBetStrategy(newStrat: BettingStrategy[T]): T =
    withBetStrategy(newStrat)

  def withBetStrategy(newStrat: BettingStrategy[T]): T

trait BetStratType
object FlatBet extends BetStratType
object Martingale extends BetStratType
object OscarGrind extends BetStratType

trait BettingStrategy[A <: Bankroll[A] & CustomerState[A]]:
  val betAmount: Double
  val option: List[Int]
  require(
    betAmount >= 0,
    s"Bet amount must be positive, instead is $betAmount"
  )
  def betType: BetStratType
  def placeBet(ctx: A): Bet
  def updateAfter(ctx: A, result: Double): BettingStrategy[A]
  protected def checkRequirement(ctx: A): Unit =
    require(
      betAmount <= ctx.bankroll,
      s"Bet amount must be equal or less of the total bankroll, instead is $betAmount when the bankroll is ${ctx.bankroll}"
    )
    require(
      ctx.customerState != Idle,
      "Bet should be placed only if the customer is playing a game"
    )

def defaultRedBet =
  List(16, 1, 3, 5, 7, 9, 12, 14, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36)

case class FlatBetting[A <: Bankroll[A] & CustomerState[A]](
    betAmount: Double,
    option: List[Int]
) extends BettingStrategy[A]:
  override def betType: BetStratType = FlatBet
  def placeBet(ctx: A): Bet =
    checkRequirement(ctx)
    (ctx.customerState: @unchecked) match
      case Playing(game) =>
        game.gameType match
          case SlotMachine => SlotBet(betAmount)
          case Roulette    => RouletteBet(betAmount, option)
          case Blackjack   => BlackJackBet(betAmount, option.head)
          case _           => ???
      // case Idle => throw new MatchError("Wrong customer state")

  def updateAfter(ctx: A, result: Double): FlatBetting[A] = this

object FlatBetting:

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      option: Int
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double,
      options: List[Int]
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, options)

  def apply[A <: Bankroll[A] & CustomerState[A]](
      betAmount: Double
  ): FlatBetting[A] =
    new FlatBetting[A](betAmount, List.empty)

case class MartingaleStrat[A <: Bankroll[A] & CustomerState[A]](
    baseBet: Double,
    betAmount: Double,
    lossStreak: Int = 0,
    option: List[Int]
) extends BettingStrategy[A]:
  override def betType: BetStratType = Martingale
  def placeBet(ctx: A): Bet =
    checkRequirement(ctx)
    (ctx.customerState: @unchecked) match
      case Playing(game) =>
        game.gameType match
          case Roulette  => RouletteBet(betAmount, option)
          case Blackjack => BlackJackBet(betAmount, option.head)
          case _         => ???
      // case Idle => throw new MatchError("Wrong customer state")

  def updateAfter(ctx: A, result: Double): MartingaleStrat[A] =
    if result < 0 then
      this.copy(betAmount = nextBet(), lossStreak = lossStreak + 1)
    else if result == 0 then this
    else copy(betAmount = baseBet, lossStreak = 0)

  def nextBet(): Double =
    baseBet * math.pow(2, lossStreak + 1)

object MartingaleStrat:

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      option: Int
  ): MartingaleStrat[A] =
    MartingaleStrat(baseBet, baseBet, 0, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      options: List[Int]
  ): MartingaleStrat[A] =
    MartingaleStrat(baseBet, baseBet, 0, options)

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      betAmount: Double,
      option: Int,
      lossStreak: Int
  ): MartingaleStrat[A] =
    new MartingaleStrat[A](baseBet, betAmount, lossStreak, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      betAmount: Double,
      options: List[Int],
      lossStreak: Int
  ): MartingaleStrat[A] =
    new MartingaleStrat[A](baseBet, betAmount, lossStreak, options)

case class OscarGrindStrat[A <: Bankroll[A] & CustomerState[A]](
    baseBet: Double,
    betAmount: Double,
    startingBankroll: Double,
    lossStreak: Int = 0,
    option: List[Int]
) extends BettingStrategy[A]:
  override def betType: BetStratType = OscarGrind
  def placeBet(ctx: A): Bet =
    checkRequirement(ctx)
    (ctx.customerState: @unchecked) match
      case Playing(game) =>
        game.gameType match
          case Roulette  => RouletteBet(betAmount, option)
          case Blackjack => BlackJackBet(betAmount, option.head)
          case _         => ???

  def updateAfter(ctx: A, result: Double): OscarGrindStrat[A] =
    if ctx.bankroll > startingBankroll then
      this.copy(betAmount = baseBet, startingBankroll = ctx.bankroll)
    else if result > 0 then
      this.copy(betAmount = betAmount + baseBet, lossStreak = 0)
    else if result == 0 then this
    else this.copy(lossStreak = lossStreak + 1)

object OscarGrindStrat:

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      bankroll: Double,
      option: Int
  ): OscarGrindStrat[A] =
    OscarGrindStrat(baseBet, baseBet, bankroll, 0, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      bankroll: Double,
      options: List[Int]
  ): OscarGrindStrat[A] =
    OscarGrindStrat(baseBet, baseBet, bankroll, 0, options)

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      bankroll: Double,
      option: Int,
      lossStreak: Int
  ): OscarGrindStrat[A] =
    OscarGrindStrat(baseBet, baseBet, bankroll, lossStreak, List(option))

  def apply[A <: Bankroll[A] & CustomerState[A]](
      baseBet: Double,
      bankroll: Double,
      options: List[Int],
      lossStreak: Int
  ): OscarGrindStrat[A] =
    OscarGrindStrat(baseBet, baseBet, bankroll, lossStreak, options)

//case class ReactiveRandomStrategy(base: Double, min: Double, max: Double) extends BettingStrategy:
//
//  def placeBet(ctx: Customer) = BetDecision(base, "random")
//
//  def updateAfter(result: GameResult) =
//    val newBase = if result.netGain < 0 then (base - 1).max(min) else (base + 1).min(max)
//    copy(base = newBase)
