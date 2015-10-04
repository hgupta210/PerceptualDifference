package ImageProcessing;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import juice.driverInterface;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import FileProcessing.FileProcessing;
import SiteContext.siteContext;

import com.google.inject.Inject;

public class ImageProcessing {
    private WebDriver wD1, wD2;
    private FileProcessing fileProcessing;
    private siteContext context;

    private static final String PAGE_BODY_KEY = "page.body";

    @Inject
    public ImageProcessing(driverInterface d, FileProcessing fileProcessing, siteContext context) {
        // this.webDriver = webDriver;
        // super();

        // this.wD2 = wD2.get();
        this.wD1 = d.getDriver1();
        this.wD2 = d.getDriver2();
        this.fileProcessing = fileProcessing;
        this.context = context;
    }

    public void isImagesSame(
            String productType)
            throws IOException {
        String fileName1 = "target\\screenshots\\" + productType + "1.png";
        String fileName2 = "target\\screenshots\\" + productType + "2.png";
        String fileName3 = "target\\screenshots\\diff_" + productType + ".png";
        String fileName4 = "target\\screenshots\\combined_" + productType + ".png";
        File combinedImageFile = new File(fileName4);

        BufferedImage img1 = null;
        BufferedImage img2 = null;

        try {
            File file1 = new File(fileName1);
            File file2 = new File(fileName2);

            img1 = ImageIO.read(file1);
            img2 = ImageIO.read(file2);

        } catch (IOException e) {
            e.printStackTrace();
        }
        int width1 = img1.getWidth(null);
        int width2 = img2.getWidth(null);
        int height1 = img1.getHeight(null);
        int height2 = img2.getHeight(null);
        // System.out.println("Image1 size: " + width1 + "x" + height1);
        // System.out.println("Image2 size: " + width2 + "x" + height2);
        // if ((width1 != width2) || (height1 != height2)) {
        // System.err.println("Error: Images dimensions mismatch");
        // System.exit(1);
        // }
        long diff = 0;
        int height = height1;
        int width = width1;
        if (height1 < height2)
            height = height2;
        if (width1 < width2)
            width = width2;

        BufferedImage img3 = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        BufferedImage img4 = new BufferedImage(width * 3, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1, rgb2;
                if (x < width1 && y < height1) {
                    rgb1 = img1.getRGB(x, y);
                    img4.setRGB(x, y, rgb1);
                } else
                    rgb1 = -1;
                if (x < width2 && y < height2) {
                    rgb2 = img2.getRGB(x, y);
                    img4.setRGB(x + width1, y, rgb2);
                } else
                    rgb2 = -1;

                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = (rgb1) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = (rgb2) & 0xff;
                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);

                if (rgb1 != rgb2 && rgb1 != -1 && rgb2 != -1) {
                    int rgb3 = new Color(255, 0, 0).getRGB();
                    img3.setRGB(x, y, rgb3);
                    img4.setRGB(x + width1 + width2, y, rgb3);
                }

                if (rgb1 != rgb2 && rgb1 == -1) {
                    int rgb3 = new Color(255, 0, 0).getRGB();
                    img3.setRGB(x, y, rgb3);
                    img4.setRGB(x + width1 + width2, y, rgb3);
                }
                if (rgb1 != rgb2 && rgb2 == -1) {
                    int rgb3 = new Color(0, 0, 255).getRGB();
                    img3.setRGB(x, y, rgb3);
                    img4.setRGB(x + width1 + width2, y, rgb3);
                }
                // if (rgb1 != rgb2) {
                // int rgb3 = new Color(255, 0, 0).getRGB();
                // img3.setRGB(x, y, rgb3);
                // img4.setRGB(x + width1 + width2, y, rgb3);
                // }
                if (rgb1 == rgb2) {
                    img3.setRGB(x, y, rgb1);
                    img4.setRGB(x + width1 + width2, y, rgb1);
                }
            }
        }
        double n = width * height * 1.3;
        double p = (diff / n / 255.0) * 100;
        p = Math.round(p * 100.0) / 100.0;
        // System.out.println("diff percent: " + (p * 100.0));
        File outputfile = new File(fileName3);
        ImageIO.write(img3, "png", outputfile);
        ImageIO.write(img4, "png", combinedImageFile);
        // createPackageImage(productType);
        fileProcessing.generateHtml(productType, p);
    }

    public void saveScreenshot(
            String product)
            throws IOException {
        if (System.getProperty("browser").equals("Chrome")) {
            saveChromeScreenshot(product, wD1, 1);
            saveChromeScreenshot(product, wD2, 2);
        } else {
            String fileName1 = "target\\screenshots\\" + product + "1.png";
            String fileName2 = "target\\screenshots\\" + product + "2.png";
            File scrFile = ((TakesScreenshot) wD1).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(fileName1));
            File scrFile2 = ((TakesScreenshot) wD2).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile2, new File(fileName2));
        }
    }

    public void saveChromeScreenshot(
            String product, WebDriver driver, int position)
            throws IOException {
        int noOfScreens = 1;

        String fileName1 = "target\\screenshots\\" + product + "_" + noOfScreens + "_" + position + ".png";
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(scrFile, new File(fileName1));

        JavascriptExecutor jse = (JavascriptExecutor) driver;
        Long totalPageHeight = (Long) jse.executeScript("return document.body.scrollHeight;");

        // int viewHeight =
        // wD1.findElement(By.cssSelector((String) context.getLocatorsMap().get(PAGE_BODY_KEY))).getSize()
        // .getHeight();

        int viewHeight = 710;
        int height = viewHeight;
        do {
            jse.executeScript("scroll(0, " + height + ")");
            height = height + viewHeight;
            noOfScreens++;
            fileName1 = "target\\screenshots\\" + product + "_" + noOfScreens + "_" + position + ".png";
            scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(scrFile, new File(fileName1));

        } while (height < totalPageHeight);

        File folder = new File("target\\screenshots");
        File[] listOfFiles = folder.listFiles();
        List<File> results = new ArrayList<File>();

        String fileName4 = "target\\screenshots\\" + product + position + ".png";
        File fullImageFile = new File(fileName4);
        Boolean flag = true;
        BufferedImage img4 = null;
        // int x = 0, y = 0;
        int length = 0;

        for (File allFiles : listOfFiles) {
            if (allFiles.getName().contains(product))
                results.add(allFiles);
        }

        for (int i = 0; i < results.size(); i++) {
            String pattern = i + 1 + "_" + position + ".png";
            // System.out.println(listOfFiles[i].getName());
            // System.out.println(pattern);
            // if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains(pattern)) {
            for (File flName : results)
                if (flName.getName().contains(pattern)) {

                    String fileCopy = flName.getAbsolutePath();
                    BufferedImage img1 = null;

                    try {
                        File file1 = new File(fileCopy);

                        img1 = ImageIO.read(file1);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int width1 = img1.getWidth(null);
                    int height1 = img1.getHeight(null);
                    if (flag) {
                        img4 = new BufferedImage(width1, height, BufferedImage.TYPE_INT_ARGB);
                        flag = false;
                    }

                    for (int y = 0; y < height1; y++) {
                        for (int x = 0; x < width1; x++) {
                            int rgb1;
                            if (x < width1 && y < height) {
                                rgb1 = img1.getRGB(x, y);
                                img4.setRGB(x, length, rgb1);
                            }
                        }
                        length++;
                    }

                }
        }
        ImageIO.write(img4, "png", fullImageFile);

    }

    public void createPackageImage(
            String productType)
            throws IOException {
        String fileName1 = "target\\screenshots\\" + productType + "1.png";
        String fileName2 = "target\\screenshots\\" + productType + "2.png";
        String fileName3 = "target\\screenshots\\combined_" + productType + ".png";
        File outputfile = new File(fileName3);

        BufferedImage img1 = null;
        BufferedImage img2 = null;

        try {
            File file1 = new File(fileName1);
            File file2 = new File(fileName2);

            img1 = ImageIO.read(file1);
            img2 = ImageIO.read(file2);

        } catch (IOException e) {
            e.printStackTrace();
        }
        int width1 = img1.getWidth(null);
        int width2 = img2.getWidth(null);
        int height1 = img1.getHeight(null);
        int height2 = img2.getHeight(null);
        // System.out.println("Image1 size: " + width1 + "x" + height1);
        // System.out.println("Image2 size: " + width2 + "x" + height2);
        // if ((width1 != width2) || (height1 != height2)) {
        // System.err.println("Error: Images dimensions mismatch");
        // System.exit(1);
        // }
        long diff = 0;
        int height = height1;
        int width = width1;
        if (height1 < height2)
            height = height2;
        if (width1 < width2)
            width = width2;

        BufferedImage img3 = new BufferedImage(width * 3, height, BufferedImage.TYPE_INT_ARGB);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb1, rgb2;
                if (x < width1 && y < height1) {
                    rgb1 = img1.getRGB(x, y);
                    img3.setRGB(x, y, rgb1);
                } else
                    rgb1 = -1;
                if (x < width2 && y < height2) {
                    rgb2 = img2.getRGB(x, y);
                    img3.setRGB(x + width1, y, rgb2);
                } else
                    rgb2 = -1;

                int r1 = (rgb1 >> 16) & 0xff;
                int g1 = (rgb1 >> 8) & 0xff;
                int b1 = (rgb1) & 0xff;
                int r2 = (rgb2 >> 16) & 0xff;
                int g2 = (rgb2 >> 8) & 0xff;
                int b2 = (rgb2) & 0xff;
                diff += Math.abs(r1 - r2);
                diff += Math.abs(g1 - g2);
                diff += Math.abs(b1 - b2);

                if (rgb1 != rgb2 && rgb1 == -1) {
                    int rgb3 = new Color(255, 0, 0).getRGB();
                    img3.setRGB(x + width1 + width2, y, rgb3);
                }
                if (rgb1 != rgb2 && rgb2 == -1) {
                    img3.setRGB(x + width1 + width2, y, rgb1);
                }
                if (rgb1 == rgb2) {
                    img3.setRGB(x + width1 + width2, y, rgb1);
                }
            }
        }
        ImageIO.write(img3, "png", outputfile);
    }
}
