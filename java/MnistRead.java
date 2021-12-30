import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class MnistRead extends JPanel{
    public static final String TRAIN_IMAGES_FILE = "resources/train-images.idx3-ubyte";
    public static final String TRAIN_LABELS_FILE = "resources/train-labels.idx1-ubyte";
//    public static final String TEST_IMAGES_FILE = "resources/t10k-images.idx3-ubyte";
//    public static final String TEST_LABELS_FILE = "resources/t10k-labels.idx1-ubyte";



    /**
     * get images of 'train' or 'test'
     *
     * @param fileName the file of 'train' or 'test' about image
     * @return one row show a `picture`
     */
    public static double[][] getImages(String fileName) {
        double[][] x;
        try (BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName))) {
            byte[] bytes = new byte[4];
            bin.read(bytes, 0, 4);
            if (!"00000803".equals(bytesToHex(bytes))) {                        // 读取魔数
                throw new RuntimeException("Please select the correct file!");
            } else {
                bin.read(bytes, 0, 4);
                int number = Integer.parseInt(bytesToHex(bytes), 16);           // 读取样本总数
                bin.read(bytes, 0, 4);
                int xPixel = Integer.parseInt(bytesToHex(bytes), 16);           // 读取每行所含像素点数
                bin.read(bytes, 0, 4);
                int yPixel = Integer.parseInt(bytesToHex(bytes), 16);           // 读取每列所含像素点数
                x = new double[number][xPixel * yPixel];
                for (int i = 0; i < number; i++) {
                    double[] element = new double[xPixel * yPixel];
                    for (int j = 0; j < xPixel * yPixel; j++) {
                        element[j] = bin.read();                                // 逐一读取像素值
                        // normalization
//                        element[j] = bin.read() / 255.0;
                    }
                    x[i] = element;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return x;
    }

    /**
     * change bytes into a hex string.
     *
     * @param bytes bytes
     * @return the returned hex string
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /**
     * get labels of `train` or `test`
     *
     * @param fileName the file of 'train' or 'test' about label
     */
    public static double[] getLabels(String fileName) {
        double[] y;
        try (BufferedInputStream bin = new BufferedInputStream(new FileInputStream(fileName))) {
            byte[] bytes = new byte[4];
            bin.read(bytes, 0, 4);
            if (!"00000801".equals(bytesToHex(bytes))) {
                throw new RuntimeException("Please select the correct file!");
            } else {
                bin.read(bytes, 0, 4);
                int number = Integer.parseInt(bytesToHex(bytes), 16);
                y = new double[number];
                for (int i = 0; i < number; i++) {
                    y[i] = bin.read();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return y;
    }

    /**
     * draw a gray picture and the image format is JPEG.
     *
     * @param pixelValues pixelValues and ordered by column.
     * @param width       width
     * @param high        high
     */
    public static BufferedImage drawGrayPicture(double[] pixelValues, int width, int high){
        BufferedImage bufferedImage = new BufferedImage(width, high, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < high; j++) {
                int pixel =(int) (255 - pixelValues[i * high + j]);
                int value = pixel + (pixel << 8) + (pixel << 16);   // r = g = b 时，正好为灰度
                bufferedImage.setRGB(j, i, value);
            }
        }
        return bufferedImage;
//        ImageIO.write(bufferedImage, "JPEG", new File(fileName));
    }

    public static void main(String[] args) throws IOException {
        MnistRead mnistRead = new MnistRead();
        mnistRead.initUI();


    }

    public void initUI(){
        JFrame jf = new JFrame("数字识别");
        jf.setSize(1000,1000);
        jf.setLocationRelativeTo(null);
        jf.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        BorderLayout borderLayout = new BorderLayout();
        jf.setLayout(borderLayout);

        jf.add(this,BorderLayout.CENTER);

        JPanel east = new JPanel();
        east.setPreferredSize(new Dimension(200,0));
        BufferedImage bufferedImage = new BufferedImage(800,1000,3);
        east.setBackground(Color.cyan);
        jf.add(east,BorderLayout.EAST);

        jf.setVisible(true);

        Graphics g = this.getGraphics();
        Graphics2D g2d = (Graphics2D)g;
        g2d.setStroke(new BasicStroke(150));
        Graphics imgg = bufferedImage.getGraphics();
        Graphics2D img2d = (Graphics2D)imgg;
        img2d.setStroke(new BasicStroke(150));

        JButton jButton = new JButton("识别");
        jButton.addActionListener(new ActionListener() {
            //点击按钮，识别图片
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage bf= changeSize(bufferedImage,28,28);
                for (int i = 0; i < bf.getWidth(); i++) {
                    for (int j = 0; j < bf.getHeight(); j++) {
                        System.out.print(bf.getRGB(i, j));
                    }
                    System.out.println();
                }
//                g.drawImage(bf,100,100,null);
//                g.drawLine(0,0,100,100);
//                System.out.println("a");
                distinguish(bf);
                for (int i = 0; i < bf.getWidth(); i++) {
                    for (int j = 0; j < bf.getHeight(); j++) {
                        System.out.print(bf.getRGB(i, j));
                    }
                    System.out.println();
                }
            }
        });
        east.add(jButton);

        this.addMouseMotionListener(new MouseMotionAdapter() {
            int x1,y1,x2,y2;
            @Override
            public void mouseDragged(MouseEvent e) {
                if(x1==0 && y1==0){
                    x1 = e.getX();
                    y1 = e.getY();
                }else {
                    x2 = e.getX();
                    y2 = e.getY();
                    g2d.drawLine(x1,y1,x2,y2);
                    imgg.drawLine(x1,y1,x2,y2);
                    x1 = x2;
                    y1 = y2;
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {

            }
        });

    }

    //识别图片
    public void distinguish(BufferedImage image){
        double[][] images = getImages(TRAIN_IMAGES_FILE);
        double[] labels = getLabels(TRAIN_LABELS_FILE);
        int similarNum = 0;//最高相似度
        int value = -1;//最高相似度对应的值
        int similar = 0;//当前相似度
        Algo algo = new Algo();
        for (int i = 0; i < images.length; i++) {
            BufferedImage sampleImg = drawGrayPicture(images[i],28,28);//获取i位置的样本图片
            similar = algo.compareImage(image,sampleImg);//对比两个图片,返回相似度
            if(similar > similarNum){
                similarNum = similar;
                value = (int)labels[i];
            }
//            System.out.println(labels[i]);
        }
        System.out.println(value);
    }

    //改变图片大小
    public BufferedImage changeSize(BufferedImage bf,int w,int h){
        BufferedImage bufferedImage = new BufferedImage(w,h,3);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(bf,0,0,w,h,null);
        return bufferedImage;
    }

}
