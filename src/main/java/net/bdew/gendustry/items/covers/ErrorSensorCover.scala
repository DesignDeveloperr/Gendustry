/*
 * Copyright (c) bdew, 2013 - 2015
 * https://github.com/bdew/gendustry
 *
 * This mod is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://bdew.net/minecraft-mod-public-license/
 */

package net.bdew.gendustry.items.covers

import cpw.mods.fml.relauncher.{Side, SideOnly}
import forestry.api.core.{ForestryAPI, IErrorLogicSource, IErrorState}
import net.bdew.gendustry.Gendustry
import net.bdew.lib.Misc
import net.bdew.lib.covers.{ItemCover, TileCoverable}
import net.bdew.lib.helpers.ChatHelper._
import net.bdew.lib.items.SimpleItem
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraftforge.common.util.ForgeDirection

trait ErrorSensor {
  def id: String
  def getUnLocalizedName: String
  def isActive(te: IErrorLogicSource): Boolean
}

object ErrorSensorNoError extends ErrorSensor {
  override def id: String = "noError"
  override def isActive(te: IErrorLogicSource): Boolean = !te.getErrorLogic.hasErrors
  override def getUnLocalizedName: String = "gendustry.cover.error.none"
}

object ErrorSensorAnyError extends ErrorSensor {
  override def id: String = "anyError"
  override def isActive(te: IErrorLogicSource): Boolean = te.getErrorLogic.hasErrors
  override def getUnLocalizedName: String = "gendustry.cover.error.any"
}

case class ErrorSensorForestry(state: IErrorState) extends ErrorSensor {
  override def id: String = state.getUniqueName
  override def isActive(te: IErrorLogicSource): Boolean = te.getErrorLogic.contains(state)
  override def getUnLocalizedName: String = "for." + state.getDescription
}

object ErrorSensors {
  val errorStateRegistry = ForestryAPI.errorStateRegistry

  val sensors = List(ErrorSensorAnyError, ErrorSensorNoError) ++ (List(
    "Forestry:invalidBiome",
    "Forestry:isRaining",
    "Forestry:notGloomy",
    "Forestry:notLucid",
    "Forestry:notDay",
    "Forestry:notNight",
    "Forestry:noFlower",
    "Forestry:noQueen",
    "Forestry:noDrone",
    "Forestry:noSky",
    "Forestry:noSpace",
    "Forestry:noPower"
  ) map (name => ErrorSensorForestry(errorStateRegistry.getErrorState(name))))

  val idMap = sensors.map(x => x.id -> x).toMap
}

object ErrorSensorCover extends SimpleItem("ErrorSensorCover") with ItemCover {
  override def isCoverTicking: Boolean = false

  override def getCoverIcon(te: TileCoverable, side: ForgeDirection, cover: ItemStack): IIcon =
    if (isEmittingSignal(te, side, cover)) itemIconOn else itemIcon

  override def getSpriteNumber = 0

  override def isValidTile(te: TileCoverable, stack: ItemStack) = te.isInstanceOf[IErrorLogicSource]

  def getErrorSensor(stack: ItemStack): Option[ErrorSensor] = {
    if (stack.hasTagCompound) {
      ErrorSensors.idMap.get(stack.getTagCompound.getString("sensor"))
    } else None
  }

  def setErrorSensor(stack: ItemStack, sensor: ErrorSensor): ItemStack = {
    val copy = stack.copy()
    if (!copy.hasTagCompound) copy.setTagCompound(new NBTTagCompound)
    copy.getTagCompound.setString("sensor", sensor.id)
    copy
  }

  def clearErrorSensor(stack: ItemStack): ItemStack = {
    val copy = stack.copy()
    if (copy.hasTagCompound) copy.setTagCompound(null)
    copy
  }

  override def isEmittingSignal(te: TileCoverable, side: ForgeDirection, cover: ItemStack): Boolean = {
    for {
      tile <- Misc.asInstanceOpt(te, classOf[IErrorLogicSource])
      sensor <- getErrorSensor(cover)
    } {
      if (sensor.isActive(tile)) return true
    }
    false
  }

  override def onInstall(te: TileCoverable, side: ForgeDirection, cover: ItemStack, player: EntityPlayerMP): ItemStack = {
    val sensor = ErrorSensors.sensors.head
    player.addChatComponentMessage(L("gendustry.cover.error.message", L(sensor.getUnLocalizedName).setColor(Color.YELLOW)))
    player.addChatComponentMessage(L("gendustry.cover.error.hint"))
    setErrorSensor(cover, sensor)
  }

  override def onRemove(cover: ItemStack): ItemStack =
    clearErrorSensor(cover)

  override def clickCover(te: TileCoverable, side: ForgeDirection, cover: ItemStack, player: EntityPlayer): Boolean = {
    if (player.inventory.getCurrentItem != null) return false
    if (!player.worldObj.isRemote) {
      val current = getErrorSensor(cover) getOrElse ErrorSensors.sensors.head
      val next = Misc.nextInSeq(ErrorSensors.sensors, current)
      te.covers(side) := setErrorSensor(cover, next)
      te.onCoversChanged()
      player.addChatComponentMessage(L("gendustry.cover.error.message", L(next.getUnLocalizedName).setColor(Color.YELLOW)))
      te.getWorldObject.notifyBlockOfNeighborChange(te.xCoord + side.offsetX, te.yCoord + side.offsetY, te.zCoord + side.offsetZ, te.getBlockType)
    }
    true
  }

  var itemIconOn: IIcon = null

  @SideOnly(Side.CLIENT)
  override def registerIcons(reg: IIconRegister) {
    itemIconOn = reg.registerIcon(Misc.iconName(Gendustry.modId, "covers", "error_on"))
    itemIcon = reg.registerIcon(Misc.iconName(Gendustry.modId, "covers", "error_off"))
  }
}
