package org.zyj.hulk.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author zhangyj
 * @date 2022/1/27 0:41
 * <p>
 * 验证粘包和拆包
 */
public class SocketClient2 {
    public static void main(String[] args) throws IOException, InterruptedException {
        Socket socket = new Socket("127.0.0.1", 7777);
        socket.setTcpNoDelay(true);
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("12345678901234567890abc".getBytes("utf8"));//报文是固定20字节
        outputStream.flush();
        outputStream.write("11111111111111111".getBytes("utf8"));
        outputStream.flush();
        InputStream inputStream = socket.getInputStream();
        byte[] b = new byte[20];

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int count = 0;

        while (true) {
            int read = inputStream.read(b, 0, 20);
            if (read == -1) {
                break;
            }
            count += read;
            baos.write(b, 0, read);
            System.out.println("接收到数据-> " + new String(baos.toByteArray(), 0, 20));
            baos.reset();
            if (count >= 40) {
                break;
            }
        }
        outputStream.close();
        inputStream.close();
        socket.close();
        ;
    }
}
