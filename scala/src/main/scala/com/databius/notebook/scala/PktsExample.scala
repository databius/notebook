package com.databius.notebook.scala

import io.pkts.PacketHandler
import io.pkts.Pcap
import io.pkts.buffer.Buffer
import io.pkts.framer.FramingException
import io.pkts.packet.Packet
import io.pkts.protocol.Protocol
import quickfix.{DataDictionary, Field, FieldMap, Group, Message}
import quickfix.field._

import java.io.{BufferedInputStream, ByteArrayOutputStream}
import java.nio.charset.StandardCharsets
import org.json4s.Xml.{toJson, toXml}
import org.json4s.jackson.JsonMethods.{pretty, render}
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write
import org.w3c.dom.{CDATASection, Document, Element}

import java.util
import java.util.{Iterator, List}
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.{OutputKeys, Transformer, TransformerFactory}
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import scala.xml.XML

import scala.jdk.CollectionConverters._

object PktsExample extends App {
  implicit val formats = org.json4s.DefaultFormats
  val pcap = Pcap.openStream("./data/bursa-fish.tcp")
  pcap.loop(new PacketHandler() {
    override def nextPacket(packet: Packet): Boolean = {
      // Step 3 - For every new packet the PacketHandler will be
      //          called and you can examine this packet in a few
      //          different ways. You can e.g. check whether the
      //          packet contains a particular protocol, such as UDP.
      if (packet.hasProtocol(Protocol.TCP)) {
        // Step 4 - Now that we know that the packet contains
        //          a UDP packet we get ask to get the UDP packet
        //          and once we have it we can just get its
        //          payload and print it, which is what we are
        //          doing below.
        //        System.out.println(packet.getPacket(Protocol.TCP).getPayload)
        val fixBytes: Buffer = packet.getPacket(Protocol.TCP).getPayload
        //        scala.io.Source.fromInputStream(is).mkString
        if (Option(fixBytes).isDefined) {
          val fixMsg = fixBytes.toString
          val msgType: MsgType = Message.identifyType(fixMsg)
          val FIXMessage = new AAA()
          val dd = new DataDictionary("FIX50SP2.xml")
          //          println(dd.getVersion)
          val fix = FIXMessage.fromString(fixMsg, dd, false)
          //          import quickfix.Message
          //          import quickfix.field.MsgType
          //          val msgType = Message.identifyType(fixMsg)

          import quickfix.InvalidMessage

          //          val index = fixMsg.indexOf(FIELD_SEPARATOR)
          //          if (index < 0) throw new InvalidMessage("Message does not contain any field separator")
          //          val beginString = messageString.substring(2, index)
          //          val messageType = getMessageType(messageString)
          //          val message = messageFactory.create(beginString, messageType)
          //          message.fromString(messageString, dataDictionary, dataDictionary != null)

          //          println(fixMsg)

          //          val xml = FIXMessage.toXML(dd)
          //          println(xml)
          println(FIXMessage.toJson(dd))
          //          val n = XML.loadString(xml)

          //          val FIXheader: Message.Header = FIXMessage.getHeader
          //          FIXheader.getString(MsgType.FIELD)
          //          val header = Header(
          //            FIXheader.getString(BeginString.FIELD),
          //            FIXheader.getString(MsgType.FIELD),
          //            FIXheader.getString(MsgSeqNum.FIELD),
          //            FIXheader.getString(SenderCompID.FIELD),
          //            FIXheader.getString(TargetCompID.FIELD),
          //            FIXheader.getString(SendingTime.FIELD)
          //          )
          //          val FIXhead = FIXMessage.bodyLength()
          //
          //          import quickfix.field.MsgType
          ////          val msgType = FIXMessage.getHeader.getString(MsgType.FIELD)
          //          println(write(header))
          //
          //          val json = toJson(n)
          //          println(write(json))
          //          println(pretty(render(json)))
        }
        //        val fixMsg = """"""
        //        new String(packet.getPacket(Protocol.TCP).getPayload.getArray,
        //                   StandardCharsets.US_ASCII)
        //        println(fixMsg)
      }
      true
    }
  })

  case class Header(
                     BeginString: String,
                     MsgType: String,
                     MsgSeqNum: String,
                     SenderCompID: String,
                     TargetCompID: String,
                     SendingTime: String
                   )

  case class NoMDEntries(
                          MDEntryType: String,
                          MDEntryPx: String,
                          MDEntrySize: String,
                          MDEntryDate: String,
                          MDEntryTime: String
                        )

  case class Body(
                   SecurityIDSource: String,
                   SecurityID: String,
                   MDReqID: String,
                   NoMDEntries: List[NoMDEntries]
                 )

  case class Trailer()

  case class MarketDataSnapshotFullRefresh(
                                            Header: Header,
                                            Body: Body,
                                            Trailer: Trailer
                                          )

  class AAA extends Message {
    def toJson(dataDictionary: DataDictionary) =
      try {

        val header1 = toJsonFields("header", header, dataDictionary)
        val body1 = toJsonFields("body", this, dataDictionary)
        val trailer1 = toJsonFields("trailer", trailer, dataDictionary)
        val x = Map("header" -> header1, "body" -> body1, "trailer" -> trailer1)
        x
      } catch {
        case e: Exception =>
          throw new RuntimeException(e)
      }

    def toJsonFields(section: String,
                     fieldMap: FieldMap,
                     dataDictionary: DataDictionary): Map[String, Any] = {
      val fieldItr = fieldMap.iterator().asScala
      val groupKeyItr = fieldMap.groupKeyIterator().asScala
      val g = groupKeyItr
        .map(groupKey => {
          val name = dataDictionary.getFieldName(groupKey)
          val groups = fieldMap.getGroups(groupKey).asScala
          name -> groups
            .map(group => {
              toJsonFields("group", group, dataDictionary)
            })
            .toList
        })
        .toMap

      val x = fieldItr
        .map(field => {
          val name = if (dataDictionary != null) {
            dataDictionary.getFieldName(field.getTag)
          } else field.getTag.toString
          val value = field.getObject.toString
          name -> value
        })
        .toMap
      x + ("group" -> g)
    }
  }

}
