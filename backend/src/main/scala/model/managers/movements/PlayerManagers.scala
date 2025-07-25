package model.managers.movements

import model.entities.Player
import model.entities.customers.BoredomFrustration
import model.entities.games.Game
import model.managers.BaseManager
import model.managers.WeightedManager
import utils.Result
import utils.Vector2D
import utils.Vector2D.direction
import utils.Vector2D.distance

object PlayerManagers:
  case class Context[P <: Player[P]](
      player: P,
      games: Seq[Game]
  ):
    protected[movements] def bestGameAvailable: Option[Game] =
      games
        .find(_.gameType == player.favouriteGame)
        .flatMap(_.lock(player.id).option())

  /** This manager will make the player go towards its favourite type of game.
    * If the game is not present, its direction is unaltered and its frustration
    * is increased by `frustration`
    * @param frustrationIncrease
    *   the increase in frustration when the favourite game is not found
    * @tparam C
    *   the customer concrete type
    */
  case class GamesAttractivenessManager[C <: Player[C] & BoredomFrustration[C]](
      frustrationIncrease: Double,
      weight: Double = 1.0
  ) extends WeightedManager[Context[C]]:
    override def update(slice: Context[C]): Context[C] =
      slice.copy(
        player =
          val customer = slice.player
          val bestGame = slice.bestGameAvailable
          bestGame match
            case Some(game) =>
              customer.addedDirection(
                direction(customer.position, game.position) * weight
              )
            case _ => customer.updateFrustration(frustrationIncrease)
      )

    override def updatedWeight(weight: Double): WeightedManager[Context[C]] =
      copy(weight = weight)

  /** This manager make the player sits to its favourite game is it is within
    * the `sittingRadius`
    * @param sittingRadius
    *   the radius within the player will sit to a game
    * @tparam C
    *   the concrete type the player
    */
  case class PlayerSitterManager[C <: Player[C]](
      sittingRadius: Double
  ) extends BaseManager[Context[C]]:

    override def update(slice: Context[C]): Context[C] =
      val bestGame = slice.bestGameAvailable
      bestGame match
        case Some(game)
            if distance(slice.player.position, game.position) < sittingRadius =>
          slice.copy(
            player = slice.player
              .play(game)
              .withPosition(game.center)
              .withDirection(Vector2D.zero),
            games = slice.games
              .map(g => if g.id == game.id then game else g)
          )
        case _ => slice
