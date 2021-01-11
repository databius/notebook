package com.databius.notebook.scala

import io.pkts.buffer.Buffer
import io.pkts.packet.Packet
import io.pkts.protocol.Protocol
import io.pkts.{PacketHandler, Pcap}
import org.json4s.DefaultFormats
import org.json4s.jackson.Serialization.write
import quickfix.{DataDictionary, FieldMap, Group, Message}

import scala.collection.JavaConverters._
import scala.collection.mutable

object FixToJsonExample extends App {
  val dd       = new DataDictionary("FIX50SP2.xml")
  val pcap     = Pcap.openStream("./data/bursa-fish.tcp")
  val messages = mutable.ListBuffer[String]()
  pcap.loop(new PacketHandler() {
    override def nextPacket(packet: Packet): Boolean = {
      if (packet.hasProtocol(Protocol.TCP)) {
        val msgBytes: Buffer = packet.getPacket(Protocol.TCP).getPayload
        Option(msgBytes).map(msg => {
          val fixMessage = new FIXMessage()
          fixMessage.fromString(msg.toString, dd, false)
          messages += fixMessage.toJson(dd)
        })
      }
      true
    }
  })
  messages.foreach(println)

  class FIXMessage extends Message {
    implicit private val formats: DefaultFormats.type =
      org.json4s.DefaultFormats

    def toJson(dataDictionary: DataDictionary): String = {
      val jsonMap = Map("header" -> toJsonFields(header, dataDictionary),
                        "body"    -> toJsonFields(this, dataDictionary),
                        "trailer" -> toJsonFields(trailer, dataDictionary))
      write(jsonMap)
    }

    def toJsonFields(fieldMap: FieldMap,
                     dataDictionary: DataDictionary): Map[String, Any] = {
      val singleFields = fieldMap
        .iterator()
        .asScala
        .map(field => {
          val name = if (dataDictionary != null) {
            dataDictionary.getFieldName(field.getTag)
          } else field.getTag.toString
          val value = field.getObject.toString
          name -> value
        })
        .toMap

      val groupFields = fieldMap
        .groupKeyIterator()
        .asScala
        .map(groupKey => {
          val name                       = dataDictionary.getFieldName(groupKey)
          val groups: mutable.Seq[Group] = fieldMap.getGroups(groupKey).asScala
          name -> groups
            .map(group => {
              toJsonFields(group, dataDictionary)
            })
            .toList
        })
        .toMap

      singleFields ++ groupFields
    }
  }

}
