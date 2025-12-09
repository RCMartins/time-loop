package pt.rcmartins.loop.model

import pt.rcmartins.loop.Constants
import pt.rcmartins.loop.model.Dir8._

import scala.util.chaining.scalaUtilChainingOps

sealed trait CharacterArea {

  def name: String
  def iconPath: String

  def connection(dir8: Dir8): Option[CharacterArea] =
    CharacterArea.allConnections.get(this).flatMap(_(dir8.dir))

  lazy val allConnections: Seq[CharacterArea] =
    CharacterArea.allConnections.getOrElse(this, IndexedSeq.empty).flatten

}

object CharacterArea {

  case object Area1_Home extends CharacterArea {
    val name: String = "Home"
    val iconPath: String = Constants.Icons.House
  }

  case object Area2_Town extends CharacterArea {
    val name: String = "Town"
    val iconPath: String = Constants.Icons.Village
  }

  case object Area3_GeneralStore extends CharacterArea {
    val name: String = "General Store"
    val iconPath: String = Constants.Icons.Shop
  }

  case object Area4_EquipmentStore extends CharacterArea {
    val name: String = "Equipment Store"
    val iconPath: String = Constants.Icons.Shop
  }

  case object Area5_Forest extends CharacterArea {
    val name: String = "Forest"
    val iconPath: String = Constants.Icons.Forest
  }

  case object Area6_MySoapShop extends CharacterArea {
    val name: String = "My Soap Shop"
    val iconPath: String = Constants.Icons.Shop
  }

  private val allAreas: Seq[CharacterArea] = Seq(
    Area1_Home,
    Area2_Town,
    Area3_GeneralStore,
    Area4_EquipmentStore,
    Area5_Forest,
    Area6_MySoapShop
  )

  private val allConnections: Map[CharacterArea, IndexedSeq[Option[CharacterArea]]] =
    allAreas
      .map(area => area -> Vector.fill(8)(Option.empty[CharacterArea]))
      .toMap
      .pipe { emptyConnections =>
        var connections: Map[CharacterArea, Vector[Option[CharacterArea]]] =
          emptyConnections

        def addConnection(
            from: CharacterArea,
            to: CharacterArea,
            directionTo: Dir8,
        ): Unit = {
          connections = connections.updatedWith(from)(
            _.map(_.updated(directionTo.dir, Some(to)))
          )
          connections = connections.updatedWith(to)(
            _.map(_.updated(directionTo.oppositeDir, Some(from)))
          )
        }

        addConnection(Area1_Home, Area2_Town, Right)
        addConnection(Area1_Home, Area5_Forest, Left)
        addConnection(Area1_Home, Area3_GeneralStore, Top)
        addConnection(Area2_Town, Area3_GeneralStore, TopLeft)
        addConnection(Area2_Town, Area4_EquipmentStore, Bottom)
        addConnection(Area2_Town, Area6_MySoapShop, Right)

        connections
      }

}
