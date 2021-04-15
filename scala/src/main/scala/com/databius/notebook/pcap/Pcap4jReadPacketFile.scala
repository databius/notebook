package com.databius.notebook.pcap

import java.io.EOFException
import java.util.concurrent.TimeoutException
import org.pcap4j.core.NotOpenException
import org.pcap4j.core.PcapHandle
import org.pcap4j.core.PcapHandle.TimestampPrecision
import org.pcap4j.core.PcapNativeException
import org.pcap4j.core.Pcaps
import org.pcap4j.packet.Packet


object Pcap4jReadPacketFile {
  private val COUNT = 5
  private val PCAP_FILE_KEY = classOf[Pcap4jReadPacketFile].getName + ".pcapFile"
  private val PCAP_FILE = System.getProperty(PCAP_FILE_KEY, "src/main/resources/echoAndEchoReply.pcap")

  @throws[PcapNativeException]
  @throws[NotOpenException]
  def main(args: Array[String]): Unit = {
    val handle = Pcaps.openOffline(PCAP_FILE, TimestampPrecision.NANO)

    for (i <- 0 until COUNT) {
      val packet = handle.getNextPacketEx
      System.out.println(handle.getTimestamp)
      System.out.println(packet)
    }
    handle.close()
  }
}

@SuppressWarnings(Array("javadoc")) class Pcap4jReadPacketFile private() {
}