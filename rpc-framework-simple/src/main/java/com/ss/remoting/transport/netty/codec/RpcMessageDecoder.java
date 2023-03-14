package com.ss.remoting.transport.netty.codec;

import com.ss.compress.Compress;
import com.ss.enums.CompressTypeEnum;
import com.ss.enums.SerializationTypeEnum;
import com.ss.extension.ExtensionLoader;
import com.ss.remoting.constants.RpcConstants;
import com.ss.remoting.dto.RpcMessage;
import com.ss.remoting.dto.RpcRequest;
import com.ss.remoting.dto.RpcResponse;
import com.ss.serialize.Serialize;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.protostuff.Rpc;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * custom protocol decoder
 * <pre>
 *   0     1     2     3     4        5     6     7     8         9          10      11     12  13  14   15 16
 *   +-----+-----+-----+-----+--------+----+----+----+------+-----------+-------+----- --+-----+-----+-------+
 *   |   magic   code        |version | full length         | messageType| codec|compress|    RequestId       |
 *   +-----------------------+--------+---------------------+-----------+-----------+-----------+------------+
 *   |                                                                                                       |
 *   |                                         body                                                          |
 *   |                                                                                                       |
 *   |                                        ... ...                                                        |
 *   +-------------------------------------------------------------------------------------------------------+
 * 4B  magic code（魔法数）   1B version（版本）   4B full length（消息长度）    1B messageType（消息类型）
 * 1B compress（压缩类型） 1B codec（序列化类型）    4B  requestId（请求的Id）
 * body（object类型数据）
 * </pre>
 * <p>
 * {@link LengthFieldBasedFrameDecoder} is a length-based decoder , used to solve TCP unpacking and sticking problems.
 * </p>
 */
@Slf4j
public class RpcMessageDecoder extends LengthFieldBasedFrameDecoder { // LengthFieldBasedFrameDecoder:自定义长度解码器
    public RpcMessageDecoder(){
        // lengthFieldOffset: magic code is 4B, and version is 1B, and then full length. so value is 5
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment(调节): full length include all data and read 9 bytes before, so the left length is (fullLength-9). so values is -9
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        // initialBytesToStrip: 我们将手动检查magic code和版本，因此不要剥离任何字节。所以值为 0
        this(RpcConstants.MAX_FRAME_LENGTH,5,4,-9,0);
    }
    /**
     * @param maxFrameLength      Maximum frame length. It decide the maximum length of data that can be received.
     *                            If it exceeds, the data will be discarded.
     *                            最大帧长度。它决定了可以接收的最大数据长度。
     *                            如果超过，数据将被丢弃。
     * @param lengthFieldOffset   Length field offset. The length field is the one that skips the specified length of byte.
     *                            长度字段偏移。长度字段是跳过指定字节长度的字段。
     * @param lengthFieldLength   The number of bytes in the length field.信息的长度
     * @param lengthAdjustment    The compensation value to add to the value of the length field
     *                            要添加到长度字段值的补偿值
     * @param initialBytesToStrip Number of bytes skipped.
     *                            If you need to receive all of the header+body data, this value is 0
     *                            if you only want to receive the body data, then you need to skip the number of bytes consumed by the header.
     *                            跳过的字节数。如果需要接收所有标头+正文数据，则此值为 0。如果您只想接收正文数据，则需要跳过标头消耗的字节数。
     */
    public RpcMessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip){
        super(maxFrameLength,lengthFieldOffset,lengthFieldLength,lengthAdjustment,initialBytesToStrip);
    }
    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in)throws Exception{
        Object decoded=super.decode(ctx,in);
        if (decoded instanceof ByteBuf){
            ByteBuf frame=(ByteBuf) decoded;
            if (frame.readableBytes()>=RpcConstants.TOTAL_LENGTH){
                try {
                    return decodeFrame(frame);
                }catch (Exception e){
                    log.error("Decode frame error",e);
                    throw e;
                }finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }

    /**
     * 解码具体的信息
     * @param in 读取的信息
     * @return RpcMessage
     */
    private Object decodeFrame(ByteBuf in){
        checkMagicNumber(in);
        checkVersion(in);
        int fullLength=in.readInt();
        Byte messageType=in.readByte();
        Byte codecType=in.readByte();
        Byte compressType=in.readByte();
        int requestId=in.readInt();
        RpcMessage rpcMessage=RpcMessage.builder()
                .messageType(messageType)
                .codec(codecType)
                .requestId(requestId).build();
        if (messageType==RpcConstants.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstants.PONG);
            return rpcMessage;
        }
        if (messageType==RpcConstants.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstants.PING);
            return rpcMessage;
        }
        int bodyLength=fullLength-RpcConstants.HEAD_LENGTH;
        if (bodyLength>0){
            byte[] bs=new byte[bodyLength];
            in.readBytes(bs);
            // 解压信息
            String compressName= CompressTypeEnum.getName(compressType);
            Compress compress= ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            bs=compress.decompress(bs);
            // 反序列化
            String codecName= SerializationTypeEnum.getName(rpcMessage.getCodec());
            log.info("codec name: [{}] ", codecName);
            Serialize serialize=ExtensionLoader.getExtensionLoader(Serialize.class).getExtension(codecName);
            if (messageType== RpcConstants.REQUEST_TYPE){
                RpcRequest tmpValue=serialize.deserialize(bs, RpcRequest.class);
                rpcMessage.setData(tmpValue);
            }
            if (messageType== RpcConstants.RESPONSE_TYPE) {
                RpcResponse tmpValue=serialize.deserialize(bs, RpcResponse.class);
                rpcMessage.setData(tmpValue);
            }
        }
        return rpcMessage;
    }
    private void checkVersion(ByteBuf in){
        // read the version and compare
        byte verison=in.readByte();
        if (verison!=RpcConstants.VERSION){
            throw new RuntimeException("version isn't compatible"+verison);
        }
    }
    private void checkMagicNumber(ByteBuf in){
        // read the first 4 bit, which is the magic number, and compare
        int magicLength=RpcConstants.MAGIC_NUMBER.length;
        byte[] tmp=new byte[magicLength];
        in.readBytes(tmp);
        for (int i=0;i<magicLength;i++){
            if (tmp[i]!=RpcConstants.MAGIC_NUMBER[i]){
                throw new IllegalArgumentException("Unknown magic code: "+ Arrays.toString(tmp));
            }
        }
    }
}
