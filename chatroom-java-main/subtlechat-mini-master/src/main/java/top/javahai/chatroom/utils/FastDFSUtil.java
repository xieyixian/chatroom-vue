package top.javahai.chatroom.utils;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

/**
 * @author Hai
 * @date 2020/6/20 - 23:43
 */
public class FastDFSUtil {
    private static StorageClient1 client1;

    static {
        try {
            //初始化FastDFS客户端配置。这里使用了一个名为fastdfs-client.properties的配置文件，其中包含了连接FastDFS所需的配置信息。
            ClientGlobal.initByProperties("fastdfs-client.properties");
            TrackerClient trackerClient = new TrackerClient();//用于连接Tracker服务器
            //通过TrackerClient对象获取到一个TrackerServer对象，用于进行文件上传、下载等操作
            TrackerServer trackerServer = trackerClient.getConnection();
            client1 = new StorageClient1(trackerServer, null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    /**
     * 上传文件
     *
     * @param file
     * @return
     * @throws IOException
     * @throws MyException
     */
    public static String upload(MultipartFile file) throws IOException, MyException {
        //获取上传文件的原始文件名
        String oldName = file.getOriginalFilename();
        //返回上传到服务器的路径
        //通过file.getBytes()方法获取上传文件的字节数组
        //文件拓展名oldName.substring(oldName.lastIndexOf(".")+1)
        //截取原始文件名中最后一个点（.）后面的字符串，即文件的后缀名
        //调用upload_file1()方法上传文件到FastDFS分布式文件系统中。这个方法需要传入文件的字节数组、文件后缀名和附加参数，它会返回上传成功后的文件路径
        return client1.upload_file1(file.getBytes(), oldName.substring(oldName.lastIndexOf(".") + 1), null);
    }

    /**
     *获取访问文件的令牌
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws MyException
     * @return
     */
//  public static StringBuilder getToken(String fileId) throws UnsupportedEncodingException, NoSuchAlgorithmException, MyException {
//    int ts = (int) Instant.now().getEpochSecond();
//    fileId=fileId.substring(7);
//    String token = ProtoCommon.getToken(fileId, ts, "FastDFS1234567890");
//    StringBuilder sb = new StringBuilder();
//    sb.append("?token=").append(token);
//    sb.append("&ts=").append(ts);
//    return sb;
//  }
}