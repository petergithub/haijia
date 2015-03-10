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
 * @version Date: Nov 22, 2012 11:59:09 AM
 */
public class YueKao2 extends HaiJia {
	private static final Logger log = LoggerFactory.getLogger(YueKao2.class);
	private static String urlYueKao2 = "http://114.242.121.99/yk2.aspx";// 科目二考试

	public static void main(String[] args) {
		log.info("YueKao2 科目二考试信息读取中");
		if (args.length >= 2) {
			username = args[0];
			pwd = args[1];
			log.info("username = {}", username);
		} else {
			log.error("Arguments are: username");
			log.error("               password");
			System.exit(1);
		}
		YueKao2 yueKao2 = new YueKao2();
		yueKao2.loginYueChe(username, pwd);
		yueKao2.yueKao2();
	}

	private void yueKao2() {
		try {
			for (int i = 0;; i++) {
				String content = getHttpGetContent(httpClient, urlYueKao2, UTF8);
				if (!isLogin(content)) {
					// displayMsg("Session is invalid; Please login", true);
					log.info("Session is invalid; Please login again");
					loginYueChe(username, pwd);
				}
				if (!isContentValid(content)) {
					continue;
				}
				log.info(
						"\r\n{}",
						removeHtmlTag(content)
								.replace("预约考试 ", "")
								.replace("取消预约", "")
								.replace("2012-", "")
								.replace(
										"预 约 科 目 二 考 试 每周一上午考试的学员，需提前两天在周六下午集中训练。&nbsp;&nbsp; 其它时间考试的学员，在考试前一天下午集中训练。 预约日期 预约时段 剩余可约人数操 作",
										""));

				if (canYueKao2(content)) {
					sleep(8000);
				}
				if (i > 10000) {
					displayMsg("Run more than " + i + " times, exit!", true);
					System.exit(0);
				}
				sleep(5000);
			}
		} catch (URISyntaxException | IOException e) {
			log.debug("URISyntaxException in YueKao2.yueKao2()", e);
			String msg = "Please check haijia.properteis";
			log.error(msg, e.getMessage());
			exit(msg);
		} catch (Exception e) {
			log.error("Exception in YueKao2.yueKao2()", e.getMessage());
			log.debug("Exception in YueKao2.yueKao2()", e);
			loginYueChe(username, pwd);
			yueKao2();
		}
	}

	private boolean canYueKao2(String content) throws IOException, URISyntaxException {
		log.debug("Enter canYueKao2()");
		Document doc = Jsoup.parse(content, UTF8);
		Elements table = doc.body().getElementsByAttributeValue("id", "tblMain");
		Elements trs = table.get(0).getElementsByTag("tr");
		for (Element tr : trs) {
			Elements tds = tr.getElementsByTag("td");
			if (tds.size() <= 0) continue;
			String count = tds.get(2).text();
			// 2013-03-04
			String date = tds.get(0).text();
			String dateTime = date + tds.get(1).text();
			boolean isZero = count.equals("0");
			String linkCancel = "";
			String msgCancel = "";
			Element linkElement = tds.get(3);
			// <a id="RepeaterKM2Exam_LinkBooking_20"
			// href="javascript:__doPostBack(RepeaterKM2Exam$ctl20$LinkBooking,&#39;)">
			// javascript:__doPostBack(RepeaterKM2Exam$ctl20$LinkBooking,&#39;)
			String js = linkElement.child(0).attr("href");
			String link = js.replace("javascript:__doPostBack('", "").replace("','')", "");
			if (isZero || !isYueKao(date, "ke2KaoDay")) {
				if ("取消预约".equals(linkElement.text())) {
					linkCancel = link;
					msgCancel = "You can cancel kao 2 on " + dateTime;
				}

				if (!isZero) {
					log.info("Exculde it on {} and count = {}", dateTime, count);
				}
				continue;
			}
			log.info("You can yue kao 2 date = {}, count = {}", dateTime, count);
			String msg = "You can yue kao 2 on " + dateTime + " and count = " + count;
			if (cancelKe2(linkCancel, msgCancel) && bookKe2(link, msg)) {
				System.exit(0);
			}
			log.debug("Exit canYueKao2() result = true");
			return true;
		}
		log.debug("Exit canYueKao2() result = false");
		return false;
	}

	/**
	 * @param target "RepeaterKM2Exam$ctl24$LinkBooking"
	 */
	private boolean bookKe2(String target, String msg) {
		log.debug("Enter bookKe2({}, {})", target, msg);
		HttpPost httpPost = new HttpPost(urlYueKao2);
		// 表单信息
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params
				.add(new BasicNameValuePair(
						"__VIEWSTATE",
						"/wEPDwULLTE2OTg2ODQxNTAPZBYCAgEPZBYCAgEPFgIeC18hSXRlbUNvdW50AhkWMmYPZBYEZg8VAwoyMDEzLTAzLTA0BuS4i+WNiAEwZAIBDw8WBB4EVGV4dAUM6aKE57qm6ICD6K+VHg9Db21tYW5kQXJndW1lbnQFEzIwMTMtMy00IDA6MDA6MDAsMTBkZAIBD2QWBGYPFQMKMjAxMy0wMy0wNAbkuIrljYgBMGQCAQ8PFgQfAQUM6aKE57qm6ICD6K+VHwIFEjIwMTMtMy00IDA6MDA6MDAsOWRkAgIPZBYEZg8VAwoyMDEzLTAzLTA1BuS4i+WNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUTMjAxMy0zLTUgMDowMDowMCwxMGRkAgMPZBYEZg8VAwoyMDEzLTAzLTA1BuS4iuWNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUSMjAxMy0zLTUgMDowMDowMCw5ZGQCBA9kFgRmDxUDCjIwMTMtMDMtMDYG5LiL5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRMyMDEzLTMtNiAwOjAwOjAwLDEwZGQCBQ9kFgRmDxUDCjIwMTMtMDMtMDYG5LiK5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRIyMDEzLTMtNiAwOjAwOjAwLDlkZAIGD2QWBGYPFQMKMjAxMy0wMy0wNwbkuIrljYgBMGQCAQ8PFgQfAQUM6aKE57qm6ICD6K+VHwIFEjIwMTMtMy03IDA6MDA6MDAsOWRkAgcPZBYEZg8VAwoyMDEzLTAzLTA4BuS4i+WNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUTMjAxMy0zLTggMDowMDowMCwxMGRkAggPZBYEZg8VAwoyMDEzLTAzLTA4BuS4iuWNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUSMjAxMy0zLTggMDowMDowMCw5ZGQCCQ9kFgRmDxUDCjIwMTMtMDMtMTEG5LiL5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRQyMDEzLTMtMTEgMDowMDowMCwxMGRkAgoPZBYEZg8VAwoyMDEzLTAzLTExBuS4iuWNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUTMjAxMy0zLTExIDA6MDA6MDAsOWRkAgsPZBYEZg8VAwoyMDEzLTAzLTEyBuS4i+WNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUUMjAxMy0zLTEyIDA6MDA6MDAsMTBkZAIMD2QWBGYPFQMKMjAxMy0wMy0xMgbkuIrljYgBMGQCAQ8PFgQfAQUM6aKE57qm6ICD6K+VHwIFEzIwMTMtMy0xMiAwOjAwOjAwLDlkZAIND2QWBGYPFQMKMjAxMy0wMy0xMwbkuIvljYgBMGQCAQ8PFgQfAQUM6aKE57qm6ICD6K+VHwIFFDIwMTMtMy0xMyAwOjAwOjAwLDEwZGQCDg9kFgRmDxUDCjIwMTMtMDMtMTMG5LiK5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRMyMDEzLTMtMTMgMDowMDowMCw5ZGQCDw9kFgRmDxUDCjIwMTMtMDMtMTQG5LiK5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRMyMDEzLTMtMTQgMDowMDowMCw5ZGQCEA9kFgRmDxUDCjIwMTMtMDMtMTUG5LiL5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRQyMDEzLTMtMTUgMDowMDowMCwxMGRkAhEPZBYEZg8VAwoyMDEzLTAzLTE1BuS4iuWNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUTMjAxMy0zLTE1IDA6MDA6MDAsOWRkAhIPZBYEZg8VAwoyMDEzLTAzLTE4BuS4iuWNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUTMjAxMy0zLTE4IDA6MDA6MDAsOWRkAhMPZBYEZg8VAwoyMDEzLTAzLTE5BuS4i+WNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUUMjAxMy0zLTE5IDA6MDA6MDAsMTBkZAIUD2QWBGYPFQMKMjAxMy0wMy0xOQbkuIrljYgBMGQCAQ8PFgQfAQUM6aKE57qm6ICD6K+VHwIFEzIwMTMtMy0xOSAwOjAwOjAwLDlkZAIVD2QWBGYPFQMKMjAxMy0wMy0yNQbkuIvljYgBMGQCAQ8PFgQfAQUM6aKE57qm6ICD6K+VHwIFFDIwMTMtMy0yNSAwOjAwOjAwLDEwZGQCFg9kFgRmDxUDCjIwMTMtMDMtMjUG5LiK5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRMyMDEzLTMtMjUgMDowMDowMCw5ZGQCFw9kFgRmDxUDCjIwMTMtMDMtMjYG5LiL5Y2IATBkAgEPDxYEHwEFDOmihOe6puiAg+ivlR8CBRQyMDEzLTMtMjYgMDowMDowMCwxMGRkAhgPZBYEZg8VAwoyMDEzLTAzLTI2BuS4iuWNiAEwZAIBDw8WBB8BBQzpooTnuqbogIPor5UfAgUTMjAxMy0zLTI2IDA6MDA6MDAsOWRkZGKqy0ld1NhYra5KP8+/66ZfUYYTSamavzpQLBF73iUz"));
		params
				.add(new BasicNameValuePair(
						"__EVENTVALIDATION",
						"/wEWGgLYrKPYCgLAr8awDAKj18iwDAL+/cGwDALhpcSwDAK80M6wDAKf+NCwDAKql8qwDALdxsywDALo59awDALLkdmwDALAr6oRAqPXrBEC/v2lEQLhpagRArzQshECn/i0EQKql64RAt3GsBEC6Oe6EQLLkb0RAu/zxNgHAtKbx9gHAq3CwNgHApDqwtgHAuuUzdgHPPpl3caYngnapmp9abLWXQHq1wNABDo6fO6YeQiYEGY="));
		params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		params.add(new BasicNameValuePair("__EVENTTARGET", target));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, UTF8));

			HttpResponse response = httpClient.execute(httpPost);
			String htmlContent = getResponseContent(response);
			String alert = getAlertMsg(htmlContent);
			log.info("alert = {}", alert);
			if (alert.equals("桩考可预约人数不足") || "未交桩考考试费".equals(alert)) {
				log.info("约考不成功：{}", alert);
				return false;
			} else if (alert.contains("预约成功")) {
				// displayMsg("Yue Ke2 successfully! " + alert, true);
				// return true;
			}
			String content = removeHtmlTag(htmlContent);
			log.info("content = {}", content);
			if (content.contains("取消预约")) {
				displayMsg("Yue Ke2 successfully! " + alert, true);
				log.debug("Exit bookKe2() result = true");
				return true;
			}
		} catch (IOException e) {
			log.error("Exception in YueKao2.bookKe2()", e);
			displayMsg(msg, true);
			log.debug("Exit bookKe2() result = false");
			return false;
		}
		log.debug("Exit bookKe2() result = false");
		return false;
	}

	/**
	 * @param target "RepeaterKM2Exam$ctl24$LinkBooking"
	 */
	private boolean cancelKe2(String target, String msg) {
		log.debug("Enter cancelKe2({}, {})", target, msg);
		HttpPost httpPost = new HttpPost(urlYueKao2);
		// 表单信息
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params
				.add(new BasicNameValuePair(
						"__VIEWSTATE",
						"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"));
		params
				.add(new BasicNameValuePair(
						"__EVENTVALIDATION",
						"/wEWGwKB6ZLjDwLAr8awDAKj18iwDAL+/cGwDALhpcSwDAK80M6wDAKf+NCwDAKql8qwDALdxsywDALo59awDALLkdmwDALAr6oRAqPXrBEC/v2lEQLhpagRArzQshECn/i0EQKql64RAt3GsBEC6Oe6EQLLkb0RAu/zxNgHAtKbx9gHAq3CwNgHApDqwtgHAuuUzdgHAs68z9gHnfKF3IuXE7UcgiJcPDgPJeWQddg+L8+soGIH0tSKOts="));
		params.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
		params.add(new BasicNameValuePair("__EVENTTARGET", target));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(params, UTF8));

			HttpResponse response = httpClient.execute(httpPost);
			String htmlContent = getResponseContent(response);
			String alert = getAlertMsg(htmlContent);
			log.info("alert = {}", alert);
			if (alert.equals("取消成功")) {
				log.info("取消成功：{}", alert);
				log.debug("Exit cancelKe2() result = true");
				return true;
			}
		} catch (IOException e) {
			log.error("IOException in YueKao2.cancelKe2()", e);
			displayMsg(msg, true);
			log.debug("Exit cancelKe2() result = false");
			return false;
		}
		displayMsg(msg, true);
		log.debug("Exit cancelKe2() result = false");
		return false;
	}
}
