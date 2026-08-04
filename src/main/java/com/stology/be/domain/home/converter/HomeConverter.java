package com.stology.be.domain.home.converter;

import com.stology.be.domain.home.dto.res.TeamActivityRes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class HomeConverter {


    public static List<TeamActivityRes.TeamActivityInfo> mergeActivities(
            List<TeamActivityRes.TeamActivityInfo> nodeActivities,
            List<TeamActivityRes.TeamActivityInfo> answerActivities,
            CursorConverter.CursorValue cursor,
            int pageSize
    ) {
        return Stream.concat(
                        nodeActivities.stream(),
                        answerActivities.stream()
                )
                .filter(activity ->
                        CursorConverter.isAfterCursor(
                                activity.getOccurredAt(),
                                activity.getActivityType(),
                                activity.getCursorId(),
                                cursor
                        )
                )
                .sorted(
                        Comparator
                                .comparing(
                                        TeamActivityRes.TeamActivityInfo::getOccurredAt,
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(
                                        activity ->
                                                activity.getActivityType()
                                                        .getRank(),
                                        Comparator.reverseOrder()
                                )
                                .thenComparing(
                                        TeamActivityRes.TeamActivityInfo::getCursorId,
                                        Comparator.reverseOrder()
                                )
                )
                .limit(pageSize + 1L)
                .toList();
    }
}
