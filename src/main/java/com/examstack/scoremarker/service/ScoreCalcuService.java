package com.examstack.scoremarker.service;

import com.examstack.common.domain.exam.AnswerSheet;
import com.examstack.common.domain.exam.AnswerSheetItem;
import com.examstack.common.domain.exam.ExamPaper;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;

/**
 * 分数计算服务
 *
 * @author: Cheung
 * @date: 2016/12/20 15:14
 * @version: 1.0
 */
@Component
public class ScoreCalcuService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScoreCalcuService.class);

	@Value("${answerSheet-pc.post.uri}")
	private String answerSheetPCPostUri;
	@Value("${examPaper-pc.get.uri}")
	private String examPaperPCGetUri;
	@Value("${answerSheet-phone.post.uri}")
	private String answerSheetPhonePostUri;
	@Value("${examPaper-phone.get.uri}")
	private String examPaperPhoneGetUri;


	RestTemplate restTemplate = new RestTemplate();
	HashMap<String, ExamPaper> examPapers_PC = new HashMap<String, ExamPaper>();
	HashMap<String, ExamPaper> examPapers_Phone = new HashMap<String, ExamPaper>();


	/**
	 * 计算得分-PC
	 *
	 * @param as 答题卡
	 */
	public void calcuScorePC(AnswerSheet as) {

		LOGGER.info("scoreMarker-PC：开始计算分数");

		// 获取examPaper
		ExamPaper examPaper = examPapers_PC.get(as.getExamPaperId());
		if (examPaper == null) {
			examPaper = this.getExamPaper_SAZX_PC(as.getExamId(), as.getExamPaperId(), as.getUserId(), as.getUuid());
			examPapers_PC.put(examPaper.getId() + "", examPaper);
		}

		Gson gson = new Gson();
		AnswerSheet target = gson.fromJson(examPaper.getAnswer_sheet(), AnswerSheet.class);
		HashMap<Integer, AnswerSheetItem> answerMap = new HashMap<Integer, AnswerSheetItem>();
		for (AnswerSheetItem item : target.getAnswerSheetItems()) {
			answerMap.put(item.getQuestionId(), item);
		}

		// 试卷满分pointMax
		as.setPointMax(target.getPointMax());

		/**
		 * 判卷
		 */
		for (AnswerSheetItem item : as.getAnswerSheetItems()) {
			if (answerMap.containsKey(item.getQuestionId())) {
				if (item.getAnswer().replaceAll(",", "").equals(answerMap.get(item.getQuestionId()).getAnswer().replaceAll(",", ""))) {
					// 该考生总分
					as.setPointRaw(as.getPointRaw() + answerMap.get(item.getQuestionId()).getPoint());
					// 各题得分pointRaw
					item.setPoint(answerMap.get(item.getQuestionId()).getPoint());
					item.setRight(true);
				}
			} else {
				as.setPointRaw(as.getPointRaw() + 0);
				item.setPoint(0);
				item.setRight(false);
			}

		}

		// 请求AnswerSheet给后台系统
		this.postAnswerSheet(answerSheetPCPostUri, as);
	}


	/**
	 * 计算得分-Phone
	 *
	 * @param as 答题卡
	 */
	public void calcuScorePhone(AnswerSheet as) {

		LOGGER.info("scoreMarker-Phone:开始计算分数");

		// 获取examPaper
		ExamPaper examPaper = examPapers_Phone.get(as.getExamPaperId());
		if (examPaper == null) {
			examPaper = this.getExamPaper_SAZX_Phone(as.getExamId(), as.getExamPaperId(), as.getUserId(), as.getUuid());
			examPapers_Phone.put(examPaper.getId() + "", examPaper);
		}

		if (examPaper == null) {
			LOGGER.error("scoreMarker-Phone：获取examPaper失败，userId：{}", as.getUserId());
			return;
		}


		Gson gson = new Gson();
		AnswerSheet target = gson.fromJson(examPaper.getAnswer_sheet(), AnswerSheet.class);
		HashMap<Integer, AnswerSheetItem> answerMap = new HashMap<Integer, AnswerSheetItem>();
		for (AnswerSheetItem item : target.getAnswerSheetItems()) {
			answerMap.put(item.getQuestionId(), item);
		}
		// 试卷满分pointMax
		as.setPointMax(target.getPointMax());


		/**
		 * 判卷
		 */
		for (AnswerSheetItem item : as.getAnswerSheetItems()) {
			if (answerMap.containsKey(item.getQuestionId())) {
				if (item.getAnswer().replaceAll(",", "").equals(answerMap.get(item.getQuestionId()).getAnswer().replaceAll(",", ""))) {
					// 该考生总分
					as.setPointRaw(as.getPointRaw() + answerMap.get(item.getQuestionId()).getPoint());
					// 各题得分pointRaw
					item.setPoint(answerMap.get(item.getQuestionId()).getPoint());
					item.setRight(true);
				}
			} else {
				as.setPointRaw(as.getPointRaw() + 0);
				item.setPoint(0);
				item.setRight(false);
			}

		}

		// 请求AnswerSheet给后台系统
		this.postAnswerSheet(answerSheetPhonePostUri, as);
	}


	/**
	 * 获取examPaper-PC
	 * uuid:防止多次访问
	 */
	private ExamPaper getExamPaper_SAZX_PC(int examId, int examPaperId, int userId, String uuid) {

		LOGGER.info("scoreMarker-PC:获取examPaper");

		ExamPaper examPaper = null;
		try {
			LOGGER.info("scoreMarker-PC请求url:{}", examPaperPCGetUri + "/" + examPaperId + "/" + userId + "/" + uuid);
			examPaper = restTemplate.getForObject(examPaperPCGetUri + "/" + examPaperId + "/" + userId + "/" + uuid, ExamPaper.class);
			LOGGER.info("scoreMarker-PC获取examPaper成功---{}", userId);
		} catch (RestClientException e) {
			LOGGER.error("scoreMarker-PC获取examPaper失败---{}", userId);
		}
		return examPaper;
	}

	/**
	 * 获取examPaper-Phone
	 * uuid:防止多次访问
	 */
	private ExamPaper getExamPaper_SAZX_Phone(int examId, int examPaperId, int userId, String uuid) {

		LOGGER.info("scoreMarker_Phone:获取examPaper");

		ExamPaper examPaper = null;
		try {
			LOGGER.info("scoreMarker-Phone请求url:{}", examPaperPhoneGetUri + "/" + examId + "/" + examPaperId + "/" + userId + "/" + uuid);
			examPaper = restTemplate.getForObject(examPaperPhoneGetUri + "/" + examId + "/" + examPaperId + "/" + userId + "/" + uuid, ExamPaper.class);
			LOGGER.info("scoreMarker-Phone获取examPaper成功---{}", userId);
		} catch (RestClientException e) {
			LOGGER.error("scoreMarker-Phone获取examPaper失败---{}", userId);
		}
		return examPaper;
	}


	/**
	 * post请求
	 *
	 * @param uri
	 * @param body
	 */
	private void postAnswerSheet(String uri, Object body) {
		try {
			LOGGER.info("scoreMarker服务：发送post请求---uri是：" + uri);
			restTemplate.postForLocation(uri, body);
		} catch (RestClientException e) {
			LOGGER.error("scoreMarker服务post请求失败：", e);
		}
	}

}
