package suzaku.app

import java.nio.ByteBuffer

import arteria.core._
import boopickle.DefaultBasic._
import suzaku.platform.{Logger, Transport}
import suzaku.ui.UIProtocol.UIChannel
import suzaku.ui.{Blueprint, UIManagerProxy, UIProtocol}
import suzaku.util.LoggerProtocol

abstract class AppBase(transport: Transport,
                       createHandler: (MessageChannelHandler[UIProtocol.type]) => AppRouterHandler = vm =>
                         new AppRouterHandler(vm))(implicit routerPickler: Pickler[RouterMessage] =
                                                     RouterMessage.defaultRouterPickler) {

  protected var logLevel = Logger.LogLevelDebug
  protected val logger: Logger = new Logger {
    import Logger._
    override def debug(message: => String): Unit = if (logLevel <= LogLevelDebug) println(message)
    override def info(message: => String): Unit  = if (logLevel <= LogLevelInfo) println(message)
    override def warn(message: => String): Unit  = if (logLevel <= LogLevelWarn) println(message)
    override def error(message: => String): Unit = if (logLevel <= LogLevelError) println(message)
  }
  protected val uiManager = new UIManagerProxy(logger, channelEstablished, flushMessages _)
  protected val router    = new MessageRouter[RouterMessage](createHandler(uiManager), false)

  // constructor
  // subscribe to messages from transport
  transport.subscribe(receive)
  // create channel for logger
  protected val loggerChannel = router.createChannel(LoggerProtocol)(MessageChannelHandler.empty[LoggerProtocol.type],
                                                                     LoggerProtocol.LoggerProtocolContext(),
                                                                     CreateLoggerChannel)

  // log about startup
  logger.info("Application starting")
  // send out first messages to establish router channel
  transport.send(router.flush())

  protected def main(): Unit

  protected def receive(data: ByteBuffer): Unit = {
    router.receive(data)
    // send pending messages
    if (router.hasPending) {
      transport.send(router.flush())
    }
  }

  protected def flushMessages(): Unit = {
    if (router.hasPending) {
      transport.send(router.flush())
    }
  }

  protected def channelEstablished(channel: UIChannel): Unit = {
    // logger.debug("UI channel established")
    // start application
    main()
  }

  def render(root: Blueprint): Unit = uiManager.render(root)
}

class AppRouterHandler(uiHandler: MessageChannelHandler[UIProtocol.type]) extends MessageRouterHandler[RouterMessage] {
  override def materializeChildChannel(id: Int,
    globalId: Int,
    router: MessageRouterBase,
    materializeChild: RouterMessage,
    contextReader: ChannelReader): Option[MessageChannelBase] = {
    materializeChild match {
      case CreateUIChannel =>
        val context = contextReader.read[UIProtocol.ChannelContext](UIProtocol.contextPickler)
        Some(new MessageChannel(UIProtocol)(id, globalId, router, uiHandler, context))
      case _ =>
        None
    }
  }
}

