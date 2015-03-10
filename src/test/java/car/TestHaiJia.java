package car;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @version Date: Dec 6, 2012 2:49:53 PM
 * @author Shang Pu
 */
public class TestHaiJia extends HaiJia {
	private static final Logger log = LoggerFactory.getLogger(TestHaiJia.class);
	private String pathYueChe2 = "c:/cache/yueche2.html";
	private String pathCancel = "c:/cache/cancel.html";
	private String pathGetCars = "c:/cache/GetCars.html";
	private String pathYueCheList = "c:/cache/yuecheList.html";

	private static String urlLoginYueChe = "http://114.242.121.99";
	private static String urlYueChe = "http://114.242.121.99/index.aspx";// 约车
	private static String urlYueChe2 = "http://114.242.121.99/ych2.aspx";// 约车
	private static String urlGetCars = "http://114.242.121.99/Han/ServiceBooking.asmx/GetCars";
	private static String urlZongHeXinXi = "http://114.242.121.99/zhxx.aspx";// 综合信息
	private static String urlYueKao = "http://114.242.121.99/yk1.aspx";// 法规考试
	private static String urlYueKao2 = "http://114.242.121.99/yk2.aspx";// 科目二考试
	private static String urlYueKao3 = "http://114.242.121.99/yk3.aspx";// 科目三考试
	// change pwd
	private static String urlchangepwd = "http://114.242.121.99/changepwd.aspx";
	// Cancel Booked car
	private static String urlCancelBook = "http://114.242.121.99/NetBooking.aspx";
	private String content;

	public void testMusicMidi() {
		try {
			URL url = this.getClass().getClassLoader().getResource("gameover.wav");
			AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
			// Get a sound clip resource.
			Clip clip = AudioSystem.getClip();
			// Open audio clip and load samples from the audio input stream.
			clip.open(audioIn);
			clip.start();
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	@Test
	public void testMusicMp3() {
		try {
			String filename = "C:\\sp\\My Music\\whitenoise\\RainyMood.mp3";
			filename = "C:\\sp\\My Music\\whitenoise\\Victoria_Falls_preview_whitenoisemp3s-com.mp3";
			File f = new File ( filename );
			MediaLocator locator = new MediaLocator ( f.toURI().toURL() );
			Player player = Manager.createPlayer ( locator );
			player.start();
			player.stop();
			player.close();
		} catch (IOException e) {
			log.error("Exception in TestHaiJia.testMusicMp3()", e);
		} catch (NoPlayerException e) {
			log.error("NoPlayerException in TestHaiJia.testMusicMp3()", e);
		}
	}

	public void testCallJs() {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("javascript");
		String jsFileName = "C:/cache/ych2.js"; // 指定md5加密文件
		FileReader reader = null;// 读取js文件
		try {
			reader = new FileReader(jsFileName);
			engine.eval(reader);
			String md5 = "";
			String imgCode = "mywy".toUpperCase();
			// mywy=8a34a4d2e2871bb9ecdbbf5083b367da
			Invocable invoke = (Invocable) engine;
			// 调用hex_md5方法，并传入参数
			md5 = (String) invoke.invokeFunction("hex_md5", imgCode);
			log.info("imgCode = {}, hex_md5 = {}", imgCode, md5);
		} catch (FileNotFoundException e) {
			log.error("FileNotFoundException in TestClass.testCallJs()", e);
		} catch (ScriptException e) {
			log.error("ScriptException in TestClass.testCallJs()", e);
		} catch (NoSuchMethodException e) {
			log.error("NoSuchMethodException in TestClass.testCallJs()", e);
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
			}
		}
	}
	public void testJsonArray() {
		String content = "[\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05195\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05198\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05163\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05172\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05191\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05176\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05164\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05169\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05152\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05171\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05183\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05184\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05194\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05173\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121218\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05174\"\r\n  }\r\n]";
		try {
			JSONArray array = new JSONArray(content);
			List<String> carList = new ArrayList<String>();
			String YYRQ = "";
			String XNSD = "";
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				YYRQ = json.getString("YYRQ");
				XNSD = json.getString("XNSD");
				String CNBH = json.getString("CNBH");
				carList.add(CNBH);
			}
			log.info("YYRQ = {}, XNSD = {}, CNBH = {}", new Object[] { YYRQ,
					XNSD, carList });
		} catch (JSONException e) {
			log.error("Exception in TestClass.testJsonArray()", e);
		}
	}

	public void testGetReturnJsonData() throws JSONException {
		// {"d":"[\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08160\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08121\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08159\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08154\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08161\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08132\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08134\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08221\"\r\n  }\r\n]_1"}
		// String content =
		// "{\"d\":\"[\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08160\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08121\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08159\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08154\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08161\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08132\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08134\"\r\n  },\r\n  {\r\n    \"YYRQ\": \"20121217\",\r\n    \"XNSD\": \"58\",\r\n    \"CNBH\": \"08221\"\r\n  }\r\n]_1\"}";
		String content = "{\"d\":\"[]_0\"}";
		content = "{\"d\":\"[\r\n  {\r\n    \"YYRQ\": \"20121211\",\r\n    \"XNSD\": \"812\",\r\n    \"CNBH\": \"05070\"\r\n  }\r\n]_1\"}";
		log.info("content = {}", content);
		content = content.replace("_0", "").replace("_1", "")
				.replace("\"[", "[").replace("]\"", "]").replace('\"', '"');
		log.info("content2 = {}", content);
		JSONObject jsonObject = new JSONObject(content);
		JSONArray array = jsonObject.getJSONArray("d");
		List<String> carList = new ArrayList<String>();
		String YYRQ = "";
		String XNSD = "";
		for (int i = 0; i < array.length(); i++) {
			JSONObject json = array.getJSONObject(i);
			YYRQ = json.getString("YYRQ");
			XNSD = json.getString("XNSD");
			String CNBH = json.getString("CNBH");
			carList.add(CNBH);
		}
		log.info("YYRQ = {}, XNSD = {}, CNBH = {}", new Object[] { YYRQ, XNSD,
				carList });
	}

	public void testBookReturnJsonData2() {
		String content = "{\"d\":\"[\r\n  {\r\n    \"Result\": false,\r\n    \"OutMSG\": \"验证码错误！\"\r\n  }\r\n]\"}";
		content = "{\"d\":[\r\n {\r\n \"Result\": false,\r\n \"OutMSG\": \"验证码错误！\"\r\n}\r\n]}";
		log.info("content = {}", content);
		try {
			JSONObject jsonObject = new JSONObject(content.replace("\"[", "[")
					.replace("]\"", "]"));
			JSONArray array = jsonObject.getJSONArray("d");
			String result = "";
			String outMSG = "";
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				result = json.getString("Result");
				outMSG = json.getString("OutMSG");
			}
			log.info("result = {}, outMSG = {}", result, outMSG);
		} catch (JSONException e) {
			log.error("JSONException in TestClass.testJsonData2()", e);
		}
	}
	public void testCancelBookedCar() throws IOException {
		httpPost = setUpHttpPost(urlCancelBook);
		// 登录表单的信息
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(
				"__VIEWSTATE",
				"/wEPDwUJMjAwMjk2ODYwD2QWAgIBD2QWAgIBDxYCHgtfIUl0ZW1Db3VudAIDFgZmD2QWBGYPFQMSMjAxMi0xMS0yNyAwOjAwOjAwCzE3OjAwLTIwOjAwATNkAgEPDxYCHg9Db21tYW5kQXJndW1lbnQFHjIwMTItMTEtMjcgMDowMDowMF81OF82MTIzNTBfOGRkAgEPZBYEZg8VAxIyMDEyLTExLTI2IDA6MDA6MDALMTM6MDAtMTc6MDABNGQCAQ8PFgIfAQUeMjAxMi0xMS0yNiAwOjAwOjAwXzE1XzYxMjEyNV84ZGQCAg9kFgRmDxUDEjIwMTItMTEtMjMgMDowMDowMAsxMzowMC0xNzowMAE0ZAIBDw8WAh8BBR4yMDEyLTExLTIzIDA6MDA6MDBfMTVfNjEyNDEyXzhkZGTGhQhEz8zDYmdP6UqBvA+glyZzWXZk1fXXrNRf7szmmA=="));
		params.add(new BasicNameValuePair(
				"__EVENTVALIDATION",
				"/wEWBALh5O21AwKc5dPrCQKc5d/JDQKc5fvNDYTAxGyB69eRD3JrxmXM/PpcYwR9YHEBQ67OvDHBtb6m"));
		params.add(new BasicNameValuePair("__EVENTTARGET",
				"repeaterNetBooking$ctl00$btnCancel"));
		params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		httpPost.setEntity(new UrlEncodedFormEntity(params, UTF8));

		HttpContext localContext = new BasicHttpContext();
		HttpResponse response = httpClient.execute(httpPost, localContext);
		log.info("getCookies() = {}", httpClient.getCookieStore().getCookies());
		response = handleRedirection(response, "http://114.242.121.99");
		content = getResponseContent(response);
		getAlertMsg(content);

		log.info("content = {}", removeHtmlTag(content));
	}

	public void selectLivingCar() throws IOException {
		File input = new File(pathYueCheList);
		Document doc = Jsoup.parse(input, "UTF-8");
		Elements el = doc.getElementsByAttributeValue("title", "预约");
		Elements e2 = doc.select("#divCarsList");
		log.info("e2 = {}", e2);
		for (Element e : el) {
			String value = e.text();
			String yyrq = e.attr("yyrq");
			log.info("yyrq = {}, carNum = {}", yyrq, value);
		}
	}

	public void changePwd() throws IOException, URISyntaxException {
		httpPost = setUpHttpPost(urlchangepwd);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("__VIEWSTATE",
				"/wEPDwUKMTIyMDg1NDMxNGRkXjs/tiNIgoiZ7hI+/ZiQU3ejIMAsXpgRqKDI+yRHxqM="));
		params.add(new BasicNameValuePair(
				"__EVENTVALIDATION",
				"/wEWBQKoo535CwKG7bicAgK1qbSRCwLFveeXBAKNovGRCFeYX5toiINfjKXm53vysWLQMCItZkC1flKtKal5xmtW"));
		params.add(new BasicNameValuePair("txtPwdOld", "pass"));
		params.add(new BasicNameValuePair("txtPassword", "aaaa"));
		params.add(new BasicNameValuePair("txtPwdAgain", "aaaa"));
		params.add(new BasicNameValuePair("txtChange", "修改密码"));
		httpPost.setEntity(new UrlEncodedFormEntity(params, UTF8));

		HttpResponse response = httpClient.execute(httpPost);
		log.info("getCookies() = {}", httpClient.getCookieStore().getCookies());
		response = handleRedirection(response, "http://114.242.121.99");
		content = getResponseContent(response);
		getAlertMsg(content);
		log.info("content = {}", removeHtmlTag(content));
	}

	public HttpResponse handleRedirection(HttpResponse response, String url)
			throws IOException {
		int statusCode = response.getStatusLine().getStatusCode();
		log.debug("Enter handleRedirection() statusCode = {}", statusCode);
		if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
				|| (statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
				|| (statusCode == HttpStatus.SC_SEE_OTHER)
				|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {
			String newUri = response.getLastHeader("Location").getValue();
			if (newUri.startsWith("/")) {
				newUri = url + newUri;
			}
			log.info("newUri = {}", newUri);
			if (response.getEntity() != null) {
				EntityUtils.consume(response.getEntity());
			}
			httpGet = setUpHttpGet(newUri);
			response = httpClient.execute(httpGet);
		}
		log.debug("Exit handleRedirection()");
		return response;
	}
	public void write(String filePath, String content)
			throws IOException {
		File file = new File(filePath);
		BufferedWriter bWriter = new BufferedWriter(new FileWriter(file));
		try {
			File folder = file.getParentFile();
			if (folder != null) {
				folder.mkdirs();
			}
			if (!file.exists()) {
				file.createNewFile();
			}
			bWriter.write(content);
			bWriter.flush();
		} finally {
			close(bWriter);
		}
	}

	@SuppressWarnings("unused")
	private void writeHtml(String path, String content) throws IOException {
		write(
				path,
				content.replace("<head>",
						"<head><meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>"));
	}
}
