package com.drajer.bsa.kar.action;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.hl7.fhir.r4.model.CanonicalType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.OperationOutcome.IssueSeverity;
import org.hl7.fhir.r4.model.OperationOutcome.OperationOutcomeIssueComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a utlities class for processing actions.
 *
 * @author nbashyam
 */
public class ActionUtils {

  private static final Logger logger = LoggerFactory.getLogger(ActionUtils.class);

  private ActionUtils() {
    throw new IllegalStateException("Action Util class");
  }

  public static Meta getMeta(String version, String profile) {

    Meta m = new Meta();
    m.setVersionId(version);
    CanonicalType ct = new CanonicalType();
    ct.setValueAsString(profile);
    List<CanonicalType> profiles = new ArrayList<>();
    profiles.add(ct);
    m.setProfile(profiles);
    m.setLastUpdated(Date.from(Instant.now()));

    return m;
  }

  public static Boolean operationOutcomeHasErrors(OperationOutcome outcome) {

    Boolean retVal = false;

    if (outcome != null && outcome.hasIssue()) {

      List<OperationOutcomeIssueComponent> issues = outcome.getIssue();

      for (OperationOutcomeIssueComponent ic : issues) {

        if (ic.getSeverity() == IssueSeverity.ERROR) {

          logger.info(" Issue of error detected ");
          retVal = true;
          break;
        }
      }
    } else {

      logger.info(" No Issues in the Report ");
    }

    return retVal;
  }

  public static Boolean isEncounterChanged(Encounter encounter1, Encounter encounter2) {
    // check lastUpdated
    Boolean retval =
        (ObjectUtils.compare(
                    encounter1.getMeta().getLastUpdated(), encounter2.getMeta().getLastUpdated())
                != 0)
            || (ObjectUtils.compare(
                    encounter1.getPeriod().getStart(), encounter2.getPeriod().getStart())
                != 0)
            || (ObjectUtils.compare(
                    encounter1.getPeriod().getEnd(), encounter2.getPeriod().getEnd())
                != 0);
    logger.info("Encounter {} is changed: {}", encounter1.getIdElement().getIdPart(), retval);
    return retval;
  }
}
