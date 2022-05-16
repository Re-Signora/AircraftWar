package work.chiro.game.application;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import javax.imageio.ImageIO;
import javax.swing.JFrame;

import work.chiro.game.compatible.ResourceProvider;
import work.chiro.game.compatible.XImage;
import work.chiro.game.config.Constants;
import work.chiro.game.scene.Scene;
import work.chiro.game.scene.SceneRun;
import work.chiro.game.windows.GameWindow;
import work.chiro.game.windows.HistoryWindow;
import work.chiro.game.windows.MainWindow;

/**
 * 程序入口
 *
 * @author hitsz
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello Aircraft War");

        ResourceProvider.setInstance(new ResourceProvider() {
            @Override
            public XImage<?> getImageFromResource(String path) throws IOException {
                BufferedImage bufferedImage = ImageIO.read(Objects.requireNonNull(Main.class.getResourceAsStream("/images/" + path)));
                return new XImage<BufferedImage>() {
                    @Override
                    public int getWidth() {
                        return bufferedImage.getWidth();
                    }

                    @Override
                    public int getHeight() {
                        return bufferedImage.getHeight();
                    }

                    @Override
                    public BufferedImage getImage() {
                        return bufferedImage;
                    }
                };
            }
        });

        // 获得屏幕的分辨率，初始化 Frame
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        JFrame frame = new JFrame();
        frame.setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        frame.setResizable(false);
        //设置窗口的大小和位置,居中放置
        frame.setBounds(((int) screenSize.getWidth() - Constants.WINDOW_WIDTH) / 2, 0,
                Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            SceneRun.newInstance(frame, Arrays.asList(
                            new Scene("Main Window", MainWindow.getInstance()),
                            new Scene("Game Window", GameWindow.getInstance()),
                            new Scene("History Window", HistoryWindow.getInstance())
                    ))
                    .setNextScene(MainWindow.class)
                    .run();
        } catch (SceneRun.SceneRunDoneException e) {
            System.out.println("All scene run done.");
        }
        System.exit(0);
    }
}
