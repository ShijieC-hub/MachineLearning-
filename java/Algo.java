import java.awt.*;
import java.awt.image.BufferedImage;

public class Algo {
    /**
     * image和 sampleImg长宽应该一致
     * @param image 传入需要比对的图片
     * @param sampleImg 样本图片
     * @return 相似度
     */
    public int compareImage(BufferedImage image,BufferedImage sampleImg){

        //先都二值化
        BufferedImage image2 = erzhi(image,128);
        BufferedImage sampleImg2 = erzhi(sampleImg,128);

        int wight = 28;
        int height = 28;
        int num = 0;//相似度

        //如果像素点一样，num++
        for (int i = 0; i < wight; i++) {
            for (int j = 0; j < height; j++) {
                if(image2.getRGB(i,j) == Color.black.getRGB() && image2.getRGB(i,j) == sampleImg2.getRGB(i,j)){
                    num++;
                }
            }
        }


        return num;
    }

    //获取灰度
    public int getgray(BufferedImage bufferedImage,int x,int y){

        int color = bufferedImage.getRGB(x,y);
        int r = (color >> 16) & 0xff;
        int g = (color >> 8 ) & 0xff;
        int b = color & 0xff;
        int gray = (int)(0.2989*r + 0.5870*g + 0.1140*b);

        return gray;

    }

    //二值化
    public BufferedImage erzhi(BufferedImage bufferedImage,int k){

        int width = bufferedImage.getWidth();
        int hight = bufferedImage.getHeight();

        BufferedImage bf = new BufferedImage(width,hight,BufferedImage.TYPE_INT_ARGB);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < hight; j++) {
                if(getgray(bufferedImage,i,j) > k){
                    bf.setRGB(i,j, Color.WHITE.getRGB());
                }else {
                    bf.setRGB(i,j,Color.BLACK.getRGB());
                }
            }
        }

        return bf;
    }



}
