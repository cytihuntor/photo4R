package print;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.DocAttributeSet;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.standard.MediaSizeName;

/*
 * 修改了 JDK8 中 MediaSize 中的以下部分,主要是支持照片打印
public static final class Engineering
{
 public static final MediaSize A = new MediaSize(3.5F, 5.0F, 25400, MediaSizeName.A);
 public static final MediaSize B = new MediaSize(4.0F, 6.0F, 25400, MediaSizeName.B);
 public static final MediaSize C = new MediaSize(5.0F, 7.0F, 25400, MediaSizeName.C);
 public static final MediaSize D = new MediaSize(6.0F, 8.0F, 25400, MediaSizeName.D);
 public static final MediaSize E = new MediaSize(8.0F, 10.0F, 25400, MediaSizeName.E);
}
*/
public class PhotoPrint4R {
	private static String dstImg = "c:/temp/images/printing.jpg";
	private static String srcImg = "c:/temp/images/rotate_src.jpg";
	
	public static void main(String[] args) throws Exception{
		if(args.length > 0 && args[0].equalsIgnoreCase("debug")){
			File file = new File("E:/temp/im4java-1.4.0/images.src/rose1.jpg");
			//File file = new File("E:/temp/im4java-1.4.0/images.src/rose2.jpg");
			cut(file,"jpg");	
		}else{
			File dir = null;
			if(args.length == 1){
				dir = new File(args[0]);
			}else{
				dir = new File("C:/Users/zjf/Documents/Bluetooth/inbox");
			}
			
			while(true){
				try{
					if(print(dir))
						Thread.sleep(200);
					else
						Thread.sleep(3000);
				}catch(Exception ex){
					ex.printStackTrace();
				}
				
			}
		}
	}
	
	private static boolean print(File dir){
		boolean result = false;
		try{
			DocFlavor flavor = DocFlavor.INPUT_STREAM.JPEG;
			PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
			DocPrintJob dpj = null;
			
			List<File> deleteFileList = new ArrayList<File>();
			
			File[] files = dir.listFiles();
			FileInputStream fis = null;
			for(File file : files){
				try{
					//判断是否接收文件完成
					while(true){
						Thread.sleep(1000);
						if(file.renameTo(file)){
							Thread.sleep(1000);
							break;
						}
					}
					
					String fileName = file.getName().toLowerCase();
					String fileType = fileName.substring(fileName.lastIndexOf(".")+1);
					dpj = defaultService.createPrintJob();
					
					//开始剪切图片
					File curFile = cut(file,fileType);
					if(null == curFile)
						continue;
					System.out.println(curFile.getName());
					//任务的属性
					DocAttributeSet docAttributes = new HashDocAttributeSet();
					docAttributes.add(sizemap.get(6));
					fis = new FileInputStream(curFile);
					Doc doc = new SimpleDoc(fis, flavor, docAttributes);
					//打印
					dpj.print(doc,null);
				}catch(Exception ioe){
					ioe.printStackTrace();
				}finally{
					fis.close();
				}
				
				deleteFileList.add(file);
			}
			
			if(deleteFileList.size() > 0)
				result = true;
			for(File file : deleteFileList)
				file.delete();
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}
	
	private static File cut(File file,String filetype){
		FileInputStream is = null;
        ImageInputStream iis = null;  
        File result = null;
        try {  
        	Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(filetype);  
            ImageReader reader = it.next(); 
            
            // 获取图片流  
            is = new FileInputStream(file);
            iis = ImageIO.createImageInputStream(is);  
            reader.setInput(iis, true);  
            ImageReadParam param = reader.getDefaultReadParam();  
            
            BufferedImage bi = reader.read(0);
            int width = bi.getWidth();
            int height = bi.getHeight();
            
            boolean isNeedRotate = false;
            if(width < height){
            	//旋转 90度
             	int dis = height*4 - width*6;
                if(dis > 0){
                	//太宽了,调宽
                	height = width*6/4;
                }else{
                	width = height*4/6;
                }
                isNeedRotate = true;
            }else{
                int dis = width*4-height*6;
                if(dis > 0){
                	//太宽了,调宽
                	width = height*6/4;
                }else{
                	height = width*4/6;
                }
            }
            
            Rectangle rect = new Rectangle(0, 0, width, height);  
            param.setSourceRegion(rect);  
            bi = reader.read(0, param);  
  
            File dstDir = new File("c:/temp/images/");
            if(!dstDir.exists())
            	dstDir.mkdirs();
            if(isNeedRotate){
            	result = new File(srcImg);
            }else
            	result = new File(dstImg);
            // 保存新图片  
            ImageIO.write(bi, "jpg", result);  
            
            if(isNeedRotate){
            	BufferedImage img = rotateImage(90, filetype);
            	if(null != img){
            		result = new File(dstImg);
            		ImageIO.write(img, "jpg", result);
            	}
            }
        }catch(Exception ioe){
        	ioe.printStackTrace(); 
        }finally {
            if (iis != null)  
                try{iis.close();}catch(Exception ex){}  
            if (is != null)  
                try{is.close();}catch(Exception ex){}  
        }
        return result;
	}
	
	
	public static BufferedImage rotateImage(int degree,String filetype) {
		FileInputStream is = null;
        ImageInputStream iis = null;  
        BufferedImage bufferedimage = null;
        //File result = null;
        try {  
        	Iterator<ImageReader> it = ImageIO.getImageReadersBySuffix(filetype);  
            ImageReader reader = it.next(); 
            
            // 获取图片流  
            is = new FileInputStream(new File(srcImg));
            iis = ImageIO.createImageInputStream(is);  
            reader.setInput(iis, true);  
            //ImageReadParam param = reader.getDefaultReadParam();  
            
            bufferedimage = reader.read(0);
            int w = bufferedimage.getWidth();
            int h = bufferedimage.getHeight();
            int type = bufferedimage.getColorModel().getTransparency();
            BufferedImage img;
            Graphics2D graphics2d;
            (graphics2d = (img = new BufferedImage(h, w, type))
                    .createGraphics()).setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics2d.translate((h - w) / 2,(w - h) / 2);  
            graphics2d.rotate(Math.toRadians(degree), w/2, h/2);
            graphics2d.drawImage(bufferedimage,null,null);
            graphics2d.dispose();
            return img;
        }catch(Exception ioe){
        	ioe.printStackTrace(); 
        }finally {
            if (iis != null)  
                try{iis.close();}catch(Exception ex){}  
            if (is != null)  
                try{is.close();}catch(Exception ex){}  
        }
		return null;
    }
	
	static Map<Integer,MediaSizeName> sizemap = new HashMap<Integer,MediaSizeName>();
	static{
		sizemap.put(5, MediaSizeName.A);
		sizemap.put(6, MediaSizeName.B);
		sizemap.put(7, MediaSizeName.C);
		sizemap.put(8, MediaSizeName.D);
		sizemap.put(10, MediaSizeName.E);
	}
}