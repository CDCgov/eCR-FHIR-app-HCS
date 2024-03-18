package com.drajer.bsa.controller;

import ca.uhn.fhir.parser.IParser;
import com.drajer.bsa.dao.PublicHealthMessagesDao;
import com.drajer.bsa.dao.impl.PublicHealthMessagesDaoImpl;
import com.drajer.bsa.model.PatientLaunchContext;
import com.drajer.bsa.model.PublicHealthMessage;
import com.drajer.bsa.service.SubscriptionNotificationReceiver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.hl7.fhir.r4.model.Bundle;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is used to receive notifications to subscriptions that are subscribed to by the
 * BSA. The implementation here is a REST hook interface per the specification.
 *
 * @author nbashyam
 */
@RestController
public class SubscriptionNotificationReceiverController {

  private final Logger logger =
      LoggerFactory.getLogger(SubscriptionNotificationReceiverController.class);

  @Autowired SubscriptionNotificationReceiver subscriptionProcessor;

  @Autowired PublicHealthMessagesDao phDao;

  @Autowired
  @Qualifier("jsonParser")
  IParser jsonParser;

  /**
   * This method is used to receive event-notifications from subscriptions.
   *
   * @param hsDetails The HealthcareSettings details passed as part of the Request Body.
   * @return This returns the HTTP Response Entity containing the JSON representation of the
   *     HealthcareSetting when successful, else returns appropriate error. Upon success a HTTP
   *     Status code of 200 is sent back. The following HTTP Errors will be sent back - 400 (BAD
   *     Request) - When the request body is not a notification FHIR R4 Bundle. - 401 (UnAuthorized)
   *     - When the incoming request does not have the security token required by the authorization
   *     server
   */
  @CrossOrigin
  @PostMapping(value = "/api/receive-notification")
  public ResponseEntity<Object> processNotification(
      @RequestBody String notificationBundle,
      HttpServletRequest request,
      HttpServletResponse response,
      PatientLaunchContext launchContext) {

    if (notificationBundle != null) {

      logger.info("Notification Bundle received, Start processing notification. ");

      Bundle bund = (Bundle) jsonParser.parseResource(notificationBundle);

      if (bund != null) {

        logger.info(" Successfully parsed incoming notification as bundle ");

        subscriptionProcessor.processNotification(bund, request, response, launchContext);

        logger.info(" Finished processing notification ");

        return new ResponseEntity<>(this, HttpStatus.OK);

      } else {

        logger.error(
            "Unable to parse Incoming Param as bundle (Has to be a Notification FHIR Bundle), hence the notification processing cannot proceed.");

        JSONObject responseObject = new JSONObject();
        responseObject.put("status", "error");
        responseObject.put(
            "message",
            "Unable to parse Incoming Param as bundle (Has to be a Notification FHIR R4 Bundle), hence the notification processing cannot proceed.");
        return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
      }

    } else {

      logger.error(
          "Unable to parse Resource Param (Has to be a Notification FHIR Bundle), hence the notification processing cannot proceed.");

      JSONObject responseObject = new JSONObject();
      responseObject.put("status", "error");
      responseObject.put(
          "message",
          "Unable to parse Resource Param in request body (Has to be a Notification FHIR R4 Bundle), hence the notification processing cannot proceed.");
      return new ResponseEntity<>(responseObject, HttpStatus.BAD_REQUEST);
    }
  }

  @CrossOrigin
  @GetMapping("/api/submitted-status")
  public ResponseEntity<Object> getSubmittedStatus(
      @RequestParam(value = "encounter") String encounterId,
      @RequestParam(value = "patient") String patientId) {
    HttpStatus status = HttpStatus.OK;
    Map<String, String> searchParams = new HashMap<>();
    searchParams.put(PublicHealthMessagesDaoImpl.PATIENT_ID, patientId);
    searchParams.put(PublicHealthMessagesDaoImpl.ENCOUNTER_ID, encounterId);
    List<PublicHealthMessage> messages = phDao.getPublicHealthMessage(searchParams);
    JSONObject responseObject = new JSONObject();
    if (messages != null && !messages.isEmpty()) {
      PublicHealthMessage message = messages.get(0);
      logger.info("phmessage found {} ", message.getxRequestId());
      responseObject.put("status", "Submitted");
      responseObject.put("submit-time", message.getSubmissionTime());
      responseObject.put("encounterId", message.getEncounterId());
      responseObject.put("patientId", message.getPatientId());
      responseObject.put("submit-data-id", message.getSubmittedDataId());
    } else {
      logger.info("phmessage NOT found. encounter={}, patient={} ", encounterId, patientId);
      responseObject.put("status", "Not Found");
      status = HttpStatus.NOT_FOUND;
    }
    return new ResponseEntity<>(responseObject.toString(), status);
  }
}
