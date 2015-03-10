package car;

import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.media.imageio.plugins.tiff.TIFFImageWriteParam;

/**
 * @version Date: Dec 6, 2012 5:41:42 PM
 * @author Shang Pu
 */
public class HaiJia {

	private static final Logger log = LoggerFactory.getLogger(HaiJia.class);

	protected static DefaultHttpClient httpClient = new DefaultHttpClient(createHttpParams());
	protected static final String UTF8 = "UTF-8";

	private static final String userdir = System.getProperties().getProperty("user.dir");
	private static final String SUCCESS = "Success";
	private static final String FAIL = "FAIL";
	protected static String urlLoginYueChe = "http://114.242.121.99";
	private static String imgCodeUrl = "http://114.242.121.99/tools/CreateCode.ashx?key=ImgCode&random=0.9671970325095187";
	protected static final String LOGIN_FORM_EVENTVALIDATION = "/wEWBgKU7PaxCgKl1bKzCQK1qbSRCwLoyMm8DwLi44eGDAKAv7D9CkWv8qjbZguciWb/US2al4x/yk1a6os+TZev6Kbjo+ma";
	protected static final String LOGIN_FORM_VIEWSTATE = "/wEPDwUJLTE5MzE4NjU3D2QWAgIBD2QWAgINDxYCHglpbm5lcmh0bWwFAjk5ZGSJY6WW52dstnUjyuwcxwqksDuWSDLaFgmMK53lVzt5PQ==";
	protected static HttpGet httpGet;
	protected static HttpPost httpPost;

	private static final String userAgent = "Mozilla/5.0 (Windows NT 5.1; rv:16.0) Gecko/20100101 Firefox/16.0";
	private static final String acceptLanguage = "en-us,zh-cn;q=0.7,en;q=0.3";
	private static final String acceptEncoding = "gzip, deflate";
	private static final String acceptCharset = "GB2312,utf-8;q=0.7,*;q=0.7";
	private static final String connection = "keep-alive";
	private static final int timeOut = 60;
	private static String jsFile = "ych2.js"; // 指定md5加密文件
	protected static final String contentTypeJson = "application/json;charset=UTF-8";
	protected static String PROPERTIES = "haijia.properties";

	protected static String username;
	protected static String pwd;

	/**
	 * login system with account info from properties file
	 * 
	 * @throws PropertiesException
	 */
	protected void loginYueChe() throws PropertiesException {
		Properties props = loadProperties(loadFile(PROPERTIES));
		String username = props.getProperty("username");
		String pwd = props.getProperty("password");
		loginYueChe(username, pwd);
	}

	/**
	 * login system with username and pwd
	 * 
	 * @param username
	 * @param pwd
	 */
	protected String loginYueChe(String username, String pwd) {
		log.debug("Enter loginYueChe({})", username);
		try {
			// String randCode = getRandCode("login", imgCodeUrl, 5);
			String randCode = getRandCodeOcr(httpClient, imgCodeUrl, 5);
			String resultMsg = FAIL;

			httpPost = setUpHttpPost(urlLoginYueChe);
			// 登录表单的信息
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("__VIEWSTATE", LOGIN_FORM_VIEWSTATE));
			params.add(new BasicNameValuePair("__EVENTVALIDATION", LOGIN_FORM_EVENTVALIDATION));
			params.add(new BasicNameValuePair("txtUserName", username));
			params.add(new BasicNameValuePair("txtPassword", pwd));
			params.add(new BasicNameValuePair("txtIMGCode", randCode));
			params.add(new BasicNameValuePair("rcode", ""));
			params.add(new BasicNameValuePair("BtnLogin", "登 录"));
			httpPost.setEntity(new UrlEncodedFormEntity(params, UTF8));

			HttpContext localContext = new BasicHttpContext();
			HttpResponse response = httpClient.execute(httpPost, localContext);
			log.debug("getCookies() = {}", httpClient.getCookieStore().getCookies());

			// response = handleRedirection(response);
			int statusCode = response.getStatusLine().getStatusCode();
			if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
					|| (statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
					|| (statusCode == HttpStatus.SC_SEE_OTHER)
					|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
				String newUri = response.getLastHeader("Location").getValue();
				// newUri = "http://114.242.121.99" + newUri;
				newUri = urlLoginYueChe + newUri;
				log.info("newUri = {}", newUri);
				if (response.getEntity() != null) {
					EntityUtils.consume(response.getEntity());
				}
				httpGet = setUpHttpGet(newUri);
				response = httpClient.execute(httpGet);
			}

			String htmlContent = getResponseContent(response);
			resultMsg = getAlertMsg(htmlContent);

			String content = removeHtmlTag(htmlContent);
			log.debug("content = {}", content);
			if (isContentValid(content) && isLogin(content) && resultMsg.equals("")) {
				return SUCCESS;
			} else if (resultMsg.contains("系统服务时间每天从07:35-20:00")
					&& !(JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(null,
							"系统服务时间每天从07:35-20:00\r\nContinute to login?"))) {
				exit("系统服务时间每天从07:35-20:00");
				return "Exit";
			} else {
				// displayMsg(resultMsg + " Please login again", false);
				log.info(resultMsg + "; Please login again");
				return loginYueChe(username, pwd);
			}
		} catch (IOException e) {
			log.error("Exception in HaiJia.loginYueChe()", e);
			// displayMsg(e.getMessage() + "; Please login again", false);
			log.info(e.getMessage() + "; Please login again");
			return loginYueChe(username, pwd);
		}
	}

	protected String getAlertMsg(String content) {
		if (content.contains("alert")) {
			String begin = "<script>alert('";
			String msg = content.substring(content.indexOf(begin) + begin.length(),
					content.indexOf("');</script>"));
			log.info("alert message = {}", msg);
			return msg;
		}
		return "";
	}

	protected static void sleep(long millis) {
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			log.error("Exception in HaiJia.sleep()", e);
		}
	}

	protected void exit(String msg) {
		JOptionPane.showMessageDialog(null, msg + " Exit!!!");
		System.exit(0);
	}

	protected void displayMsg(String msg, boolean openNotepad) {
		log.debug("Enter displayMsg({},{})", msg, openNotepad);
		if (openNotepad) {
			try {
				Runtime.getRuntime().exec("notepad");
			} catch (IOException e) {
				log.error("Failed to open notepad Exception in GetLivingCars.displayMsg()", e);
			}
			File file = new File("C:\\cache\\falls.mp3");
			try {
				playMusic(file);
			} catch (NoPlayerException | IOException e) {
				log.error("Failed to play music. Exception in HaiJia.displayMsg()", e);
			}
		}
		JOptionPane.showMessageDialog(null, msg);
		log.debug("Exit displayMsg()");
	}

	public void playMusic(File file) throws NoPlayerException, IOException {
		MediaLocator locator = new MediaLocator(file.toURI().toURL());
		Player player = Manager.createPlayer(locator);
		player.start();
		player.stop();
		player.close();
	}

	/**
	 * 获取指定url的验证码图片
	 */
	protected File getImage(HttpClient httpClient, String url, String imageName) {
		log.debug("Enter getImage({})", url);
		File image = new File(imageName);
		FileOutputStream out = null;
		HttpGet get = new HttpGet(url);
		try {
			out = new FileOutputStream(image);
			HttpResponse response = httpClient.execute(get);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream is = entity.getContent();
				byte[] buf = new byte[1024];
				int len = -1;
				while ((len = is.read(buf)) > -1) {
					out.write(buf, 0, len);
				}
			}
		} catch (IOException e) {
			log.error("Exception in HaiJia.getImage()", e);
		} finally {
			close(out);
		}
		log.debug("Exit getImage() image path = {}", image.getAbsolutePath());
		return image;
	}

	/**
	 * return random code
	 */
	protected String getRandCodeOcr(DefaultHttpClient httpClient, String url, int frequent) {
		log.debug("Enter getRandCodeOcr({})", frequent);
		String tempDirPath = System.getProperty("java.io.tmpdir")
				+ System.getProperty("file.separator");
		tempDirPath = "";
		String fileName = tempDirPath + "haijia.gif";
		File image = getImage(httpClient, url, fileName);
		ImageIcon icon = new ImageIcon(image.getAbsolutePath());
		if (MediaTracker.ERRORED == icon.getImageLoadStatus()) {
			log.info("Image is broken, reload again.");
			sleep(1000 / frequent);
			return getRandCodeOcr(httpClient, url, frequent);
		}
		String result = ocrImage(image);
		try {
			if (!isRandomCodeValid(result)) {
				return getRandCodeOcr(httpClient, url, frequent);
			}
		} finally {
			// delete temp image and tesseract log file
			image.delete();
			new File("tesseract.log").delete();
		}
		log.debug("Exit getRandCodeOcr()");
		return result;
	}

	/**
	 * return random code with user input, display input dialog with random code image and ask input
	 * from user
	 * 
	 * @param msg TODO
	 * @param frequent the count of requests is sent every second.
	 */
	protected String getRandCode(String msg, String url, int frequent) {
		log.debug("Enter getRandCode({})", frequent);
		byte[] image = getCodeByte(url);
		ImageIcon icon = new ImageIcon(image);
		if (MediaTracker.ERRORED == icon.getImageLoadStatus()) {
			log.info("Image is broken, reload.");
			sleep(1000 / frequent);
			return getRandCode(msg, url, frequent);
		}
		String title = "Random code";
		String text = " Or Click Cancel to exit";
		if (msg.contains("login")) {
			title += " for login";
		} else {
			title += " for booking car";
			text = msg + text;
		}
		JLabel label = new JLabel(text, JLabel.CENTER);
		label.setIcon(icon);
		log.info("Please input " + title);
		String input = JOptionPane.showInputDialog(null, label, title, JOptionPane.DEFAULT_OPTION);
		log.info("input = {}", input);
		if (input == null) {
			exit("You didn't input the code.");
		}
		if (input.equals("")) {
			log.info("input is empty");
			return getRandCode(msg, url, frequent);
		}
		log.debug("Exit getRandCode()");
		return input;
	}

	/**
	 * check if there are non alpha or non number, get randcode image again, return false
	 */
	private static boolean isRandomCodeValid(String randomCode) {
		log.debug("randomCode = {}", randomCode);
		boolean isValid = randomCode.matches("[0-9a-zA-Z]{4}");
		log.debug("Exit isRandomCodeValid() isValid = {}", isValid);
		return isValid;
	}

	/**
	 * @return code in image
	 */
	public static String ocrImage(File image) {
		String result = "";
		try {
			result = ocrRandcodeWithTesseract(image, "gif").replace(" ", "");
			log.info("result = {}", result);
		} catch (IOException e) {
			log.error("IOException in HaiJia.ocrImage()", e);
		} catch (InterruptedException e) {
			log.error("InterruptedException in HaiJia.ocrImage()", e);
		}
		return result;
	}

	public static String ocrRandcodeWithTesseract(File imageFile, String imageFormat)
			throws IOException, InterruptedException {
		String LANG_OPTION = "-l";
		String tessPath = new File("resources/tesseract-ocr").getAbsolutePath();
		File tempImage = createImage(imageFile, imageFormat);

		String output = "output";
		StringBuffer resultSb = new StringBuffer();

		List<String> cmdArgs = new ArrayList<String>();
		cmdArgs.add(tessPath + File.separator + "tesseract");
		cmdArgs.add("");
		cmdArgs.add(output);
		cmdArgs.add(LANG_OPTION);
		cmdArgs.add("eng");
		cmdArgs.set(1, tempImage.getName());
		log.debug("cmdArgs = {}", cmdArgs);

		ProcessBuilder pb = new ProcessBuilder();
		pb.command(cmdArgs);
		pb.redirectErrorStream(true);
		Process process = pb.start();
		int returnCode = process.waitFor();
		log.debug("returnCode = {}", returnCode);

		// delete temp working files
		tempImage.delete();

		String outputTxt = output + ".txt";
		if (returnCode == 0) {
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(outputTxt),
					UTF8));

			String str;
			while ((str = in.readLine()) != null) {
				resultSb.append(str);
			}
			in.close();
		} else {
			throw new IOException(getErrorMessage(returnCode));
		}

		new File(outputTxt).delete();
		log.info("result of OCR:{}", resultSb);
		return resultSb.toString();
	}

	private static String getErrorMessage(int returnCode) {
		String msg;
		switch (returnCode) {
			case 1:
				msg = "Errors accessing files. There may be spaces in your image's filename.";
				break;
			case 29:
				msg = "Cannot recognize the image or its selected region.";
				break;
			case 31:
				msg = "Unsupported image format.";
				break;
			default:
				msg = "Errors occurred.";
		}
		return msg;
	}

	/**
	 * 获取指定url的验证码图片字节信息
	 */
	private byte[] getCodeByte(String url) {
		HttpGet get = new HttpGet(url);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		HttpEntity entity = null;
		try {
			HttpResponse response = httpClient.execute(get);
			entity = response.getEntity();
			if (entity != null) {
				InputStream is = entity.getContent();
				byte[] buf = new byte[1024];
				int len = -1;
				while ((len = is.read(buf)) > -1) {
					baos.write(buf, 0, len);
				}
			}
		} catch (Exception e) {
			log.error("Exception in getCodeByte(): {}", e.getMessage());
		} finally {
			try {
				EntityUtils.consume(entity);
			} catch (IOException e) {
				log.error("Exception in HaiJia.getCodeByte()", e);
			}
		}
		return baos.toByteArray();
	}

	protected static boolean isLogin(String content) {
		return !content.contains("账号");
	}

	protected boolean isContentValid(String content) {
		// content.equals("") ||
		if (content.contains("Service Unavailable") || content.contains("运行时错误")
				|| content.contains("Internal Server Error")) {
			return false;
		}
		String msg = "您当前已通过，或正在进行科目三考试";
		if (content.contains(msg)) {
			log.info(msg);
			exit(msg);
		}
		return true;
	}

	protected HttpResponse handleRedirection(HttpResponse response, String url) throws IOException {
		int statusCode = response.getStatusLine().getStatusCode();
		log.debug("Enter handleRedirection() statusCode = {}", statusCode);
		if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
				|| (statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
				|| (statusCode == HttpStatus.SC_SEE_OTHER)
				|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
			String location = response.getLastHeader("Location").getValue();
			location = toRedirectURL(location, url);
			log.info("newUrl = {}", location);
			if (response.getEntity() != null) {
				EntityUtils.consume(response.getEntity());
			}
			httpGet = setUpHttpGet(location);
			response = httpClient.execute(httpGet);
		}
		log.debug("Exit handleRedirection()");
		return response;
	}

	private String toRedirectURL(String location, String homeUrl) {
		if (location == null || location.trim().length() == 0) {
			location = "/";
		}
		String tmp = location.toLowerCase();
		if (!tmp.startsWith("http://") && !tmp.startsWith("https://")) {
			if (homeUrl == null) {
				throw new IllegalArgumentException(
						"homeUrl is null, can not find relative protocol and host name");
			}
			return homeUrl + location;
		}
		return location;
	}

	/**
	 * 移除html标签
	 */
	protected static String removeHtmlTag(String content) {
		if (content == null || content.isEmpty()) {
			return "";
		}
		// 定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
		String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
		// 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style> }
		String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>";
		// 定义HTML标签的正则表达式
		String regEx_html = "<[^>]+>";

		String temp = content;
		// 去除js
		temp = temp.replaceAll(regEx_script, "");
		// 去除style
		temp = temp.replaceAll(regEx_style, "");
		// 去除html
		temp = temp.replaceAll(regEx_html, "");
		// 合并空格
		temp = temp.replaceAll("\\s+", " ");

		return temp.trim();
	}

	protected String getResponseContent(HttpResponse response) throws IOException {
		return EntityUtils.toString(response.getEntity(), (Charset) null);
	}

	protected static HttpPost setUpHttpPost(String url) {
		httpPost = new HttpPost(url);
		httpPost.setHeader("User-Agent", userAgent);
		httpPost.setHeader("Accept-Language", acceptLanguage);
		httpPost.setHeader("Accept-Encoding", acceptEncoding);
		httpPost.setHeader("Connection", connection);
		return httpPost;
	}

	protected static HttpGet setUpHttpGet(String url) {
		httpGet = new HttpGet(url);
		httpGet.setHeader("User-Agent", userAgent);
		// 用逗号分隔显示可以同时接受多种编码
		httpGet.setHeader("Accept-Language", acceptLanguage);
		// 以下这条如果不加会发现无论你设置Accept-Charset为gbk还是utf-8，他都会默认返回gb2312（本例针对google.cn来说）
		httpGet.setHeader("Accept-Charset", acceptCharset);
		return httpGet;
	}

	/**
	 * 获取指定的GET页面
	 */
	protected String getHttpGetContent(HttpClient httpClient, String url, String charsetName)
			throws IOException {
		Charset charset;
		if (charsetName == null || charsetName.isEmpty()) {
			charset = null;
		} else {
			charset = Charset.forName(charsetName);
		}
		String content = "";
		HttpGet get = new HttpGet(url);
		try {
			HttpResponse resp = httpClient.execute(get);
			content = EntityUtils.toString(resp.getEntity(), charset);
		} finally {
			get.abort();
		}
		return content;
	}

	private static final HttpParams createHttpParams() {
		final HttpParams params = new BasicHttpParams();
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpConnectionParams.setConnectionTimeout(params, timeOut * 1000);
		HttpConnectionParams.setSoTimeout(params, timeOut * 1000);
		HttpConnectionParams.setSocketBufferSize(params, 8192 * 5);
		return params;
	}

	/**
	 * @return true if yyrq is not in the properties file; return false if properties file does not
	 *         exist
	 * @param yyrq 预约日期
	 * @param kemu
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	protected boolean isYueKao(String yyrq, String kemu) {
		log.debug("Enter isYueKao({})", yyrq);
		yyrq = yyrq.replace("-", "");
		// if (yyrq.equals("20130326")) {
		// return false;
		// }

		Properties props;
		try {
			props = loadProperties(loadFile(PROPERTIES));
		} catch (PropertiesException e) {
			log.error("PropertiesException in HaiJia.isYueKao()", e);
			return false;
		}
		// day=20121203,20121204
		String days = props.getProperty(kemu);
		String[] daysArray = days.split(",");
		for (String day : daysArray) {
			day = day.trim();
			if (day.length() == 0) continue;
			if (yyrq.startsWith(day)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return properties
	 * @param file
	 * @throws PropertiesException
	 */
	protected Properties loadProperties(File file) throws PropertiesException {
		Properties props = new Properties();
		try {
			FileInputStream in = new FileInputStream(file);
			props.load(in);
			in.close();
		} catch (IOException e) {
			log.error("IOException in HaiJia.loadProperties()", e);
			throw new PropertiesException(e);
		}
		log.debug("props.toString() = {}", props.toString());
		return props;
	}

	/**
	 * @throws PropertiesException
	 * @throws IllegalArgumentException if filename is inside the jar
	 */
	public static File loadFile(String filename) throws PropertiesException {
		log.debug("read file from jar folder level - userdir {}", userdir);
		File file = new File(userdir + File.separator + filename);
		if (!file.exists()) {
			log.debug("file is not exist, read file through the system class loader");
			try {
				URI uri = ClassLoader.getSystemResource(filename).toURI();
				log.debug("uri = {}", uri);
				file = new File(uri);
			} catch (URISyntaxException e) {
				log.error("URISyntaxException in HaiJia.loadFile()", e);
				throw new PropertiesException(e);
			}
		}
		return file;
	}

	/**
	 * digest the code with MD5
	 */
	protected String md5Code(String code) {
		String md5 = "";
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		FileReader reader = null;// 读取js文件
		try {
			try {
				reader = new FileReader(loadFile(jsFile));
				engine.eval(reader);
			} catch (IllegalArgumentException e) {
				log.debug("read file inside the jar");
				// read file inside jar "/ych2.js" 返回读取指定资源的输入流
				InputStream is = HaiJia.class.getResourceAsStream("/" + jsFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(is));
				String script = "";
				String line;
				while ((line = br.readLine()) != null) {
					script = script + line + System.getProperty("line.separator");
				}
				log.debug("script = {}", script);
				engine.eval(script);
			}

			if (engine instanceof Invocable) {
				Invocable invoke = (Invocable) engine;
				// 调用hex_md5方法，并传入参数
				md5 = (String) invoke.invokeFunction("hex_md5", code);
			}
		} catch (Exception e) {
			log.error("Exception in GetCars.md5ImgCode()", e);
		} finally {
			if (reader != null) try {
				reader.close();
			} catch (IOException e) {
			}
		}
		log.debug("md5 = {}", md5);
		return md5;
	}

	public static File createImage(File imageFile, String imageFormat) {
		File tempFile = null;
		try {
			Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(imageFormat);
			ImageReader reader = readers.next();

			ImageInputStream iis = ImageIO.createImageInputStream(imageFile);
			reader.setInput(iis);
			// Read the stream metadata
			IIOMetadata streamMetadata = reader.getStreamMetadata();

			// Set up the writeParam
			TIFFImageWriteParam tiffWriteParam = new TIFFImageWriteParam(Locale.US);
			tiffWriteParam.setCompressionMode(ImageWriteParam.MODE_DISABLED);

			// Get tif writer and set output to file
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("tiff");
			ImageWriter writer = writers.next();

			BufferedImage bi = reader.read(0);
			IIOImage image = new IIOImage(bi, null, reader.getImageMetadata(0));
			tempFile = tempImageFile(imageFile);
			ImageOutputStream ios = ImageIO.createImageOutputStream(tempFile);
			writer.setOutput(ios);
			writer.write(streamMetadata, image, tiffWriteParam);
			ios.close();

			writer.dispose();
			reader.dispose();
		} catch (Exception e) {
			log.error("Exception in ImageIOHelper.createImage()", e);
		}
		return tempFile;
	}

	public static File tempImageFile(File imageFile) {
		String path = imageFile.getPath();
		StringBuffer strB = new StringBuffer(path);
		strB.insert(path.lastIndexOf('.'), 0);
		return new File(strB.toString().replaceFirst("(?<=\\.)(\\w+)$", "tif"));
	}

	/**
	 * close it quietly
	 */
	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
