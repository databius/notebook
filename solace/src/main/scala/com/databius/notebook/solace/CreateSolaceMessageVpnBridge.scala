package com.databius.notebook.solace

import java.net.URI

import com.spotify.scio.Args
import org.apache.http.HttpHost
import org.apache.http.auth.{AuthScope, UsernamePasswordCredentials}
import org.apache.http.client.methods.{HttpGet, HttpPost}
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{
  BasicCredentialsProvider,
  CloseableHttpClient,
  HttpClientBuilder
}
import org.json4s.jackson.JsonMethods
import org.json4s.jackson.JsonMethods.{compact, render}
import org.json4s.jackson.Serialization.write
import org.json4s.{DefaultFormats, JValue}
import org.slf4j.{Logger, LoggerFactory}

/**
  * @param queueName        The name of the Queue.
  * @param ingressEnabled   Enable or disable the reception of messages to the Queue (Incoming).
  * @param egressEnabled    Enable or disable the transmission of messages from the Queue (Outgoing).
  * @param msgVpnName       The name of the Message VPN.
  * @param maxMsgSpoolUsage The maximum message spool usage allowed by the Queue, in megabytes (MB).
  * @param owner            The Client Username that owns the Queue and has permission equivalent to "delete". The default value is "".
  * @param permission       The permission level for all consumers of the Queue, excluding the owner. The default value is "no-access". Enum: [ no-access, read-only, consume, modify-topic, delete ]
  * @param maxBindCount     The maximum number of consumer flows that can bind to the Queue. The default value is 1000.
  * @param accessType       The access type for delivering messages to consumer flows bound to the Queue. The default value is "exclusive". Enum: [ exclusive, non-exclusive ]
  */
case class Queue(queueName: String,
                 ingressEnabled: Boolean = true,
                 egressEnabled: Boolean = true,
                 msgVpnName: String,
                 maxMsgSpoolUsage: Long = 25000L,
                 owner: String = "bridge_user",
                 permission: String = "no-access",
                 maxBindCount: Long = 1000L,
                 accessType: String = "exclusive")
    extends SolaceConf

/**
  * @param msgVpnName        The name of the Message VPN.
  * @param queueName         The name of the Queue.
  * @param subscriptionTopic The topic of the Subscription.
  */
case class QueueSubscription(msgVpnName: String,
                             queueName: String,
                             subscriptionTopic: String)
    extends SolaceConf

/**
  * @param bridgeName                              The name of the Bridge.
  * @param bridgeVirtualRouter                     The virtual router of the Bridge. Enum: [ primary, backup, auto ]
  * @param msgVpnName                              The name of the Message VPN.
  * @param remoteAuthenticationBasicClientUsername The Client Username the Bridge uses to login to the remote Message VPN.
  * @param remoteAuthenticationBasicPassword       The password for the Client Username.
  * @param remoteAuthenticationScheme              The authentication scheme for the remote Message VPN. The default value is "basic". Enum: [ basic, client-certificate ]
  * @param enabled                                 Enable or disable the Bridge.
  */
case class Bridge(bridgeName: String,
                  bridgeVirtualRouter: String = "auto",
                  msgVpnName: String,
                  remoteAuthenticationBasicClientUsername: String,
                  remoteAuthenticationBasicPassword: String,
                  remoteAuthenticationScheme: String = "basic",
                  enabled: Boolean = true)
    extends SolaceConf

/**
  * @param bridgeName                  The name of the Bridge.
  * @param bridgeVirtualRouter         The virtual router of the Bridge. Enum: [ primary, backup, auto ]
  * @param clientUsername              The Client Username the Bridge uses to login to the remote Message VPN. This per remote Message VPN value overrides the value provided for the Bridge overall.
  * @param password                    The password for the Client Username.
  * @param compressedDataEnabled       Enable or disable data compression for the remote Message VPN connection.
  * @param egressFlowWindowSize        The number of outstanding guaranteed messages that can be transmitted over the remote Message VPN connection before an acknowledgement is received.
  * @param enabled                     Enable or disable the remote Message VPN.
  * @param msgVpnName                  The name of the Message VPN.
  * @param queueBinding                The queue binding of the Bridge in the remote Message VPN. The default value is "".
  * @param remoteMsgVpnLocation        The location of the remote Message VPN as either an FQDN with port, IP address with port, or virtual router name (starting with "v:").
  * @param remoteMsgVpnName            The name of the remote Message VPN.
  * @param unidirectionalClientProfile The Client Profile for the unidirectional Bridge of the remote Message VPN. The Client Profile must exist in the local Message VPN, and it is used only for the TCP parameters.
  */
case class RemoteMsgVpn(bridgeName: String,
                        bridgeVirtualRouter: String = "auto",
                        clientUsername: String = "bridge_user",
                        password: String = "bridge_user_password",
                        compressedDataEnabled: Boolean = false,
                        egressFlowWindowSize: Long,
                        enabled: Boolean = true,
                        msgVpnName: String,
                        queueBinding: String = "",
                        remoteMsgVpnLocation: String,
                        remoteMsgVpnName: String,
                        unidirectionalClientProfile: String)
    extends SolaceConf

/**
  * @param bridgeName              The name of the Bridge.
  * @param bridgeVirtualRouter     The virtual router of the Bridge. Enum: [ primary, backup, auto ]
  * @param deliverAlwaysEnabled    Enable or disable deliver-always for the Bridge remote subscription topic instead of a deliver-to-one remote priority.
  * @param msgVpnName              The name of the Message VPN.
  * @param remoteSubscriptionTopic The topic of the Bridge remote subscription.
  */
case class RemoteSubscription(bridgeName: String,
                              bridgeVirtualRouter: String = "auto",
                              deliverAlwaysEnabled: Boolean = true,
                              msgVpnName: String,
                              remoteSubscriptionTopic: String)
    extends SolaceConf

object CreateSolaceMessageVpnBridge {
  val LOG: Logger =
    LoggerFactory.getLogger(CreateSolaceMessageVpnBridge.getClass)
  implicit val formats: DefaultFormats.type = DefaultFormats
  private val client: CloseableHttpClient = HttpClientBuilder.create().build()

  def initialize(brokerUrl: String,
                 user: String,
                 password: String): HttpClientContext = {
    val localUrl = new URI(brokerUrl)
    val scheme: String = localUrl.getScheme
    val host: String = localUrl.getHost
    val port: Int = localUrl.getPort

    val targetHost = new HttpHost(host, port, scheme)
    val authScope = new AuthScope(targetHost)
    val credentials =
      new UsernamePasswordCredentials(user, password)
    val provider = new BasicCredentialsProvider
    provider.setCredentials(authScope, credentials)

    val context: HttpClientContext = HttpClientContext.create
    context.setTargetHost(targetHost)
    context.setCredentialsProvider(provider)
    context
  }

  def getRequest[T <: SolaceConf](url: String)(
      implicit context: HttpClientContext): JValue = {
    val httpRequest = new HttpGet(url)

    LOG.debug(httpRequest.toString)
    val httpResponse = client.execute(httpRequest, context)
    val entity = httpResponse.getEntity
    val content = JsonMethods.parse(entity.getContent())
    LOG.debug(compact(content))
    content
  }

  def postRequest[T <: SolaceConf](url: String, params: T)(
      implicit context: HttpClientContext): JValue = {
    val httpRequest = new HttpPost(url)
    httpRequest.addHeader("content-type", "application/json; utf-8")
    httpRequest.setEntity(new StringEntity(write(params)))

    LOG.debug(httpRequest.toString)
    val httpResponse = client.execute(httpRequest, context)
    val entity = httpResponse.getEntity
    val content = JsonMethods.parse(entity.getContent())
    LOG.debug(compact(content))
    content
  }

  def getMessageVpns()(implicit context: HttpClientContext): List[String] = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns?select=msgVpnName"
    val content = getRequest(url)
    (content \ "data" \ "msgVpnName")
      .extractOpt[List[String]]
      .getOrElse(List.empty[String])
  }

  def getQueues(msgVpnName: String)(
      implicit context: HttpClientContext): List[String] = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns/$msgVpnName/queues"
    val content = getRequest(url)
    (content \ "data" \ "queueName")
      .extractOpt[List[String]]
      .getOrElse(List.empty[String])
  }

  def createQueue(queue: Queue)(implicit context: HttpClientContext): Unit = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns/${queue.msgVpnName}/queues"
    postRequest(url, queue)
  }

  def getQueueSubscriptions(msgVpnName: String, queueName: String)(
      implicit context: HttpClientContext): List[String] = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns" +
        s"/$msgVpnName/queues/$queueName/subscriptions"
    val content = getRequest(url)

    LOG.debug(compact(render(content)))
    (content \ "data" \ "remoteSubscriptionTopic")
      .extractOpt[List[String]]
      .getOrElse(List.empty[String])
  }

  def createQueueSubscription(queueSubscription: QueueSubscription)(
      implicit context: HttpClientContext): Unit = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns" +
        s"/${queueSubscription.msgVpnName}/queues/${queueSubscription.queueName}/subscriptions"
    postRequest(url, queueSubscription)
  }

  def getBridges(msgVpnName: String)(
      implicit context: HttpClientContext): List[String] = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns/$msgVpnName/bridges"
    val content = getRequest(url)
    (content \ "data" \ "bridgeName")
      .extractOpt[List[String]]
      .getOrElse(List.empty[String])
  }

  def createBridge(bridge: Bridge)(
      implicit context: HttpClientContext): Unit = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns/${bridge.msgVpnName}/bridges"
    postRequest(url, bridge)
  }

  def getRemoteMsgVpns(msgVpnName: String,
                       bridgeName: String,
                       bridgeVirtualRouter: String)(
      implicit context: HttpClientContext): List[String] = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns/$msgVpnName/bridges/$bridgeName,$bridgeVirtualRouter/remoteMsgVpns"
    val content = getRequest(url)
    (content \ "data" \ "remoteMsgVpnName")
      .extractOpt[List[String]]
      .getOrElse(List.empty[String])
  }

  def createRemoteMsgVpn(remoteMsgVpn: RemoteMsgVpn)(
      implicit context: HttpClientContext): Unit = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns" +
        s"/${remoteMsgVpn.msgVpnName}/bridges/${remoteMsgVpn.bridgeName},${remoteMsgVpn.bridgeVirtualRouter}/remoteMsgVpns"
    postRequest(url, remoteMsgVpn)
  }

  def getRemoteSubscriptions(msgVpnName: String,
                             bridgeName: String,
                             bridgeVirtualRouter: String)(
      implicit context: HttpClientContext): List[String] = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns/$msgVpnName/bridges/$bridgeName,$bridgeVirtualRouter/remoteSubscriptions"
    val content = getRequest(url)

    LOG.debug(compact(render(content)))
    (content \ "data" \ "remoteSubscriptionTopic")
      .extractOpt[List[String]]
      .getOrElse(List.empty[String])
  }

  def createRemoteSubscription(remoteSubscription: RemoteSubscription)(
      implicit context: HttpClientContext): Unit = {
    val url =
      s"${context.getTargetHost.toString}/SEMP/v2/config/msgVpns" +
        s"/${remoteSubscription.msgVpnName}/bridges/${remoteSubscription.bridgeName},${remoteSubscription.bridgeVirtualRouter}/remoteSubscriptions"
    postRequest(url, remoteSubscription)
  }

  def main(cmdlineArgs: Array[String]) {
    val args = Args(cmdlineArgs)

    val direction: String = args.required("direction")
    val mode: String = args.required("mode")

    val localBroker: String = args.required("localBroker")
    val localUsername: String = args.required("localUsername")
    val localPassword: String = args.required("localPassword")
    val localMsgVpnName: String = args.required("localMsgVpnName")
    val localMsgVpnLocation: String = args.required("localMsgVpnLocation")
    val localBridgeUsername: String = args.required("localBridgeUsername")
    val localBridgePassword: String = args.required("localBridgePassword")
    val localBridgeProfile: String = args.required("localBridgeProfile")
    val remoteBroker: String = args.required("remoteBroker")
    val remoteUsername: String = args.required("remoteUsername")
    val remotePassword: String = args.required("remotePassword")
    val remoteBridgeProfile: String = args.required("remoteBridgeProfile")
    val remoteMsgVpnName: String = args.required("remoteMsgVpnName")
    val remoteMsgVpnLocation: String = args.required("remoteMsgVpnLocation")
    val remoteBridgeUsername: String = args.required("remoteBridgeUsername")
    val remoteBridgePassword: String = args.required("remoteBridgePassword")
    val bridgeName: String = args.required("bridgeName")
    val bridgeVirtualRouter: String = args.required("bridgeVirtualRouter")
    val egressFlowWindowSize: Long = args.long("egressFlowWindowSize")
    val subscriptionTopicsOnRemote: List[String] =
      args.list("subscriptionTopicsOnRemote")
    var subscriptionTopicsOnLocal: List[String] = List[String]()

    var localQueueBinding: String = ""
    var remoteQueueBinding: String = ""
    if (mode == "guaranteed") {
      localQueueBinding = args.required("localQueueBinding")
      remoteQueueBinding = args.required("remoteQueueBinding")
    }
    if (direction == "bi-directional") {
      subscriptionTopicsOnLocal = args.list("subscriptionTopicsOnLocal")
    }

    implicit val localContext: HttpClientContext =
      initialize(localBroker, localUsername, localPassword)
    val remoteContext: HttpClientContext =
      initialize(remoteBroker, remoteUsername, remotePassword)

    /**
      * 1. Create a bridge in a local Message VPN.
      * See Configuring VPN Bridges https://docs.solace.com/Configuring-and-Managing/Configuring-VPN-Bridges.htm.
      */
    val bridgeOnLocal = Bridge(
      bridgeName = bridgeName,
      bridgeVirtualRouter = bridgeVirtualRouter,
      msgVpnName = localMsgVpnName,
      remoteAuthenticationBasicClientUsername = remoteUsername,
      remoteAuthenticationBasicPassword = remotePassword
    )
    createBridge(bridgeOnLocal)

    /**
      * 2. Configure the remote Message VPN that the bridge will connect to.
      * See Configuring Remote Message VPNs https://docs.solace.com/Configuring-and-Managing/Configuring-VPN-Bridges.htm#Config-Remote-VPNs.
      */
    val remoteMsgVpnOnLocal = RemoteMsgVpn(
      bridgeName = bridgeName,
      bridgeVirtualRouter = bridgeVirtualRouter,
      clientUsername = remoteBridgeUsername,
      password = remoteBridgePassword,
      egressFlowWindowSize = egressFlowWindowSize,
      msgVpnName = localMsgVpnName,
      queueBinding = remoteQueueBinding,
      remoteMsgVpnLocation = remoteMsgVpnLocation,
      remoteMsgVpnName = remoteMsgVpnName,
      unidirectionalClientProfile = remoteBridgeProfile
    )
    createRemoteMsgVpn(remoteMsgVpnOnLocal)

    mode match {
      case "direct" =>
        /**
          * 3. Configure the topic subscriptions for the bridge.
          * See Configuring Remote Subscription Topics https://docs.solace.com/Configuring-and-Managing/Configuring-VPN-Bridges.htm#remote-topic-sub.
          */
        subscriptionTopicsOnRemote
          .map { remoteSubscriptionTopic =>
            RemoteSubscription(
              bridgeName = bridgeName,
              bridgeVirtualRouter = bridgeVirtualRouter,
              msgVpnName = localMsgVpnName,
              remoteSubscriptionTopic = remoteSubscriptionTopic
            )
          }
          .foreach(createRemoteSubscription)
      case "guaranteed" =>
        /**
          * 4. Create a durable queue that is provisioned on the remote Message VPN to associate with the bridge.
          * See Configuring Message Spool Queues https://docs.solace.com/Configuring-and-Managing/Configuring-VPN-Bridges.htm#Config-Msg-Spool-Queues.
          */
        val queueOnLocal = Queue(queueName = localQueueBinding,
                                 owner = remoteUsername,
                                 msgVpnName = localMsgVpnName)
        val queueOnRemote = Queue(queueName = remoteQueueBinding,
                                  owner = localUsername,
                                  msgVpnName = remoteMsgVpnName)
        createQueue(queueOnLocal)
        createQueue(queueOnRemote)(remoteContext)
        subscriptionTopicsOnRemote
          .map { remoteSubscriptionTopic =>
            QueueSubscription(
              msgVpnName = remoteMsgVpnName,
              queueName = remoteQueueBinding,
              subscriptionTopic = remoteSubscriptionTopic
            )
          }
          .foreach(createQueueSubscription(_)(remoteContext))
    }

    /**
      * 8. If you want to establish a bi-directional bridge between the Message VPNs (that is, for messages pass over the bridge in both directions),
      * you must repeat steps 1 through 6 to configure another bridge that uses the same Message VPNs but in the reverse order.
      *
      * That is, this second bridge must be created in the Message VPN that was previously specified as the remote Message VPN,
      * and the remote Message VPN this second bridge is to link to must be the Message VPN that was previously specified as the local Message VPN.
      *
      */
    if (direction == "bi-directional") {
      val bridgeOnRemote = Bridge(
        bridgeName = bridgeName,
        bridgeVirtualRouter = bridgeVirtualRouter,
        msgVpnName = remoteMsgVpnName,
        remoteAuthenticationBasicClientUsername = localUsername,
        remoteAuthenticationBasicPassword = localPassword
      )
      val remoteMsgVpnOnRemote = RemoteMsgVpn(
        bridgeName = bridgeName,
        bridgeVirtualRouter = bridgeVirtualRouter,
        clientUsername = localBridgeUsername,
        password = localBridgePassword,
        egressFlowWindowSize = egressFlowWindowSize,
        msgVpnName = remoteMsgVpnName,
        queueBinding = localQueueBinding,
        remoteMsgVpnLocation = localMsgVpnLocation,
        remoteMsgVpnName = localMsgVpnName,
        unidirectionalClientProfile = localBridgeProfile
      )
      createBridge(bridgeOnRemote)(remoteContext)
      createRemoteMsgVpn(remoteMsgVpnOnRemote)(remoteContext)

      mode match {
        case "direct" =>
          subscriptionTopicsOnLocal
            .map { remoteSubscriptionTopic =>
              RemoteSubscription(
                bridgeName = bridgeName,
                bridgeVirtualRouter = bridgeVirtualRouter,
                msgVpnName = remoteMsgVpnName,
                remoteSubscriptionTopic = remoteSubscriptionTopic
              )
            }
            .foreach(createRemoteSubscription(_)(remoteContext))
        case "guaranteed" =>
          subscriptionTopicsOnLocal
            .map { localSubscriptionTopic =>
              QueueSubscription(
                msgVpnName = localMsgVpnName,
                queueName = localQueueBinding,
                subscriptionTopic = localSubscriptionTopic
              )
            }
            .foreach(createQueueSubscription)
      }
    }
  }
}
