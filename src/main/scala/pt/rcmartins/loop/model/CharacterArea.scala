package pt.rcmartins.loop.model

import pt.rcmartins.loop.Constants
import zio.json.{JsonDecoder, JsonEncoder}

import scala.util.chaining.scalaUtilChainingOps
import scala.util.{Left, Right}

sealed trait CharacterArea {

  val id: Int
  def name: String
  def iconPath: String

  def connection(dir8: Dir8): Option[CharacterArea] =
    CharacterArea.allConnections.get(this).flatMap(_(dir8.dir))

  lazy val allConnections: Seq[CharacterArea] =
    CharacterArea.allConnections.getOrElse(this, IndexedSeq.empty).flatten

}

object CharacterArea {

  case object Area1_Home extends CharacterArea {
    val id: Int = 1
    val name: String = "Home"
    val iconPath: String = Constants.Icons.House
  }

  case object Area2_Neighborhood extends CharacterArea {
    val id: Int = 2
    val name: String = "Neighborhood"
    val iconPath: String = Constants.Icons.Village
  }

  case object Area3_Market extends CharacterArea {
    val id: Int = 3
    val name: String = "Town"
    val iconPath: String = Constants.Icons.Village
  }

  case object Area4_GeneralStore extends CharacterArea {
    val id: Int = 4
    val name: String = "General Store"
    val iconPath: String = Constants.Icons.Shop
  }

  case object Area5_EquipmentStore extends CharacterArea {
    val id: Int = 5
    val name: String = "Equipment Store"
    val iconPath: String = Constants.Icons.Shop
  }

  case object Area6_MySoapShop extends CharacterArea {
    val id: Int = 6
    val name: String = "My Soap Shop"
    val iconPath: String = Constants.Icons.Shop
  }

  case object Area7_Forest extends CharacterArea {
    val id: Int = 7
    val name: String = "Forest"
    val iconPath: String = Constants.Icons.Forest
  }

  private val allAreas: Seq[CharacterArea] = Seq(
    Area1_Home,
    Area2_Neighborhood,
    Area3_Market,
    Area4_GeneralStore,
    Area5_EquipmentStore,
    Area6_MySoapShop,
    Area7_Forest,
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

        addConnection(Area1_Home, Area4_GeneralStore, Dir8.Top)
        addConnection(Area1_Home, Area7_Forest, Dir8.Left)
        addConnection(Area1_Home, Area2_Neighborhood, Dir8.Right)
        addConnection(Area1_Home, Area5_EquipmentStore, Dir8.BottomRight)
        addConnection(Area1_Home, Area6_MySoapShop, Dir8.Bottom)
        addConnection(Area2_Neighborhood, Area4_GeneralStore, Dir8.TopLeft)
        addConnection(Area2_Neighborhood, Area3_Market, Dir8.Right)
        addConnection(Area2_Neighborhood, Area5_EquipmentStore, Dir8.Bottom)
        addConnection(Area2_Neighborhood, Area6_MySoapShop, Dir8.BottomLeft)
        addConnection(Area3_Market, Area6_MySoapShop, Dir8.Top)
        addConnection(Area6_MySoapShop, Area7_Forest, Dir8.TopLeft)
        addConnection(Area6_MySoapShop, Area5_EquipmentStore, Dir8.Right)

        connections
      }

  private[model] val allMap: Map[Int, CharacterArea] =
    allAreas.map(area => area.id -> area).toMap

  implicit val decoder: JsonDecoder[CharacterArea] =
    JsonDecoder.int.mapOrFail { id =>
      allMap.get(id) match {
        case Some(characterArea) => Right(characterArea)
        case None                => Left(s"Unknown CharacterArea id: $id")
      }
    }

  implicit val encoder: JsonEncoder[CharacterArea] =
    JsonEncoder.int.contramap[CharacterArea](_.id)

}
