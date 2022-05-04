import org.bytedeco.ffmpeg.avcodec.AVPacket;
import org.bytedeco.ffmpeg.avformat.AVFormatContext;
import org.bytedeco.ffmpeg.avformat.AVInputFormat;
import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avdevice;
import org.bytedeco.ffmpeg.global.avformat;

import java.io.File;
import java.io.FileOutputStream;

import static java.lang.Thread.sleep;
import static org.bytedeco.ffmpeg.global.avformat.avformat_alloc_context;
import static org.bytedeco.ffmpeg.global.avutil.av_strerror;


/**
 * 重采样
 */
public class Resample {

    /**
     * 重采样
     * 1 从电脑音频设备采集数据
     * 2 重采样
     * 3 输出到文件（pmc)
     */
    public void resampleFromAudioDevice() throws Exception {

        //1 注册设备
        avdevice.avdevice_register_all();

        //2 设置格式
        AVInputFormat input_format = avformat.av_find_input_format("avfoundation");
//        AVInputFormat input_format = new AVInputFormat();

        //3 打开设备，获取设备上下文句柄
        AVFormatContext fmt_cxt = avformat_alloc_context();
        String deviceName = ":0";
        int ret = avformat.avformat_open_input(fmt_cxt, deviceName, input_format, null);
        byte[] errors = new byte[1024];
        if (ret < 0) {
            av_strerror(ret, errors, 1024);
            System.out.println("打开音频设备失败: " + new String(errors));
            return;
        }
        System.out.println("打开音频设备成功");

        //4 获取数据
        AVPacket avPacket = new AVPacket();
        String file = "/Users/lee/Desktop/audio.pcm";
        //追加模式
        FileOutputStream fileOutputStream = new FileOutputStream(new File(file), true);
        int count = 0;
        while (avformat.av_read_frame(fmt_cxt, avPacket) == 0 && count < 10) {
            System.out.println(count + ":" + avPacket.size());
            count++;
            sleep(1000);

            byte[] output = new byte[avPacket.size()];
            avPacket.data().get(output);
            fileOutputStream.write(output);
            fileOutputStream.flush();

            //释放引用
            avcodec.av_packet_unref(avPacket);
        }

        //5 回收内存
        avformat.avformat_close_input(fmt_cxt);
    }

}
