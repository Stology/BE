package com.stology.be.domain.study.converter;

import com.stology.be.domain.member.entity.Member;
import com.stology.be.domain.node.entity.Template;
import com.stology.be.domain.study.dto.StudyReqDTO;
import com.stology.be.domain.study.dto.StudyResDTO;
import com.stology.be.domain.study.entity.MemberStudy;
import com.stology.be.domain.study.entity.Study;
import com.stology.be.support.TestFixtures;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class StudyConverterTest {

    @Test
    void toCreateStudy_shouldMapDtoToStudy() {
        StudyReqDTO.CreateStudy dto = new StudyReqDTO.CreateStudy(
                "스터디A",
                7L,
                LocalDate.of(2026, 7, 20),
                "설명"
        );
        Template template = TestFixtures.template(7L);
        Member member = TestFixtures.member(1L);

        Study study = StudyConverter.toCreateStudy(dto, template, member);

        assertNotNull(study);
        assertEquals("스터디A", study.getName());
        assertEquals("설명", study.getDescription());
        assertEquals(member.getId(), study.getLeaderMemberId());
        assertEquals(dto.startDate(), study.getStartDate());
        assertSame(template, study.getTemplate());
    }

    @Test
    void toGetReviewerCount_shouldWrapCounts() {
        StudyResDTO.GetReviewerCount response = StudyConverter.toGetReviewerCount(3, 5);

        assertNotNull(response);
        assertEquals(3, response.reviewerCount());
        assertEquals(5, response.maxReviewerCount());
    }

    @Test
    void toCreateMemberStudy_shouldLinkStudyAndMember() {
        Study study = TestFixtures.study(10L);
        Member member = TestFixtures.member(2L);

        MemberStudy memberStudy = StudyConverter.toCreateMemberStudy(study, member);

        assertNotNull(memberStudy);
        assertSame(study, memberStudy.getStudy());
        assertSame(member, memberStudy.getMember());
    }
}
