package com.relog.relog.ai;

import com.relog.relog.ai.dto.MonthlyAnalysisResult;
import com.relog.relog.ai.dto.QuarterlyAnalysisResult;
import com.relog.relog.event.entity.Event;
import com.relog.relog.friend.entity.Friend;
import com.relog.relog.gift.entity.Gift;
import java.util.List;

public interface AiAnalysisService {

    MonthlyAnalysisResult analyzeMonthly(List<Event> events, List<Gift> gifts);

    QuarterlyAnalysisResult analyzeQuarterly(List<Event> events, List<Friend> friends);
}
