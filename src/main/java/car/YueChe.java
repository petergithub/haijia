package car;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shang Pu
 * @version Date: Nov 18, 2012 7:34:09 PM
 */
public class YueChe extends HaiJia {
	private static final Logger log = LoggerFactory.getLogger(YueChe.class);
	private static final String COMMA = ",";
	private static final String DASH = "-";

	private static final String urlYueChe2 = "http://114.242.121.99/ych2.aspx";// 约车
	private static final String urlGetCars = "http://114.242.121.99/Han/ServiceBooking.asmx/GetCars";
	private static final String urlBookingCar = "http://114.242.121.99/Han/ServiceBooking.asmx/BookingCar";
	private static final String bookingCodeUrl = "http://114.242.121.99/tools/CreateCode.ashx?key=BookingCode&random=0.6569643824735577";
	private static final String[] yysd = { "812", "15", "58" };// 预约时段
	private static final Map<String, String> timeMap = new HashMap<String, String>();
	{
		timeMap.put("812", "morning");
		timeMap.put("15", "afternoon");
		timeMap.put("58", "evening");
	}

	private String keMuNumber = "3";
	private List<String> excludeDayList = new ArrayList<String>();

	public static void main(String[] args) {
		args = new String[] { "37232119890202132X", "0202" };
		if (args.length >= 2) {
			username = args[0];
			pwd = args[1];
			log.info("username = {}", username);
		} else {
			log.error("Arguments are: username");
			log.error("               password");
			System.exit(1);
		}
		YueChe yueChe = new YueChe();
		yueChe.loginYueChe(username, pwd);
		log.info("login successfully");
		yueChe.getLivingCar();
	}

	private boolean exculde(String yyrq, String time) throws PropertiesException {
		// if (yyrq.equals("20130225")) {
		// return true;
		// }
		File file = loadFile(PROPERTIES);
		Properties props = loadProperties(file);

		// day=20121203,20121204
		String days = props.getProperty("day");
		String[] daysArray = days.split(COMMA);

		for (String day : daysArray) {
			if (!excludeDayList.contains(day)) {
				excludeDayList.add(day);
			}
		}

		for (String day : excludeDayList) {
			if (yyrq.equals(day)) {
				return true;
			}
		}

		// time=812,15,58
		String times = props.getProperty("time");
		String[] timesArray = times.split(COMMA);
		for (String s : timesArray) {
			if (time.equals(s)) {
				return true;
			}
		}
		// exclude=20121203,20121204-812,
		String exclude = props.getProperty("daytime");
		String[] excludeArray = exclude.split(COMMA);
		for (String s : excludeArray) {
			if (s.contains(DASH)) {
				int dashIndex = s.indexOf(DASH);
				if (yyrq.equals(s.substring(0, dashIndex)) && time.equals(s.substring(dashIndex + 1))) {
					return true;
				}
			} else {
				if (yyrq.equals(s)) {
					return true;
				}
			}
		}
		return false;
	}

	private void getLivingCar() {
		for (int i = 0;; i++) {
			String htmlContent;
			try {
				htmlContent = getHttpGetContent(httpClient, urlYueChe2, UTF8);
			} catch (IOException e1) {
				log.error("IOException in YueChe.getLivingCar()", e1);
				continue;
			}
			if (!isContentValid(htmlContent)) {
				continue;
			}
			String content = removeHtmlTag(htmlContent);
			if (!isLogin(content)) {
				log.debug("Session is invalid; Please login again");
				loginYueChe(username, pwd);
			}
			content = content.replace("网 上 预 约 训 练", "")
					.replace("预约日期 上午(08:00-12:00) 下午(13:00-17:00) 夜间(17:00-20:00) ", "")
					.replace(" 您当前可约的训练类型：", "").replace("提示:为什么不约上一辆教练车", "");
			log.info("\r\n" + content);
			if (content.contains("科目三")) {
				keMuNumber = "3";
			} else if (content.contains("科目二")) {
				keMuNumber = "2";
			} else {
				displayMsg("there is not any car you can book.", false);
				System.exit(0);
			}
			log.debug("keMuNumber = {}", keMuNumber);
			// 2012-11-22 yysd[0], yysd[1], yysd[2]

			try {
				if (isLivingCarAndBook(htmlContent, "", yysd[0], yysd[1], yysd[2])) {
					sleep(8000);
				}
			} catch (PropertiesException e) {
				log.debug("Exception in YueChe.getLivingCar()", e);
				String msg = "Please check haijia.properteis";
				log.error(msg, e.getMessage());
				exit(msg);
			}
			if (i > 10000) {
				displayMsg("Run more than " + i + " times, exit!", true);
				System.exit(0);
			}
			sleep(3000);
		}

	}

	/**
	 * check if there is car can be booked date = "2012-11-21" times = { "812", "15", "58" }
	 * 
	 * @throws PropertiesException
	 */
	private boolean isLivingCarAndBook(String content, String date, String... times)
			throws PropertiesException {
		Document doc = Jsoup.parse(content, "UTF-8");
		for (String time : times) {
			log.debug("time = " + time);
			Elements es = doc.body().getElementsByAttributeValue("yysd", time);
			for (Element e : es) {
				String value = e.text();
				String yyrq = e.attr("yyrq");
				// bookCar("2012-12-25", "15", "08011");
				if (hasCar(value)) {
					List<String> carList = getCarList(yyrq, time);
					if (exculde(yyrq, time)) {
						log.info("Exculde {} {}", yyrq, timeMap.get(time));
						continue;
					}
					log.info("预约日期 = {},预约时段 = {}, 车数 = {}, 车号 = {}", new Object[] { yyrq, time, value,
							carList });
					// if (carList.contains("08088")) {
					// bookCar(yyrq, time, "08088");
					// }
					// for (String cnbh : carList) {
					// bookCar(yyrq, time, cnbh);
					// }
					if (date == null || date.equals("") || !yyrq.equals(date)) {
						displayMsg("There are " + value + " cars in the " + timeMap.get(time) + " on " + yyrq,
								true);
						log.info("There are " + value + " cars in the " + timeMap.get(time) + " on " + yyrq);
						return true;
					}
				}
			}
		}
		return false;
	}

	protected void bookCar(String yyrq, String time, String cnbh) {
		try {
			HttpPost httpPost = new HttpPost(urlBookingCar);
			String jsonRequestStr;
			jsonRequestStr = buildBookCarRequestStr(yyrq, time, cnbh);
			httpPost.setEntity(new StringEntity(jsonRequestStr));
			httpPost.setHeader("Content-Type", contentTypeJson);
			HttpResponse response = httpClient.execute(httpPost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
				log.debug("StatusCode = {}", HttpStatus.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			response = handleRedirection(response, urlLoginYueChe);

			String content = getResponseContent(response);
			// {"d":"[\r\n  {\r\n    \"Result\": false,\r\n    \"OutMSG\": \"验证码错误！\"\r\n  }\r\n]"}
			log.debug("content = {}", content);
			JSONObject jsonObject = new JSONObject(content);
			String value = jsonObject.getString("d");
			JSONArray array = new JSONArray(value);
			for (int i = 0; i < array.length(); i++) {
				JSONObject json = array.getJSONObject(i);
				String result = json.getString("Result");
				String outMSG = json.getString("OutMSG");
				log.info("result = {}, outMSG = {}", result, outMSG);
				if (result.equals("true")) {
					String msg = "booked car " + cnbh + " in the " + timeMap.get(time) + " on " + yyrq;
					log.info(msg + " successfully!");
					excludeDayList.add(yyrq);
					log.info(" {} . add {} to exclude list", outMSG, yyrq);
					displayMsg(msg + " successfully!", false);
				} else {
					log.info("outMSG = {}", outMSG);
					if ("该日已预约过小时".equals(outMSG) || "您已经完成了科目二的所有训练".equals(outMSG)) {
						excludeDayList.add(yyrq);
						log.info(" {} . add {} to exclude list", outMSG, yyrq);
					}
				}
			}
			EntityUtils.consume(response.getEntity());
		} catch (Exception e) {
			log.error("Exception in YueChe.bookCar(): {}", e.getMessage());
		}
	}

	private List<String> getCarList(String yyrq, String time) {
		List<String> carList = new ArrayList<String>();
		try {
			HttpPost httpPost = new HttpPost(urlGetCars);
			String jsonRequestStr = buildGetCarRequestStr(yyrq, time);
			httpPost.setEntity(new StringEntity(jsonRequestStr));
			httpPost.setHeader("Content-Type", contentTypeJson);

			HttpResponse response = httpClient.execute(httpPost);
			String content = getResponseContent(response);
			log.debug("content = {}", content);
			if (content.contains("_0")) {
				// no car left {"d":"[]_0"}
				log.debug("Exit getCarList() no car is left");
				return carList;
			}

			resolveContent(carList, content);
		} catch (JSONException e) {
			log.debug("JSONException in YueChe.getCarList()", e);
			log.error("JSONException in YueChe.getCarList(): {}", e.getMessage());
		} catch (IOException e) {
			log.debug("IOException in YueChe.getCarList()", e);
			log.error("IOException in YueChe.getCarList(): {}", e.getMessage());
		}
		log.debug("Exit getCarList()");
		return carList;
	}

	private void resolveContent(List<String> carList, String content) throws JSONException {
		// content = content.replace("_2", "").replace("_3", "").replace("_4",
		// "").replace("_1", "");
		// remove _1, _2, _3, _4
		content = content.replaceAll("_[0-9]", "");
		content = content.replace("\\r\\n", "").replace("\\", "").replace("]\"", "]")
				.replace("\"[", "[");
		log.debug("content = {}", content);
		JSONObject jsonObject = new JSONObject(content);
		JSONArray array = jsonObject.getJSONArray("d");
		for (int i = 0; i < array.length(); i++) {
			JSONObject json = array.getJSONObject(i);
			String CNBH = json.getString("CNBH");
			carList.add(CNBH);
		}
	}

	private String buildBookCarRequestStr(String yyrq, String time, String cnbh) throws JSONException {
		String msg = "book car " + cnbh + " in the " + timeMap.get(time) + " on " + yyrq;
		log.info("msg = {}", msg);
		// String randCode = getRandCode(msg, bookingCodeUrl, 5);
		String randCode = getRandCodeOcr(httpClient, bookingCodeUrl, 5);
		String imgCode = md5Code(randCode.toUpperCase());
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("yyrq", yyrq);
		jsonRequest.put("xnsd", time);
		jsonRequest.put("cnbh", cnbh);
		jsonRequest.put("KMID", keMuNumber);
		jsonRequest.put("imgCode", imgCode);
		String jsonRequestStr = jsonRequest.toString();
		log.debug("jsonRequestStr = {}", jsonRequestStr);
		return jsonRequestStr;
	}

	private String buildGetCarRequestStr(String yyrq, String yysd) throws JSONException {
		JSONObject jsonRequest = new JSONObject();
		jsonRequest.put("yyrq", yyrq);
		jsonRequest.put("yysd", yysd);
		jsonRequest.put("xllxID", keMuNumber);
		jsonRequest.put("pageSize", "435");
		jsonRequest.put("pageNum", "1");
		String jsonRequestStr = jsonRequest.toString();
		log.debug("jsonRequestStr = {}", jsonRequestStr);
		return jsonRequestStr;
	}

	private boolean hasCar(String value) {
		if (isInteger(value) || !value.equals("无") && !value.equals("已约")) {
			return true;
		}
		return false;
	}

	private static boolean isInteger(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
