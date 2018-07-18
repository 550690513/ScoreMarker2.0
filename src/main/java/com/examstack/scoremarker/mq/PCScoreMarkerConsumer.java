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
 * MQ消费者-PC
 *
 * @author: Cheung
 * @date: 2017/03/24 11:25
 * @version: 2.0
 */
public class PCScoreMarkerConsumer implements MessageListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(PCScoreMarkerConsumer.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();

	@Autowired
	private ScoreCalcuService scoreCalcuService;

	@Override
	public void onMessage(Message msg) {
		try {
			LOGGER.info("ScoreMarker-PC：接收到mq消息，开始处理。。。");
			// 解析出内容
			AnswerSheet answerSheet = MAPPER.readValue(msg.getBody(), AnswerSheet.class);
			// 计算得分
			scoreCalcuService.calcuScorePC(answerSheet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}