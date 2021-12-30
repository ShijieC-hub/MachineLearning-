import java.awt.*;
import java.util.Arrays;

public class Algo {
    public Algo() {
    }

    /**
     *
     * @param sampleImg 待比对图片矩阵， 28 * 28
     * @param dateSet 数据集
     * @param labels 标签集
     * @return 预测值
     */
    public static int forceMatch(int[][] sampleImg, int[][] dateSet, double[] labels) {
        int[] sampleArr = new int[sampleImg.length * sampleImg[0].length];
        int index = 0;
        //头位置存相似度，尾位置存标签
        int[] result = new int[]{0, 0};
        int similarity;
        int[] dateSetTemp = new int[28 * 28];
        //将待比对矩阵存入数组
        for(int i = 0; i < sampleImg.length; ++i) {
            for(int j = 0; j < sampleImg[0].length; ++j) {
                sampleArr[index++] = sampleImg[i][j];
            }
        }

        //遍历数据集中图片
        for (int i = 0; i < dateSet.length; i++) {
            //重置相似度值
            similarity = 0;
            //将数据集中的图像二值化
            dateSetTemp = getBinaryImg(28, 28, dateSet[i]);
            //遍历数组
            for (int j = 0; j < 28 * 28; j++) {
                if (dateSetTemp[j] == sampleArr[j]) {
                    similarity++;
                }
            }
            if (similarity > result[0]) {
                result[0] = similarity;
                result[1] = (int) labels[i];
            }
        }
        return result[1];
    }


    public static int[] getBinaryImg(int w, int h, int[] inputs) {
        int[] gray = new int[w * h];
        int[] newpixel = new int[w * h];
        for (int index = 0; index < w * h; index++) {
            int red = (inputs[index] & 0x00FF0000) >> 16;
            int green = (inputs[index] & 0x0000FF00) >> 8;
            int blue = inputs[index] & 0x000000FF;
            gray[index] = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);
        }
        //求出最大灰度值zmax和最小灰度值zmin
        int Gmax = gray[0], Gmin = gray[0];
        for (int index = 0; index < w * h; index++) {
            if (gray[index] > Gmax) {
                Gmax = gray[index];
            }
            if (gray[index] < Gmin) {
                Gmin = gray[index];
            }
        }

        //获取灰度直方图
        int i, j, t, count1 = 0, count2 = 0, sum1 = 0, sum2 = 0;
        int bp, fp;
        int[] histogram = new int[256];
        for (t = Gmin; t <= Gmax; t++) {
            for (int index = 0; index < w * h; index++) {
                if (gray[index] == t)
                    histogram[t]++;
            }
        }

        /*
         * 迭代法求出最佳分割阈值
         * */
        int T = 0;
        int newT = (Gmax + Gmin) / 2;//初始阈值
        while (T != newT)
        //求出背景和前景的平均灰度值bp和fp
        {
            for (i = 0; i < T; i++) {
                count1 += histogram[i];//背景像素点的总个数
                sum1 += histogram[i] * i;//背景像素点的灰度总值
            }
            bp = (count1 == 0) ? 0 : (sum1 / count1);//背景像素点的平均灰度值

            for (j = i; j < histogram.length; j++) {
                count2 += histogram[j];//前景像素点的总个数
                sum2 += histogram[j] * j;//前景像素点的灰度总值
            }
            fp = (count2 == 0) ? 0 : (sum2 / count2);//前景像素点的平均灰度值
            T = newT;
            newT = (bp + fp) / 2;
        }
        int finestYzt = newT; //最佳阈值

        //二值化
        for (int index = 0; index < w * h; index++) {
            if (gray[index] > finestYzt)
                newpixel[index] = Color.WHITE.getRGB();
            else newpixel[index] = Color.BLACK.getRGB();
        }
        return newpixel;
    }


}
