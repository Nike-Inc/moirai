package com.nike.moirairiposteexample.error;

import com.nike.backstopper.apierror.ApiError;
import com.nike.backstopper.apierror.projectspecificinfo.ProjectSpecificErrorCodeRange;
import com.nike.backstopper.apierror.sample.SampleProjectApiErrorsBase;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.List;

/**
 * Returns the project specific errors for this application.
 *
 * <p>Individual projects should feel free to rename this class to something specific, e.g. [MyProject]ApiErrorsImpl
 *
 * <p>NOTE: This extends {@link SampleProjectApiErrorsBase} for a reasonable base of "core errors". You may want to
 * create a similar reusable base class to be used by this project (and potentially others) that uses error codes and
 * messages of your choosing for the core errors.
 */
@Singleton
public class ProjectApiErrorsImpl extends SampleProjectApiErrorsBase {

    private static final List<ApiError> PROJECT_SPECIFIC_API_ERRORS = Arrays.asList(ProjectApiError.values());

    @Override
    protected List<ApiError> getProjectSpecificApiErrors() {
        return PROJECT_SPECIFIC_API_ERRORS;
    }

    /**
     * @return the range of errors for this project. This is used to verify that {@link #getProjectSpecificApiErrors()}
     * doesn't include any error codes outside the range you have reserved for your project. See the class javadocs for
     * {@link ProjectSpecificErrorCodeRange} for suggestions on how to manage cross-project error ranges in the same
     * org.
     */
    @Override
    protected ProjectSpecificErrorCodeRange getProjectSpecificErrorCodeRange() {
        return ProjectSpecificErrorCodeRange.ALLOW_ALL_ERROR_CODES;
    }

}
