package org.deltaproject.manager.dummy;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.deltaproject.manager.utils.OFUtil;
import org.projectfloodlight.openflow.protocol.OFFactories;
import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.U32;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DummyController implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(DummyController.class);

    public static final int SEND_BUFFER_SIZE = 4 * 1024 * 1024;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private DefaultChannelGroup cg;

    private Set<OFVersion> ofVersions;

    protected static List<U32> ofBitmaps;
    protected static OFFactory defaultFactory;
    protected Timer timer;

    private OFChannelInitializer initializer;
    private OFChannelHandler handler;

    private int testHandShakeType;
    private int ofport;

    public DummyController(String ofv, int port) {
        ofVersions = new HashSet<OFVersion>();

        if (ofv.equals("1.0")) {
            ofVersions.add(OFVersion.OF_10);
            defaultFactory = OFFactories.getFactory(OFVersion.OF_10);
        } else if (ofv.equals("1.3")) {
            ofVersions.add(OFVersion.OF_13);
            defaultFactory = OFFactories.getFactory(OFVersion.OF_13);
        }

        ofBitmaps = OFUtil.computeOurVersionBitmaps(ofVersions);

        timer = new HashedWheelTimer();
        testHandShakeType = 0;
        ofport = port;
    }

    public long getTimeOut() {
        while (handler.getTimeOut() == 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return handler.getTimeOut();
    }

    public void setHandShakeTest(int type) {
        testHandShakeType = type;
    }

    public OFMessage sendOFMessage(OFMessage m) {
        handler.sendOFMessage(m);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        OFMessage res = handler.getResponse();
        return res;
    }

    public OFMessage sendRawPacket(byte[] m) {
        handler.sendRawPkt(m);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        OFMessage res = handler.getResponse();
        return res;
    }

    public OFMessage sendRawPacket(ByteBuf m) {
        handler.sendRawPkt(m);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        OFMessage res = handler.getResponse();
        return res;
    }

    public boolean isOFHandlerActive() {
        if (this.initializer.getHandler() != null && this.initializer.getHandler().isChannelActive()) {
            handler = this.initializer.getHandler();
            return true;
        } else
            return false;
    }

    public void stopNetty() {
        ChannelGroupFuture cf = cg.close();
        cf.awaitUninterruptibly();
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();

        log.info("Stop Dummy Controller");
    }

    public void bootstrapNetty() {
        try {
            bossGroup = new NioEventLoopGroup();
            workerGroup = new NioEventLoopGroup();

            ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class).option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_SNDBUF, SEND_BUFFER_SIZE);

            initializer = new OFChannelInitializer(timer, ofBitmaps, defaultFactory, testHandShakeType);
            bootstrap.childHandler(initializer);

            cg = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            Set<InetSocketAddress> addrs = new HashSet<InetSocketAddress>();
            addrs.add(new InetSocketAddress(InetAddress.getByAddress(IPv4Address.NONE.getBytes()), 6633));

            for (InetSocketAddress sa : addrs) {
                cg.add(bootstrap.bind(sa).channel());
                log.info("Listening for switch connections on {}", sa);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

        this.bootstrapNetty();
        try {
            while (!Thread.currentThread().interrupted()) {

            }
        } finally {
            stopNetty();
            log.info("shutdown controller");
        }
    }
}
