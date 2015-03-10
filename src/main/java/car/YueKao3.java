package car;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Shang Pu
 * @version Date: Dec 5, 2012 2:53:40 PM
 */
public class YueKao3 extends HaiJia {
	private static final Logger log = LoggerFactory.getLogger(YueKao3.class);
	protected String urlYueKao3 = "http://114.242.121.99/yk3.aspx";// 科目三考试

	public static void main(String[] args) {
		log.info("科目三考试信息读取中");
		args = new String[] { "152324199005130027", "0513" };
		if (args.length >= 2) {
			username = args[0];
			pwd = args[1];
			log.info("username = {}", username);
		} else {
			log.error("Arguments are: username");
			log.error("               password");
			System.exit(1);
		}
		YueKao3 yueKao3 = new YueKao3();
		yueKao3.loginYueChe(username, pwd);
		yueKao3.yueKao3();
	}

	public void yueKao3() {
		try {
			for (int i = 0;; i++) {
				String content = getHttpGetContent(httpClient, urlYueKao3, UTF8);
				if (!isLogin(content)) {
					// displayMsg("Session is invalid; Please login again",
					// true);
					log.info("Session is invalid; Please login again");
					loginYueChe(username, pwd);
				}
				if (!isContentValid(content)) {
					continue;
				}
				log.info("{}",
						removeHtmlTag(content).replace(" 预约日期 预约时段 剩余可约人数操 作 ", "").replace("预约考试 ", "")
								.replace("取消预约", "").replace("2012-", ""));

				if (canYueKao3(content)) {
					sleep(8000);
				}
				if (i > 10000) {
					displayMsg("Run more than " + i + " times, exit!", true);
					System.exit(0);
				}
				sleep(5000);
			}
		} catch (URISyntaxException | IOException e) {
			log.debug("Exception in YueKao3.yueKao3()", e);
			String msg = "Please check haijia.properteis";
			log.error(msg, e.getMessage());
			exit(msg);
		} catch (Exception e) {
			log.error("Exception in YueKao3.yueKao3()", e.getMessage());
			// displayMsg(e.getMessage() + "; Please login again", false);
			yueKao3();
		}
	}

	private boolean canYueKao3(String content) throws URISyntaxException, IOException {
		Document doc = Jsoup.parse(content, UTF8);
		Elements table = doc.body().getElementsByAttributeValue("id", "tblMain");
		Elements trs = table.get(0).getElementsByTag("tr");
		for (Element tr : trs) {
			Elements tds = tr.getElementsByTag("td");
			if (tds.size() > 0) {
				String count = tds.get(2).text();
				if (!count.equals("0")) {
					String date = tds.get(0).text() + tds.get(1).text();
					if (!isYueKao(tds.get(0).text(), "ke3KaoDay")) {
						log.info("Exculde it on {} and count = {}", date, count);
						continue;
					}

					log.info("You can yue kao 3 date = {}, count = {}", date, count);
					String msg = "You can yue kao 3 on " + date + " and count = " + count;
					String js = tds.get(3).child(0).attr("href");
					String link = js.replace("javascript:__doPostBack('", "").replace("','')", "");
					bookKe3(link, msg);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * @param target "RepeaterKM3Exam$ctl00$LinkBooking"
	 */
	public boolean bookKe3(String target, String msg) {
		HttpPost httpPost = new HttpPost(urlYueKao3);
		// 表单信息
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params
				.add(new BasicNameValuePair(
						"__VIEWSTATE",
						"/wEPDwULLTE2OTg2ODQxNTAPZBYCAgEPZBYCAgEPFgIeC18hSXRlbUNvdW50AgMWBmYPZBYEZg8VAwoyMDEyLTEyLTA4BuS4i+WNiAEwZAIBDw8WBB4EVGV4dAUM6aKE57qm6ICD6K+VHg9Db21tYW5kQXJndW1lbnQFFDIwMTItMTItOCAwOjAwOjAwLDEyZGQCAQ9kFgRmDxUDCjIwMTItMTItMTIG5LiK5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRUyMDEyLTEyLTEyIDA6MDA6MDAsMTFkZAICD2QWBGYPFQMKMjAxMi0xMi0xOQbkuIrljYgCMTFkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRUyMDEyLTEyLTE5IDA6MDA6MDAsMTFkZGS2wei13+kV/gmweKOh7pbzusGwBY2xsHKDirAHf1wLnw=="));
		params.add(new BasicNameValuePair("__EVENTVALIDATION",
				"/wEWBAKOlc3YAQL39sz6BQLans/6BQK1xcj6BYpjCr85EhlPe7EercMLssXihTD019qGNGVZawZ1egZC"));
		params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		params.add(new BasicNameValuePair("__EVENTTARGET", target));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, UTF8));

			HttpResponse response = httpClient.execute(httpPost);
			String htmlContent = getResponseContent(response);
			String alert = getAlertMsg(htmlContent);
			log.info("alert = {}", alert);
			if (alert.equals("科目三需要约满学时")) {
				log.info("约考不成功：{}", alert);
				return false;
			}
			String content = removeHtmlTag(htmlContent);
			log.info("content = {}", content);
			if (content.contains("取消预约")) {
				displayMsg("Yue Ke3 successfully! " + alert, true);
				return true;
			}
		} catch (IOException e) {
			displayMsg(msg, true);
			log.error("Exception in YueKao3.bookKe3()", e);
		}
		displayMsg("Yue Ke2 successfully!", true);
		return true;
	}
}
