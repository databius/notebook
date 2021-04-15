package com.databius.notebook.pcap

import java.io.EOFException
import java.util
import java.util.concurrent.TimeoutException
import org.pcap4j.core.NotOpenException
import org.pcap4j.core.PcapHandle
import org.pcap4j.core.PcapNativeException
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.IpV4Packet
import org.pcap4j.packet.Packet
import org.pcap4j.packet.SimpleBuilder
import org.pcap4j.util.IpV4Helper

import scala.jdk.CollectionConverters._


object Pcap4jDefragmentEcho {
  private val PCAP_FILE_KEY: String = classOf[Pcap4jDefragmentEcho].getName + ".pcapFile"
//  private val PCAP_FILE: String = System.getProperty(PCAP_FILE_KEY, "src/main/resources/flagmentedEcho.pcap")
  private val PCAP_FILE = System.getProperty(PCAP_FILE_KEY, "data/bursa-ens4f0-all-20210414‐131800.pcap")

  @throws[PcapNativeException]
  @throws[NotOpenException]
  def main(args: Array[String]): Unit = {
    val handle: PcapHandle = Pcaps.openOffline(PCAP_FILE)
    val ipV4Packets = new util.HashMap[Short, util.List[IpV4Packet]]
    val originalPackets = new util.HashMap[Short, Packet]

    while (
      true
    ) {

      val packet: Packet = handle.getNextPacketEx
      System.out.println(handle.getTimestamp)
      System.out.println(packet)
      val ipv4 = packet.get(classOf[IpV4Packet])
      val id = ipv4.getHeader.getIdentification
      if (ipV4Packets.containsKey(id)) {
        ipV4Packets.get(id).add(packet.get(classOf[IpV4Packet]))
      }
      else {
        val list: util.List[IpV4Packet] = new util.ArrayList[IpV4Packet]
        list.add(packet.get(classOf[IpV4Packet]))
        ipV4Packets.put(id, list)
        originalPackets.put(id, packet)
      }

    }
    for (id <- ipV4Packets.keySet.asScala) {
      val list: util.List[IpV4Packet] = ipV4Packets.get(id)
      val defragmentedIpV4Packet: IpV4Packet = IpV4Helper.defragment(list)
      val builder: Packet.Builder = originalPackets.get(id).getBuilder
      builder.getOuterOf(classOf[IpV4Packet.Builder]).payloadBuilder(new SimpleBuilder(defragmentedIpV4Packet))
      System.out.println(builder.build)
    }
    handle.close()
  }
}

@SuppressWarnings(Array("javadoc")) class Pcap4jDefragmentEcho private() {
}
