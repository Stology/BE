package com.stology.be.domain.upload.dto.res;

import java.util.List;

public record RecentFilesRes(
        List<RecentFileRes> files
) {
}