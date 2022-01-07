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
import java.util.*;
import java.util.Map.Entry;

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
        g2d.setStroke(new BasicStroke(130));
        Graphics imgg = bufferedImage.getGraphics();
        Graphics2D img2d = (Graphics2D)imgg;
        img2d.setStroke(new BasicStroke(130));

        JButton jButton = new JButton("识别");
        jButton.addActionListener(new ActionListener() {
            //点击按钮，识别图片
            @Override
            public void actionPerformed(ActionEvent e) {
                BufferedImage bf= changeSize(bufferedImage,28,28);
                distinguish(bf);
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
        ArrayList<Key_Value> hanmingarr = new ArrayList<>();//存放汉明距离,key=汉明距离，value=对应的值
        int hanmingsize = 0;//汉明距离
        Algo algo = new Algo();
        BufferedImage image2 = algo.erzhi(image,200,false);
        for (int i = 0; i < images.length; i++) {
            BufferedImage sampleImg = drawGrayPicture(images[i],28,28);//获取i位置的样本图片
            //先都二值化
            BufferedImage sampleImg2 = algo.erzhi(sampleImg,200,true);
            hanmingsize = algo.compareImage(image2,sampleImg2);//对比两个图片,返回汉明距离
            Key_Value a = new Key_Value(hanmingsize,(int)labels[i]);//存放汉明距离，对应的值,都是Integer类型
            hanmingarr.add(a);

        }
        //现在已经获得了需要对比的图片和所有样本图片的汉明距离以及对应的值，存放在了hanmingarr中
        int k = 200;//k
        //对hanmingarr按key进行排序
        Collections.sort(hanmingarr,new Comparator<Key_Value>(){
            @Override
            public int compare(Key_Value o1, Key_Value o2) {
                return o1.getKey() - o2.getKey();
            }
        });
        for (int i = 0; i < k; i++) {
            System.out.println(hanmingarr.get(i).getKey()+":"+hanmingarr.get(i).getValue());
        }
        int maxValue = -1;
        int value = -1;
        int num0 = 0;
        int num1 = 0;
        int num2 = 0;
        int num3 = 0;
        int num4 = 0;
        int num5 = 0;
        int num6 = 0;
        int num7 = 0;
        int num8 = 0;
        int num9 = 0;
        for (int i = 0; i < k; i++) {
            if(hanmingarr.get(i).getValue() == 0){
                num0++;
                if(num0>maxValue){
                    maxValue = num0;
                    value = 0;
                }
            }
            if(hanmingarr.get(i).getValue() == 1){
                num1++;
                if(num1>maxValue){
                    maxValue = num1;
                    value = 1;
                }
            }
            if(hanmingarr.get(i).getValue() == 2){
                num2++;
                if(num2>maxValue){
                    maxValue = num2;
                    value = 2;
                }
            }
            if(hanmingarr.get(i).getValue() == 3){
                num3++;
                if(num3>maxValue){
                    maxValue = num3;
                    value = 3;
                }
            }
            if(hanmingarr.get(i).getValue() == 4){
                num4++;
                if(num4>maxValue){
                    maxValue = num4;
                    value = 4;
                }
            }
            if(hanmingarr.get(i).getValue() == 5){
                num5++;
                if(num5>maxValue){
                    maxValue = num5;
                    value = 5;
                }
            }
            if(hanmingarr.get(i).getValue() == 6){
                num6++;
                if(num6>maxValue){
                    maxValue = num6;
                    value = 6;
                }
            }
            if(hanmingarr.get(i).getValue() == 7){
                num7++;
                if(num7>maxValue){
                    maxValue = num7;
                    value = 7;
                }
            }
            if(hanmingarr.get(i).getValue() == 8){
                num8++;
                if(num8>maxValue){
                    maxValue = num8;
                    value = 8;
                }
            }
            if(hanmingarr.get(i).getValue() == 9){
                num9++;
                if(num9>maxValue){
                    maxValue = num9;
                    value = 9;
                }
            }
        }

        System.out.println("识别值为：" + value);
    }

    //改变图片大小
    /**
     *
     * @param bf 要改变的图片
     * @param w 要改为的宽
     * @param h 要改为的高
     * @return 该过后的图片
     */
    public BufferedImage changeSize(BufferedImage bf,int w,int h){
        BufferedImage bufferedImage = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(bf.getScaledInstance(w,h,Image.SCALE_SMOOTH),0,0,null);
        return bufferedImage;
    }

}
