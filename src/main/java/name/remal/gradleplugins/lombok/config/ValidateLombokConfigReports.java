package name.remal.gradleplugins.lombok.config;

import org.gradle.api.reporting.ConfigurableReport;
import org.gradle.api.reporting.ReportContainer;
import org.gradle.api.reporting.SingleFileReport;
import org.gradle.api.tasks.Internal;

public interface ValidateLombokConfigReports extends ReportContainer<ConfigurableReport> {

    @Internal
    SingleFileReport getXml();

    @Internal
    SingleFileReport getHtml();

}
