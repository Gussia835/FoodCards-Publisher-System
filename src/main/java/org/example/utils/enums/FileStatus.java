package org.example.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.utils.Constants;

@Getter
@AllArgsConstructor
public enum FileStatus {
    IN_PROGRESS(Constants.EXT_IN_PROGRESS, Constants.FOLDER_IN_PROGRESS),
    SUCCESS(Constants.EXT_SUCCESS, Constants.FOLDER_SUCCESS),
    ERROR(Constants.EXT_ERROR, Constants.FOLDER_ERROR);

    private final String extension;
    private final String folderName;
}
