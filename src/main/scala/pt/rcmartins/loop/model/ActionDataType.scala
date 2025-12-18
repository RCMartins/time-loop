package pt.rcmartins.loop.model

import zio.json.{JsonDecoder, JsonEncoder}

import scala.util.chaining.scalaUtilChainingOps

sealed trait ActionDataType {
  val id: ActionId
}

object ActionDataType {

  sealed trait Arc1DataType extends ActionDataType

  object Arc1DataType {

    case object WakeUp extends Arc1DataType { val id: ActionId = ActionId(1L) }

    case object SearchLivingRoom extends Arc1DataType { val id: ActionId = ActionId(2L) }

    case object PickupSimpleSoapMold extends Arc1DataType { val id: ActionId = ActionId(3L) }

    case object ExploreForestForLavender extends Arc1DataType { val id: ActionId = ActionId(4L) }

    case object FindMysteriousSorcerer extends Arc1DataType { val id: ActionId = ActionId(5L) }

    case object TalkMysteriousSorcerer extends Arc1DataType { val id: ActionId = ActionId(6L) }

    case object FirstLoopFadingAway extends Arc1DataType { val id: ActionId = ActionId(7L) }

    case object PickupCoins extends Arc1DataType { val id: ActionId = ActionId(8L) }

    case object SearchKitchen extends Arc1DataType { val id: ActionId = ActionId(9L) }

    case object CookRice extends Arc1DataType { val id: ActionId = ActionId(10L) }

    case object SearchGarden extends Arc1DataType { val id: ActionId = ActionId(11L) }

    case object PickRosemaryGarden extends Arc1DataType { val id: ActionId = ActionId(12L) }

    case object GoToGeneralStore extends Arc1DataType { val id: ActionId = ActionId(13L) }

    case object GoToBackHome extends Arc1DataType { val id: ActionId = ActionId(14L) }

    case object BuyGlycerin extends Arc1DataType { val id: ActionId = ActionId(15L) }

    case object BuyFrozenMomo extends Arc1DataType { val id: ActionId = ActionId(16L) }

    case object BuyGoodSoapMold extends Arc1DataType { val id: ActionId = ActionId(17L) }

    case object CookMomo extends Arc1DataType { val id: ActionId = ActionId(18L) }

    case object MeltGlycerin extends Arc1DataType { val id: ActionId = ActionId(19L) }

    case object MoldSoap extends Arc1DataType { val id: ActionId = ActionId(20L) }

    case object CreateSoap extends Arc1DataType { val id: ActionId = ActionId(21L) }

    case object GoToTown extends Arc1DataType { val id: ActionId = ActionId(22L) }

    case object TalkWithPeopleInTown extends Arc1DataType { val id: ActionId = ActionId(23L) }

    case object SellSoapToPeople extends Arc1DataType { val id: ActionId = ActionId(24L) }

    case object ExploreTown extends Arc1DataType { val id: ActionId = ActionId(25L) }

    case object GoToEquipamentStore extends Arc1DataType { val id: ActionId = ActionId(26L) }

    case object BuyBigBag extends Arc1DataType { val id: ActionId = ActionId(27L) }

    case object BuyHugeBag extends Arc1DataType { val id: ActionId = ActionId(28L) }

    case object GoToForest extends Arc1DataType { val id: ActionId = ActionId(29L) }

    case object PickupBerries extends Arc1DataType { val id: ActionId = ActionId(30L) }

    case object PickupPrettyFlower extends Arc1DataType { val id: ActionId = ActionId(31L) }

    case object SellFlowerInStore extends Arc1DataType { val id: ActionId = ActionId(32L) }

    case object BuyEmptyShop extends Arc1DataType { val id: ActionId = ActionId(33L) }

    case object PrepareShopForBusiness extends Arc1DataType { val id: ActionId = ActionId(34L) }

  }

  object Arc2DataType {

    case object GoToMySoapShop extends Arc1DataType { val id: ActionId = ActionId(35L) }

  }

  private[model] val all: Map[ActionId, ActionDataType] =
    Seq(
      Arc1DataType.WakeUp,
      Arc1DataType.SearchLivingRoom,
      Arc1DataType.PickupSimpleSoapMold,
      Arc1DataType.ExploreForestForLavender,
      Arc1DataType.FindMysteriousSorcerer,
      Arc1DataType.TalkMysteriousSorcerer,
      Arc1DataType.FirstLoopFadingAway,
      Arc1DataType.PickupCoins,
      Arc1DataType.SearchKitchen,
      Arc1DataType.CookRice,
      Arc1DataType.SearchGarden,
      Arc1DataType.PickRosemaryGarden,
      Arc1DataType.GoToGeneralStore,
      Arc1DataType.GoToBackHome,
      Arc1DataType.BuyGlycerin,
      Arc1DataType.BuyFrozenMomo,
      Arc1DataType.BuyGoodSoapMold,
      Arc1DataType.CookMomo,
      Arc1DataType.MeltGlycerin,
      Arc1DataType.MoldSoap,
      Arc1DataType.CreateSoap,
      Arc1DataType.GoToTown,
      Arc1DataType.TalkWithPeopleInTown,
      Arc1DataType.SellSoapToPeople,
      Arc1DataType.ExploreTown,
      Arc1DataType.GoToEquipamentStore,
      Arc1DataType.BuyBigBag,
      Arc1DataType.BuyHugeBag,
      Arc1DataType.GoToForest,
      Arc1DataType.PickupBerries,
      Arc1DataType.PickupPrettyFlower,
      Arc1DataType.SellFlowerInStore,
      Arc1DataType.BuyEmptyShop,
      Arc1DataType.PrepareShopForBusiness,
      Arc2DataType.GoToMySoapShop,
    ).pipe { seq =>
      val result = seq.map(a => a.id -> a).toMap
      if (result.size != seq.size)
        throw new IllegalStateException("Duplicate ActionDataType ids found")
      result
    }

  implicit val decoder: JsonDecoder[ActionDataType] =
    JsonDecoder.long.mapOrFail { idLong =>
      val actionId = ActionId(idLong)
      all.get(actionId) match {
        case Some(actionDataType) => Right(actionDataType)
        case None                 => Left(s"Unknown ActionDataType id: $idLong")
      }
    }

  implicit val encoder: JsonEncoder[ActionDataType] =
    JsonEncoder.long.contramap[ActionDataType](_.id.id)

}
