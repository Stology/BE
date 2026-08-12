package com.stology.be.domain.report.entity;

import com.stology.be.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ReportReadHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long studyId;

    @Column(nullable = false)
    private Long lastReadReportId;

    public void updateLastReadReportId(Long reportId) {
        this.lastReadReportId = reportId;
    }
}
