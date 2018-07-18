package com.examstack.scoremarker.mq;

import com.examstack.common.domain.exam.AnswerSheet;
import com.examstack.scoremarker.service.ScoreCalcuService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * MQ消费者-Phone
 *
 * @author: Cheung
 * @date: 2017/03/24 13:42
 * @version: 2.0
 */
public class PhoneScoreMarkerConsumer implements MessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(PhoneScoreMarkerConsumer.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private ScoreCalcuService scoreCalcuService;

	@Override
	public void onMessage(Message msg) {
		try {
			LOGGER.info("ScoreMarker-Phone：接收到mq消息，开始处理。。。");
			// 解析出内容
			AnswerSheet answerSheet = MAPPER.readValue(msg.getBody(), AnswerSheet.class);
			// 计算得分
			scoreCalcuService.calcuScorePhone(answerSheet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}