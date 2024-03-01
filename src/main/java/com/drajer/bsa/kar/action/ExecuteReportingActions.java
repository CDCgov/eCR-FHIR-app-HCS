package com.drajer.bsa.kar.action;

import com.drajer.bsa.dao.PublicHealthMessagesDao;
import com.drajer.bsa.dao.impl.PublicHealthMessagesDaoImpl;
import com.drajer.bsa.ehr.service.EhrQueryService;
import com.drajer.bsa.kar.model.BsaAction;
import com.drajer.bsa.model.BsaTypes.BsaActionStatusType;
import com.drajer.bsa.model.KarProcessingData;
import com.drajer.bsa.model.NotificationContext;
import com.drajer.bsa.model.PublicHealthMessage;
import com.drajer.bsa.utils.BsaServiceUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Encounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

public class ExecuteReportingActions extends BsaAction {

  private final Logger logger = LoggerFactory.getLogger(ExecuteReportingActions.class);

  private PublicHealthMessagesDao phDao;

  @Value("${encounter.has.changed.enable:false}")
  Boolean encounterChangeEnabled;

  @Override
  public BsaActionStatus process(KarProcessingData data, EhrQueryService ehrService) {
    BsaActionStatus actStatus = new ExecuteReportingActionsStatus();
    actStatus.setActionId(this.getActionId());

    // Check Timing constraints and handle them before we evaluate conditions.
    BsaActionStatusType status = processTimingData(data);

    // Get the Resources that need to be retrieved.
    ehrService.getFilteredData(data, getInputData());

    // Ensure the activity is In-Progress and the Conditions are met.
    if (status != BsaActionStatusType.SCHEDULED
        && Boolean.TRUE.equals(conditionsMet(data, ehrService))) {

      logger.info(" All conditions in the Actions have been met for {}", this.getActionId());

      // Execute sub Actions
      executeSubActions(data, ehrService);

      // Execute Related Actions.
      executeRelatedActions(data, ehrService);

      actStatus.setActionStatus(BsaActionStatusType.COMPLETED);

    } else {

      logger.info(
          " Action may be executed in the future or Conditions have not been met, so cannot proceed any further. ");
      logger.info(" Setting Action Status : {}", status);
      actStatus.setActionStatus(status);
    }

    data.addActionStatus(data.getExecutionSequenceId(), actStatus);

    return actStatus;
  }

  public void setPhDao(PublicHealthMessagesDao phDao) {
    this.phDao = phDao;
  }

  public PublicHealthMessagesDao getPhDao() {
    return phDao;
  }

  @Override
  public Boolean conditionsMet(KarProcessingData kd, EhrQueryService ehrService) {
    Boolean retval = super.conditionsMet(kd, ehrService);
    if (Boolean.TRUE.equals(retval) && encounterChangeEnabled) {
      retval = isEncounterChanged(kd);
    }
    return retval;
  }

  private Boolean isEncounterChanged(KarProcessingData kd) {
    boolean retval = true;
    NotificationContext context = kd.getNotificationContext();
    if (context != null) {
      PublicHealthMessage msg = getPublicHealthMessage(context, kd);
      if (msg != null && msg.getSubmittedFhirData() != null) {
        Bundle bundle = (Bundle) jsonParser.parseResource(msg.getSubmittedFhirData());
        Encounter encounter = BsaServiceUtils.findEncounterFromBundle(bundle);
        if (encounter != null && kd.getContextEncounter() != null) {
          logger.info(
              "Last submitted Encounter Id: {}, lastUpdate: {}",
              encounter.getId(),
              encounter.getMeta().getLastUpdated());
          retval =
              ObjectUtils.compare(
                      encounter.getMeta().getLastUpdated(),
                      kd.getContextEncounter().getMeta().getLastUpdated())
                  != 0;
          logger.info("Encounter {} is changed: {}", encounter.getIdElement().getIdPart(), retval);
        }
      }
    }
    return retval;
  }

  private PublicHealthMessage getPublicHealthMessage(
      NotificationContext nc, KarProcessingData data) {
    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(PublicHealthMessagesDaoImpl.FHIR_SERVER_URL, nc.getFhirServerBaseUrl());
    searchParams.put(PublicHealthMessagesDaoImpl.PATIENT_ID, nc.getPatientId());
    searchParams.put(
        PublicHealthMessagesDaoImpl.NOTIFIED_RESOURCE_ID, nc.getNotificationResourceId());
    searchParams.put(
        PublicHealthMessagesDaoImpl.NOTIFIED_RESOURCE_TYPE, nc.getNotificationResourceType());
    List<PublicHealthMessage> messages = phDao.getPublicHealthMessage(searchParams);
    if (messages != null && !messages.isEmpty()) return messages.get(0);
    else return null;
  }
}
